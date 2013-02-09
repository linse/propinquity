package propinquity.hardware;

/**
 * Encodes the type of a packet and the associated command code.
 *
 */
public enum PacketType {
	MODE(0),
	CLEAR(1),

	PROX(2),

	ACCEL_XYZ(3),
	ACCEL_INT0(4),
	ACCEL_INT1(5),

	ACCEL_CONF(6),

	COLOR(7),
	COLOR_DUTY(8),
	COLOR_PERIOD(9),
	COLOR_WAVEFORM(13),

	VIBE(10),
	VIBE_DUTY(11),
	VIBE_PERIOD(12);

	final int code;

	PacketType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
