package com.reactnativetopusescpos

import java.io.OutputStream

interface ESCPOSConnection {
  val IsConnected: Boolean;

  val IsReconnecting: Boolean;

  val SupportsAutoReconnection: Boolean;

  fun getOutputStream(): OutputStream?;
}
