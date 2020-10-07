import * as React from "react";
import { View, Text, Button, AppState } from "react-native";
import { TopusEscpos, Device } from "react-native-topus-escpos";

type State = {
	devices: Device[];
	isLoadingDevices: boolean;
	selectedDevice?: Device;

	isFindingDevices: boolean;
	foundDevices: Device[];
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
					title="Test write"
					onPress={async () => {
						for (let i = 0; i < 3; i++) {
							await TopusEscpos.fancyText("SOME DATA");
							await TopusEscpos.writeLine("Normal text");
						}

						await TopusEscpos.feed(10);

						await TopusEscpos.cut();

						await TopusEscpos.feed(5);
					}}
				/>

				<Button
					title="Barcode"
					onPress={async () => {
						await TopusEscpos.barcode("Test barcode");
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
