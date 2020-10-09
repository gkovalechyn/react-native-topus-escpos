import { NativeModules } from "react-native";

export type BluetoothDevice = {
	name: string;
	address: string;
	bondState: number;

	ids: Array<string>;
};

export enum CutMode {
	FULL = 48,
	PARTIAL = 49,
}

export enum FontSize {
	SIZE_1 = 0,
	SIZE_2 = 1,
	SIZE_3 = 2,
	SIZE_4 = 3,
	SIZE_5 = 4,
	SIZE_6 = 5,
	SIZE_7 = 6,
	SIZE_8 = 7,
}

export enum Underline {
	NONE = 48,
	ONE_DOT_THICK = 49,
	TWO_DOT_THICK = 50,
}

export enum ColorMode {
	BLACK_ON_WHITE = 0,
	WHITE_ON_BLACK = 1,
}

export enum FontName {
	FONT_A = 48,
	FONT_B = 49,
	FONT_C = 50,
}

export enum Justification {
	LEFT = 48,
	CENTER = 49,
	RIGHT = 50,
}

export interface FontSizeObj {
	width?: FontSize;
	height?: FontSize;
}

export interface WriteOptions {
	isBold?: boolean;
	colorMode?: ColorMode;
	fontName?: FontName;
	fontSize?: FontSizeObj;
	justification?: Justification;
	lineSpacing?: number;
	underline?: Underline;
}

export enum BarcodeType {
	UPCA = 0,
	UPCA_B = 65,
	UPCE_A = 1,
	UPCE_B = 66,
	JAN13_A = 2,
	JAN13_B = 67,
	JAN8_A = 3,
	JAN8_B = 68,
	CODE39_A = 4,
	CODE39_B = 69,
	ITF_A = 5,
	ITF_B = 70,
	CODABAR_A = 6,
	CODABAR_B = 71,
	CODE93 = 72,
	CODE128 = 73,
}

export enum BarcodeHRIPosition {
	NOT_PRINTED = 48,
	ABOVE = 49,
	BELOW = 50,
	ABOVE_AND_BELOW = 51,
}

export enum BarcodeHRIFont {
	FONT_A = 48,
	FONT_B = 49,
	FONT_C = 50,
}

export type BarcodeOptions = {
	type?: BarcodeType;
	hriPosition?: BarcodeHRIPosition;
	hriFont?: BarcodeHRIFont;
	justification?: Justification;
};

export enum CharCode {
	CP437_USA_STANDARD_EUROPE = 0,
	KATAKANA = 1,
	CP850_MULTILINGUAL = 2,
	CP860_PORTUGUESE = 3,
	CP863_CANADIAN_FRENCH = 4,
	CP865_NORDIC = 5,
	CP851_GREEK = 11,
	CP853_TURKISH = 12,
	CP857_TURKISH = 13,
	CP737_GREEK = 14,
	ISO8859_7_GREEK = 15,
	WPC1252 = 16,
	CP866_CYRILLIC_2 = 17,
	CP852_LATIN2 = 18,
	CP858_EURO = 19,
	KU42_THAI = 20,
	TIS11_THAI = 21,
	TIS18_THAI = 26,
	TCVN_3_1_VIETNAMESE = 30,
	TCVN_3_2_VIETNAMESE = 31,
	PC720_ARABIC = 32,
	WPC775_BALTICRIM = 33,
	CP855_CYRILLIC = 34,
	CP861_ICELANDIC = 35,
	CP862_HEBREW = 36,
	CP864_ARABIC = 37,
	CP869_GREEK = 38,
	ISO8859_2_LATIN2 = 39,
	ISO8859_15_LATIN9 = 40,
	CP1098_FARSI = 41,
	CP1118_LITHUANIAN = 42,
	CP1119_LITHUANIAN = 43,
	CP1125_UKRAINIAN = 44,
	WCP1250_LATIN2 = 45,
	WCP1251_CYRILLIC = 46,
	WCP1253_GREEK = 47,
	WCP1254_TURKISH = 48,
	WCP1255_HEBREW = 49,
	WCP1256_ARABIC = 50,
	WCP1257_BALTICRIM = 51,
	WCP1258_VIETNAMESE = 52,
	KZ_1048_KAZAKHSTAN = 53,
	USER_DEFINED_PAGE = 255,
}

type TopusEscposType = {
	isBluetoothSupported(): Promise<boolean>;

	isBluetoothEnabled(): Promise<boolean>;

	findBluetoothDevices(): Promise<BluetoothDevice[]>;

	enableBluetooth(): Promise<void>;

	getBluetoothPairedDevices(): Promise<BluetoothDevice[]>;

	isConnected(): Promise<boolean>;

	isReconnecting(): Promise<boolean>;

	deviceSupportsAutoReconnection(): Promise<boolean>;

	connectToBluetoothDevice(address: string): Promise<void>;

	disconnect(): Promise<void>;

	write(data: string, style: WriteOptions): Promise<void>;

	writeDefaultStyle(data: string): Promise<void>;

	writeLine(data: string, style: WriteOptions): Promise<void>;

	writeLineDefaultStyle(data: string): Promise<void>;

	setDefaultStyle(style: WriteOptions): Promise<void>;

	writeBarcode(data: string, options: BarcodeOptions): Promise<void>;

	/**
	 * Sets the encoding used to transform the string into bytes and the printer code page according to the given character
	 * code. If your printer uses a different code page or encoding than specified please use @see setPrinterCodePage and @see setStringEncoding
	 *
	 * @param code The codepage to set to
	 */
	setCharacterCodeTable(code: CharCode): Promise<void>;

	setPrinterCodePage(page: number): Promise<void>;

	setStringEncoding(encoding: string): Promise<void>;

	feed(amount: number): Promise<void>;

	cut(mode: CutMode): Promise<void>;

	barcode(data: string, options: BarcodeOptions): Promise<void>;

	// Error codes
	BLUETOOTH_NOT_AVAILABLE: string;
	BLUETOOTH_NOT_ENABLED: string;
	BLUETOOTH_ENABLE_REQ_IN_PROGRESS: string;
	USER_NOT_ENABLED_BLUETOOTH: string;
	ALREADY_DISCOVERING_BLUETOOTH_DEVICES: string;
	CANNOT_DISCOVER_WHILE_CONNECTED: string;
	PERMISSION_REQUIRED: string;
	DEVICE_NOT_FOUND: string;
	DEVICE_NOT_PAIRED: string;
	NOT_CONNECTED: string;
	CONNECTION_LOST: string;
	INVALID_CODE_PAGE: string;

	// Event codes
	EVENT_CONNECTED: string;
	EVENT_DISCONNECTED: string;
};

const TopusEscpos = NativeModules.TopusEscpos as TopusEscposType;

export { TopusEscpos };
