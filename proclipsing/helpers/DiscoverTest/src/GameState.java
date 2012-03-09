
public class GameState {

	// parent class so we have all infos such as xpans etc.
	// in xpan this is not passed, but accessed as a global variable within DiscoverTest 
	DiscoverTest game;
	
	public GameState(DiscoverTest game) {
		this.game = game;
	}

	public int getProxReading(int player, int patch) {
			return 0;
	}
	
	public Boolean isPatchActive(int player, int patch) {
		return true;
	}
	
	public void activatePatch(int player, int patch, Boolean active) {
		
	}

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
	
	public void setPatchColorForAll(int[] color) {
		for (int player=1; player <= 2; player++) {
			for (int patch=1; patch<=3; patch++) {
				setPatchColor(player, patch, color);
			}
		}
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
		
	}
	
	
}
