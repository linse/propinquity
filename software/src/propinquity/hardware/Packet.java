package propinquity.hardware;

public class Packet {

	final int dest_addr;
	final PacketType type;
	final int[] payload;

	public Packet(int dest_addr, PacketType type, int[] payload) {
		this.dest_addr = dest_addr;

		this.type = type;

		this.payload = payload;
	}

	public int getDestAddr() {
		return dest_addr;
	}

	public PacketType getPacketType() {
		return type;
	}

	public int[] getPayload() {
		return payload;
	}

}