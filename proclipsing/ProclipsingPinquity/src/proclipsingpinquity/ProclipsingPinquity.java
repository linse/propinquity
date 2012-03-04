package proclipsingpinquity;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;

import processing.core.PApplet;
import processing.core.PFont;
import processing.serial.Serial;

public class ProclipsingPinquity extends PApplet {
	
	//game states
	public enum GameState { XBEE_INIT, PLAYER_LIST, LEVEL_SELECT, PLAY, HIGHSCORE }
	
	private GameState gameState;
	private PFont font;
	private Player[] players;
	private XBeeManager xbeeManager;
	//private PlayerList playerList;
	//private HighScore highScore;

	public void setup() {
		// setup general stuff
		size(1024, 768, OPENGL);
		smooth(); // anti-aliasing for graphic display
		font = loadFont("SansSerif-10.vlw");
		textFont(font); // use the font for text
		// required by the xbee api library, needs to be in your data folder.
		PropertyConfigurator.configure(dataPath("") + "log4j.properties");
		
		// setup game
		players = new Player[Settings.NUM_PLAYERS];
		xbeeManager = new XBeeManager(this);
		System.exit(0);
	}





	// init players according to serial port map
	// TODO maybe too generic ;-)
	void initPlayers(HashMap niToPortMap) {
		Object[] keys = niToPortMap.keySet().toArray();
		Arrays.sort(keys);
		if (Settings.strictScan && Settings.strictDiscovery
				&& keys.length != Settings.NUM_PLAYERS * Settings.XPANS_PER_PLAYER) {
			System.err
					.println("Number of local Xbees does not fit to number of players and networks (xpans) per player!");
			System.exit(1);
		}
		// make xpans for players from NI / serial port pairs
		int player = 0;
		int xpan = 0;
		players[player] = new Player();
		for (int i = 0; i < keys.length && xpan < Settings.XPANS_PER_PLAYER
				&& player < Settings.NUM_PLAYERS; i++) {
			players[player].xpans[xpan++] = new Xpan(
					(String) niToPortMap.get(keys[i]));
			// if player has all xpans and is not last player
			if (xpan == Settings.XPANS_PER_PLAYER && player != Settings.NUM_PLAYERS - 1) {
				player++;
				players[player] = new Player();
				xpan = 0;
			}
		}

	}

	// defines the data object
	class ProximityData {
		int value;
		String address;
	}

	public void draw() {
//		println("draw");
//		ProximityData data = new ProximityData(); // create a data object
//		data = getProximityData(); // put data into the data object
////		if (millis() > 10000) { // just some startup delay for now
////			// just buzz the gloves for now
////			for (Player player : players) {
////				if (player != null) {
////					player.broadcastVibe(200);
////				}
////			}
////		}
	}

	// queries the XBee for incoming proximity data frames
	// and parses them into a data object
	ProximityData getProximityData() {

		for (Player player : players) {
			if (player != null) {
				println("start receive from player ");
				player.receiveProxReadings();
			}
		}
		ProximityData data = new ProximityData();

		// try { // read proximity data package here
		// }
		// catch (XBeeException e) {
		// println("Error receiving response: " + e);
		// }
		// finally {
		// }
		return data; // sends the data back to the calling function
	}

	// key pressed - do something
	public void keyPressed(){
		int keyNum = Character.getNumericValue(key);
		println(key);

//		  if (gameState == STATE_XBEE_INIT) {
//		    switch(key) {
//		      case ENTER:
//		        xbeeManager.save();
//		        //xbeeManager.dispose();
//		        //initPlayerListCtrl();
//		        gameState++;
//		        println("gamestate = " + gameState);
//		        break;
//		    }
//		  }
//		  else if (gameState == STATE_PLAYER_LIST) {
//		    switch(key) {
//		      case ENTER:
//		        playerList.process();
//		        if (playerList.isDone()) {
//		          gameState++;
//		          println("gamestate = " + gameState);
//		        }
//		        break;
//		    }
//		  }	
	} 
	
	public Player[] getPlayers() {
		return players;
	}
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { proclipsingpinquity.ProclipsingPinquity.class
				.getName() });
	}
}
