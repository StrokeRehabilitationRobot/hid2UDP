import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;



public class hid2udp {

	static HidServices hidServices;
	static ArrayList<HidDevice> boards;
	static HidDevice hidDevice;
	static DatagramSocket serverSocket;
	static int numFloats;
    static ByteOrder be;
    static int packetSize;

	
	public static void main(String[] args) throws SocketException
	{
		hidServices = null;
		hidDevice = null;
		boards = new ArrayList<HidDevice>();
		packetSize = 70;
	    numFloats = (packetSize / 4) - 1;
	    InetAddress IPAddress;
	    int port,read,val,boardID;
		boolean HIDconnected = true;
		byte[] receiveData = new byte[packetSize];
		byte[] sendData = new byte[packetSize];
		byte[] message;
		byte[] rawMessage;
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
					//UDP
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
		            serverSocket.receive(receivePacket);
					rawMessage = receivePacket.getData();
					IPAddress = receivePacket.getAddress();
		            port = receivePacket.getPort();
		            
		            boardID = getBoardID(rawMessage); //get the board ID
		            message = removeBoardID(rawMessage); //remake message without the board
		            
		            //Testing print test statements
		            
		            //float [] uncodedMessage = parse(message);
		            //printArray(uncodedMessage);
					//System.out.println( "ID " + getID(rawMessage));
					//choose the board to send message
					//System.out.println( "board " + getBoard(rawMessage));
					//System.out.println( "ID " + getID(message));
					//System.out.println( "board " + getBoard(message));
		            //System.out.println("board " + boardID);
		            hidDevice = boards.get(boardID);
		            
		            val = hidDevice.write(message, message.length, (byte) 0);
					
					if (val > 0) 
					{
						read = hidDevice.read(message, 1000);
						
						
						if (read > 0) {
							//System.out.println("asldjf");
							//printArray(parse(message));
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
			
		}
		
	}
	
	//shuts things down
	public static void disconnect()
	{
		
		hidServices.shutdown();
		serverSocket.close();
		
	}
	
	
	public static void connect()
	{
		
		int vid = 0x3742;
		int pid = 0x7;
		HidDevice hidTemp = null;
		
		if (hidServices == null)
			hidServices = HidManager.getHidServices();
		// Provide a list of attached devices
		
		for (HidDevice h : hidServices.getAttachedHidDevices()) 
		{
			if (h.isVidPidSerial(vid, pid, null)) 
			{
				hidDevice = h;
				hidDevice.open();
				boards.add(hidDevice);
				System.out.println("Found! " + hidDevice);

			}
		}
		
	}
	
	// returns the message ID number
	static int getID(byte[] bytes)
	{
		
		return bytes[1];	
	}
	
	//return the board number
	static int getBoardID(byte[] bytes)
	{
		
		
		return bytes[0];	
	}
	
	static byte[] removeBoardID(byte[] bytes)
	{
		byte[] message = new byte[64];
		
		for (int i = 0; i < 64; i++)
		{
			message[i] = bytes[i+4];
		}
		
		return message;
		
	}
	
	// Returns the float array of the bytes
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
	
	
	static byte[] command(float[] values)
	{
		byte[] message = new byte[packetSize];
		ByteBuffer.wrap(message).order(be).putInt(0, (int)values[0]).array();
		for (int i = 0; i < numFloats && i < values.length; i++) {
			int baseIndex = (4 * i) + 4;
			ByteBuffer.wrap(message).order(be).putFloat(baseIndex, values[i]).array();
		}
		return message;
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
