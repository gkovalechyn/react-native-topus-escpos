package com.reactnativetopusescpos

import java.io.OutputStream

interface ESCPOSConnection {
	val IsConnected: Boolean;

	val IsReconnecting: Boolean;

	val SupportsAutoReconnection: Boolean;

	val CanReconnect: Boolean;

	fun connect();

	fun attemptReconnect();

	fun disconnect();

	fun getOutputStream(): OutputStream?;
}
