package com.reactnativetopusescpos

import android.bluetooth.BluetoothDevice
import java.io.OutputStream

class BluetoothConnection: ESCPOSConnection {
  var connectedDevice: BluetoothDevice? = null;

  override val IsConnected: Boolean
    get() = this.connectedDevice != null;

  override val IsReconnecting: Boolean
    get() = TODO("Not yet implemented")

  override val SupportsAutoReconnection: Boolean
    get() = TODO("Not yet implemented")

  override fun getOutputStream(): OutputStream? {
    TODO("Not yet implemented")
  }
}
