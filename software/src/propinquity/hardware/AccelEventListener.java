package propinquity.hardware;

/**
 * Any class implementing this interface can register with a class implementing {@link propinquity.hardware.HardwareInterface} in order to receive callbacks when new accel data is recieved.
 *
 */
public interface AccelEventListener {

	public void accelXYZEvent(Patch patch);
	
	public void accelInterrupt0Event(Patch patch);

	public void accelInterrupt1Event(Patch patch);

}