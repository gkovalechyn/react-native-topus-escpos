import * as React from "react";
import {
	View,
	Text,
	Button,
	AppState,
	NativeEventEmitter,
	NativeModules,
	EventSubscription,
} from "react-native";
import {
	TopusEscpos,
	BluetoothDevice,
	BarcodeHRIPosition,
	Justification,
	BarcodeHRIFont,
	Underline,
	FontSize,
	FontName,
	BarcodeType,
	CharCode,
	ColorMode,
} from "react-native-topus-escpos";

type State = {
	devices: BluetoothDevice[];
	isLoadingDevices: boolean;
	selectedDevice?: BluetoothDevice;

	isConnecting: boolean;
	writeState: string;

	isFindingDevices: boolean;
	foundDevices: BluetoothDevice[];
};

export default class App extends React.Component<{}, State> {
	readonly state: State = {
		isLoadingDevices: false,
		devices: [],

		writeState: "",

		isFindingDevices: false,
		isConnecting: false,
		foundDevices: [],
	};

	public render() {
		return (
			<View>
				<Text>{this.state.isLoadingDevices ? "TRUE" : "FALSE"}</Text>
				{this.state.devices.map((device) => {
					return (
						<Button
							title={device.name}
							key={device.address}
							onPress={() => {
								this.setState({ isConnecting: true });
								TopusEscpos.connectToBluetoothDevice(device.address).finally(
									() => {
										this.setState({ isConnecting: false });
									}
								);
								this.setState({ selectedDevice: device });
							}}
						/>
					);
				})}

				<Text>Connecting? {this.state.isConnecting ? "true" : "false"}</Text>

				<Text>
					Finding devices? {this.state.isFindingDevices ? "true" : "false"}
				</Text>

				<Text>Write state: {this.state.writeState}</Text>

				{this.state.foundDevices.map((device) => {
					return (
						<Text key={device.address}>
							{device.name} - {device.address}
						</Text>
					);
				})}

				<Button title="Discover" onPress={() => this.findDevices()} />

				<Button
					title="Set character table"
					onPress={() => {
						TopusEscpos.setCharacterCodeTable(CharCode.KATAKANA);
					}}
				/>

				<Button
					title="Test write"
					onPress={async () => {
						this.setState({ writeState: "Before" });

						await TopusEscpos.write("áéíóúçã", {});

						this.setState({ writeState: "After first" });

						await TopusEscpos.writeLine("Part 2", { isBold: true });

						this.setState({ writeState: "After second" });

						await TopusEscpos.write("Part 3", {
							isBold: true,
							underline: Underline.ONE_DOT_THICK,
						});

						this.setState({ writeState: "After third" });

						await TopusEscpos.write("Part 4", {
							isBold: true,
							underline: Underline.TWO_DOT_THICK,
							fontSize: { width: FontSize.SIZE_3, height: FontSize.SIZE_5 },
						});

						this.setState({ writeState: "After fourth" });

						await TopusEscpos.write("Part 5", {
							underline: Underline.TWO_DOT_THICK,
							fontSize: { width: FontSize.SIZE_5, height: FontSize.SIZE_5 },
						});

						this.setState({ writeState: "After fifth" });

						await TopusEscpos.feed(1);

						this.setState({ writeState: "After feed" });

						await TopusEscpos.writeLine("FONT A", {
							fontName: FontName.FONT_A,
						});

						this.setState({ writeState: "After font a" });

						await TopusEscpos.writeLine("FONT B", {
							fontName: FontName.FONT_B,
						});

						this.setState({ writeState: "After font b" });

						await TopusEscpos.writeLine("FONT C", {
							colorMode: ColorMode.WHITE_ON_BLACK,
						});

						this.setState({ writeState: "After font c" });
					}}
				/>

				<Button
					title="Barcode"
					onPress={async () => {
						await TopusEscpos.barcode("{ABC123510283", {
							type: BarcodeType.CODE128,
							justification: Justification.CENTER,
							hriFont: BarcodeHRIFont.FONT_B,
							hriPosition: BarcodeHRIPosition.BELOW,
						});
					}}
				/>

				<Button
					title="Log something"
					onPress={async () => {
						console.log("AAAAAAA");
						console.log("AAAAAAA");
						console.log("AAAAAAA");
						console.log("AAAAAAA");
						console.log("AAAAAAA");
					}}
				/>
			</View>
		);
	}

	private connectedEventListener?: EventSubscription;
	private disconnectedEventListener?: EventSubscription;

	public componentDidMount() {
		this.loadDevices();
		this.findDevices();

		console.log("COMPONENT MOUNTED");

		AppState.addEventListener("change", (newState) => {
			console.log("New app state changed to: " + newState);

			if (newState === "active" && this.state.selectedDevice) {
				TopusEscpos.connectToBluetoothDevice(this.state.selectedDevice.address);
			}

			if (newState === "inactive" || newState === "background") {
				TopusEscpos.disconnect();
			}
		});

		const eventEmitter = new NativeEventEmitter(NativeModules.TopusEscpos);
		this.connectedEventListener = eventEmitter.addListener(
			TopusEscpos.EVENT_CONNECTED,
			this.onConnected.bind(this)
		);

		this.disconnectedEventListener = eventEmitter.addListener(
			TopusEscpos.EVENT_DISCONNECTED,
			this.onDisconnected.bind(this)
		);
	}

	private onConnected() {
		console.log("Connected");
	}

	private onDisconnected() {
		console.log("Disconnected");
	}

	private async loadDevices() {
		const isBluetoothSupported = await TopusEscpos.isBluetoothSupported();
		console.log(`Is bluetooth supported: ${isBluetoothSupported}`);

		const isBluetoothEnabled = await TopusEscpos.isBluetoothEnabled();
		console.log(`Is bluetooth enabled: ${isBluetoothEnabled}`);

		if (!isBluetoothEnabled) {
			await TopusEscpos.enableBluetooth();
		}

		this.setState({ isLoadingDevices: true });
		TopusEscpos.getBluetoothPairedDevices()
			.then((devices) => {
				console.log(JSON.stringify(devices));
				this.setState({ devices: devices });
			})
			.finally(() => {
				this.setState({ isLoadingDevices: false });
			});
	}

	private async findDevices() {
		const isConnected = await TopusEscpos.isConnected();

		if (!isConnected) {
			this.setState({ isFindingDevices: true });

			TopusEscpos.findBluetoothDevices()
				.then((devices) => {
					console.log(devices);
					this.setState({ foundDevices: devices });
				})
				.finally(() => {
					this.setState({ isFindingDevices: false });
				});
		}
	}

	public componentWillUnmount() {
		if (this.connectedEventListener) {
			this.connectedEventListener.remove();
		}

		if (this.disconnectedEventListener) {
			this.disconnectedEventListener.remove();
		}
	}
}
