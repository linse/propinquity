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
	int mode = MODE_CHECK_SERIAL;

	final int XBEE_DISCOVER_TIMEOUT = 5 * 1000; // 5 sec

	Player[] players;

	ArrayList foundProxs;
	ArrayList foundVibes;
	ArrayList foundAccels;
	ArrayList foundUndefs;

	public static DiscoverTest game;
	
	public void setup() {
		game = this;

		// 1. scan test for local xbees 
		XBeeManager.instance().init();

		// 2. discover test for remote xbees
		foundProxs = new ArrayList();
		foundVibes = new ArrayList();
		foundAccels = new ArrayList();
		foundUndefs = new ArrayList();
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
			
			for (int i=0;i<2;i++) {
				for (XPan xpan : players[i].xpans.get(0)) {
					if (xpan != null) {
						xpan.broadcastProxConfig(10000);
					}
				}
			}
			
			
			exit();
		}
	}

	void printDiscovered() {
		println("Discovered proximity patches");
		for(int i=0; i<foundProxs.size() ; i++)
			println(foundProxs.get(i));
		println("Discovered vibration gloves");
		for(int i=0; i<foundVibes.size() ; i++)
			println(foundVibes.get(i));
		println("Discovered accelerometer anklets");
		for(int i=0; i<foundAccels.size() ; i++)
			println(foundAccels.get(i));
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
			case 'A':
				foundAccels.add(name);
				println(" Found acceleration patch: " + name + " at "+millis());
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
	}

	public static void main(String _args[]) {
		PApplet.main(new String[] { DiscoverTest.class.getName() });
	}
}

