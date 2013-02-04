package propinquity.hardware;

/**
 * Interface that describe what a hardware implementation must provided as external features. This is subsequently implemented by any XBee control code or hardware simluator code.
 *
 */
public interface HardwareInterface {

	public void addPatch(Patch patch);

	public boolean removePatch(Patch patch);

	public void addGlove(Glove glove);

	public boolean removeGlove(Glove glove);

	public void addProxEventListener(ProxEventListener listener);

	public boolean removeProxEventListener(ProxEventListener listener);

	public void addAccelEventListener(AccelEventListener listener);

	public boolean removeAccelEventListener(AccelEventListener listener);

	public void sendPacket(Packet packet);

}