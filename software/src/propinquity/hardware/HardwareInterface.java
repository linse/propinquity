package propinquity.hardware;

public interface HardwareInterface {

	public void addPatch(Patch patch);

	public void removePatch(Patch patch);

	public void addGlove(Glove glove);

	public void removeGlove(Glove glove);

	public void addProxEventListener(ProxEventListener listener);

	public void removeProxEventListener(ProxEventListener listener);

	public void sendPacket(Packet packet);

}