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

	void receiveProxReadings() {
		println("start prox readings receive");
		if (this.xpans[PROX_1]!=null) {
			this.xpans[PROX_1].receiveProxReadings();
		}
		if (this.xpans[PROX_2]!=null) {
			this.xpans[PROX_2].receiveProxReadings();
		}
		println("end prox readings receive");
	}
	
	void broadcastVibe(int value) {
		if (this.xpans[VIBE]!=null) {
			this.xpans[VIBE].broadcastVibe(value);
		}
	}

	public void setRemoteProximityStepLength(int stepLength) {
		if (this.xpans[PROX_1]!=null) {
			this.xpans[PROX_1].broadcastProxConfig(stepLength);
		}
		if (this.xpans[PROX_2]!=null) {
			this.xpans[PROX_2].broadcastProxConfig(stepLength);
		}
	}

}
