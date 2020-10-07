package com.reactnativetopusescpos

import java.io.OutputStream

class USBConnection : ESCPOSConnection {
	override val IsConnected: Boolean
		get() = TODO("Not yet implemented")

	override val IsReconnecting: Boolean
		get() = false

	override val SupportsAutoReconnection: Boolean
		get() = false

	override val CanReconnect: Boolean
		get() = TODO("Not yet implemented")

	override fun connect() {
		TODO("Not yet implemented")
	}

	override fun attemptReconnect() {
		TODO("Not yet implemented")
	}

	override fun disconnect() {
		TODO("Not yet implemented")
	}

	override fun getOutputStream(): OutputStream? {
		TODO("Not yet implemented")
	}
}
