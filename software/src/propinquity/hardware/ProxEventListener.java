package propinquity.hardware;

/**
 * Any class implementing this interface can register with a class implementing {@link propinquity.hardware.HardwareInterface} in order to receive callbacks when new prox data is recieved.
 *
 */
public interface ProxEventListener {

	public void proxEvent(Patch patch);

}