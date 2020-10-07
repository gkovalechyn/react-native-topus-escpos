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

	deviceSupportsAutoReconnection(): Promise<boolean>;

	connectToBluetoothDevice(address: string): Promise<void>;

	disconnect(): Promise<void>;

	writeLine(data: string): Promise<void>;

	feed(amount: number): Promise<void>;

	cut(): Promise<void>;

	fancyText(data: string): Promise<void>;

	barcode(data: string): Promise<void>;
};

const TopusEscpos = NativeModules.TopusEscpos as TopusEscposType;

export { TopusEscpos };
