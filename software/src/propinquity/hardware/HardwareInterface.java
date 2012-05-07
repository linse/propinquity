package propinquity.hardware;

public interface HardwareInterface {

	public void addPatch(Patch patch);

	public boolean removePatch(Patch patch);

	public void addGlove(Glove glove);

	public boolean removeGlove(Glove glove);

	public void addProxEventListener(ProxEventListener listener);

	public boolean removeProxEventListener(ProxEventListener listener);

	public void sendPacket(Packet packet);

}