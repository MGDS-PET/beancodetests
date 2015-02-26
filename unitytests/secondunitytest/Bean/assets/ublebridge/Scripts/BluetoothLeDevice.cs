using System;
using System.Collections.Generic;
using UnityEngine;
using startechplus;

namespace startechplus.ble
{
	public delegate void Action<T1, T2, T3, T4, T5>(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5);

	public class BluetoothLeDevice : MonoBehaviour
	{
		public Action<string> StateUpdateAction;
		public Action StartupAction;
		public Action ShutdownAction;
		public Action<string> ErrorAction;
		public Action<string, string> ConnectedPeripheralAction;
		public Action<string, string> DisconnectedPeripheralAction;
		public Action<string, string> DiscoveredPeripheralAction;
		public Action<string, string> RetrievedPeripheralWithServiceAction;
		public Action<string, string> RetrievedPeripheralWithUUIDAction;
		public Action<string, string> DiscoveredServiceAction;
		public Action<string, string, string> DiscoveredCharacteristicAction;
		public Action<string, string, string> DidWriteCharacteristicAction;
		public Action<string, string, string> DidUpdateNotificationStateForCharacteristicAction;
		public Action<string, string, string, byte[]> DidUpdateCharacteristicValueAction;
		public Action<string, string, string, string> DidWriteDescriptorAction;
		public Action<string, string, string, string, byte[]> DidReadDescriptorValueAction;
		public Action<string, string, string, string> DiscoveredDescriptorAction;
		public Action<string, string> DidUpdateRssiAction;

		public bool isLowerCaseUUID = false;

		private bool Initialized;

		public void OnStateUpdate(string message)
		{
			if(StateUpdateAction != null)
				StateUpdateAction(message);
		}

		public void OnError(string message)
		{
			if(ErrorAction != null)
				ErrorAction(message);
		}

		private string getToken(string currentString, int startIndex, out int stopIndex)
		{

			//Debug.Log(currentString);
			//Debug.Log(startIndex);
			//Debug.Log("---");

			string message = currentString.Substring(startIndex);
			//Debug.Log(message);

			int splitIndex = message.IndexOf(":");
			//Debug.Log(splitIndex);

			string tokenLength = message.Substring(0,splitIndex);
			//Debug.Log(tokenLength);

			string token = message.Substring(splitIndex + 1, Int32.Parse(tokenLength));
			//Debug.Log(token);

			stopIndex = splitIndex + 1 + token.Length + startIndex;
			//Debug.Log(stopIndex);


			return token;
		}

		private string[] ParseMessage(string message, int expectedValues)
		{
			string[] values = new string[expectedValues];
			int lastIndex = 0;

			for(int i = 0; i < expectedValues; i++)
			{
				values[i] = getToken(message, lastIndex, out lastIndex);
			}

			return values;
		}

		public void OnDiscoveredPeripheral(string message)
		{
			string[] tokens = ParseMessage(message, 2);

			if(DiscoveredPeripheralAction != null)
			{
				if(isLowerCaseUUID)
					DiscoveredPeripheralAction(tokens[0].ToUpper(), tokens[1]);
				else
					DiscoveredPeripheralAction(tokens[0], tokens[1]);
			}

		}

		public void OnStartup(string message)
		{
			if(StartupAction != null)
				StartupAction();
		}
		
		public void OnShutdown(string message)
		{
			if(ShutdownAction != null)
				ShutdownAction();
		}

		public void OnRetrievedPeripheralWithServiceUUIDs(string message)
		{
			string[] tokens = ParseMessage(message, 2);

			Debug.Log("OnRetrievedPeripheralWithServiceUUIDs " + tokens[0] +  ", " + tokens[1]);

			if(RetrievedPeripheralWithServiceAction != null)
			{
				if(isLowerCaseUUID)
					RetrievedPeripheralWithServiceAction(tokens[0].ToUpper(), tokens[1]);
				else
					RetrievedPeripheralWithServiceAction(tokens[0], tokens[1]);
			}
		}

		public void OnRetrievedPeripheralWithUUID(string message)
		{
			string[] tokens = ParseMessage(message, 2);
			
			Debug.Log("OnRetrievedPeripheralWithUUID " + tokens[0] +  ", " + tokens[1]);
			
			if(RetrievedPeripheralWithUUIDAction != null)
			{
				if(isLowerCaseUUID)
					RetrievedPeripheralWithUUIDAction(tokens[0].ToUpper(), tokens[1]);
				else 
					RetrievedPeripheralWithUUIDAction(tokens[0], tokens[1]);
			}
		}

		public void OnConnectedPeripheral(string message)
		{
			string[] tokens = ParseMessage(message, 2);
			
			Debug.Log("OnConnectedPeripheral " + tokens[0] +  ", " + tokens[1]);
			
			if(ConnectedPeripheralAction != null)
			{
				if(isLowerCaseUUID)
					ConnectedPeripheralAction(tokens[0].ToUpper(), tokens[1]);
				else
					ConnectedPeripheralAction(tokens[0], tokens[1]);
			}
		}

		public void OnDiscoveredService(string message)
		{
			string[] tokens = ParseMessage(message, 2);
			
			Debug.Log("OnDiscoveredService " + tokens[0] +  ", " + tokens[1]);
			
			if(DiscoveredServiceAction != null)
			{
				if(isLowerCaseUUID)
					DiscoveredServiceAction(tokens[0].ToUpper(), tokens[1].ToUpper());
				else
					DiscoveredServiceAction(tokens[0], tokens[1]);
			}
		}

		public void OnDisconnectedPeripheral(string message)
		{
			string[] tokens = ParseMessage(message, 2);
			
			Debug.Log("OnDisconnectedPeripheral " + tokens[0] +  ", " + tokens[1]);
			
			if(DisconnectedPeripheralAction != null)
			{
				if(isLowerCaseUUID)
					DisconnectedPeripheralAction(tokens[0].ToUpper(), tokens[1]);
				else
					DisconnectedPeripheralAction(tokens[0], tokens[1]);
			}
		}

		public void OnDiscoveredCharacteristic(string message)
		{

			string[] tokens = ParseMessage(message, 3);
			
			Debug.Log("OnDiscoveredCharacteristic " + tokens[0] +  ", " + tokens[1]+  ", " + tokens[2]);
			
			if(DiscoveredCharacteristicAction != null)
			{
				if(isLowerCaseUUID)
					DiscoveredCharacteristicAction(tokens[0].ToUpper(), tokens[1].ToUpper() ,tokens[2].ToUpper());
				else
					DiscoveredCharacteristicAction(tokens[0], tokens[1] ,tokens[2]);
			}

		}

		public void OnDiscoveredDescriptor(string message)
		{
			
			string[] tokens = ParseMessage(message, 3);
			
			Debug.Log("OnDiscoveredDescriptor " + tokens[0] +  ", " + tokens[1] +  ", " + tokens[2]);
			
			if(DiscoveredDescriptorAction != null)
			{
				if(isLowerCaseUUID)
					DiscoveredDescriptorAction(tokens[0].ToUpper(), "" , tokens[1].ToUpper(), tokens[2].ToUpper());
				else
					DiscoveredDescriptorAction(tokens[0], "" ,tokens[1], tokens[2]);
			}
			
		}

		public void OnDidWriteCharacteristic(string message)
		{
			string[] tokens = ParseMessage(message, 3);
			
			Debug.Log("OnDidWriteCharacteristic " + tokens[0] +  ", " + tokens[1]+  ", " + tokens[2]);
			
			if(DidWriteCharacteristicAction != null)
			{
				if(isLowerCaseUUID)
					DidWriteCharacteristicAction(tokens[0].ToUpper(), tokens[1].ToUpper(), tokens[2].ToUpper());
				else
					DidWriteCharacteristicAction(tokens[0], tokens[1], tokens[2]);
			}
		}

		public void OnDidUpdateNotificationStateForCharacteristicAction(string message)
		{
			string[] tokens = ParseMessage(message, 3);
			
			Debug.Log("OnDidUpdateNotificationStateForCharacteristicAction " + tokens[0] +  ", " + tokens[1]+  ", " + tokens[2]);
			
			if(DidUpdateNotificationStateForCharacteristicAction != null)
			{
				if(isLowerCaseUUID)
					DidUpdateNotificationStateForCharacteristicAction(tokens[0].ToUpper(), tokens[1].ToUpper(), tokens[2].ToUpper());
				else
					DidUpdateNotificationStateForCharacteristicAction(tokens[0], tokens[1], tokens[2]);
			}
		}

		public void OnBluetoothData (string message)
		{

			string[] tokens = ParseMessage(message, 4);

			string peripheral = tokens[0];
			string service = tokens[1];
			string characteristic = tokens[2];
			string base64Data = tokens[3];

			if (base64Data != null)
			{
				byte[] bytes = System.Convert.FromBase64String(base64Data);
				if (bytes.Length > 0)
				{
					if (DidUpdateCharacteristicValueAction != null)
					{
						if(isLowerCaseUUID)
							DidUpdateCharacteristicValueAction(peripheral.ToUpper(), service.ToUpper(), characteristic.ToUpper(), bytes);
						else
							DidUpdateCharacteristicValueAction(peripheral, service, characteristic, bytes);
					}
				}
			}
		}

		public void OnDidWriteDescriptor(string message)
		{
			string[] tokens = ParseMessage(message, 4);
			
			Debug.Log("OnDidWriteDescriptor " + tokens[0] +  ", " + tokens[1] +  ", " + tokens[2]+  ", " + tokens[3]);
			
			if(DidWriteDescriptorAction != null)
			{
				if(isLowerCaseUUID)
					DidWriteDescriptorAction(tokens[0].ToUpper(), tokens[1].ToUpper(), tokens[2].ToUpper(), tokens[3].ToUpper());
				else
					DidWriteDescriptorAction(tokens[0], tokens[1], tokens[2], tokens[3]);
			}
		}

		public void OnDescriptorRead(string message)
		{
			string[] tokens = ParseMessage(message, 4);
			
			string peripheral = tokens[0];
			string service = tokens[1];
			string characteristic = tokens[2];
			string descriptor = tokens[3];
			string base64Data = tokens[4];
			
			if (base64Data != null)
			{
				byte[] bytes = System.Convert.FromBase64String(base64Data);
				if (bytes.Length > 0)
				{
					if (DidReadDescriptorValueAction != null)
					{
						if(isLowerCaseUUID)
							DidReadDescriptorValueAction(peripheral.ToUpper(), service.ToUpper(), characteristic.ToUpper(), descriptor.ToUpper(), bytes);
						else
							DidReadDescriptorValueAction(peripheral, service, characteristic, descriptor, bytes);
					}
				}
			}
		}

		public void OnRssiUpdate(string message)
		{
			string[] tokens = ParseMessage(message, 2);
			
			Debug.Log("OnRssiUpdate " + tokens[0] +  ", " + tokens[1]);

			if(DidUpdateRssiAction != null)
			{
				if(isLowerCaseUUID)
					DidUpdateRssiAction(tokens[0].ToUpper(), tokens[1].ToUpper());
				else
					DidUpdateRssiAction(tokens[0], tokens[1]);
			}
		}
	}
}
