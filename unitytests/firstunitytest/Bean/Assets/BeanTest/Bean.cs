using UnityEngine;
using System.Collections;
using startechplus.ble;
using UnityEngine.UI;
using System.Net;

using System.IO;
using System.Text;
using System;

/**
 * In the Start() function the IBleBridge object is instantiated based on whether the Unity Player is running on Android or iOS.  
 * Once started all interaction with the Bluetooth Device via the IBleBridge is handled via Action() callbacks.  These callbacks are set when the varioius IBleBridge functions are called.
 * These callbacks are created by you, see the examples in this file.  For details about the Action() callbakcs see IBleBridge.cs.
 * @see Start()
 * 
 * Use SendToBean("20 chars max") to send strings to the bean
 * strings that are 13 chars of less from the Bean will be received without error. Parsing is done in DidUpdateCharacteristicValueAction();
 */ 

public class Bean : MonoBehaviour {
	
	public Text logText;
	public ScrollRect scrollRect;
	public const string baseUUID = "A495-FF10-C5B1-4B44-B512-1370F02D74DE";
	public const string serialUUID = "A495FF11-C5B1-4B44-B512-1370F02D74DE";
	public const string scratch1ServUUID = "A495FF20-C5B1-4B44-B512-1370F02D74DE";
	public const string scratch1CharUUID = "A495FF21-C5B1-4B44-B512-1370F02D74DE";
	public int discoveredDevices;
	public Text beanNameDisp1;
	public Text beanNameDisp2;
	public string beanName1 = "Bean_A";
	public string beanName2 = "Bean_B";
	public Text beanNameConnectedDisp;
	public Toggle toggleSelect1;
	public Toggle toggleSelect2;
	public Toggle toggleDiscover1;
	public Toggle toggleDiscover2;

	/**
	 * Connected to the Scan button in the Unity Editor.
	 */
	public void Scan()
	{
		if (beanNameConnected == "")
		{
			updateLog ("Applicaton: Scanning for ble devices...");

			deviceSelectedID = null;

			toggleDiscover1.isOn = false;
			toggleDiscover2.isOn = false;
			beanID1 = "";
			beanID1 = "";
			bleBridge.ScanForPeripheralsWithServiceUUIDs (null, this.DiscoveredPeripheralAction);
		}
	}

	public void StopScan()
	{
		updateLog("Applicaton: Scanning stopped...");
		
		bleBridge.StopScanning();
		
	}
	
	/**
	 * Connected to the Connect button in the Unity Editor.
	 */
	public void Connect()
	{
		if(deviceSelectedID != null)
		{
			bleBridge.ConnectToPeripheralWithIdentifier(deviceSelectedID, this.ConnectedPeripheralAction, this.DiscoveredServiceAction, 
			                                            this.DiscoveredCharacteristicAction, this.DiscoveredDescriptorAction, this.DisconnectedPeripheralAction);
		}
		else
		{
			updateLog("Applicaton: Bean not found...");
		}
		
	}
	
	/**
	 * Connected to the Disconnect button in the Unity Editor.
	 */
	public void Disconnect()
	{
		if(deviceSelectedID != null){
			bleBridge.DisconnectFromPeripheralWithIdentifier(deviceSelectedID, this.DisconnectedPeripheralAction);
		}
		else
			updateLog("Applicaton: Bean not found...");

		beanNameConnected = "";
		beanNameConnectedDisp.text = beanNameConnected;
	}

	public void bean1Select()
	{
		if (beanNameConnected == "")  // if not connected to a bean
		{
			if (toggleSelect1.isOn == true)
			{
				toggleSelect1.isOn = false;
				deviceSelectedID = null;
			}
			else if (beanID1 != "") // if bean of the same name discovered
			{
				deviceSelectedID = beanID1;
				
				toggleSelect1.isOn = true;
				toggleSelect2.isOn = false;
			}
		}

	}

    public void bean2Select()
    {
		if (beanNameConnected == "") // if not connected to a bean
		{
			if (toggleSelect2.isOn == true)
			{
				toggleSelect2.isOn = false;
				deviceSelectedID = null;
			}
			else if (beanID2 != "")
			{
				deviceSelectedID = beanID2;

				toggleSelect1.isOn = false;
				toggleSelect2.isOn = true;
			}
		}
	}

	/**
	 * Connected to the Blue button in the Unity Editor.
	 */
	public void Blue1Char()
	{
		SendToBean("B");
	}
	
	/**
	 * Connected to the White button in the Unity Editor.
	 */
	public void White1Char()
	{
		SendToBean("W");
	}
	
	/**
	 * Connected to the Green button in the Unity Editor.
	 */
	public void Green20Char()
	{
		SendToBean("HELLOGREEN1234567890");
	}
	
	/**
	 * Connected to the Orange button in the Unity Editor.
	 */
	public void Orange20Char()
	{
		SendToBean("TURNORANGE1234567890");
	}
	
	private void SendToBean(string data)
	{
		if(deviceSelectedID != null)
		{
			byte[] writeByte = new byte[data.Length];
			char[] charArr;
			
			charArr = data.ToCharArray(0, data.Length);
			for (int i = 0; i < data.Length; i++){
				writeByte[i] = (byte)charArr[i];
			}
			
			bleBridge.WriteCharacteristicWithIdentifiers(deviceSelectedID, scratch1ServUUID, scratch1CharUUID, writeByte, data.Length, true, this.DidWriteCharacteristicAction);
			
		}
		else
		{
			updateLog("Applicaton: Bean not found...");
		}
		
	}
	
	/**
	 * Connected to the Gray button in the Unity Editor.
	 */
	public void ClearLog()
	{
		logText.text = "";
	}

	private string deviceSelectedID;
	private string beanNameConnected = "";
	private string beanID1;
	private string beanID2;
	private IBleBridge bleBridge;
	
	private void updateLog(string newline)
	{
		if(logText != null)
		{
			logText.text += newline + "\n\n";
			if(scrollRect != null)
			{
				if(logText.preferredHeight > scrollRect.gameObject.GetComponent<RectTransform>().rect.height)
				{
					logText.gameObject.GetComponent<ContentSizeFitter>().verticalFit = ContentSizeFitter.FitMode.PreferredSize;
					scrollRect.verticalNormalizedPosition = 0;
				}
				
			}
		}
		
	}
	
	
	/**
	 * Called when the Bluetooth adapter changes states, such as enabled by the user after the app has started.
	 */
	private void StateUpdateAction(string state)
	{
		updateLog("Adapter: State Update = " + state);
	}
	
	/**
	 * Called when the IBleBridge.Startup() function has finished creating all the native resources and is ready to start connecting to BLE devices.
	 */
	private void StartupAction()
	{
		updateLog("Bridge: Startup");
	}
	
	
	/**
	 * Called when the IBleBridge.Shutdown() function has finished and the native resources are ready to be released. 
	 */
	private void ShutdownAction()
	{
		updateLog("Bridge: Shutdown");
	}
	
	/**
	 * Called when there is an error on the native side of the code.
	 */
	private void ErrorAction(string error)
	{
		updateLog("Error: " + error);
	}
	
	/**
	 * Called when a Bluetooth device / peripheral is found via the IBleBridge.ScanForPeripheralsWithServiceUUIDs().
	 */
	private void DiscoveredPeripheralAction(string peripheralId, string name)
	{
		updateLog("Bridge: Discovered Device = " + name + ", " + peripheralId);

		if (name == beanName1)
		{
			beanID1 = peripheralId;
			if (toggleDiscover1 != null)
				toggleDiscover1.isOn = true;
		}
		if (name == beanName2)
		{
			beanID2 = peripheralId;
			if (toggleDiscover2 != null)
				toggleDiscover2.isOn = true;
		}
	}
	
	/**
	 * Called when a Bluetooth device / peripheral is found via the IBleBridge.RetrieveListOfPeripheralsWithServiceUUIDs().  
	 * On iOS this is faster then scanning. However,there is no diffrence between RetrieveListOfPeripheralsWithServiceUUIDs() and ScanForPeripheralsWithServiceUUIDs() on Android.
	 */
	private void RetrievedConnectedPeripheralAction(string peripheralId, string name)
	{
		updateLog("Bridge: Retrieved Device = " + name + ", " + peripheralId);
	}
	
	/**
	 * Called when a successful connection has been established with a Bluetooth device.  This is usually do to a call to IBleBridge.ConnectToPeripheralWithIdentifier()
	 */
	private void ConnectedPeripheralAction(string peripheralId, string name)
	{
		updateLog("Bridge: Connected to Device = " + name + ", " + peripheralId);
		beanNameConnected = name;
		beanNameConnectedDisp.text = beanNameConnected;
	}
	
	/**
	 * Called when a Bluetooth device has been disconnected, either from a call to IBleBridge.DisconnectFromPeripheralWithIdentifier() or the device has been shut off or gone out of range.
	 */
	private void DisconnectedPeripheralAction(string peripheralId, string name)
	{
		updateLog("Bridge: Disconnected from device = " + name + ", " + peripheralId);
	}
	
	/**
	 * Called when a Service has been discovered.  Services are automatically scanned for with a call to IBleBridge.ConnectToPeripheralWithIdentifiers().
	 */
	private void DiscoveredServiceAction(string peripheralId, string service)
	{
		updateLog("Bridge: Discovered Service = " + service + ", " + peripheralId);
	}
	
	/**
	 * Called when a Characteristic has been discovered.  Characteristic are automatically scanned for with a call to IBleBridge.ScaConnectToPeripheralWithIdentifier().
	 */
	private void DiscoveredCharacteristicAction(string peripheralId, string service, string characteristic)
	{
		updateLog("Bridge: Discovered Characteristic = " + characteristic + ", " + peripheralId);
		
		if(characteristic == serialUUID) //Bean's Serial UUID (i think)
		{
			updateLog("Application: Subscribing to Bean");
			bleBridge.SubscribeToCharacteristicWithIdentifiers(peripheralId, service, characteristic, 
			                                                   this.DidUpdateNotifiationStateForCharacteristicAction, 
			                                                   this.DidUpdateCharacteristicValueAction, false);
		}
		
	}
	
	/**
	 * Called when a Characterisic has been successully written to, after a call to IBleBridge.WriteCharacteristicWithIdentifiers() and withResponse it true.
	 */
	private void DidWriteCharacteristicAction(string peripheralId, string service, string characteristic)
	{
		updateLog("Bridge: Did Write Characteristic = " + characteristic + ", " + service + ", " + peripheralId);
	}
	
	/**
	 * Called when the notification state has changed on a Characteristic, after a call to IBleBridge.SubscribeToCharacteristicWithIdentifiers() or IBleBridge.UnSubscribeFromCharacteristicWithIdentifiers()
	 */ 
	private void DidUpdateNotifiationStateForCharacteristicAction(string peripheralId, string service, string characteristic)
	{
		updateLog("Bridge: Did Update Notification State = " + characteristic + ", " + service + ", " + peripheralId);
	}
	
	/**
	 * Called when a Characteristic value has been updated, either in reponse to a Notification / Indication or a call to IBleBridge.ReadCharacteristicWithIdentifiers()
	 */ 
	private void DidUpdateCharacteristicValueAction(string peripheralId, string service, string characteristic, byte[] data)
	{
		if (peripheralId == deviceSelectedID && characteristic == serialUUID)
		{
			string asciiString = String.Empty;
			for (int i = 5; i < data.Length-2; i++) //MAXIMUM 13 CHARACTERS
				asciiString = asciiString+Convert.ToChar(data[i]);
			updateLog("Serial Received: " + asciiString);
		}
	}
	
	/**
	 * Called when a Descriptor has been writen to either from a call to IBleBridge.SubscribeToCharacteristicWithIdentifiers() or IBleBridge.UnSubscribeFromCharacteristicWithIdentifiers() or IBleBridge.WriteDescriptorWithIdentifiers()
	 */ 
	private void DidWriteDescriptorAction(string peripheralId, string characteristic, string descriptor)
	{
		updateLog("Bridge: Did Write Descriptor = " + descriptor + ", " + characteristic + ", " + peripheralId);
	}
	
	/**
	 * Called when a Descriptor has been read after a call to IBleBridge.ReadDescriptorWithIdentifiers()
	 */ 
	private void DidReadDescriptorAction(string peripheralId, string characteristic, string descriptor,  byte[] data)
	{
		updateLog("Bridge: Did Read Descriptor = " + descriptor + ", " + characteristic + ", " + peripheralId);
	}
	
	/**
	 * Called when a Descriptor has been discovered.  Descriptors are automatically scanned for with a call to IBleBridge.ConnectToPeripheralWithIdentifier().
	 */
	private void DiscoveredDescriptorAction(string peripheralId, string service, string characteristic, string descriptor)
	{
		updateLog("Bridge: Discovered Descriptor = " + descriptor + ", " + characteristic + ", " + service + ", " + peripheralId);
	}
	
	/**
	 * Called when the RSSI or Received Signal Strength Indicator and changed, either durring a scan or after a call to IBleBridge.ReadRssiWithIdentifier()
	 */ 
	private void DidUpdateRssiAction(string peripheralId, string rssi)
	{
		//updateLog("Bridge: RSSI Update = " + rssi + ", " + peripheralId); <-- spams the log
	}
	
	// Use this for initialization
	void Start () {

		Screen.orientation = ScreenOrientation.Landscape;

		if(logText != null)
			logText.text = "";

		discoveredDevices = 0;
		if (beanNameDisp1 != null)
			beanNameDisp1.text = beanName1;

		if (beanNameDisp2 != null)
			beanNameDisp2.text = beanName2;

		beanID1 = "";
		beanID2 = "";
		
		//Determine which native IBleBridge to use based on the runtime platform; Android or iOS
		switch(Application.platform)
		{
		case RuntimePlatform.Android:
			bleBridge = new AndroidBleBridge();
			break;
		case RuntimePlatform.IPhonePlayer:
			bleBridge = new iOSBleBridge();
			break;
		default:
			bleBridge = new DummyBleBridge(); //modify this class if you want to emulate ble interaction in the editor...
			break;
		}
		
		//Startup the native side of the code and include our callbacks...
		bleBridge.Startup(true, this.StartupAction, this.ErrorAction, this.StateUpdateAction, this.DidUpdateRssiAction);
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}
}


