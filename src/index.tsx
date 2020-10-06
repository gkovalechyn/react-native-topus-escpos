import { NativeModules } from "react-native";

export type Device = {
	name: string;
	address: string;
	bondState: number;

	ids: Array<string>;
};

type TopusEscposType = {
	isBluetoothSupported(): Promise<boolean>;

	isBluetoothEnabled(): Promise<boolean>;

	findBluetoothDevices(): Promise<Device[]>;

	enableBluetooth(): Promise<void>;

	getBluetoothPairedDevices(): Promise<Device[]>;

	isConnected(): Promise<boolean>;

	isReconnecting(): Promise<boolean>;
};

const TopusEscpos = NativeModules.TopusEscpos as TopusEscposType;

export { TopusEscpos };
