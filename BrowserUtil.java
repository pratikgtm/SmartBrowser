import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.io.*;
import java.net.*;
import javafx.scene.control.TextArea;

public class BrowserUtil {
	
	public DHT<CustomCache> dht;
	private String peerName;
	private String discoveredPeerName;
	private String discoveredPeerAddr;
	public PeerServer peerServer;	
	
	public boolean isPeersPresent(String name) throws UnknownHostException, SocketException, IOException {
	
		DatagramSocket peerSocket = new DatagramSocket();  // creating UDP socket
		InetAddress IPAddress = InetAddress.getByName(Constants.BROADCAST_ADDR);  // broadcasting
	     	
	    byte[] sendData = new byte[1024];
	    String pingMsg = Constants.BROADCAST_MSG;
	    sendData = pingMsg.getBytes();
	    DatagramPacket sendPacket =   new DatagramPacket(sendData, sendData.length, IPAddress, Constants.PORT_NO);
	    peerSocket.send(sendPacket);
	    peerSocket.setSoTimeout(2000);

		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    while(true) {
	    	try {
	    		peerSocket.receive(receivePacket);	    		
	    		discoveredPeerName = (new String(receivePacket.getData())).trim();
	    		discoveredPeerAddr = receivePacket.getAddress().getHostAddress();
	    		peerSocket.close();
	    		return true;

	    	} catch (SocketTimeoutException e) {
	    		peerSocket.close();
	    		return false;
	    	}
	    }  
	}

	@SuppressWarnings("unchecked")
	public BrowserUtil(String name, TextArea home_dashboard) {	
		peerName = name;	
		discoveredPeerName = "";
		discoveredPeerAddr = "";

		home_dashboard.setText("Welcome "+name+"\n");

		if(Constants.CONNECT_USERNAME.length()!=0 && Constants.CONNECT_IP.length()!=0) { //connecting manually...
			discoveredPeerName = Constants.CONNECT_USERNAME;
			discoveredPeerAddr = Constants.CONNECT_IP;
			
			home_dashboard.appendText("Connecting to Peer : "+discoveredPeerName+" @"+discoveredPeerAddr+":"+Constants.PORT_NO+"\n");
			BrowserUtilImpl(name, discoveredPeerName, discoveredPeerAddr, Constants.PORT_NO, home_dashboard);
		} else {		
			try {
				if(isPeersPresent(name)) {
					home_dashboard.appendText("Found peer : "+discoveredPeerName+" @"+discoveredPeerAddr+":"+Constants.PORT_NO+"\n");
					home_dashboard.appendText("Connecting to Peer : "+discoveredPeerName+" @"+discoveredPeerAddr+":"+Constants.PORT_NO+"\n");
					BrowserUtilImpl(name, discoveredPeerName, discoveredPeerAddr, Constants.PORT_NO, home_dashboard);
				} else {
					home_dashboard.appendText("No peer found !");
					dht = new Implementation<>(name);
					startPeer();
				}
			} catch(Exception e) {
				home_dashboard.appendText("RemoteException found !!! " + e);
			}		
		}
	}
	
	public void BrowserUtilImpl(String name, String rName, String host, int port, TextArea home_dashboard) {

		try {
			dht = new Implementation<>(name, rName, host, port);
			home_dashboard.appendText("Connection successfull !\n");
			startPeer();
		} catch (RemoteException e) {
			home_dashboard.appendText("RemoteException " + e + "\n");			
		} catch (NotBoundException e) {
			home_dashboard.appendText("RMI Registry error." + e + "\n");
		}
	}

	private void startPeer() {
		peerServer = new PeerServer(this, peerName);
	}

	public void checkit(String s) {
		System.out.println(s);
	}
}
