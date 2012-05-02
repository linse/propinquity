package propinquity.hardware;

public interface HardwareInterface {

	public void addPatch(Patch patch);

	public void removePatch(Patch patch);

	public void addGlove(Glove glove);

	public void removeGlove(Glove glove);

	public void sendPacket(Packet packet);

}