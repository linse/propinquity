package propinquity.hardware;

import processing.core.PApplet;

public class Patch {

	final int address;

	boolean active;

	int vibe_level, vibe_period, vibe_duty, color_period, color_duty, prox;

	int[] color;

	HardwareInterface hardware;

	public Patch(int address, HardwareInterface hardware) {
		this.hardware = hardware;
		this.address = address;

		active = false;

		vibe_level = 0;
		vibe_period = 0;
		vibe_duty = 0;

		color = new int[3];
		color[0] = 0;
		color[1] = 0;
		color[2] = 0;

		color_period = 0;
		color_duty = 0;

		prox = 0;
	}

	public void setActive(boolean active) {
		this.active = active;
		hardware.sendPacket(new Packet(address, PacketType.CONF, new int[] {active?1:0}));
	}

	public boolean getActive() {
		return active;
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

	public void setColor(int red, int green, int blue) {
		color[0] = PApplet.constrain(red, 0, 255);
		color[1] = PApplet.constrain(green, 0, 255);
		color[2] = PApplet.constrain(blue, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.COLOR, new int[] {color[0], color[1], color[2]}));
	}

	public void setColorPeriod(int period) {
		color_period = PApplet.constrain(period, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.COLOR_PERIOD, new int[] {color_period}));
	}

	public void setColorDuty(int duty) {
		color_duty = PApplet.constrain(duty, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.COLOR_DUTY, new int[] {color_duty}));
	}

	public void setProx(int prox) {
		this.prox = PApplet.constrain(prox, 0, 1024);
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

	public int[] getColor() {
		return color;
	}

	public int getColorPeriod() {
		return color_period;
	}

	public int getColorDuty() {
		return color_duty;
	}

	public int getProx() {
		return prox;
	}

	public int getAddress() {
		return address;
	}

}