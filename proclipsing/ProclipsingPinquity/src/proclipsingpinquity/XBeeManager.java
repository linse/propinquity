package proclipsingpinquity;

import java.util.ArrayList;
import java.util.HashMap;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;

import processing.core.PApplet;
import processing.serial.Serial;

public class XBeeManager extends PApplet {

	HashMap<String, String> niToPortMap;
	
	public XBeeManager(ProclipsingPinquity game) {
		println("Creating xbeemanager");
		
		//1. local xbees
		boolean discoverLocalXBees = true;
		if (discoverLocalXBees) {
			discoverLocalXbees();
			game.initPlayers(niToPortMap);
		}
		else { // take the ones we used before
			loadLocalXbees();
		}
		System.out.println("Local xbees: " + niToPortMap);
		
		//2. remote xbees
		boolean discoverRemoteXBees = true;
		if (discoverRemoteXBees) {
			for (Player player: game.getPlayers()) {
				for (int i = 0; i<Settings.XPANS_PER_PLAYER; i++) {
					if (player!=null && player.xpans[i]!=null) {
						player.xpans[i].printRemoteNodes();
					}
				}
			}
			// TODO run this when all threads are returned!!
			for (Player player : game.getPlayers()) {
				// TODO not used yet
				if (player != null) {
					player.setRemoteProximityStepLength(20);
				}
			}	
		}
		else {
			
		}

	}

	void discoverLocalXbees() {
		// 1. list potential ports
		ArrayList<String> serialPorts = getLocalXbeeSerialPorts();
		if (serialPorts.size() == 0) {
			System.err.println("No local Xbees found. ");
			System.exit(1);
		} else {
			println("Xbee serial ports: " + serialPorts);
		}
		// 2. get matching xbee node identifiers
		niToPortMap = getNIToPortMap(serialPorts);
		if (niToPortMap.size()==0) {
			System.err.println("No local xbees found, maybe replug and try again.");
		}
		if (Settings.strictScan && (niToPortMap.size() != Settings.LOCAL_XBEES)) {
			System.err.println("Not all local xbees found! Are they plugged in?");
			System.exit(1);
		}
	}
	
	void loadLocalXbees() {
		niToPortMap = new HashMap<String,String>();
		niToPortMap.put("P2_VIBE", "/dev/tty.usbserial-A8004Z0D");
		niToPortMap.put("P2_PROX1", "/dev/tty.usbserial-A8004Z0e");
		niToPortMap.put("P2_PROX2", "/dev/tty.usbserial-A8004xAX");
		//niToPortMap = load();
	}
	
	public void save() {
		// TODO Auto-generated method stub
		
	}

	// not here??
	private void initPlayers(HashMap<String, String> niToPortMap) {
		// TODO Auto-generated method stub
		
	}

	// map NI to serial port so that we can assign
	// ports to players!
	private HashMap<String, String> getNIToPortMap(ArrayList<String> serialPorts) {
		HashMap<String, String> niToPortMap = new HashMap<String, String>();
		try {
			for (int i = 0; i < serialPorts.size(); i++) {
				String port = serialPorts.get(i).toString();
				XBee xbee = new XBee();
				xbee.open(port, Settings.SERIAL_BAUDRATE);
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
	
}
