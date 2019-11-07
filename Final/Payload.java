import java.io.Serializable;
//Make it serializable so we can send it across the network
public class Payload implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8631878017121002054L;
	public PayloadType payloadType;
	public String message;
	public int number,index,x,y;
	//TODO add relevant datatypes, you can share variables based on payloadType
	public Payload(PayloadType type, String message) {
		this.payloadType = type;
		this.message = message;
	}
	public Payload(PayloadType type, int x, int y) {
		this.payloadType = type;
		this.x = x;
		this.y = y;
	}
	public Payload(PayloadType type, int x, int y, int index) {
		this.payloadType = type;
		this.x = x;
		this.y = y;
		this.index = index;
	}
	@Override
	public String toString() {
		return "Payload[payloadType: " + payloadType.toString() + ", X: " + x + ", Y: " + y; 
	}
}