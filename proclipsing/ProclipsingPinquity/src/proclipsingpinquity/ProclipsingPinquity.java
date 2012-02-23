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

	static final String version = "0.9";
	static final int NUM_PLAYERS = 2;
	static final int XPANS_PER_PLAYER = 3;
	static final int LOCAL_XBEES = 6;
	static final int SERIAL_BAUDRATE = 115200;
	PFont font;
	Player[] players;

	public void setup() {
		size(1024, 768, OPENGL);
		smooth(); // anti-aliasing for graphic display
		font = loadFont("SansSerif-10.vlw");
		textFont(font); // use the font for text
		players = new Player[NUM_PLAYERS];

		// required by the xbee api library, needs to be in your data folder.
		PropertyConfigurator.configure(dataPath("") + "log4j.properties");

		ArrayList<String> serialPorts = getLocalXbeeSerialPorts();
		if (serialPorts.size() == 0) {
			println("No local Xbees found. ");
		} else {
			println("Xbee serial ports:");
			println(serialPorts);

			HashMap<String, String> niToPortMap = getNIToPortMap(serialPorts);
			if (niToPortMap.size() != LOCAL_XBEES) {
				println("Not all local xbees found! Are they plugged in?");
				return;
			} else {
				println("Local xbees: " + niToPortMap);
				initPlayers(niToPortMap);
			}
		}
	}

	ArrayList<String> getLocalXbeeSerialPorts() {

		String[] allSerialPorts = Serial.list();
		String osName = System.getProperty("os.name");
		ArrayList<String> localXbeeSerialPorts = new ArrayList<String>();

		for (int i = 0; i < allSerialPorts.length; i++) {
			// on mac it _has_ to be an usbserial device
			if (!((osName.indexOf("Mac") != -1) && (allSerialPorts[i]
					.indexOf("tty.usbserial") == -1))) {
				localXbeeSerialPorts.add(allSerialPorts[i]);
			}
		}
		if (localXbeeSerialPorts.size() == 0) { // TODO throw exception?
			println("** Error opening serial ports. **");
			println("Are your local XBees plugged in to your computer?");
			exit();
		}
		return localXbeeSerialPorts;
	}

	// map NI to serial port so that we can assign
	// ports to players!
	HashMap<String, String> getNIToPortMap(ArrayList<String> serialPorts) {
		HashMap<String, String> niToPortMap = new HashMap<String, String>();
		try {
			for (int i = 0; i < serialPorts.size(); i++) {
				String port = serialPorts.get(i).toString();
				XBee xbee = new XBee();
				xbee.open(port, SERIAL_BAUDRATE);
				// Timeout 70000 if no other devices and not chained.
				AtCommandResponse response = (AtCommandResponse) xbee
						.sendSynchronous(new AtCommand("NI"), 70000);
				if (response.isOk()) {
					int[] bytes = response.getValue();
					StringBuffer buffer = new StringBuffer();
					for (int b : bytes) {
						buffer.append((char) b);
					}
					String NI = buffer.toString();
					niToPortMap.put(NI, port);
					println(NI + ":" + port);
				}
				xbee.close();
			}
		} catch (XBeeException e) {
			// println("Could not open serial port: "+e);
		}
		return niToPortMap;
	}

	// init players according to serial port map
	// TODO maybe too generic ;-)
	void initPlayers(HashMap niToPortMap) {
		Object[] keys = niToPortMap.keySet().toArray();
		Arrays.sort(keys);
		if (keys.length != NUM_PLAYERS * XPANS_PER_PLAYER) {
			println("Number of local Xbees does not fit to number of players and networks (xpans) per player!");
			exit();
		}
		// make xpans for players from NI / serial port pairs
		int player = 0;
		int xpan = 0;
		players[player] = new Player();
		for (int i = 0; i < keys.length && xpan < XPANS_PER_PLAYER
				&& player < NUM_PLAYERS; i++) {
			players[player].xpans[xpan++] = new Xpan(
					(String) niToPortMap.get(keys[i]));
			// if player has all xpans and is not last player
			if (xpan == XPANS_PER_PLAYER && player != NUM_PLAYERS - 1) {
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
		ProximityData data = new ProximityData(); // create a data object
		data = getProximityData(); // put data into the data object
		if (millis() > 10000) { // just some startup delay for now
			// just buzz the gloves for now
			players[1].broadcastVibe();
			players[0].broadcastVibe();
		}
	}

	// queries the XBee for incoming proximity data frames
	// and parses them into a data object
	ProximityData getProximityData() {

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

	public static void main(String _args[]) {
		PApplet.main(new String[] { proclipsingpinquity.ProclipsingPinquity.class
				.getName() });
	}
}
