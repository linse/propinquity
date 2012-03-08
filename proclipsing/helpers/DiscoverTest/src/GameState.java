import xbee.XBeeReader;


public class GameState {
	public class VibeState {
		int period;
		int duty;
	}
	public class PatchState {
		Boolean active;
		int[] color = {0,0,0};
		int proxvalue;
	}
	PatchState[] patches = new PatchState[16];
	VibeState[] vibestates = new VibeState[2];
	Player[] players;
	
	public static final int VIBE_PATTERN_0 = 0;
	public static final int VIBE_PATTERN_1 = 1;
	public static final int VIBE_PATTERN_2 = 2;
	public static final int VIBE_PATTERN_3 = 3;
	public static final int VIBE_PATTERN_4 = 4;
	int lastpattern = 0;
	
	public GameState() {
		for (int i=0;i<16;i++) patches[i] = new PatchState();
		for (int i=0;i<2;i++) vibestates[i] = new VibeState();
	}
	
	public int getProxReading(int patch) {
		return this.patches[patch].proxvalue;
	}
	
	public Boolean isPatchActive(int patch) {
		return this.patches[patch].active;
	}
	
	public void activatePatch(int patch, Boolean active) {
		this.patches[patch].active = active;
		//sendPatchState(patch);
	}

	public void setPatchColor(int patch, int[] color) {
			this.patches[patch].color[0] = color[0];
			this.patches[patch].color[1] = color[1];
			this.patches[patch].color[2] = color[2];
			sendPatchState(patch);
	}

	public void sendPatchState(int patch) {
		XPan xpan = Player.address_to_xpan.get(patch);
			xpan.sendOutgoing(patch, 
							  XPan.getProxStatePacket(this.patches[patch].active, 
								  					  this.patches[patch].color, 1000, 255));
	}
	
	public static final int MAPPING_1 = 0;
	public static final int MAPPING_2 = 1;
	public static final int MAPPING_3 = 2;
	public static final int MAPPING_4 = 3;
	public static final int MAPPING_5 = 4;
	
	public void setVibeMapping(int map) {
		
	}

	public void update() {
		
		// Player 1		
		int average = (this.patches[1].proxvalue + this.patches[2].proxvalue) / 2;
//		DiscoverTest.game.println("Player 1: " + average);
		
		// Player 2	
		int average2 = (this.patches[9].proxvalue + this.patches[10].proxvalue) / 2;
//		DiscoverTest.game.println("Player 2: " + average2);

		int pattern = VIBE_PATTERN_0;
		if (average2 > 400) {
			pattern = VIBE_PATTERN_1;
		}
		else if (average2 > 300) {
			pattern = VIBE_PATTERN_2;
		}
		else if (average2 > 100) {
			pattern = VIBE_PATTERN_3;
		}
		else  {
			pattern = VIBE_PATTERN_4;
		}
		if (pattern != lastpattern) {
			DiscoverTest.game.println("pattern: " + pattern);
			lastpattern = pattern;
			switch (pattern) {
			case VIBE_PATTERN_1:
				vibestates[0].period = 500;
				vibestates[0].duty = 127;
				break;
			case VIBE_PATTERN_2:
				vibestates[0].period = 1000;
				vibestates[0].duty = 127;
				break;
			case VIBE_PATTERN_3:
				vibestates[0].period = 1000;
				vibestates[0].duty = 64;
			default:
				vibestates[0].period = 1000;
				vibestates[0].duty = 0;
			}
			sendVibeState(0, 13);
		}		
		
		// for each player
		//   calculate average prox value
		//   map to vibe pulse
		//   if vibe pulse changed
		//     send vibe pulse to opposite player
		
		
		
	}

	private void sendVibeState(int vibe, int addr) {
		XPan xpan = Player.address_to_xpan.get(addr);
		if (xpan != null) {
		xpan.sendOutgoing(addr, 
						  XPan.getVibePacket(vibestates[vibe].period, vibestates[vibe].duty));
		}
	}

	public void xBeeEvent(XBeeReader xbee) {
		int[] packet = XPan.decodePacket(xbee);
		if (packet == null) return;  
		switch (packet[0]) {
		case XPan.PROX_IN_PACKET_TYPE:
			assert(packet.length == XPan.PROX_IN_PACKET_LENGTH);

			int patch = (packet[1] >> 1);                
			this.patches[patch].proxvalue = ((packet[4] & 0xFF) << 8) | (packet[5] & 0xFF);;
//			DiscoverTest.game.println(this.patches[patch].proxvalue);
			break;
		}
		
	}

	public Player getPlayerForPatch(int patch) {
	    if (patch >= 1 && patch <= 4) return players[0];
	    else if (patch >= 9 && patch <= 16) return players[1];
	    else return null;
	  }
	  
	  public int getPlayerIndexForPatch(int patch) {
	    if (patch >= 1 && patch <= 4) return 0;
	    else if (patch >= 9 && patch <= 16) return 1;
	    else return -1;
	  }
	
	
}
