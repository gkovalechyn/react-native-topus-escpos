package com.reactnativetopusescpos

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.Style
import com.github.anastaciocintra.escpos.barcode.BarCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TopusEscposModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context), ActivityEventListener {
	init {
		context.addActivityEventListener(this);
	}

	companion object {
		const val REQUEST_ENABLE_BT: Int = 1;
		const val REQUEST_FIND_BT_DEVICES: Int = 2;
		const val REQUEST_FINE_LOC_PERMISSION: Int = 3;
		const val TAG = "TPESCPOS";
	}

	private var bluetoothAdapter: BluetoothAdapter? = null;
	private val requestMap = HashMap<Int, Promise>();
	private var connection: ESCPOSConnection? = null;
	private var escpos: EscPos? = null;

	// Bluetooth device discovery fields
	private var isDiscoveringDevices = false;
	private var discoveryCancelJob: Job? = null;
	private val discoveredDevices = ArrayList<BluetoothDevice>();
	private val discoveryReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent == null) {
				return;
			}

			Log.d(TAG, "Intent action: ${intent.action}")

			if (intent.action == BluetoothDevice.ACTION_FOUND) {
				discoveredDevices.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
			}
		}
	}

	override fun getName(): String {
		return "TopusEscpos"
	}

	private fun getAdapter(): BluetoothAdapter? {
		if (this.bluetoothAdapter == null) {
			this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		return this.bluetoothAdapter;
	}

	@ReactMethod
	fun isBluetoothSupported(promise: Promise) {
		promise.resolve(this.getAdapter() != null);
	}

	@ReactMethod
	fun isBluetoothEnabled(promise: Promise) {
		val adapter = this.getAdapter();

		if (adapter == null) {
			promise.resolve(false);
		}

		promise.resolve(adapter!!.isEnabled);
	}

	@ReactMethod
	fun findBluetoothDevices(promise: Promise) {
		if (!this.assertBluetoothEnabled(promise)) {
			return;
		}

		if (this.isDiscoveringDevices) {
			return promise.reject(ErrorCode.ALREADY_DISCOVERING_BLUETOOTH_DEVICES.code, "Device discovery is already in progress");
		}

		// Is connected
		if (this.connection != null && (this.connection!!.IsConnected || this.connection!!.IsReconnecting)) {
			return promise.reject(ErrorCode.CANNOT_DISCOVER_WHILE_CONNECTED.code, "Cannot search for bluetooth devices while connected to one");
		}

		val hasPermission = ContextCompat.checkSelfPermission(this.reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

		if (!hasPermission) {
			val adapter = this.getAdapter()!!;

			this.discoveredDevices.clear();
			this.isDiscoveringDevices = true;

			val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND);
			this.reactApplicationContext.registerReceiver(this.discoveryReceiver, intentFilter);
			this.requestMap[REQUEST_FIND_BT_DEVICES] = promise;

			this.discoveryCancelJob = GlobalScope.launch {
				delay(20000);
				endBluetoothDeviceDiscovery();
			}

			adapter.startDiscovery();
		} else {
			ActivityCompat.requestPermissions(
				this.reactApplicationContext.currentActivity!!,
				Array(1) { Manifest.permission.ACCESS_FINE_LOCATION },
				REQUEST_FINE_LOC_PERMISSION
			);

			promise.reject(ErrorCode.PERMISSION_REQUIRED.code, "A permission is required for this functionality, a request was made to the user");
		}
	}

	@ReactMethod
	fun isFindingBluetoothDevices(promise: Promise) {
		return promise.resolve(this.isDiscoveringDevices);
	}


	@ReactMethod
	fun enableBluetooth(promise: Promise) {
		if (!this.assertBluetoothSupported(promise)) {
			return;
		}

		val adapter = this.getAdapter()!!;

		if (adapter.isEnabled) {
			return promise.resolve(true);
		}

		if (this.requestMap.containsKey(REQUEST_ENABLE_BT)) {
			return promise.reject(ErrorCode.BLUETOOTH_ENABLE_REQ_IN_PROGRESS.code, "There is already a request to enable bluetooth in progress");
		}

		val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.reactApplicationContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT, Bundle.EMPTY);

		this.requestMap[REQUEST_ENABLE_BT] = promise;
	}

	@ReactMethod
	fun getBluetoothPairedDevices(promise: Promise) {
		if (!this.assertBluetoothEnabled(promise)) {
			return;
		}

		val adapter = this.getAdapter()!!;

		val pairedDevices = adapter.bondedDevices;
		val responseArray = Arguments.createArray();

		pairedDevices.forEach { device ->
			val jsonDevice = this.bluetoothDeviceToJson(device);

			responseArray.pushMap(jsonDevice);
		};

		promise.resolve(responseArray);
	}

	@ReactMethod
	fun isConnected(promise: Promise) {
		if (this.connection == null) {
			return promise.resolve(false);
		}

		return promise.resolve(true);
	}

	@ReactMethod
	fun isReconnecting(promise: Promise) {
		if (this.connection == null) {
			return promise.resolve(false);
		}

		return promise.resolve(this.connection!!.IsReconnecting);
	}

	@ReactMethod
	fun deviceSupportsAutoReconnection(promise: Promise) {
		if (this.connection == null) {
			return promise.resolve(false);
		}

		return promise.resolve(this.connection!!.SupportsAutoReconnection);
	}

	@ReactMethod
	fun connectToBluetoothDevice(address: String, promise: Promise) {
		if (!this.assertBluetoothEnabled(promise)) {
			return;
		}

		if (this.isDiscoveringDevices) {
			this.endBluetoothDeviceDiscovery();
		}

		if (this.connection != null) {
			this.connection!!.disconnect();

			this.escpos = null;
			this.connection = null;
		}

		val adapter = this.getAdapter()!!;
		val pairedDevices = adapter.bondedDevices;

		val device: BluetoothDevice? = pairedDevices.find { d -> d.address == address };

		// If the device isn't paired, just throw an error and have the end application tell the user to pair to the device first
		// @TODO Improve this so the pairing can be done here and not require the application/user to handle it
		if (device == null) {
			promise.reject(ErrorCode.DEVICE_NOT_PAIRED.code, "Device at address $address is not paired, pair with the device first to then connect to it.");
			return;
		}

		this.connection = BluetoothConnection(device);

		try {
			this.connection!!.connect();
			this.escpos = EscPos(this.connection!!.getOutputStream());

			promise.resolve(null);
		} catch (e: IOException) {
			this.connection = null;
			this.escpos = null;

			promise.reject(e);
		}
	}

	@ReactMethod
	fun disconnect(promise: Promise) {
		if (!this.assertIsConnected(promise)) {
			return;
		}

		this.connection!!.disconnect();
		this.connection = null;
		this.escpos = null;

		promise.resolve(null);
	}

	@ReactMethod
	fun writeLine(data: String, promise: Promise) {
		if (!this.assertIsConnected(promise)) {
			return;
		}

		var writeSucceeded = false;

		try {
			this.escpos!!.writeLF(data);
			writeSucceeded = true;
		} catch (e: IOException) {
			Log.e(TAG, "TopusEscpos::writeLine - $e");
			// Lost connection
		}

		// We're going to assume that failed writes mean a disconnection
		if (!writeSucceeded) {
			val connection = this.connection!!;
			var reconnectionSucceeded = false;

			while (connection.CanReconnect) {
				try {
					connection.attemptReconnect();
					reconnectionSucceeded = true;
					break;
				} catch (e: IOException) {
					Log.e(TAG, "TopusEscpos::writeLine reconnection - $e");
					//
				}
			}

			if (!reconnectionSucceeded) {
				promise.reject(ErrorCode.CONNECTION_LOST.code, "Lost connection to device");
				return;
			}

			this.escpos = EscPos(connection.getOutputStream());
			// Successfully reconnected, attempt to write again. If this one fails, close the connection and don't attempt reconnects
			try {
				this.escpos!!.writeLF(data);
			} catch (e: IOException) {
				Log.e(TAG, "TopusEscpos::writeLine reconnection write - $e");
				promise.reject(ErrorCode.CONNECTION_LOST.code, "Lost connection to device");
				return;
			}
		}

		promise.resolve(null);
	}

	@ReactMethod
	fun feed(amount: Int, promise: Promise) {
		if (!this.assertIsConnected(promise)) {
			return;
		}

		this.escpos!!.feed(amount);
		promise.resolve(null);
	}

	@ReactMethod
	fun cut(promise: Promise) {
		if (!this.assertIsConnected(promise)) {
			return;
		}

		this.escpos!!.cut(EscPos.CutMode.FULL);
		promise.resolve(null);
	}

	@ReactMethod
	fun fancyText(data: String, promise: Promise) {
		if (!this.assertIsConnected(promise)) {
			return;
		}

		val style = Style().setBold(true).setUnderline(Style.Underline.TwoDotThick);

		this.escpos!!.writeLF(style, data);
		promise.resolve(null);
	}

	@ReactMethod
	fun barcode(data: String, promise: Promise) {
		if (!this.assertIsConnected(promise)) {
			return;
		}

		val barcode = BarCode();
		this.escpos!!.write(barcode, data);
	}

	private fun endBluetoothDeviceDiscovery() {
		val adapter = this.getAdapter()!!;

		this.discoveryCancelJob!!.cancel();

		this.reactApplicationContext.unregisterReceiver(this.discoveryReceiver);

		adapter.cancelDiscovery();

		this.isDiscoveringDevices = false;
		this.discoveryCancelJob = null;

		val promise = this.requestMap[REQUEST_FIND_BT_DEVICES];

		if (promise == null) {
			Log.e(TAG, "Device discovery ended but there was no promise to return the response");
			return;
		}

		val responseArray = Arguments.createArray();

		this.discoveredDevices.forEach { device ->
			responseArray.pushMap(this.bluetoothDeviceToJson(device));
		};

		promise.resolve(responseArray);
	}

	private fun bluetoothDeviceToJson(device: BluetoothDevice): WritableMap {
		val responseObj = Arguments.createMap();
		responseObj.putString("name", device.name);
		responseObj.putString("address", device.address);
		// responseObj.putInt("type", device.type); Only in API version 18
		responseObj.putInt("bondState", device.bondState);

		val idList = Arguments.createArray();

		if (device.uuids != null) {
			device.uuids.forEach { id ->
				idList.pushString(id.toString())
			};
		}

		responseObj.putArray("ids", idList);

		return responseObj;
	}

	private fun assertBluetoothSupported(promise: Promise): Boolean {
		if (this.getAdapter() == null) {
			promise.reject(ErrorCode.BLUETOOTH_NOT_AVAILABLE.code, "Bluetooth is not available for this device");
			return false;
		}

		return true;
	}

	private fun assertBluetoothEnabled(promise: Promise): Boolean {
		if (this.assertBluetoothSupported(promise)) {
			val adapter = this.getAdapter()!!;

			if (!adapter.isEnabled) {
				promise.reject(ErrorCode.BLUETOOTH_NOT_ENABLED.code, "Bluetooth is not enabled for this device");
				return false;
			}

			return true;
		}

		return false;
	}

	private fun assertIsConnected(promise: Promise): Boolean {
		if (this.connection == null) {
			promise.reject(ErrorCode.NOT_CONNECTED.code, "Not connected to any ESCPOS device");
			return false;
		}

		return true;
	}

	override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == REQUEST_ENABLE_BT) {
			val promise = this.requestMap[requestCode];

			if (promise == null) {
				Log.e(TAG, "Received activity result for request code $REQUEST_ENABLE_BT but no promise was found to respond to");
				return;
			}

			if (resultCode == Activity.RESULT_OK) {
				promise.resolve(null);
			} else {
				promise.reject(ErrorCode.USER_NOT_ENABLED_BLUETOOTH.code, "The user did not allow bluetooth to be enabled");
			}

			this.requestMap.remove(REQUEST_ENABLE_BT);
		}
	}

	override fun onNewIntent(intent: Intent?) {
		// Don't care about this
		// Not that I read the documentation to know what this does anyway
	}
}
