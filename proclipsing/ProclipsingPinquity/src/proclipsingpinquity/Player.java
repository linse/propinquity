package proclipsingpinquity;

import processing.core.PApplet;

public class Player extends PApplet {

	static final int XPANS_PER_PLAYER = 3;
	static final int PROX_1 = 0;
	static final int PROX_2 = 1;
	static final int VIBE = 2;

	Xpan[] xpans;

	Player() {
		this.xpans = new Xpan[XPANS_PER_PLAYER];
	}

	void broadcastVibe() {
		this.xpans[VIBE].broadcastVibe(200);
	}

}
