import java.io.Serializable;

import java.io.Serializable;

public class Payload implements Serializable{
	private static final long serialVersionUID = 2L;
	public String name = null;
	public int id, x, y, target = -1;
	public PayloadType payloadType;
	
	public Payload(int id, PayloadType type) {
		this(id, type, 0,0, null);
	}
	public Payload(int id, PayloadType type, int x, int y) {
		this(id, type, x, y, null);
	}
	public Payload(int id, PayloadType type, int x, int y, String extra) {
		this(id, type, x, y, extra, -1);
	}
	public Payload(int id, PayloadType type, int x, int y, String extra, int target) {
		this.id = id;
		payloadType = type;
		this.x = x;
		this.y = y;
		this.name = extra;
		this.target = target;
	}
	public Payload(PayloadType type, int x, int y) {
		this.x = x;
		this.y = y;
		payloadType = type;
	}
	@Override
	public String toString() {
		return this.id + "-" + this.payloadType.toString() + "(" + x + "," + y +") - " + name;
	}
}
