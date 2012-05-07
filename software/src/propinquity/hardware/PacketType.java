package propinquity.hardware;

public enum PacketType {
	
	PROX(1),

	CONF(2),

	COLOR(3),
	COLOR_DUTY(4),
	COLOR_PERIOD(5),

	VIBE_LEVEL(6),
	VIBE_DUTY(7),
	VIBE_PERIOD(8);
	
	final int code;

	PacketType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
