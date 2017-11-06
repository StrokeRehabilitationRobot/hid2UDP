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
    static ByteOrder be;

	
	public static void main(String[] args) throws SocketException
	{
		hidServices = null;
		hidDevice = null;
		int packetSize = 64;
	    numFloats = (packetSize / 4) - 1;
	    InetAddress IPAddress;
	    int port,read,val;
		boolean HIDconnected = true;
		byte[] receiveData = new byte[packetSize];
		byte[] sendData = new byte[packetSize];
		byte[] message;
		be = ByteOrder.LITTLE_ENDIAN;
		
		
		serverSocket = new DatagramSocket(9876);
		DatagramPacket sendPacket;
		DatagramPacket receivePacket ;
		
		

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
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		            serverSocket.receive(receivePacket);
					message = receivePacket.getData();
					IPAddress = receivePacket.getAddress();
		            port = receivePacket.getPort();
		            //printArray(parse(message));
					System.out.println(getID(message));
		            long startTime = System.nanoTime();
		            val = hidDevice.write(message, message.length, (byte) 0);
					
					if (val > 0) 
					{
						read = hidDevice.read(message, 1000);
						long endTime = System.nanoTime();
						
						if (read > 0) {
							//System.out.println("asldjf");
							printArray(parse(message));
							//System.out.println(getID(message));
				            sendPacket = new DatagramPacket(message, message.length, IPAddress, port);
							serverSocket.send(sendPacket);

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
					//disconnect();
				}
			}
			
//			if (hidDevice != null) {
//				hidDevice.close();
//				
//			}
//			if (hidServices != null) {
//				// Clean shutdown
//				hidServices.shutdown();
//			}
		}
		
	}
	
	public static void disconnect()
	{
		
		hidServices.shutdown();
		serverSocket.close();
		
	}
	
	
	public static void connect()
	{
		
		int vid = 0x3742;
		int pid = 0x7;
		
		if (hidServices == null)
			hidServices = HidManager.getHidServices();
		// Provide a list of attached devices
		hidDevice = null;
		for (HidDevice h : hidServices.getAttachedHidDevices()) 
		{
			if (h.isVidPidSerial(vid, pid, null)) 
			{
				hidDevice = h;
				hidDevice.open();
				System.out.println("Found! " + hidDevice);

			}
		}
		
	}
	
	static int getID(byte[] bytes)
	{
		
		
		return bytes[0];
		
	}
	
	static float[] parse(byte[] bytes) 
    {
        float[] returnValues = new float[numFloats];
        int baseIndex;
   
        for (int i = 0; i < numFloats; i++) {
            baseIndex = (i*4)+4;
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
