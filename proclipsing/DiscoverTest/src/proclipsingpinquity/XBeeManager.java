package proclipsingpinquity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;

import processing.core.PApplet;
import processing.serial.Serial;

public class XBeeManager {

//	List<String> requiredLocalNIs = new ArrayList(Arrays.asList("P1_PROX1", "P1_PROX2", "P1_VIBE"));
	List<String> requiredLocalNIs = new ArrayList(Arrays.asList("P2_PROX1", "P2_PROX2", "P2_VIBE"));
//	List<String> requiredLocalNIs = new ArrayList(Arrays.asList("P1_PROX1", "P1_PROX2", "P1_VIBE", "P2_PROX1", "P2_PROX2", "P2_VIBE"));
	SortedMap<String, String> niToPortMap;
	
	static private XBeeManager manager = null;
	static public XBeeManager instance() {
		if (XBeeManager.manager == null) XBeeManager.manager = new XBeeManager();
		return XBeeManager.manager;
	}
	
	public XBeeManager() {
		ProclipsingPinquity.game.println("Creating xbeemanager");
	}
	
	public Boolean init() {
		//1. local xbees
		boolean discoverLocalXBees = true;
		if (discoverLocalXBees) {
			discoverLocalXbees();
		}
		else { // take the ones we used before
			loadLocalXbees();
		}
		System.out.println("Local xbees: " + niToPortMap);

		initPlayers();
		return true;
	}

	void discoverLocalXbees() {
		// 1. list potential ports
		ArrayList<String> serialPorts = getLocalXbeeSerialPorts();
		if (serialPorts.size() == 0) {
			System.err.println("No local Xbees found. ");
			System.exit(1);
		} else {
			ProclipsingPinquity.game.println("Xbee serial ports: " + serialPorts);
		}
		// 2. get matching xbee node identifiers
		niToPortMap = getNIToPortMap(serialPorts);
		if (niToPortMap.size()==0) {
			System.err.println("No local xbees found, maybe replug and try again.");
			System.exit(1);
		}
		if (Settings.strictScan && (niToPortMap.size() != Settings.LOCAL_XBEES)) {
			System.err.println("Not all local xbees found! Are they plugged in?");
			System.exit(1);
		}
	}
	
	void loadLocalXbees() {
		niToPortMap = new TreeMap<String,String>();
		niToPortMap.put("P2_VIBE", "/dev/tty.usbserial-A8004Z0D");
		niToPortMap.put("P2_PROX1", "/dev/tty.usbserial-A8004Z0e");
		niToPortMap.put("P2_PROX2", "/dev/tty.usbserial-A8004xAX");
		//niToPortMap = load();
	}
	
	public void save() {
		// TODO Auto-generated method stub
		
	}

	// init players according to serial port map
	// TODO maybe too generic ;-)
	void initPlayers() {
		Player[] players = new Player[Settings.NUM_PLAYERS];
		
		if (Settings.strictScan && Settings.strictDiscovery
				&& this.niToPortMap.size() != Settings.NUM_PLAYERS * Settings.XPANS_PER_PLAYER) {
			System.err
					.println("Number of local Xbees does not fit to number of players and networks (xpans) per player!");
			System.exit(1);
		}
		// make xpans for players from NI / serial port pairs
		int player = 0;
		int xpan = 0;
		for (Map.Entry<String, String> entry : this.niToPortMap.entrySet()) {
			if (entry.getKey().startsWith("P1_")) player = 0;
			else if (entry.getKey().startsWith("P2_")) player = 1;
			else {
				ProclipsingPinquity.game.println("Unknown local XBee: " + entry.getKey());
				break;
			}
			if (players[player] == null) players[player] = new Player();

			if (entry.getKey().endsWith("PROX1")) xpan = Player.PROX_1;
			else if (entry.getKey().endsWith("PROX2")) xpan = Player.PROX_2;
			else if (entry.getKey().endsWith("VIBE")) xpan = Player.VIBE;
			else {
				ProclipsingPinquity.game.println("Unknown local XBee: " + entry.getKey());
				break;
			}
			players[player].xpans[xpan] = new Xpan(entry.getKey());
		}
	}

	// map NI to serial port so that we can assign
	// ports to players!
	private SortedMap<String, String> getNIToPortMap(ArrayList<String> serialPorts) {
		SortedMap<String, String> niToPortMap = new TreeMap<String, String>();
		while (!this.requiredLocalNIs.isEmpty()) {
			if (serialPorts.isEmpty()) {
				ProclipsingPinquity.game.println("No more serial ports - giving up.");
				break;
			}
			for (Iterator<String> i = serialPorts.iterator(); i.hasNext();) {
				String port = i.next();
				ProclipsingPinquity.game.println("Communicating with port " + port);
				XBee xbee = new XBee();
				try {
					xbee.open(port, Settings.SERIAL_BAUDRATE);
				}
				catch (XBeeException e) {
					ProclipsingPinquity.game.println("Could not open serial port " + port + " : " + e);
					continue;
				}
				try {
					// Timeout 70000 if no other devices and not chained.
					AtCommandResponse response = (AtCommandResponse)xbee
						.sendSynchronous(new AtCommand("NI"), 7000);
					if (response.isOk()) {
						int[] bytes = response.getValue();
						StringBuffer buffer = new StringBuffer();
						for (int b : bytes) {
							buffer.append((char) b);
						}
						String NI = buffer.toString();
						niToPortMap.put(NI, port);
						ProclipsingPinquity.game.println(NI + ":" + port);
						i.remove();
						this.requiredLocalNIs.remove(NI);
					}
					else {
						ProclipsingPinquity.game.println("NI command failed: " + response);
					}
					xbee.close();
				} catch (XBeeException e) {
					ProclipsingPinquity.game.println("Could not read NI of XBee on port " + port + " : " + e);
				}
			}
		}
		return niToPortMap;
	}
	
	private ArrayList<String> getLocalXbeeSerialPorts() {

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
			System.err.println("** Error opening serial ports. **");
			System.err.println("Are your local XBees plugged in to your computer?");
			System.exit(1);
		}
		return localXbeeSerialPorts;
	}
	
	public String getPort(String nodeid) {
		return this.niToPortMap.get(nodeid);
	}
	
}
