import * as React from "react";
import { View, Text, Button } from "react-native";
import { TopusEscpos, Device } from "react-native-topus-escpos";

type AppState = {
	devices: Device[];
	isLoadingDevices: boolean;

	isFindingDevices: boolean;
	foundDevices: Device[];
};

export default class App extends React.Component<{}, AppState> {
	readonly state: AppState = {
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
						<Text key={device.address}>
							{device.name} - {device.address}
						</Text>
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
			</View>
		);
	}

	public componentDidMount() {
		this.loadDevices();
		this.findDevices();
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

	private findDevices() {
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
