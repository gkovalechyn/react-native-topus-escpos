package com.reactnativetopusescpos

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import java.lang.RuntimeException

const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
typealias PermissionCallback = (device: UsbDevice, result: Boolean) -> Unit

class USBService(private val context: Context) {
	private val usbReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (ACTION_USB_PERMISSION == intent.action) {
				val device: UsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
				val callback = callbackMap[device];
				val succeeded = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

				if (callback != null) {
					callbackMap.remove(device);

					callback.invoke(device, succeeded);
				} else {
					Log.w(TopusEscposModule.TAG, "Received USB permission event for usb device but there was no callback registered");
				}
			}
		}
	}

	private val callbackMap = HashMap<UsbDevice, PermissionCallback>();

	init {
		val filter = IntentFilter(ACTION_USB_PERMISSION)
		context.registerReceiver(this.usbReceiver, filter);
	}

	val AndroidUsbManager: UsbManager
		get() = context.getSystemService(Context.USB_SERVICE) as UsbManager;


	fun getDevices(): Map<String, UsbDevice> {
		return this.AndroidUsbManager.deviceList
	}

	fun requestPermissionToConnect(device: UsbDevice, callback: PermissionCallback) {
		if (this.callbackMap.containsKey(device)) {
			throw RuntimeException("Permission request already in progress for device: ${device.deviceName}")
		}

		this.callbackMap[device] = callback;

		val permissionIntent = PendingIntent.getBroadcast(this.context, 0, Intent(ACTION_USB_PERMISSION), 0);
		this.AndroidUsbManager.requestPermission(device, permissionIntent);
	}

	fun connectToDevice(device: UsbDevice): USBConnection {
		return USBConnection(device, this.AndroidUsbManager);
	}
}
