package propinquity.hardware;

public enum PacketType {
	
	CONF_PACKET(1),
	COLOR_PACKET(2),
	VIBE_PACKET(3),
	PROX_PACKET(4);

	final int code;

	PacketType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
