package propinquity.hardware;

import processing.core.PApplet;
import propinquity.Color;

/**
 * The Patch class reflects the patch the player will wear. 
 * Making changes (e.g. vibration and color settings) in this class should transparently propagate down to the hardware via the HardwareInterface.
 * If the Patch is registered with a HardwareInterface it will have it's prox update as data is receive from the remote device. This registration is not automatic and must be done externally via {@link HardwareInterface#addPatch(Patch patch)}.
 *
 */
public class Patch implements HardwareConstants {

	public static final int MIN_RANGE = 100;
	public static final int MAX_RANGE = 700;
	public static final int MIN_SWEETSPOT = 500; //Disabled for now
	public static final int MAX_SWEETSPOT = 500;

	final int address;

	boolean active;

	int vibe_level, vibe_period, vibe_duty, color_period, color_duty, prox;

	int[] color;

	HardwareInterface hardware;

	PatchDaemon daemon;

	/**
	 * Contruct a Patch with the specified address (usually the address of the associate XBee) and use the given HardwareInterface for low level comunication.
	 *
	 * @param address the address of the Patch, normally this is the address of the associate XBee.
	 * @param hardware the HardwareInterface use to send data. The Patch does not auto register with the HardwareInterface. this must be done externally.
	 */
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

		if(USE_DAEMON) daemon = new PatchDaemon();
	}

	/**
	 * Clear all output, doesn't affect active flag.
	 *
	 */
	public void clear() {
		hardware.sendPacket(new Packet(address, PacketType.CLEAR, new int[0]));

		color[0] = 0;
		color[1] = 0;
		color[2] = 0;

		color_period = 0;
		color_duty = 0;

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
		if(!active) prox = 0; //Clear prox when not active
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
	 * Configure the color and propagate changes to the HardwareInterface.
	 *
	 * @param color the color
	 */
	public void setColor(Color color) {
		setColor(color.r, color.g, color.b);
	}

	/**
	 * Configure the color and propagate changes to the HardwareInterface.
	 *
	 * @param red the red color level, constrained to the range 0-255.
	 * @param green the green color level, constrained to the range 0-255.
	 * @param blue the blue color level, constrained to the range 0-255.
	 */
	public void setColor(int red, int green, int blue) {
		if(MIN_PACK && color[0] == red && color[1] == green && color[2] == blue) return;
		color[0] = PApplet.constrain(red, 0, 255);
		color[1] = PApplet.constrain(green, 0, 255);
		color[2] = PApplet.constrain(blue, 0, 255);
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.COLOR, new int[] {color[0], color[1], color[2]}));
	}

	/**
	 * Configure the color period and propagate changes to the HardwareInterface.
	 *
	 * @param period the color period, constrained to the range 0-255.
	 */
	public void setColorPeriod(int period) {
		if(MIN_PACK && color_period == period) return;
		color_period = PApplet.constrain(period, 0, 255);
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.COLOR_PERIOD, new int[] {color_period}));
	}

	/**
	 * Configure the color duty and propagate changes to the HardwareInterface.
	 *
	 * @param duty the color duty cycle, constrained to the range 0-255. 0 = 0% duty cycle 255=100% duty cycle.
	 */
	public void setColorDuty(int duty) {
		if(MIN_PACK && color_duty == duty) return;
		color_duty = PApplet.constrain(duty, 0, 255);
		if(MANUAL_PACK) hardware.sendPacket(new Packet(address, PacketType.COLOR_DUTY, new int[] {color_duty}));
	}

	/**
	 * Sets the value of the prox sensor for this device. Normally this should be by the HardwareInterface which this device is registered with as the data arrives from the real device. It should only be called elsewhere for testing.
	 *
	 * @param prox the prox value, constrained to the range 0-1024.
	 */
	public void setProx(int prox) {
		//Prevent straggler packet from changing the prox value when active if false
		if(active) this.prox = PApplet.constrain(prox, 0, 1024);
		else this.prox = 0;
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
	 * Get the color levels.
	 *
	 * @return an array of rgb color levels, constrained to the range 0-255.
	 */
	public int[] getColor() {
		return color;
	}

	/**
	 * Get the color period.
	 *
	 * @return the color period, constrained to the range 0-255.
	 */
	public int getColorPeriod() {
		return color_period;
	}

	/**
	 * Get the color duty.
	 *
	 * @return the color duty, constrained to the range 0-255. 0 = 0% duty cycle 255=100% duty cycle.
	 */
	public int getColorDuty() {
		return color_duty;
	}

	/**
	 * The current value of the prox sensor, constrained to the 0-1024 range.
	 *
	 * @return the prox value, constrained to the 0-1024 range.
	 */
	public int getProx() {
		//Return 0 if not active
		if(active) return prox;
		else return 0;
	}

	/**
	 * Get the "zone" which the prox is reading.
	 *
	 * @return 0 if there is nothing in range, 1 if something is in range, 2 if something is in the "sweet spot"
	 */
	public int getZone() {
		if(prox < MIN_RANGE || prox > MAX_RANGE) return 0; //Out of range or too close
		else if(prox > MIN_SWEETSPOT && prox < MAX_SWEETSPOT) return 2; //In the sweet spot
		else return 1; //Else normal zone
	}

	/**
	 * Sets the patch in a preset "mode". Assumes that the patch color, vibe duty and color duty are already set.
	 * 
	 * 0 is color only, 1 is color blink and vibe blink, 2 is fast color blink and vibe
	 *
	 * @param mode the mode to put the patch in.
	 */
	public void setMode(int mode) {
		switch(mode) {
			case 0:
			default: { //Not in range: just patch color
				setColor(color[0], color[1], color[2]);
				setColorPeriod(0);
				setVibeLevel(0);
				break;
			}
			case 1: { //In range: color and vibe pulse
				setColor(color[0], color[1], color[2]);
				setColorPeriod(SLOW_BLINK);
				setColorDuty(127);

				// setVibeLevel(150);
				// setVibeDuty(127);
				// setVibePeriod(SLOW_BLINK);
				break;
			}
			case 2: { //Sweet stop: vibe one, fast color pulse
				setColor(color[0], color[1], color[2]);
				setColorPeriod(FAST_BLINK);
				setColorDuty(127);

				// setVibeLevel(150);
				// setVibeDuty(127);
				// setVibePeriod(0);
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

	class PatchDaemon implements Runnable {

		Thread thread;
		boolean running;

		PatchDaemon() {
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

				hardware.sendPacket(new Packet(address, PacketType.COLOR, new int[] {color[0], color[1], color[2]}));
				hardware.sendPacket(new Packet(address, PacketType.COLOR_DUTY, new int[] {color_duty}));
				hardware.sendPacket(new Packet(address, PacketType.COLOR_PERIOD, new int[] {color_period}));

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