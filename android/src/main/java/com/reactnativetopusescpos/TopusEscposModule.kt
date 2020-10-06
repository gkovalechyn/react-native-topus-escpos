package com.reactnativetopusescpos

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import android.bluetooth.BluetoothAdapter

class TopusEscposModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return "TopusEscpos"
  }


  // Example method
  // See https://facebook.github.io/react-native/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Int, b: Int, promise: Promise) {

    promise.resolve(a * b)
  }

  @ReactMethod
  fun isBluetoothEnabled(promise: Promise) {

  }

  @ReactMethod
  fun findDevices(promise: Promise) {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter();

    if (bluetoothAdapter == null) {
      promise.reject(ErrorCode.BLUETOOTH_NOT_AVAILABLE.code, "Bluetooth is not available for this device");
      return;
    }


  }

  @ReactMethod
  fun enableBluetooth(promise: Promise) {

  }


}
