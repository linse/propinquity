package propinquity;

import processing.core.*;
import propinquity.hardware.*;

/**
 * A hacky sketch to test patches or gloves, provded buttons and sliders to control active, vibe and LEDS as well as giving prox value back.
 *
 */
public class PlayTest extends PApplet implements ProxEventListener {

	// Unique serialization ID
	static final long serialVersionUID = 6340508174717159418L;

	public static final int[] PATCH_ADDR = new int[] {
		1, 2, 3, 6, 7, 8
	};

	public static final Color[] PATCH_COLORS = new Color[] {
		Color.blue(), Color.green(), Color.green(), Color.green(), Color.green(), Color.green()
	};

	public static final int[] GLOVE_ADDR = new int[] {
		5, 10
	};

	boolean[] patchStates;

	XBeeBaseStation xbeeBaseStation;

	Patch[] patches;
	Glove[] gloves;

	boolean toggle = false;

	Thread ram;

	public void setup() {
		size(500, 500);

		xbeeBaseStation = new XBeeBaseStation();
		xbeeBaseStation.scan();
		xbeeBaseStation.addProxEventListener(this);

		patches = new Patch[PATCH_ADDR.length];
		for(int i = 0;i < PATCH_ADDR.length;i++) {
			patches[i] = new Patch(PATCH_ADDR[i], xbeeBaseStation);
			xbeeBaseStation.addPatch(patches[i]);
		}

		gloves = new Glove[GLOVE_ADDR.length];
		for(int i = 0;i < GLOVE_ADDR.length;i++) {
			gloves[i] = new Glove(GLOVE_ADDR[i], xbeeBaseStation);
			xbeeBaseStation.addGlove(gloves[i]);
		}

		patchStates = new boolean[PATCH_ADDR.length];

		ram = new Thread(new Ram());
		ram.setDaemon(true);
		ram.start();
	}

	public void draw() {
		if(xbeeBaseStation.isScanning()) background(30, 0, 0);
		else background(0);

		if(xbeeBaseStation.listXBees() != null && xbeeBaseStation.listXBees().length > 0) {
			stroke(150);
			fill(0, 75, 0);
		} else {
			stroke(150);
			fill(75, 0, 0);
		}

		rect(5, height-30, 25, 25);

		for(int i = 0;i < patches.length;i++) {
			if(patchStates[i]) {
				stroke(150);
				fill(0, 75, 0);
			} else {
				stroke(150);
				fill(75, 0, 0);
			}
			rect(width/patches.length*i, 30, 25, 25);
		}
	}
	
	public void keyPressed() {
		if(key != CODED) {
			if(key == 's') {
				xbeeBaseStation.scan();
			} else if(key >= 48 && key <= 57) {
				int num = keyCode-49;
				if(num == -1) num = 9;

				if(toggle) {
					if(num < patches.length) {
						if(patchStates[num]) {
							patches[num].setColor(PATCH_COLORS[num]);
							patches[num].setActive(true);
						} else {
							patches[num].setColor(PATCH_COLORS[num]);
							patches[num].setActive(false);
						}

						patchStates[num] = !patchStates[num];
					}
				} else {
					if(num < patches.length) {
						patches[num].setColor(PATCH_COLORS[num]);
						patches[num].setActive(true);

						patchStates[num] = true;
					}
				}
			}
		}
	}

	public void keyReleased() {
		if(toggle) return;
		if(key != CODED) {
			if(key >= 48 && key <= 57) {
				int num = keyCode-49;
				if(num == -1) num = 9;

				if(num < patches.length) {

					patches[num].setColor(PATCH_COLORS[num]);
					patches[num].setActive(false);

					patchStates[num] = false;
				}
			}
		}
	}

	public void proxEvent(Patch patch) {

	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.PlayTest" });
	}

	class Ram implements Runnable {

		boolean running;

		Ram() {
			running = true;
		}

		public void run() {
			while(running) {
				for(int i = 0;i < patches.length;i++) {
					patches[i].setActive(patchStates[i]);
					patches[i].setColor(PATCH_COLORS[i]);
				}
				try {
					Thread.sleep(100);
				} catch(Exception e) {

				}
			}
		}

	}

}
