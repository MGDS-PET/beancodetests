package com.startechplus.unityblebridge;

import java.util.HashMap;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public class BluetoothPeripheral 
{
	public BluetoothDevice device;
	public int rssi;
	public byte[] scanRecord;
	public AdvertisedData advertisedData;
	public BluetoothGatt gatt;
	public HashMap<String, PeripheralCharacteristic> characteristics = new HashMap<String, PeripheralCharacteristic>();
	
	public BluetoothPeripheral(BluetoothDevice _device, int _rssi, byte[] _scanRecord)
	{
		device = _device;
		rssi = _rssi;
		scanRecord = _scanRecord;
		advertisedData = Utils.parseAdertisedData(scanRecord);
	}
}
