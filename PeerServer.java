import java.io.*;
import java.net.*;

public class PeerServer extends Thread {
	BrowserUtil context = null;
	DataInputStream inputStream = null;
	String name = null;

	PeerServer(BrowserUtil browserUtil, String _name) {
		context = browserUtil;
		name = _name;
		start(); // start method calls run() of this thread
	}

	public void run() {
    context.checkit("thread running");
		try {
      DatagramSocket serverSocket = new DatagramSocket(Constants.PORT_NO); // creating UDP socket
      byte[] receiveData = new byte[1024];
      byte[] sendData  = new byte[1024];
      while(true) {
       	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
       	try {
          serverSocket.receive(receivePacket);
        } catch (IOException e) {
          System.out.println("IOException : "+e);
        }
       	String sentence = (new String(receivePacket.getData())).trim();
       	InetAddress IPAddress = receivePacket.getAddress();
       	int port = receivePacket.getPort();
            	
       	if(sentence.equals(Constants.BROADCAST_MSG)) {
       		sendData = name.getBytes();
       		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
       		try {
            serverSocket.send(sendPacket);
          } catch (IOException e) {
            System.out.println("IOException : "+e);
          }
       	}
      }     
  	} catch (SocketException e) {
      System.out.println("SocketException : "+e);
    }
  }
}