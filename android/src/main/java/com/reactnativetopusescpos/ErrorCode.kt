package com.reactnativetopusescpos;

public enum class ErrorCode(val code: String) {
	BLUETOOTH_NOT_AVAILABLE("bluetooth-not-available"),
	BLUETOOTH_NOT_ENABLED("bluetooth-not-enabled"),
	BLUETOOTH_ENABLE_REQ_IN_PROGRESS("bluetooth-enable0-request-in-progress"),
	USER_NOT_ENABLED_BLUETOOTH("user-not-enabled-bluetooth"),
	ALREADY_DISCOVERING_BLUETOOTH_DEVICES("already-discovering-bluetooth-devices"),
	CANNOT_DISCOVER_WHILE_CONNECTED("cannot-discover-while-connected"),
	PERMISSION_REQUIRED("permission-required"),
	DEVICE_NOT_FOUND("device-not-found"),
	DEVICE_NOT_PAIRED("device-not-paired"),
	NOT_CONNECTED("not-connected"),
	CONNECTION_LOST("connection-lost"),
	INVALID_CODE_PAGE("invalid-code-page")
}
