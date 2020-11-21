package com.reactnativetopusescpos

import android.hardware.usb.*
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.lang.RuntimeException

class USBConnection(private val device: UsbDevice, private val usbManager: UsbManager) : ESCPOSConnection {
	private var claimedInterface: UsbInterface? = null;
	private var outEndpoint: UsbEndpoint? = null;
	private var usbDeviceConnection: UsbDeviceConnection? = null;
	private var outputStream: AndroidUSBOutputStream? = null;

	override val IsConnected: Boolean
		get() {
			return this.outputStream != null;
		}

	override val IsReconnecting: Boolean
		get() = false

	override val SupportsAutoReconnection: Boolean
		get() = false

	override val CanReconnect: Boolean
		get() = false

	override fun connect() {
		for (i in 0 until this.device.interfaceCount) {
			val iface = this.device.getInterface(i);

			Log.d(TopusEscposModule.TAG, "Found interface $i: ${iface.name} - ${iface.endpointCount} endpoints, Class=${iface.interfaceClass}, Protocol=${iface.interfaceProtocol}, Subclass=${iface.interfaceSubclass}")

			for (j in 0 until iface.endpointCount) {
				val endpoint = iface.getEndpoint(j);

				Log.d(TopusEscposModule.TAG, "Found endpoint $j: ${endpoint.address}, Dir:${endpoint.direction}, Type=${endpoint.type}, MaxPacketSize=${endpoint.maxPacketSize}, Interval=${endpoint.interval}, Number=${endpoint.endpointNumber}")

				if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.direction == UsbConstants.USB_DIR_OUT) {
					Log.d(TopusEscposModule.TAG, "Found suitable endpoint ${endpoint.address} ($j) for interface ${iface.name} ($i)")

					this.usbDeviceConnection = this.usbManager.openDevice(this.device);

					if (!this.usbDeviceConnection!!.claimInterface(iface, true)) {
						Log.w(TopusEscposModule.TAG, "Failed to claim interface $i (${iface.name}) Skipping.");
						this.usbDeviceConnection = null;
						continue;
					}

					this.claimedInterface = iface;
					this.outEndpoint = endpoint;

					this.outputStream = AndroidUSBOutputStream(endpoint, this.usbDeviceConnection!!);
					Log.d(TopusEscposModule.TAG, "RETURNING");
					return;
				}
			}
		}

		throw IOException("Unable to connect to USB device, no output endpoint found");
	}

	override fun attemptReconnect() {
		throw RuntimeException("The USB connection does not support reconnection");
	}

	override fun disconnect() {
		if (!this.IsConnected) {
			return;
		}

		this.outputStream!!.close();

		this.usbDeviceConnection!!.releaseInterface(this.claimedInterface);
		this.usbDeviceConnection!!.close();

		this.outputStream = null;
		this.usbDeviceConnection = null;
		this.outEndpoint = null;
		this.claimedInterface = null;
	}

	override fun getOutputStream(): OutputStream? {
		return this.outputStream;
	}
}
