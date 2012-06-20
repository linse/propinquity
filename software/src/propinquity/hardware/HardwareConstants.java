package propinquity.hardware;

/**
 * Convenient interface to hold some constants which are common the all hardware and may also need to be accessed elsewhere.
 *
 */
public interface HardwareConstants {

	static final boolean MIN_PACK = false;
	static final boolean MANUAL_PACK = true;
	static final boolean USE_DAEMON = false;
	static final int DAEMON_PERIOD = 300;

	static final int SLOW_BLINK = 15;
	static final int FAST_BLINK = 10;

	static final int DEFAULT_DUTY_CYCLE = 127;

}