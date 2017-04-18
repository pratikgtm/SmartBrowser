import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.net.*;
import java.util.List;
import java.util.Map;

public class Implementation<E> extends UnicastRemoteObject implements Peer<E>, DHT<E> {
		
	private String name;	
	private String key;
	private Peer<E> successor;	
	private Peer<E> predecessor;	
	private HashMap<String, E> storage = new HashMap<>();	
	private Map<String, Peer<E>> fingers = new LinkedHashMap<>();
	
	public Implementation(String name) throws RemoteException {
		this.name = name;
		key = Key.generate(name, Constants.N);
		successor = this;
		predecessor = this;
		Registry registry = null;
		System.out.println("X");
		try {
			registry = LocateRegistry.createRegistry(Constants.PORT_NO);
			registry.rebind(name, this);
		} catch (Exception e) {
			System.out.println("Server already installed");
			e.printStackTrace();
		}
		System.out.println("Y");
		registry.rebind(name, this);
	}
	
	@SuppressWarnings("unchecked")
	public Implementation(String name, String otherName, String host, int port) throws RemoteException, NotBoundException {
		this(name);
		System.out.println("out of this");
		Registry registry = LocateRegistry.getRegistry(host, port);		
		System.out.println("going to lookup " + otherName + " " + registry);
		Peer<E> other = (Peer<E>) registry.lookup(otherName);		
		System.out.println("going in join");		
		join(other);
	}
	
	@Override
	public void join(Peer<E> other) {
		try {
		boolean joined = false;
			while(!joined) {
				System.out.println("in loop " + other);
				Peer<E> pred = other.getPredecessor();
				String otherKey = other.getKey();
				String predKey = pred.getKey();
				System.out.println("otherkey " + otherKey + " , predkey " + predKey);
				
				if(Key.between(key, predKey, otherKey)) {
					System.out.println("inside 1 " + pred);
					pred.setSuccessor(this);
					System.out.println("inside 2");
					other.setPredecessor(this);
					System.out.println("inside 3");
					setSuccessor(other);
					System.out.println("inside 4");
					setPredecessor(pred);
					
					Map<String, E> handover = successor.handover(predKey, key);
					for(String k : handover.keySet())
						storage.put(k, handover.get(k));
					
					joined = true;
				} else
					other = other.getSuccessor();
			}
			updateRouting();
		} catch(RemoteException e) {
			System.err.println("Error joining " + other);
			e.printStackTrace();
		}
	}
	
	public void leave() {
		try {			
			for(String k : storage.keySet())
				successor.addStored(k, storage.get(k));
			System.out.println(name + ": Done.");
			
			successor.setPredecessor(predecessor);
			predecessor.setSuccessor(successor);
			successor = this;
			predecessor = this;
			updateRouting();
		} catch(RemoteException e) {
			System.err.println("Error leaving.");
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, E> handover(String oldPredKey, String newPredKey) throws RemoteException {
		Map<String, E> handover = new LinkedHashMap<>();
		List<String> keys = new ArrayList<String>(storage.keySet());
		for(String k : keys)
			if(Key.between(k, oldPredKey, newPredKey))
				handover.put(k, storage.remove(k));
		return handover;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	@Override
	public Peer<E> getSuccessor() throws RemoteException {
		return successor;
	}

	@Override
	public Peer<E> getPredecessor() throws RemoteException {
		return predecessor;
	}

	@Override
	public void setSuccessor(Peer<E> succ) throws RemoteException {
		successor = succ;
	}

	@Override
	public void setPredecessor(Peer<E> pred) throws RemoteException {
		predecessor = pred;
	}

	@Override
	public void probe(String key, int count) throws RemoteException {
		if(this.key.equals(key) && count > 0) {
			System.out.println("Probe returned after " + count + " hops.");
		} else {
			System.out.println(name + ": Forwarding probe to " + successor);
			successor.probe(key, count+1);
		}
	}

	@Override
	public Peer<E> lookup(String key) throws RemoteException {
		String predKey = predecessor.getKey();
		if(Key.between(key, predKey, getKey()))
			return this;
		else if(fingers.keySet().size() < 3) {
			return successor.lookup(key);
		}
		else {
			String[] keys = {};
			keys = fingers.keySet().toArray(keys);
			for(int i=0; i<(keys.length-1); i++) {
				String currentKey = keys[i];
				String nextKey = keys[i+1];
				if(Key.between(key, currentKey, nextKey)) {
					Peer<E> currentPeer = fingers.get(currentKey);
					Peer<E> peer = currentPeer.getSuccessor();
					return peer.lookup(key);
				}
			}
			return fingers.get(keys[keys.length-1]).getSuccessor().lookup(key);
		}
		
	}
	
	@Override
	public E getStored(String key) throws RemoteException {
		return storage.get(key);
	}
	
	@Override
	public void addStored(String key, E value) throws RemoteException {
		storage.put(key, value);
	}
	
	@Override
	public void removeStored(String key) throws RemoteException {
		storage.remove(key);
	}

	@Override
	public E get(String key) {
		try {
			String k = Key.generate(key, Constants.N);
			Peer<E> peer = lookup(k);
			return peer.getStored(k);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void put(String key, E object) {
		try {
			String k = Key.generate(key, Constants.N);
			Peer<E> peer = lookup(k);
			peer.addStored(k, object);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void remove(String key) {
		try {
			String k = Key.generate(key, Constants.N);
			Peer<E> peer = lookup(k);
			peer.removeStored(k);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<E> listAll() {
		Peer<E> currentPeer = this;
		ArrayList<E> all = new ArrayList<>();
		try {
			do {
				for(E element : currentPeer.getValues())
					all.add(element);
				currentPeer = currentPeer.getSuccessor();				
			} while(!this.equals(currentPeer));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return all;
	}

	@Override
	public List<E> getValues() throws RemoteException {
		ArrayList<E> values = new ArrayList<>(storage.values());
		return values;
	}
	
	private List<Peer<E>> allPeers() {
		ArrayList<Peer<E>> peers = new ArrayList<>();
		try {
			Peer<E> current = this;
			do {
				peers.add(current);
				current = current.getSuccessor();
			} while(!this.equals(current));
		} catch(RemoteException e){
			System.err.println("Error finding all peers!");
		}
		Collections.sort(peers, new Comparator<Peer<E>>() {
			@Override
			public int compare(Peer<E> n1, Peer<E> n2) {
				try {
					String key1 = n1.getKey();
					String key2 = n2.getKey();
					int i1 = Integer.parseInt(key1, 2);
					int i2 = Integer.parseInt(key2, 2);
					
					if(i1 > i2)
						return 1;
					if(i1 < i2)
						return -1;
					else
						return 0;
				}catch(RemoteException e) {
					return 0;
				}
			}
		});
		return peers;
	}
	
	public void updateFingers(List<Peer<E>> peers) {
		Map<String, Peer<E>> fingers = new LinkedHashMap<>();
		fingers.put(key, this);
		try {
			int myIndex = peers.indexOf(this);
			
			for(int i=1; i<peers.size(); i = i*2) {
				int peerIndex = (myIndex + i) % peers.size();
				Peer<E> n = peers.get(peerIndex);
				fingers.put(n.getKey(), n);
			}
		} catch(RemoteException e) {
			e.printStackTrace();
		}
		this.fingers = fingers;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Peer<?>)
			try {
				return key.equals(((Peer<?>) other).getKey());
			} catch (RemoteException e) {
				e.printStackTrace();
				return false;
			}
		else
			return false;
	}

	@Override
	public void updateRouting() {
		try {
			List<Peer<E>> peers = allPeers();
			for(Peer<E> n : peers)
				n.updateFingers(peers);
		} catch (RemoteException e) {
			System.out.println("RemoteException " + e);
		}
	}
}
