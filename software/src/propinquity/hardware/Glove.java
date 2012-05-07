package propinquity.hardware;

import processing.core.PApplet;

public class Glove {

	final int address;

	int vibe_level, vibe_period, vibe_duty;

	HardwareInterface hardware;

	public Glove(int address, HardwareInterface hardware) {
		this.hardware = hardware;
		this.address = address;

		vibe_level = 0;
		vibe_period = 0;
		vibe_duty = 0;
	}


	public void setVibe(int level, int period, int duty) {
		setVibeLevel(level);
		setVibePeriod(period);
		setVibeDuty(duty);
	}

	public void setVibeLevel(int level) {
		vibe_level = PApplet.constrain(level, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.VIBE_LEVEL, new int[] {vibe_level}));
	}

	public void setVibePeriod(int period) {
		vibe_period = PApplet.constrain(period, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.VIBE_PERIOD, new int[] {vibe_period}));
	}

	public void setVibeDuty(int duty) {
		vibe_duty = PApplet.constrain(duty, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.VIBE_DUTY, new int[] {vibe_duty}));
	}

	public int getVibeLevel() {
		return vibe_level;
	}

	public int getVibePeriod() {
		return vibe_period;
	}

	public int getVibeDuty() {
		return vibe_duty;
	}

	public int getAddress() {
		return address;
	}

}