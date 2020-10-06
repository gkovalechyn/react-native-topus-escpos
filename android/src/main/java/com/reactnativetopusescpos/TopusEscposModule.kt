package com.reactnativetopusescpos

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.react.bridge.*

class TopusEscposModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context), ActivityEventListener {
  init {
    context.addActivityEventListener(this);
  }

  companion object {
    const val REQUEST_ENABLE_BT: Int = 1;
    const val TAG = "TPESCPOS";
  }

  private var bluetoothAdapter: BluetoothAdapter? = null;
  private val requestMap = HashMap<Int, Promise>();

  override fun getName(): String {
    return "TopusEscpos"
  }

  private fun getAdapter(): BluetoothAdapter? {
    if (this.bluetoothAdapter == null) {
      this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    return this.bluetoothAdapter;
  }


  // Example method
  // See https://facebook.github.io/react-native/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Int, b: Int, promise: Promise) {

    promise.resolve(a * b)
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
  fun findDevices(promise: Promise) {
    if (!this.assertBluetoothEnabled(promise)) {
      return;
    }

    val adapter = this.getAdapter()!!;
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
  fun getPairedDevices(promise: Promise) {
    if (!this.assertBluetoothEnabled(promise)) {
      return;
    }

    val adapter = this.getAdapter()!!;

    val pairedDevices = adapter.bondedDevices;
    var responseArray = Arguments.createArray();

    pairedDevices.forEach { device ->
      val responseObj = Arguments.createMap();
      responseObj.putString("name", device.name);
      responseObj.putString("address", device.address);
      // responseObj.putInt("type", device.type); Only in API version 18
      responseObj.putInt("bondState", device.bondState);

      val idList = Arguments.createArray();
      device.uuids.forEach { id ->
        idList.pushString(id.toString())
      };

      responseObj.putArray("ids", idList);

      responseArray.pushMap(responseObj);
    };

    promise.resolve(responseArray);
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
