package com.reactnativetopusescpos

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothConnection(device: BluetoothDevice) : ESCPOSConnection {
	private val device: BluetoothDevice = device;

	// https://stackoverflow.com/questions/4632524/how-to-find-the-uuid-of-serial-port-bluetooth-device/11622794#11622794
	// https://stackoverflow.com/questions/13964342/android-how-do-bluetooth-uuids-work
	// https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	private var socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

	private var isReconnecting = false;
	private var remainingReconnectionAttempts = 1;

	override val IsConnected: Boolean
		get() = this.socket.isConnected;

	override val IsReconnecting: Boolean
		get() = isReconnecting;

	override val SupportsAutoReconnection: Boolean
		get() = true;

	override val CanReconnect: Boolean
		get() = this.remainingReconnectionAttempts > 0;

	@Throws(IOException::class)
	override fun connect() {
		this.socket.connect();
		this.remainingReconnectionAttempts = 1; // Reset to 1 on successful reconnect
	}

	@Throws(IOException::class)
	override fun attemptReconnect() {
		this.remainingReconnectionAttempts--;

		this.socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		this.connect();
	}

	override fun disconnect() {
		if (this.socket.isConnected) {
			try {
				this.socket.close();
			} catch (exception: IOException) {
				// Ignore IO exceptions in this case because they would be thrown when the socket closes.
			}
		}
	}

	override fun getOutputStream(): OutputStream? {
		return if (this.socket.isConnected) {
			socket.outputStream;
		} else {
			null;
		}
	}
}
