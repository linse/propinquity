package propinquity.hardware;

public class Packet {

	final PacketType type;
	final int[] payload;

	public Packet(PacketType type, int[] payload) {
		this.type = type;

		this.payload = payload;
	}

	public int[] getPayload() {
		return payload;
	}

	public PacketType getPacketType() {
		return type;
	}

	public int getCode() {
		return type.getCode();
	}

}