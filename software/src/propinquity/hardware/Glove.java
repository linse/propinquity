package propinquity.hardware;

import processing.core.PApplet;

/**
 * The Glove class reflects the glove the player will wear. 
 * Making changes (e.g. vibration settings) in this class should transparently propagate down to the hardware via the HardwareInterface.
 *
 */
public class Glove implements HardwareConstants {

	final int address;

	boolean active;

	int vibe_level, vibe_period, vibe_duty;

	HardwareInterface hardware;

	GloveDameon daemon;

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

		if(USE_DAEMON) daemon = new GloveDameon();
	}

	/**
	 * Clear all output, doesn't affect active flag.
	 *
	 */
	public void clear() {
		hardware.sendPacket(new Packet(address, PacketType.CLEAR, new int[0]));

		vibe_level = 0;
		vibe_period = 0;
		vibe_duty = 0;
	}

	/**
	 * Enables or disables the device. This will stop/start all output (vibration, color, prox sensor, other).
	 *
	 * @param active the new state of the device.
	 */
	public void setActive(boolean active) {
		if(MIN_PACK && this.active == active) return;
		this.active = active;
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.CONF, new int[] {active?1:0}));
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
		if(MIN_PACK && vibe_level == level) return;
		vibe_level = PApplet.constrain(level, 0, 255);
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.VIBE_LEVEL, new int[] {vibe_level}));
	}

	/**
	 * Configure the vibration period and propagate changes to the HardwareInterface.
	 *
	 * @param period the vibration period, constrained to the range 0-255.
	 */
	public void setVibePeriod(int period) {
		if(MIN_PACK && vibe_period == period) return;
		vibe_period = PApplet.constrain(period, 0, 255);
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.VIBE_PERIOD, new int[] {vibe_period}));
	}

	/**
	 * Configure the vibration duty and propagate changes to the HardwareInterface.
	 *
	 * @param duty the vibration duty cycle, constrained to the range 0-255. 0 = 0% duty cycle 255=100% duty cycle.
	 */
	public void setVibeDuty(int duty) {
		if(MIN_PACK && vibe_duty == duty) return;
		vibe_duty = PApplet.constrain(duty, 0, 255);
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.VIBE_DUTY, new int[] {vibe_duty}));
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
	 * Sets the patch in a preset "mode". Assumes that the patch color, vibe duty and color duty are already set.
	 * 
	 * 0 is color only, 1 is color blink and vibe blink, 2 is fast color blink and vibe
	 *
	 * @param mode the mode to put the patch in.
	 */
	public void setMode(int mode) {
		if(mode > 1) mode = 1; //TODO: Hack to make only 1 zone
		switch(mode) {
			case 0:
			default: { //Not in range: just patch color
				setVibeLevel(0);
				break;
			}
			case 1: { //In range: color and vibe pulse
				// setVibeDuty(127);
				setVibePeriod(0);
				setVibeLevel(255);
				break;
			}
			case 2: { //Sweet stop: vibe one, fast color pulse
				setVibePeriod(0);
				setVibeLevel(255);
				break;
			}
		}

	}

	/**
	 * Get address. This usually corresponds to the XBee address of the device.
	 *
	 * @return the address of the device.
	 */
	public int getAddress() {
		return address;
	}

	class GloveDameon implements Runnable {

		Thread thread;
		boolean running;

		GloveDameon() {
			running = true;

			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}

		void stop() {
			running = false;
			if(thread != null) while(thread.isAlive()) Thread.yield();
		}

		public void run() {
			while(running) {
				hardware.sendPacket(new Packet(address, PacketType.CONF, new int[] {active?1:0}));

				hardware.sendPacket(new Packet(address, PacketType.VIBE_LEVEL, new int[] {vibe_level}));
				hardware.sendPacket(new Packet(address, PacketType.VIBE_DUTY, new int[] {vibe_duty}));
				hardware.sendPacket(new Packet(address, PacketType.VIBE_PERIOD, new int[] {vibe_period}));

				try {
					Thread.sleep(DAEMON_PERIOD);
				} catch(Exception e) {

				}
			}
		}

	}

}