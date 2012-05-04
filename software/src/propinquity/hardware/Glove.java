package propinquity.hardware;

import processing.core.PApplet;

public class Glove {

	final int address;

	int vibe_level, vibe_period, vibe_duty;

	public Glove(int address) {
		this.address = address;

		vibe_level = 0;
		vibe_period = 0;
		vibe_duty = 0;
	}


	public void setVibe(int level, int period, int duty) {
		vibe_level = PApplet.constrain(level, 0, 255);
		vibe_period = PApplet.constrain(period, 0, 255);
		vibe_duty = PApplet.constrain(duty, 0, 255);
	}

	public void setVibeLevel(int level) {
		vibe_level = PApplet.constrain(level, 0, 255);
	}

	public void setVibePeriod(int period) {
		vibe_period = PApplet.constrain(period, 0, 255);
	}

	public void setVibeDuty(int duty) {
		vibe_duty = PApplet.constrain(duty, 0, 255);
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

}