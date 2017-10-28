import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;



public class hid2udp {

	static HidServices hidServices;
	static HidDevice hidDevice;
	static DatagramSocket serverSocket;
	static int numFloats;
	
	public static void main(String[] args)
	{
		hidServices = null;
		hidDevice = null;
		int packetSize = 64;
	    numFloats = (packetSize / 4) - 1;
	    InetAddress IPAddress;
	    int port ;
	    ByteOrder be = ByteOrder.LITTLE_ENDIAN;
		boolean HIDconnected = true;
		byte[] receiveData = new byte[packetSize];
		byte[] sendData = new byte[packetSize];
		try {
			serverSocket = new DatagramSocket(9876);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		

		//connect to the deveince
		connect();
		
		
		while(HIDconnected)
		{

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (hidDevice != null) 
			{
				
				try 
				{
	
		            serverSocket.receive(receivePacket);
					byte[] message = receivePacket.getData();
					IPAddress = receivePacket.getAddress();
		            port = receivePacket.getPort();
					
		            int val = hidDevice.write(message, message.length, (byte) 0);
					
					if (val > 0) 
					{
						int read = hidDevice.read(message, 1000);
			            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			            serverSocket.send(sendPacket);

						if (read > 0) {
							
							printArray(parse(message));
						} 
						else 
						{
							System.out.println("Read failed");
						}

					}
				} 
				catch (Throwable t) 
				{
					t.printStackTrace(System.out);
					disconnect();
				}
			}
			
			if (hidDevice != null) {
				hidDevice.close();
			}
			if (hidServices != null) {
				// Clean shutdown
				hidServices.shutdown();
			}
		}
		
	}
	
	public static void disconnect()
	{
		
		hidServices.shutdown();
		serverSocket.close();
		
	}
	
	
	public static void connect()
	{
		
		int vid = 0;
		int pid = 0;
		
		if (hidServices == null)
			hidServices = HidManager.getHidServices();
		// Provide a list of attached devices
		hidDevice = null;
		for (HidDevice h : hidServices.getAttachedHidDevices()) {
			if (h.isVidPidSerial(vid, pid, null)) {
				hidDevice = h;
				hidDevice.open();
				System.out.println("Found! " + hidDevice);

			}
		}
		
	}
	
	static float[] parse(byte[] bytes) 
    {
        float[] returnValues = new float[numFloats];

        // println "Parsing packet"
        for (int i = 0; i < 1; i++) {
            int baseIndex = i ;
            returnValues[i] = ByteBuffer.wrap(bytes).order(be).getFloat(baseIndex);
        }

        return returnValues;
    }
	
	static void printArray(float[] anArray) 
	{
        System.out.println(Arrays.toString(anArray));
    }

    static void printArray(byte[] anArray) 
    {
        System.out.println(Arrays.toString(anArray));
    }
	
	

}
