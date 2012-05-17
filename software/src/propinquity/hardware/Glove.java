package propinquity.hardware;

import processing.core.PApplet;

/**
 * The Glove class reflects the glove the player will wear. 
 * Making changes (e.g. vibration settings) in this class should transparently propagate down to the hardware via the HardwareInterface.
 *
 */
public class Glove {

	final int address;

	boolean active;

	int vibe_level, vibe_period, vibe_duty;

	HardwareInterface hardware;

	/**
	 * Contruct a Glove with the specified address (usually the address of the associate XBee) and use the given HardwareInterface for low level comunication.
	 *
	 * @param address the address of the Glove, normally this is the address of the associate XBee.
	 * @param hardware the HardwareInterface use to send data. The Glove does not auto register with the HardwareInterface. this must be done externally.
	 */
	public Glove(int address, HardwareInterface hardware) {
		this.hardware = hardware;
		this.address = address;

		active = false;

		vibe_level = 0;
		vibe_period = 0;
		vibe_duty = 0;
	}

	/**
	 * Clear all output, doesn't affect active flag.
	 *
	 */
	public void clear() {
		setVibeLevel(0);
		setVibePeriod(0);
		setVibeDuty(0);
	}

	/**
	 * Enables or disables the device. This will stop/start all output (vibration, color, prox sensor, other).
	 *
	 * @param active the new state of the device.
	 */
	public void setActive(boolean active) {
		this.active = active;
		hardware.sendPacket(new Packet(address, PacketType.CONF, new int[] {active?1:0}));
	}

	/**
	 * Gets the current state of the device, enabled or disabled
	 *
	 * @return true if the device is enable false otherwise.
	 */
	public boolean getActive() {
		return active;
	}

	/**
	 * Configure the vibration and propagate changes to the HardwareInterface.
	 *
	 * @param level the vibration level, constrained to the range 0-255.
	 * @param period the vibration period, constrained to the range 0-255.
	 * @param duty the vibration duty cycle, constrained to the range 0-255. 0 = 0% duty cycle 255=100% duty cycle.
	 */
	public void setVibe(int level, int period, int duty) {
		setVibeLevel(level);
		setVibePeriod(period);
		setVibeDuty(duty);
	}
	
	/**
	 * Configure the vibration level and propagate changes to the HardwareInterface.
	 *
	 * @param level the vibration level, constrained to the range 0-255.
	 */
	public void setVibeLevel(int level) {
		if(vibe_level == level) return;
		vibe_level = PApplet.constrain(level, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.VIBE_LEVEL, new int[] {vibe_level}));
	}

	/**
	 * Configure the vibration period and propagate changes to the HardwareInterface.
	 *
	 * @param period the vibration period, constrained to the range 0-255.
	 */
	public void setVibePeriod(int period) {
		if(vibe_period == period) return;
		vibe_period = PApplet.constrain(period, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.VIBE_PERIOD, new int[] {vibe_period}));
	}

	/**
	 * Configure the vibration duty and propagate changes to the HardwareInterface.
	 *
	 * @param duty the vibration duty cycle, constrained to the range 0-255. 0 = 0% duty cycle 255=100% duty cycle.
	 */
	public void setVibeDuty(int duty) {
		if(vibe_duty == duty) return;
		vibe_duty = PApplet.constrain(duty, 0, 255);
		hardware.sendPacket(new Packet(address, PacketType.VIBE_DUTY, new int[] {vibe_duty}));
	}

	/**
	 * Get the vibration level.
	 *
	 * @return the vibration level, constrained to the range 0-255.
	 */
	public int getVibeLevel() {
		return vibe_level;
	}

	/**
	 * Get the vibration period.
	 *
	 * @return the vibration period, constrained to the range 0-255.
	 */
	public int getVibePeriod() {
		return vibe_period;
	}

	/**
	 * Get the vibration duty.
	 *
	 * @return the vibration duty, constrained to the range 0-255. 0 = 0% duty cycle 255=100% duty cycle.
	 */
	public int getVibeDuty() {
		return vibe_duty;
	}

	/**
	 * Get address. This usually corresponds to the XBee address of the device.
	 *
	 * @return the address of the device.
	 */
	public int getAddress() {
		return address;
	}

}