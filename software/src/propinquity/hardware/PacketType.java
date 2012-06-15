package propinquity.hardware;

/**
 * Encodes the type of a packet and the associated command code.
 *
 */
public enum PacketType {
	
	PROX(1),

	CONF(2),
	CLEAR(3),

	COLOR(4),
	COLOR_DUTY(5),
	COLOR_PERIOD(6),

	VIBE_LEVEL(7),
	VIBE_DUTY(8),
	VIBE_PERIOD(9);
	
	final int code;

	PacketType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
