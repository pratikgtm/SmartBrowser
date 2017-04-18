import java.util.List;

public interface DHT<V> {
	public V get(String key);
	public void put(String key, V element);
	public void remove(String key);
	public void join(Peer<V> other);
	public void leave();
	public List<V> listAll();
	public void updateRouting();
}
