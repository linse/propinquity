package propinquity.xbee;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import processing.core.PApplet;
import processing.serial.Serial;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;

import xbee.*;

public class XBeeManager implements Runnable {
	PApplet parent;
	HashMap<String, String> ports;
	boolean debug;
	Thread thread;
	boolean done;

	boolean initialized;
	// String[] initPorts;
	// int initPortIndex;
	// long initLastCheck;
	boolean initFound;
	String initNodeId;

	final String XBEE_PORTS_FILE = "xbees.lst";
	final int XBEE_BAUDRATE = 115200;
	final int XBEE_RESPONSE_TIMEOUT = 5000;

	final int XBEE_WIDTH = 200;
	final int XBEE_NEXT_WIDTH = 50;
	final int XBEE_NEXT_HEIGHT = 20;
	final int XBEE_SCAN_WIDTH = 50;
	final int XBEE_SCAN_HEIGHT = 20;
	final int XBEE_NEXT_ID = 1;
	final int XBEE_SCAN_ID = 2;

	ControlP5 controlP5;
	Button plNextButton;
	Button plScanButton;

	public XBeeManager(PApplet p) {
		parent = p;
		controlP5 = new ControlP5(p);
		done = false;
		ports = new HashMap<String, String>();
		initialized = false;
		debug = false;
	}

	public void init() {
		// create button to add new players
		plScanButton = controlP5.addButton("SCAN", 0, parent.width / 2 + 60,
				parent.height / 2 + 50, XBEE_SCAN_WIDTH, XBEE_SCAN_HEIGHT);
		plScanButton.setId(XBEE_SCAN_ID);

		// create next button
		plNextButton = controlP5.addButton("NEXT", 0, parent.width / 2 + 60
				+ XBEE_SCAN_WIDTH + 10, parent.height / 2 + 50,
				XBEE_NEXT_WIDTH, XBEE_NEXT_HEIGHT);
		plNextButton.setId(XBEE_NEXT_ID);

		// load from file if it exists
		if (new File(parent.dataPath(XBEE_PORTS_FILE)).exists()) {
			load();
		}
		// autodetect
		else {
			scan();
		}
	}

	public void scan() {
		if (thread != null)
			return;

		thread = new Thread(this);
		thread.start();
	}

	public boolean isScanning() {
		return thread != null;
	}

	public void run() {
		ports = new HashMap<String, String>();
		initialized = false;

		String[] initPorts = Serial.list();
		long initLastCheck;
		PApplet.println("Initializing XBees...");

		String osName = System.getProperty("os.name");

		for (int initPortIndex = 0; initPortIndex < initPorts.length; initPortIndex++) {

			// if we are on a Mac, then filter out the ports that don't start by
			// tty.us
			if ((osName.indexOf("Mac") != -1)
					&& (initPorts[initPortIndex].indexOf("tty.usbserial") == -1)) {
				PApplet.println(" Skipping port: " + initPorts[initPortIndex]);
				continue;
			}

			PApplet.print(" Connecting to port: " + initPorts[initPortIndex]
					+ " ... ");
			XBeeReader xbee = new XBeeReader(parent, new Serial(parent,
					initPorts[initPortIndex], XBEE_BAUDRATE));
			PApplet.println();
			PApplet.println("   -starting xbee");
			xbee.startXBee();

			// take a break to give some time to start
			try {
				Thread.sleep(250);
			} catch (InterruptedException ie) {
			}

			PApplet.println("   -get NI");
			xbee.getNI();

			initLastCheck = parent.millis();
			initFound = false;

			while (parent.millis() - initLastCheck < XBEE_RESPONSE_TIMEOUT
					&& !initFound) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
				}
			}

			if (initFound) {
				PApplet.println(initNodeId);
				ports.put(initNodeId, initPorts[initPortIndex]);
			} else {
				PApplet.println("no XBee found");
			}

			// clean up
			PApplet.println("   -stopping xbee");
			xbee.stopXBee();
			PApplet.println("   -waiting");
			while (xbee.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
				PApplet.println("   .");
			}
			PApplet.println("   -done");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
			}
		}

		// done
		initialized = true;

		// clear thread
		thread = null;
		PApplet.println("Scan done");
	}

	public boolean isInitialized() {
		return initialized;
	}

	// Get a XBeeReader of the XBee with the matching NodeIdentifier (NI)
	public XBeeReader reader(String ni) {
		String port = ports.get(ni);
		if (port == null)
			return null;
		XBeeReader xbee = new XBeeReader(parent, new Serial(parent, port,
				XBEE_BAUDRATE));
		xbee.DEBUG = debug;
		return xbee;
	}

	public void xBeeEvent(XBeeReader xbee) {
		// PApplet.println("xbee event: xbee manager");
		XBeeDataFrame data = xbee.getXBeeReading();
		data.parseXBeeRX16Frame();

		int[] buffer = data.getBytes();
		initNodeId = "";
		for (int i = 0; i < buffer.length; i++) {
			initNodeId += (char) buffer[i];
		}

		initFound = true;
	}

	public String foundPortIds() {
		Set<String> nodes = ports.keySet();
		Iterator<String> it = nodes.iterator();

		String nodesString = "";
		while (it.hasNext())
			nodesString += it.next() + ", ";

		if (nodesString.length() < 2)
			return "";
		return nodesString.substring(0, nodesString.length() - 2);
	}

	public void save() {
		String[] xbeeList = new String[ports.size()];
		int i = 0;
		Iterator<String> it = ports.keySet().iterator();
		while (it.hasNext()) {
			String nodeId = (String) it.next();
			xbeeList[i++] = nodeId + "=" + (String) ports.get(nodeId);
		}
		parent.saveStrings(parent.dataPath(XBEE_PORTS_FILE), xbeeList);
	}

	public void load() {
		PApplet.println("Loading XBee configuration");
		String[] xbeeList = parent.loadStrings(XBEE_PORTS_FILE);
		for (int i = 0; i < xbeeList.length; i++) {
			int equalIndex = xbeeList[i].indexOf('=');
			if (equalIndex != -1) {
				String nodeId = xbeeList[i].substring(0, equalIndex);
				String port = xbeeList[i].substring(equalIndex + 1);
				PApplet.println(" Using port: " + port + " ... " + nodeId);
				ports.put(nodeId, port);
			}
		}

		initialized = true;
	}

	public boolean isDone() {
		return done;
	}

	public void dispose() {
		if (controlP5 != null) {
			controlP5.dispose();
			controlP5 = null;
		}
	}

	public void controlEvent(ControlEvent theEvent) {
		switch (theEvent.controller().id()) {
		case (XBEE_NEXT_ID):
			if (!initialized)
				return;
			save();
			done = true;
			break;
		case (XBEE_SCAN_ID):
			scan();
			break;
		}
	}
}
