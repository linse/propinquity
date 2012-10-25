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
		Color.red(), Color.green(), Color.blue(), Color.violet(), Color.blue(), Color.blue()
	};

	public static final int[] GLOVE_ADDR = new int[] {
		5, 10
	};

	boolean[] patchStates;

	XBeeBaseStation xbeeBaseStation;

	Patch[] patches;
	Glove[] gloves;

	boolean toggle = false;
	boolean blink = true;
	boolean ninja = false;

	Thread ram;

	boolean usegloves = false;

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
			rect(50+width/patches.length*i, 30, 25, 25);
		}
	}
	
	public void keyPressed() {
		if(key != CODED) {
			if(key == 's') {
				xbeeBaseStation.scan();
			} else if(key == 'g') {
				usegloves = !usegloves;

				if(!usegloves) {
					clearGloves();
				}
			} else if(key == 'b') {
				blink = !blink;

				if(!blink) {
					for(int i = 0;i < patches.length;i++) {
						patches[i].setMode(0);
					}
				}
			} else if(key >= 48 && key <= 57) {
				int num = keyCode-49;
				if(num == -1) num = 9;
				if(ninja) {
					if(num == 0) {
						patches[0].setColor(PATCH_COLORS[num]);
						patches[0].setActive(true);
						patches[1].setColor(PATCH_COLORS[num]);
						patches[1].setActive(true);
						patches[2].setColor(PATCH_COLORS[num]);
						patches[2].setActive(true);
						patches[3].setColor(PATCH_COLORS[num]);
						patches[3].setActive(true);
						patches[4].setColor(PATCH_COLORS[num]);
						patches[4].setActive(false);
						patches[5].setColor(PATCH_COLORS[num]);
						patches[6].setActive(false);
					} else if(num == 1) {
						patches[0].setColor(PATCH_COLORS[num]);
						patches[0].setActive(true);
						patches[1].setColor(PATCH_COLORS[num]);
						patches[1].setActive(true);
						patches[2].setColor(PATCH_COLORS[num]);
						patches[2].setActive(false);
						patches[3].setColor(PATCH_COLORS[num]);
						patches[3].setActive(false);
						patches[4].setColor(PATCH_COLORS[num]);
						patches[4].setActive(false);
						patches[5].setColor(PATCH_COLORS[num]);
						patches[6].setActive(false);
					} else if(num == 2) {
						patches[0].setColor(PATCH_COLORS[num]);
						patches[0].setActive(false);
						patches[1].setColor(PATCH_COLORS[num]);
						patches[1].setActive(false);
						patches[2].setColor(PATCH_COLORS[num]);
						patches[2].setActive(true);
						patches[3].setColor(PATCH_COLORS[num]);
						patches[3].setActive(true);
						patches[4].setColor(PATCH_COLORS[num]);
						patches[4].setActive(true);
						patches[5].setColor(PATCH_COLORS[num]);
						patches[6].setActive(true);
					}
				} else if(toggle) {
					if(num < patches.length) {
						if(patchStates[num]) {
							patches[num].setColor(PATCH_COLORS[num]);
							patches[num].setActive(true);
						} else {
							patches[num].setColor(PATCH_COLORS[num]);
							patches[num].setActive(false);
							clearGloves();
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

	public void clearGloves() {
		for(Glove glove : gloves) {
			glove.setActive(false);
			glove.clear();
			glove.setVibeDuty(HardwareConstants.DEFAULT_DUTY_CYCLE);
		}
	}

	public void keyReleased() {
		if(toggle) return;
		if(ninja) return;
		if(key != CODED) {
			if(key >= 48 && key <= 57) {
				int num = keyCode-49;
				if(num == -1) num = 9;

				if(num < patches.length) {

					patches[num].setColor(PATCH_COLORS[num]);
					patches[num].setActive(false);

					patchStates[num] = false;

					clearGloves();
				}
			}
		}
	}

	public void proxEvent(Patch patch) {
		for(int i = 0;i < patches.length;i++) {
			if(patches[i] == patch) {
				if(usegloves) {
					if(patch.getZone() > 0) {
						if(i < patches.length/2) {
							gloves[0].setActive(true);
							gloves[0].setMode(1);
						} else {
							gloves[1].setActive(true);
							gloves[1].setMode(1);
						}
					} else {
						if(i < patches.length/2) {
							gloves[0].setActive(false);
						} else {
							gloves[1].setActive(false);
						}
					}
				}

				if(blink) {
					patches[i].setMode(patches[i].getZone());
				}
				return;
			}
		}

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
					// patches[i].setActive(patchStates[i]);
					// patches[i].setCol12311231123123or(PATCH_COLORS[i]);
				}
				try {
					Thread.sleep(100);
				} catch(Exception e) {

				}
			}
		}

	}

}
