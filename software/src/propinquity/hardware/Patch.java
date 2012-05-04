package propinquity.hardware;

import processing.core.PApplet;

public class Patch {

	final int address;

	int vibe_level, vibe_period, vibe_duty, color_period, color_duty, prox;

	int[] color;

	public Patch(int address) {
		this.address = address;

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

	public void setColor(int red, int green, int blue) {
		color[0] = PApplet.constrain(red, 0, 255);
		color[1] = PApplet.constrain(green, 0, 255);
		color[2] = PApplet.constrain(blue, 0, 255);
	}

	public void setColorPeriod(int period) {
		color_period = PApplet.constrain(period, 0, 255);
	}

	public void setColorDuty(int duty) {
		color_duty = PApplet.constrain(duty, 0, 255);
	}

	public void setProx(int prox) {
		this.prox = PApplet.constrain(prox, 0, 255);
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

}