package propinquity.hardware;

import processing.core.PApplet;

/**
 * The Glove class reflects the glove the player will wear. 
 * Making changes (e.g. vibration settings) in this class should transparently propagate down to the hardware via the HardwareInterface.
 *
 */
public class Glove extends Patch { 
//TODO: Think this though and probably drop the Glove class completely

	/**
	 * Contruct a Glove with the specified address (usually the address of the associate XBee) and use the given HardwareInterface for low level comunication.
	 *
	 * @param address the address of the Glove, normally this is the address of the associate XBee.
	 * @param hardware the HardwareInterface use to send data. The Glove does not auto register with the HardwareInterface. this must be done externally.
	 */
	public Glove(int address, HardwareInterface hardware) {
		super(address, hardware);

		if(daemon != null) daemon.setModeFlag(1);
	}

	/**
	 * Enables or disables the device. This will stop/start all output (vibration, color, prox sensor, other).
	 *
	 * @param active the new state of the device.
	 */
	public void setActivationMode(int mode) {
		super.setActivationMode(mode & ~Mode.PROX); // Don't allow setting PROX mode
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
				setColor(color[0], color[1], color[2]);
				setColorPeriod(0);
				setVibeLevel(0);
				break;
			}
			case 1: { //In range: color and vibe pulse
				setColor(color[0], color[1], color[2]);
				setColorPeriod(SLOW_BLINK);
				setColorDuty(127);

				// setVibeDuty(127);
				setVibePeriod(0);
				setVibeLevel(150);
				break;
			}
			case 2: { //Sweet stop: vibe one, fast color pulse
				setColor(color[0], color[1], color[2]);
				setColorPeriod(FAST_BLINK);
				setColorDuty(127);

				setVibePeriod(0);
				setVibeLevel(150);
				break;
			}
		}

	}

}