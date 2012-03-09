import java.util.ArrayList;

import processing.core.PApplet;
import processing.serial.*;
import xbee.*;


public class DiscoverTest extends PApplet {

	final int MODE_CHECK_SERIAL = 0;
	final int MODE_CHECK_XPAN = 1;
	final int MODE_SEND_DISCOVER = 2;
	final int MODE_RECEIVE_DISCOVER = 3;
	final int MODE_CHECKS_DONE = 4;
	final int MODE_RUNNING = 5;
	int mode = MODE_CHECK_SERIAL;

	final int XBEE_DISCOVER_TIMEOUT = 5000; // 5 sec

	Player[] players;

	ArrayList<String> foundProxs;
	ArrayList<String> foundVibes;
	ArrayList<String> foundUndefs;

	public static DiscoverTest game;
	public GameState gamestate;
	
	public void setup() {
		game = this;
		gamestate = new GameState(this);

		// 1. scan test for local xbees 
		XBeeManager.instance().init();

		// 2. discover test for remote xbees
		foundProxs = new ArrayList<String>();
		foundVibes = new ArrayList<String>();
		foundUndefs = new ArrayList<String>();
	}


	public void draw() {
		int startDiscover = 0; // time when discover package is sent

		// scan test for local xbees via serial
		if (mode == MODE_CHECK_SERIAL && XBeeManager.instance().hasAllNIs()) {
			//println("Local XBees found: " + xbeeManager.getNodeIDs() + ".");
			mode++;
		}
		// set up networks for all local xbees
		else if (mode == MODE_CHECK_XPAN) {
			initPlayers();
			mode++;
		}
		// discover remote xbees
		else if (mode == MODE_SEND_DISCOVER) {
			println("Scanning for remote xbees...");
			println("Player 1:");
			players[0].discoverRemoteXbees();
			println("Player 2:");
			players[1].discoverRemoteXbees();
			startDiscover = millis();
			mode++;
		}
		else if (mode == MODE_RECEIVE_DISCOVER) {
			if (millis()-startDiscover <= XBEE_DISCOVER_TIMEOUT) {
				print(".");
				delay(1000); // 1 sec
			}
			else {
				mode++;
				// TODO: record - which xbees have we??
			}
		}
		else if (mode == MODE_CHECKS_DONE) {
			println("");
			println("Checks done");
			printDiscovered();
			mode++;
		}
		else if (mode == MODE_RUNNING) {
			render(gamestate);
		}
	}

	private void render(GameState gamestate) {
		
		
		
		
	}


	void printDiscovered() {
		println("Discovered proximity patches");
		for(int i=0; i<foundProxs.size() ; i++)
			println(foundProxs.get(i));
		println("Discovered vibration gloves");
		for(int i=0; i<foundVibes.size() ; i++)
			println(foundVibes.get(i));
		println("Discovered undefined remote xbee senders");
		for(int i=0; i<foundUndefs.size() ; i++)
			println(foundUndefs.get(i));
	}

	void initPlayers() {
		players = new Player[2];
		players[0] = new Player();
		players[1] = new Player();

		// TODO: Node IDs are hard coded, we want this from the scan!!
		// node identifyers of local xbees for player 1 
		ArrayList<String[]> NIS_PLAYER1 = new ArrayList<String[]>();
		String[] proxNIs = {"P1_PROX1","P1_PROX2"};
		String[] accelNI = {"P1_ACCEL"};
		String[] vibeNI = {"P1_VIBE"};
		NIS_PLAYER1.add(proxNIs);
		NIS_PLAYER1.add(accelNI);
		NIS_PLAYER1.add(vibeNI);
		// player 2
		ArrayList<String[]> NIS_PLAYER2 = new ArrayList<String[]>();
		String[] proxNIs2 = {"P2_PROX1","P2_PROX2"};
		String[] accelNI2 = {"P2_ACCEL"};
		String[] vibeNI2 = {"P2_VIBE"};
		NIS_PLAYER2.add(proxNIs2);
		NIS_PLAYER2.add(accelNI2);
		NIS_PLAYER2.add(vibeNI2);

		players[0].init(NIS_PLAYER1);
		players[1].init(NIS_PLAYER2);
	}

	void xBeeDiscoverEvent(XBeeReader xbee) {
		XBeeDataFrame data = xbee.getXBeeReading();
		println("Received ApiId " + data.getApiID());
		if (data.getApiID() != XBeeReader.ATCOMMAND_RESPONSE) return;
		data.parseXBeeRX16Frame();

		int[] buffer = data.getBytes();

		if (buffer.length > 11) {
			//check first letter of NI parameter
			String name = "";
			for(int i = 11; i < buffer.length; i++)
				name += (char)buffer[i];

			switch (buffer[11]) {
			case 'P':
				foundProxs.add(name);
				println(" Found proximity patch: " + name + " at "+millis());
				break;
			case 'V':
				foundVibes.add(name);
				println(" Found vibration patch: " + name + " at "+millis());
				break;
			default:
				foundUndefs.add(name);
				println(" Found undefined patch: " + name + " at "+millis());
				break;
			}
		}
	}

	public void xBeeEvent(XBeeReader xbee) {
		if (mode == MODE_CHECK_SERIAL) {
			XBeeManager.instance().xBeeEvent(xbee);
		}
		else if (mode == MODE_RECEIVE_DISCOVER) {
			xBeeDiscoverEvent(xbee);
		}
		else if (mode == MODE_RUNNING) {
			int[] packet = XPan.decodePacket(xbee);
			if (packet == null) return;  
			switch (packet[0]) {
			case XPan.PROX_IN_PACKET_TYPE:
				assert(packet.length == XPan.PROX_IN_PACKET_LENGTH);

				int patch = (packet[1] >> 1);                
				int player = getPlayerIndexForPatch(patch);

				if (player != -1) {
					int proximity = ((packet[4] & 0xFF) << 8) | (packet[5] & 0xFF);;
					println(proximity);
				}
				else {
					System.err.println("Trouble in paradise, we received a packet from patch '"+ patch + "', which is not assigned to a player");
				}
				break;
			}
		}
	}

	public int getPlayerIndexForPatch(int patch) {
		if (patch >= 1 && patch <= 4) return 0;
		else if (patch >= 9 && patch <= 16) return 1;
		else return -1;
	}

	public void keyPressed() {
		if (mode == MODE_RUNNING) {
		  switch (key) {
		  	case '1' : // set vibe mapping
		  		System.out.println("Set vibe mapping 1.");
		  		gamestate.setVibeMapping(GameState.MAPPING_1);
		  		break;
		  	case '2' :
		  		System.out.println("Set vibe mapping 2.");
		  		gamestate.setVibeMapping(GameState.MAPPING_2);
		  		break;
		  	case '3' :
		  		System.out.println("Set vibe mapping 3.");
		  		gamestate.setVibeMapping(GameState.MAPPING_3);
		  		break;
		  	case '4' :
		  		System.out.println("Set vibe mapping 4.");
		  		gamestate.setVibeMapping(GameState.MAPPING_4);
		  		break;
		  	case '5' :
		  		System.out.println("Set vibe mapping 5.");
		  		gamestate.setVibeMapping(GameState.MAPPING_5);
		  		break;
		  	case 'r' : // set color
		  		System.out.println("Set everything red.");
		  		int[] red = {255,0,0};
		  		gamestate.setPatchColorForAll(red);
		  		break;
		  	case 'g' : // set color
		  		System.out.println("Set everything green.");
		  		int[] green = {0,255,0};
		  		gamestate.setPatchColorForAll(green);
		  		break;
		  	case 'b' : // set color
		  		System.out.println("Set everything blue.");
		  		int[] blue = {0,0,255};
		  		gamestate.setPatchColorForAll(blue);
		  		break;
		  	case 'a' :
		  		// toggle patch 1 player 1
		  		System.out.println("Toggle patch 1 player 1.");
		  		gamestate.activatePatch(1, 1, !gamestate.isPatchActive(1, 1));
		  		break;
		  	case 's' :
		  		// toggle patch 2 player 1
		  		System.out.println("Toggle patch 2 player 1.");
		  		gamestate.activatePatch(1, 2, !gamestate.isPatchActive(1, 2));
		  		break;
		  	case 'd' :
		  		// toggle patch 3 player 1
		  		System.out.println("Toggle patch 3 player 1.");
		  		gamestate.activatePatch(1, 3, !gamestate.isPatchActive(1, 3));
		  		break;
		  	case 'j' :
		  		// toggle patch 1 player 2
		  		System.out.println("Toggle patch 1 player 2.");
		  		gamestate.activatePatch(2, 1, !gamestate.isPatchActive(2, 1));
		  		break;
		  	case 'k' :
		  		// toggle patch 2 player 2
		  		System.out.println("Toggle patch 2 player 2.");
		  		gamestate.activatePatch(2, 2, !gamestate.isPatchActive(2, 2));
		  		break;
		  	case 'l' :
		  		// toggle patch 3 player 2
		  		System.out.println("Toggle patch 3 player 2.");
		  		gamestate.activatePatch(2, 3, !gamestate.isPatchActive(2, 3));
		  		break;
		    case 'q':
		    	// stop vibe and stop blink
		    	gamestate.setVibeForAll(0,0);
		  		delay(1000); // 1 sec so the packets can be transmitted for sure
		    	int[] noColor = {0,0,0};
		  		gamestate.setPatchColorForAll(noColor);
		  		delay(1000); // 1 sec so the packets can be transmitted for sure
		    	System.exit(0);
		    	break;      
		  }
		}
	}
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { DiscoverTest.class.getName() });
	}
}

