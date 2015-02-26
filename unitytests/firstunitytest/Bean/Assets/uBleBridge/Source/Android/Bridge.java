package com.startechplus.unityblebridge;

import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.HashMap;

public class Bridge {

	private HashMap<String, BluetoothPeripheral> peripherals = new HashMap<String, BluetoothPeripheral>();
	

	Context context;
	private static final String TAG = "UnityBleBridge";
	
	private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	public static String gameObjectName = "BleBridge";
	private static final long SCAN_PERIOD = 60000;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning = false;
	private Handler mHandler;
	private PeripheralFilter peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);

	static Bridge _instance = null;

	public Bridge() {
		_instance = this;
		mHandler = new Handler(Looper.getMainLooper());
	}

	public static Bridge instance() {
		if (_instance == null)
			_instance = new Bridge();

		return _instance;

	}

	public void setContext(Context ctx) {
		this.context = ctx;
		Log.i(TAG, "Application context set...");
	}

	private static final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate", "Powered Off");
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate", "Unknown");
					break;
				case BluetoothAdapter.STATE_ON:
					UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate", "Powered On");
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate", "Unknown");
					break;
				}
			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				//BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			} else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				//BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				//BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			}
		}
	};

	public static void RegisterBroadcastReciever(Activity activity) {
		// Register for broadcasts on BluetoothAdapter state change
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		activity.registerReceiver(mReceiver, filter);
	}

	public static void DeregisterBroadcastReciever(Activity activity) {
		activity.unregisterReceiver(mReceiver);
	}

	/*
	 * public static boolean onActivityResult(int requestCode, int resultCode,
	 * Intent data){ boolean retVal = true;
	 * 
	 * switch (requestCode) { case ActivityResultCodes.REQUEST_ENABLE_BT:
	 * if(resultCode == Activity.RESULT_OK) {
	 * UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate",
	 * "Powered On"); } else { UnityPlayer.UnitySendMessage (gameObjectName,
	 * "OnStateUpdate", "Powered Off"); } break;
	 * 
	 * default: retVal = false; break; }
	 * 
	 * return retVal; }
	 */

	private void onUpdatePeripheral(BluetoothPeripheral peripheral, String event, String identifier) {
		String message;

		String ident = identifier == null ? "Unknown" : identifier;

		Iterator<Entry<java.lang.String, BluetoothPeripheral>> it = peripherals.entrySet().iterator();

		while (it.hasNext()) {
			Entry<java.lang.String, BluetoothPeripheral> kvp = it.next();

			BluetoothPeripheral listPeripheral = kvp.getValue();

			if (listPeripheral.equals(peripheral)) {

				message = String.format("%d:%s%d:%s", kvp.getKey().length(), kvp.getKey(), ident.length(), ident);

				UnityPlayer.UnitySendMessage(gameObjectName, event, message);

				return;
			}
		}

		String newKey = UUID.randomUUID().toString();

		if (peripheral.device.getAddress() != null && peripheral.device.getAddress().length() > 0)
			newKey = peripheral.device.getAddress();

		Log.i(TAG, "onUpdatePeripheral() : adding peripheral : " + newKey );
		peripherals.put(newKey, peripheral);

		message = String.format("%d:%s%d:%s", newKey.length(), newKey, ident.length(), ident);

		UnityPlayer.UnitySendMessage(gameObjectName, event, message);

	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			

			BluetoothPeripheral peripheral = new BluetoothPeripheral(device, rssi, scanRecord);

			String deviceName = device.getName();

			if (deviceName == null) {
				deviceName = peripheral.advertisedData.getName();
			}
						
			if(peripheralFilter.filterWith(peripheral.device.getAddress(), PeripheralFilter.PERIPHERAL_UUID))
			{
				Log.i(TAG, "onLeScan() : " + device.getAddress().toString() + ", " + rssi + ", matched filter...");
				onUpdatePeripheral(peripheral, "OnDiscoveredPeripheral", deviceName);
			}
			else
			{
				Log.i(TAG, "onLeScan() : " + device.getAddress().toString() + ", " + rssi + ", mismatched filter...");
			}
		}
	};

	public void startup(String goName, boolean asCentral) {
		Log.i(TAG, "startup(" + goName + ", " + asCentral + ")");

		gameObjectName = goName;

		if (context == null) {
			Log.i(TAG, "startup() : " + "context == null");
			return;
		}

		UnityPlayer.UnitySendMessage(gameObjectName, "OnStartup", "Active");

		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter();
			RegisterBroadcastReciever(UnityPlayer.currentActivity);

			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				Log.i(TAG, "startup() : " + "ble disabled, asking user...");

				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				UnityPlayer.currentActivity.startActivityForResult(enableBtIntent, ActivityResultCodes.REQUEST_ENABLE_BT);
			} else {
				Log.i(TAG, "startup() : " + "ble enabled...");

				UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate", "Powered On");
			}
		} else {

			Log.i(TAG, "startup() : " + "ble not supported...");

			UnityPlayer.UnitySendMessage(gameObjectName, "OnStateUpdate", "Unsupported");
		}

	}
	
	private void sendCharacteristicUpdate(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
	{
		String cValue = Base64.encodeToString(characteristic.getValue(), Base64.DEFAULT);
		
		String message = String.format("%d:%s%d:%s%d:%s", gatt.getDevice().getAddress().length(), gatt.getDevice().getAddress(), characteristic.getUuid().toString().length(), characteristic.getUuid().toString(), cValue.length(), cValue);
                    
        UnityPlayer.UnitySendMessage(gameObjectName, "OnBluetoothData", message);
	}
	

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
		{
			Log.i(TAG, "onCharacteristicChanged() : " + characteristic.getUuid().toString());
			sendCharacteristicUpdate(gatt, characteristic);
		}
		
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
		{
			if(status == BluetoothGatt.GATT_SUCCESS)
			{
				Log.i(TAG, "onCharacteristicRead() : " + characteristic.getUuid().toString());
				sendCharacteristicUpdate(gatt, characteristic);
			}
			else
			{
				Log.i(TAG, "onCharacteristicRead() : failed : " + status);
			}
		}
		
		@Override
		public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
		{
			if(status == BluetoothGatt.GATT_SUCCESS)
			{
				Log.i(TAG, "onCharacteristicWrite() : " + characteristic.getUuid().toString());
				
				String peripheralAddress = gatt.getDevice().getAddress();
										
				String message = String.format("%d:%s%d:%s", peripheralAddress.length(), peripheralAddress, characteristic.getUuid().toString().length(), characteristic.getUuid().toString() );
			    				
			    UnityPlayer.UnitySendMessage (gameObjectName, "OnDidWriteCharacteristic", message);
				
			}
			else
			{
				Log.i(TAG, "onCharacteristicWrite() : failed : " + status);
			}
		}
		
		@Override
		public void onDescriptorRead (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
		{
			BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
			
			String dValue = Base64.encodeToString(descriptor.getValue(), Base64.DEFAULT);
			
			String message = String.format("%d:%s%d:%s%d:%s", gatt.getDevice().getAddress().length(), gatt.getDevice().getAddress(), characteristic.getUuid().toString().length(), characteristic.getUuid().toString(), descriptor.getUuid().toString().length(), descriptor.getUuid().toString(), dValue.length(), dValue);
	                    
	        UnityPlayer.UnitySendMessage(gameObjectName, "OnDescriptorRead", message);
		}
		
		@Override
		public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
		{
			if(status == BluetoothGatt.GATT_SUCCESS)
			{
				BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
				
				Log.i(TAG, "onDescriptorWrite() : " + descriptor.getUuid().toString());
				
				String peripheralAddress = gatt.getDevice().getAddress();
										
				String message = String.format("%d:%s%d:%s%d:%s", peripheralAddress.length(), peripheralAddress, characteristic.getUuid().toString().length(), characteristic.getUuid().toString(), descriptor.getUuid().toString().length(), descriptor.getUuid().toString() );
			    				
			    UnityPlayer.UnitySendMessage (gameObjectName, "OnDidWriteDescriptor", message);
				
			}
			else
			{
				Log.i(TAG, "onDescriptorWrite() : failed : " + status);
			}
		}
		
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

			Log.i(TAG, "onConnectionStateChange()");

			// String intentAction;

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "onConnectionStateChange() : BluetoothProfile.STATE_CONNECTED");

				BluetoothDevice device = gatt.getDevice();

				BluetoothPeripheral peripheral = peripherals.get(device.getAddress());

				if (peripheral != null) {
					Log.i(TAG, "onConnectionStateChange() : BluetoothProfile.STATE_CONNECTED = " + peripheral.device.getAddress());

					peripheral.gatt = gatt;
					onUpdatePeripheral(peripheral, "OnConnectedPeripheral", peripheral.advertisedData.getName());
					gatt.discoverServices();

				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

				Log.i(TAG, "onConnectionStateChange() : BluetoothProfile.STATE_DISCONNECTED");

				BluetoothDevice device = gatt.getDevice();

				BluetoothPeripheral peripheral = peripherals.get(device.getAddress());

				if (peripheral != null)
					onUpdatePeripheral(peripheral, "OnDisconnectedPeripheral", peripheral.advertisedData.getName());
			}
		}

		@Override
		// New services discovered
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			Log.i(TAG, "onServicesDiscovered()");

			if (status == BluetoothGatt.GATT_SUCCESS) {
				List<BluetoothGattService> services = gatt.getServices();
				for (int i = 0; i < services.size(); i++) {

					String peripheralId = "Unknown";

					BluetoothPeripheral peripheral = peripherals.get(gatt.getDevice().getAddress());

					if (peripheral != null)
						peripheralId = peripheral.device.getAddress();

					String sUuid = services.get(i).getUuid().toString();

					if(peripheralFilter.filterWith(sUuid, PeripheralFilter.SERVICE_UUID))
					{
						Log.i(TAG, "onServicesDiscovered() : sUuid matched filter = " + sUuid);
	
						String message = String.format("%d:%s%d:%s", peripheralId.length(), peripheralId, sUuid.length(), sUuid);
	
						UnityPlayer.UnitySendMessage(gameObjectName, "OnDiscoveredService", message);
	
						List<BluetoothGattCharacteristic> chars = services.get(i).getCharacteristics();
	
						for (int j = 0; j < chars.size(); j++) {
							String cUuid = chars.get(j).getUuid().toString();
	
							Log.i(TAG, "onServicesDiscovered() : cUuid = " + cUuid);
																					
							if(peripherals.containsKey(peripheralId))
							{
								PeripheralCharacteristic peripheralCharacteristic = new PeripheralCharacteristic(chars.get(j));
								
								peripherals.get(peripheralId).characteristics.put(cUuid, peripheralCharacteristic);
								
								message = String.format("%d:%s%d:%s", peripheralId.length(), peripheralId, cUuid.length(), cUuid);
								
								UnityPlayer.UnitySendMessage(gameObjectName, "OnDiscoveredCharacteristic", message);
								
								List<BluetoothGattDescriptor> descriptors = chars.get(j).getDescriptors();
								
								for(int k = 0; k < descriptors.size(); k++)
								{
									BluetoothGattDescriptor descriptor = descriptors.get(k);
									
									peripheralCharacteristic.descriptors.put(descriptor.getUuid().toString(), descriptor);
									
									message = String.format("%d:%s%d:%s%d:%s", peripheralId.length(), peripheralId, cUuid.length(), cUuid, descriptor.getUuid().toString().length(), descriptor.getUuid().toString());
									
									UnityPlayer.UnitySendMessage(gameObjectName, "OnDiscoveredDescriptor", message);
								
								}//descriptors loop
							}

						} //chars loop
					}
					else
					{
						Log.i(TAG, "onServicesDiscovered() : sUuid mismatched filter = " + sUuid);
					}
				}
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

	};

	public void shutdown() {
		Log.i(TAG, "shutdown()");

		UnityPlayer.UnitySendMessage(gameObjectName, "OnShutdown", "Inactive");

		DeregisterBroadcastReciever(UnityPlayer.currentActivity);

		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			if (mScanning) {
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}
	}

	public void pauseWithState(boolean isPaused) {
		Log.i(TAG, "pauseWithState(" + isPaused + ")");
	}
	
	private void scanForPeripherals()
	{
		Log.i(TAG, "scanForPeripherals()");

		if (mScanning) {
			Log.i(TAG, "scanForPeripherals() : " + "already scanning...");
			return;
		}

		// Stops scanning after a pre-defined scan period.
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mScanning) {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					Log.i(TAG, "scanForPeripherals() : " + "scanning timeout...");
				}
			}
		}, SCAN_PERIOD);

		mScanning = true;

		Log.i(TAG, "scanForPeripherals() : " + "starting scan...");

		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	public void scanForPeripheralsWithServiceUUIDs(String serviceUUIDsString) {
		
		if(serviceUUIDsString != null && serviceUUIDsString.length() > 0)
		{
			String[] uuids = serviceUUIDsString.split("\\|");
			
			
			if(uuids.length > 0)
			{
				peripheralFilter = new PeripheralFilter(PeripheralFilter.SERVICE_UUID);
				
				for(int i = 0; i < uuids.length; i++)
				{
					peripheralFilter.addUuid(uuids[i]);
					
				}
			}
			else
			{
				peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);
			}
			
		}
		else
		{
			peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);
			
		}
				

		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			scanForPeripherals();
		} else {
			Log.i(TAG, "scanForPeripheralsWithServiceUUIDs() : Bluetooth Adapter Disabled...");
		}

	}

	public void connectToPeripheralWithIdentifier(String peripheralId) {
		
		Log.i(TAG, "connectToPeripheralWithIdentifier()");
		
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) 
		{
			Log.i(TAG, "connectToPeripheralWithIdentifier(" + (peripheralId == null ? "null" : peripheralId) + ")");

			if (peripherals.containsKey(peripheralId)) {
				if (mScanning)
					stopScanning();

				Log.i(TAG, "connectToPeripheralWithIdentifier() : connecting...");
				BluetoothPeripheral peripheral = peripherals.get(peripheralId);
				peripheral.device.connectGatt(context, true, mGattCallback);
			}
		} else {
			Log.i(TAG, "connectToPeripheralWithIdentifier() : Bluetooth Adapter Disabled...");
		}

	}

	public void disconnectFromPeripheralWithIdentifier(String peripheralId) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "disconnectFromPeripheralWithIdentifier()");
			
			if (peripherals.containsKey(peripheralId)) {
				
				if (mScanning)
					stopScanning();

				Log.i(TAG, "disconnectFromPeripheralWithIdentifier() : disconnecting...");
				
				BluetoothPeripheral peripheral = peripherals.get(peripheralId);
				
				if(peripheral.gatt != null)
					peripheral.gatt.disconnect();
			}
			
		} else {
			Log.i(TAG, "disconnectFromPeripheralWithIdentifier() :  Bluetooth Adapter Disabled...");
		}
	}

	public void retrieveListOfPeripheralsWithServiceUUIDs(String serviceUUIDsString) {
		
		if(serviceUUIDsString != null && serviceUUIDsString.length() > 0)
		{
			String[] uuids = serviceUUIDsString.split("\\|");
			
			
			if(uuids.length > 0)
			{
				peripheralFilter = new PeripheralFilter(PeripheralFilter.SERVICE_UUID);
				
				for(int i = 0; i < uuids.length; i++)
				{
					peripheralFilter.addUuid(uuids[i]);
					
				}
			}
			else
			{
				peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);
			}
			
		}
		else
		{
			peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);
			
		}
		
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "retrieveListOfPeripheralsWithServiceUUIDs()");
			scanForPeripherals();
		} else {
			Log.i(TAG, "retrieveListOfPeripheralsWithServiceUUIDs() :  Bluetooth Adapter Disabled...");
		}
	}

	public void retrieveListOfPeripheralsWithUUIDs(String uuidsString) {
		
		if(uuidsString != null && uuidsString.length() > 0)
		{
			String[] uuids = uuidsString.split("\\|");
			
			
			if(uuids.length > 0)
			{
				peripheralFilter = new PeripheralFilter(PeripheralFilter.PERIPHERAL_UUID);
				
				for(int i = 0; i < uuids.length; i++)
				{
					peripheralFilter.addUuid(uuids[i]);
					
				}
			}
			else
			{
				peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);
			}
			
		}
		else
		{
			peripheralFilter = new PeripheralFilter(PeripheralFilter.NONE);
			
		}
		
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "retrieveListOfPeripheralsWithUUIDs()");
			scanForPeripherals();
		} else {
			Log.i(TAG, "retrieveListOfPeripheralsWithUUIDs() :  Bluetooth Adapter Disabled...");
		}
	}

	public void stopScanning() {

		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "stopScanning()");
			
			if (mScanning) {
				Log.i(TAG, "stopScanning()");
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
			
		} else {
			Log.i(TAG, "stopScanning() :  Bluetooth Adapter Disabled...");
		}

		
	}

	public void subscribeToCharacteristicWithIdentifiers(String peripheralId, String serviceId, String characteristicId, boolean isIndication) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "subscribeToCharacteristicWithIdentifiers()");
			
			if(peripheralId != null && serviceId != null && characteristicId != null)
			{
				if(peripherals.containsKey(peripheralId) && peripherals.get(peripheralId).characteristics.containsKey(characteristicId))
				{
					BluetoothGatt gatt = peripherals.get(peripheralId).gatt;
					
					BluetoothGattCharacteristic characteristic = peripherals.get(peripheralId).characteristics.get(characteristicId).characteristic;
					
					Log.i(TAG, "subscribeToCharacteristicWithIdentifiers() : " + characteristicId);
					
					gatt.setCharacteristicNotification(characteristic, true);
					
					BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
					
					if(descriptor != null)
					{
						if(isIndication)
						{
							Log.i(TAG, "subscribeToCharacteristicWithIdentifiers() : Indication...");
							descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
						}
						else
						{
							Log.i(TAG, "subscribeToCharacteristicWithIdentifiers() : Notification...");
							descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						}
						
						gatt.writeDescriptor(descriptor);
					}
					
				}
			}
			
		} else {
			Log.i(TAG, "subscribeToCharacteristicWithIdentifiers() :  Bluetooth Adapter Disabled...");
		}
	}

	public void unSubscribeFromCharacteristicWithIdentifiers(String peripheralId, String serviceId, String characteristicId) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "unSubscribeFromCharacteristicWithIdentifiers()");
			
			if(peripheralId != null && serviceId != null && characteristicId != null)
			{
				if(peripherals.containsKey(peripheralId) && peripherals.get(peripheralId).characteristics.containsKey(characteristicId))
				{
					BluetoothGatt gatt = peripherals.get(peripheralId).gatt;
					BluetoothGattCharacteristic characteristic = peripherals.get(peripheralId).characteristics.get(characteristicId).characteristic;
					
					gatt.setCharacteristicNotification(characteristic, false);
					
					BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
					
					if(descriptor != null)
					{
						descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
						
					}
					
				}
			}
			
		} else {
			Log.i(TAG, "unSubscribeFromCharacteristicWithIdentifiers() :  Bluetooth Adapter Disabled...");
		}
	}

	public void readCharacteristicWithIdentifiers(String peripheralId, String serviceId, String characteristicId) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "readCharacteristicWithIdentifiers()");
			
			if(peripheralId != null && serviceId != null && characteristicId != null)
			{
				if(peripherals.containsKey(peripheralId) && peripherals.get(peripheralId).characteristics.containsKey(characteristicId))
				{
					BluetoothGatt gatt = peripherals.get(peripheralId).gatt;
					BluetoothGattCharacteristic characteristic = peripherals.get(peripheralId).characteristics.get(characteristicId).characteristic;
					
					gatt.readCharacteristic(characteristic);
					
				}
			}
			
		} else {
			Log.i(TAG, "readCharacteristicWithIdentifiers() :  Bluetooth Adapter Disabled...");
		}
	}
	
	public void readDescriptorWithIdentifiers(String peripheralId, String serviceId, String characteristicId, String descriptorId) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "readCharacteristicWithIdentifiers()");
			
			if(peripheralId != null && serviceId != null && characteristicId != null)
			{
				if(peripherals.containsKey(peripheralId) && peripherals.get(peripheralId).characteristics.containsKey(characteristicId))
				{
					BluetoothGatt gatt = peripherals.get(peripheralId).gatt;
					BluetoothGattDescriptor descriptor = peripherals.get(peripheralId).characteristics.get(characteristicId).descriptors.get(descriptorId);
					
					if(descriptor != null)
					{
						gatt.readDescriptor(descriptor);
					}
					
				}
			}
			
		} else {
			Log.i(TAG, "readCharacteristicWithIdentifiers() :  Bluetooth Adapter Disabled...");
		}
	}

	public void writeCharacteristicWithIdentifiers(String peripheralId, String serviceId, String characteristicId, byte[] data, int length, boolean withResponse) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "writeCharacteristicWithIdentifiers()");
			if(peripheralId != null && serviceId != null && characteristicId != null)
			{
				if(peripherals.containsKey(peripheralId) && peripherals.get(peripheralId).characteristics.containsKey(characteristicId))
				{
					BluetoothGatt gatt = peripherals.get(peripheralId).gatt;
					BluetoothGattCharacteristic characteristic = peripherals.get(peripheralId).characteristics.get(characteristicId).characteristic;
					
					characteristic.setValue(data);
					
					gatt.writeCharacteristic(characteristic);
					
				}
			}
		} else {
			Log.i(TAG, "writeCharacteristicWithIdentifiers() :  Bluetooth Adapter Disabled...");
		}
	}
	
	
	
	public void writeDescriptorWithIdentifiers(String peripheralId, String serviceId, String characteristicId, String descriptorId, byte[] data, int length) {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.i(TAG, "writeCharacteristicWithIdentifiers()");
			if(peripheralId != null && serviceId != null && characteristicId != null)
			{
				if(peripherals.containsKey(peripheralId) && peripherals.get(peripheralId).characteristics.containsKey(characteristicId))
				{
					BluetoothGatt gatt = peripherals.get(peripheralId).gatt;
					BluetoothGattDescriptor descriptor = peripherals.get(peripheralId).characteristics.get(characteristicId).descriptors.get(descriptorId);
					
					if(descriptor != null)
					{
						descriptor.setValue(data);
						gatt.writeDescriptor(descriptor);
					}
					
				}
			}
		} else {
			Log.i(TAG, "writeCharacteristicWithIdentifiers() :  Bluetooth Adapter Disabled...");
		}
	}
}
