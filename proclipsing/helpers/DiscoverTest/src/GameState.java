import xbee.XBeeReader;

public class GameState {
	// parent class so we have all infos such as xpans etc.
	// in xpan this is not passed, but accessed as a global variable within DiscoverTest 
	DiscoverTest game;
	
	public GameState(DiscoverTest game) {
		this.game = game;
	}

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

	/*
	public void setPatchColor(int player, int patch, int[] color) {
		int addr = getPatchAddress(player, patch); 
		// first or second base station?
		int localXbee = (patch <= 2) ? 0 : 1; 
		// get the right xpan for player and patch
		XPan xpan = game.players[player-1].xpans.get(Player.PROX)[localXbee]; 
		if (xpan!=null) {
			xpan.sendOutgoing(addr, XPan.getProxStatePacket(true, color, 1000, 128));
		}
	}
	*/

	// TODO ugly hard coded solution
	private int getPatchAddress(int player, int patch) {
		switch (player) {
			case 1: 
				return getPlayer1PatchAddress(patch);
			case 2: 
				return getPlayer2PatchAddress(patch);
			default:
				System.err.println("No player "+player+".");
				return -1;		
		}
	}
	

	// TODO ugly hard coded solution
	private int getPlayer1PatchAddress(int patch) {
		switch (patch) {
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				return 3;
			default:
				System.err.println("No patch "+patch+".");
				return -1;		
		}
	}

	
	// TODO ugly hard coded solution
	private int getPlayer2PatchAddress(int patch) {
		switch (patch) {
		case 1:
			return 9;
		case 2:
			return 10;//A
		case 3:
			return 11;//B
		default:
			System.err.println("No patch "+patch+".");
			return -1;		
		}
	}

	public void setPatchColorForAll(int[] color) {
/*
  		for (int player=1; player <= 2; player++) {
 
			for (int patch=1; patch<=3; patch++) {
				setPatchColor(player, patch, color);
			}
		}
	*/
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

	public void setVibeMapping(int mapping) {
		switch (mapping) {
		case MAPPING_1:
			setVibeForAll(2000, 128);
			break;
		case MAPPING_2:
			setVibeForAll(1000, 128);
			break;
		case MAPPING_3:
			setVibeForAll(800, 128);
			break;
		case MAPPING_4:
			setVibeForAll(600, 128);
			break;
		case MAPPING_5:
			setVibeForAll(400, 128);
			break;
		}
	}

	
	public void setVibeForAll(int period, int duty) {
/*
		for (Player player : game.players) {
			// make all patches vibe
			for (XPan xpan : player.xpans.get(Player.PROX)) {
				if (xpan != null) {
					xpan.broadcastVibe(period, duty);
				}
			}
			// make all gloves vibe
			for (XPan xpan : player.xpans.get(Player.VIBE)) {
				if (xpan != null) {
					xpan.broadcastVibe(period, duty);
				}
			}
		}
		*/
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
