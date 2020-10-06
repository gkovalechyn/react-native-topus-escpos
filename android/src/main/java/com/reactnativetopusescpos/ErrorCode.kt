package com.reactnativetopusescpos;

public enum class ErrorCode(val code: String) {
	BLUETOOTH_NOT_AVAILABLE("bluetooth-not-available"),
	BLUETOOTH_NOT_ENABLED("bluetooth-not-enabled"),
	BLUETOOTH_ENABLE_REQ_IN_PROGRESS("bluetooth-enable0-request-in-progress"),
	USER_NOT_ENABLED_BLUETOOTH("user-not-enabled-bluetooth"),
	ALREADY_DISCOVERING_BLUETOOTH_DEVICES("already-discovering-bluetooth-devices"),
	CANNOT_DISCOVER_WHILE_CONNECTED("cannot-discover-while-connected"),
	PERMISSION_REQUIRED("permission-required"),
	PERMISSION_REQ_IN_PROGRESS("permission-request-in-progress"),
	PERMISSION_DENIED("permission-denied")
}
