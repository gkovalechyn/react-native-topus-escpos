package com.reactnativetopusescpos;

public enum class ErrorCode(val code: String) {
  BLUETOOTH_NOT_AVAILABLE("bluetooth-not-available"),
  BLUETOOTH_NOT_ENABLED("bluetooth-not-enabled"),
  BLUETOOTH_ENABLE_REQ_IN_PROGRESS("bluetooth-enable0-request-in-progress"),
  USER_NOT_ENABLED_BLUETOOTH("user-not-enabled-bluetooth")
}
