import * as React from "react";
import { View, Text, Button, AppState } from "react-native";
import { FontSize, FontName, BarcodeType, CharCode } from "../../src/index";
import {
	TopusEscpos,
	BluetoothDevice,
	BarcodeHRIPosition,
	Justification,
	BarcodeHRIFont,
	Underline,
} from "react-native-topus-escpos";

type State = {
	devices: BluetoothDevice[];
	isLoadingDevices: boolean;
	selectedDevice?: BluetoothDevice;

	isFindingDevices: boolean;
	foundDevices: BluetoothDevice[];
};

export default class App extends React.Component<{}, State> {
	readonly state: State = {
		isLoadingDevices: false,
		devices: [],

		isFindingDevices: false,
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
								TopusEscpos.connectToBluetoothDevice(device.address);
								this.setState({ selectedDevice: device });
							}}
						/>
					);
				})}

				<Text>
					Finding devices? {this.state.isFindingDevices ? "true" : "false"}
					{this.state.foundDevices.map((device) => {
						return (
							<Text key={device.address}>
								{device.name} - {device.address}
							</Text>
						);
					})}
				</Text>

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
						await TopusEscpos.write("áéíóúçã", {});
						await TopusEscpos.writeLine("Part 2", { isBold: true });
						await TopusEscpos.write("Part 3", {
							isBold: true,
							underline: Underline.ONE_DOT_THICK,
						});
						await TopusEscpos.write("Part 4", {
							isBold: true,
							underline: Underline.TWO_DOT_THICK,
							fontSize: { width: FontSize.SIZE_3, height: FontSize.SIZE_5 },
						});
						await TopusEscpos.write("Part 5", {
							underline: Underline.TWO_DOT_THICK,
							fontSize: { width: FontSize.SIZE_5, height: FontSize.SIZE_5 },
						});
						await TopusEscpos.writeLine("", {});

						await TopusEscpos.writeLine("FONT A", {
							fontName: FontName.FONT_A,
						});
						await TopusEscpos.writeLine("FONT B", {
							fontName: FontName.FONT_B,
						});
						await TopusEscpos.writeLine("FONT C", {
							fontName: FontName.FONT_C,
						});
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
			</View>
		);
	}

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
}
