import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface Peer<V> extends Remote {
	public String getKey() throws RemoteException;
	public Peer<V> getSuccessor() throws RemoteException;
	public Peer<V> getPredecessor() throws RemoteException;
	public void setSuccessor(Peer<V> succ) throws RemoteException;
	public void setPredecessor(Peer<V> pred) throws RemoteException;
	public void probe(String key, int count) throws RemoteException;
	public Peer<V> lookup(String key) throws RemoteException;
	public V getStored(String key) throws RemoteException;
	public void addStored(String key, V value) throws RemoteException;
	public void removeStored(String key) throws RemoteException;
	public Map<String, V> handover(String oldPredKey, String newPredKey) throws RemoteException;
	public List<V> getValues() throws RemoteException;
	public void updateFingers(List<Peer<V>> peers) throws RemoteException;
}
