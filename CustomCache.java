import java.io.Serializable;

public class CustomCache implements Serializable {
	String URL;
	byte[] data;	
	String timestamp;

	public CustomCache(String URL, byte[] data, String timestamp) {
		this.URL = URL;
		this.data = data;
		this.timestamp = timestamp;
	}
}