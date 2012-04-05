package propinquity;

import java.io.File;
import java.util.*;

import processing.core.*;
import processing.serial.*;
import controlP5.*;

import xbee.*;

public class XBeeManager implements Runnable, UIElement {

	final String XBEE_PORTS_FILE = "xbees.lst";
	final int XBEE_BAUDRATE = 115200;
	final int XBEE_RESPONSE_TIMEOUT = 5000;

	Propinquity parent;

	boolean isVisible;

	ControlP5 controlP5;
	Button plNextButton;
	Button plScanButton;

	Thread scanningThread;

	boolean xbeeDebug;

	String nodeID;
	HashMap<String, Serial> xbeePorts;
	HashMap<String, XBeeReader> xbeeReaders;

	/**
	 * Create a new XBeeManager with xbeeDebug turned off.
	 *
	 * @param parent the parent Propinquity object.
	 */
	public XBeeManager(Propinquity parent) {
		this(parent, false);
	}

	/**
	 * Create a new XBeeManager.
	 *
	 * @param parent the parent Propinquity object.
	 * @param xbeeDebug the XBEE xbeeDebug mode.
	 */
	public XBeeManager(Propinquity parent, boolean xbeeDebug) {
		this.parent = parent;

		isVisible = true;

		controlP5 = new ControlP5(parent);

		//Button to scan for Xbees
		plScanButton = controlP5.addButton("XbeeManager Scan", 0, parent.width / 2 + 60, parent.height / 2 + 50, 50, 20);
		plScanButton.setCaptionLabel("SCAN");

		//Next button
		plNextButton = controlP5.addButton("XbeeManager Next", 0, parent.width / 2 + 60 + 50 + 10, parent.height / 2 + 50, 50, 20);
		plNextButton.setCaptionLabel("NEXT");

		this.xbeeDebug = xbeeDebug;
		xbeePorts = new HashMap<String, Serial>();
		xbeeReaders = new HashMap<String, XBeeReader>();

		scan();
	}

	public void scan() {
		if (scanningThread != null && scanningThread.isAlive()) return;
		else {
			scanningThread = new Thread(this);
			scanningThread.start();
		}
	}

	public boolean isScanning() {
		if(scanningThread != null && scanningThread.isAlive()) return true;
		else return false;
	}

	public void reset() {
		for(XBeeReader reader : xbeeReaders.values()) {
			reader.stopXBee();
			while (reader.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
				System.out.println("   .");
			}
		}

		for(Serial port : xbeePorts.values()) {
			port.stop();
		}

		xbeeReaders.clear();
		xbeePorts.clear();
	}

	public void run() {
		reset();

		System.out.println("Starting XBee Scan");

		String[] availablePorts = Serial.list();
		String osName = System.getProperty("os.name");

		for (int portNum = 0; portNum < availablePorts.length; portNum++) {
			if ((osName.indexOf("Mac") != -1) && (availablePorts[portNum].indexOf("tty.usbserial") == -1)) {
				System.out.println("\tSkipping port: " + availablePorts[portNum]);
				continue;
			}

			PApplet.print("\tConnecting to port: " + availablePorts[portNum] + " ... ");
			Serial xbeePort = new Serial(parent, availablePorts[portNum], XBEE_BAUDRATE);
			XBeeReader xbeeReader = new XBeeReader(parent, xbeePort);
			xbeeReader.DEBUG = xbeeDebug;

			System.out.println("\t\tStarting XBee");
			xbeeReader.startXBee();

			//Take a break to give some time to start
			try {
				Thread.sleep(250);
			} catch (InterruptedException ie) {

			}

			System.out.println(" \t\tGetting NI");
			xbeeReader.getNI();

			synchronized(nodeID) {
				nodeID = null;

				try {
					nodeID.wait(XBEE_RESPONSE_TIMEOUT);
				} catch(InterruptedException ie) {

				}
			}

			if(nodeID != null) {
				System.out.println("\t\tFound XBee: "+nodeID);
				xbeePorts.put(nodeID, xbeePort);
				xbeeReaders.put(nodeID, xbeeReader);
			} else {
				System.out.println("\t\tDevice is not an XBee");
			}
		}

		System.out.println("Scan Complete");
	}

	// Get a XBeeReader of the XBee with the matching NodeIdentifier (NI)
	public XBeeReader reader(String ni) {
		return xbeeReaders.get(ni);
	}

	public void xBeeEvent(XBeeReader reader) {
		XBeeDataFrame data = reader.getXBeeReading();
		data.parseXBeeRX16Frame();

		int[] buffer = data.getBytes();
		nodeID = "";
		for (int i = 0; i < buffer.length; i++) {
			nodeID += (char) buffer[i];
		}

		nodeID.notify(); //TODO Test
	}

	public void dispose() {
		reset();
		if (controlP5 != null) {
			controlP5.dispose();
			controlP5 = null;
		}
	}

	public void show() {
		isVisible = true;
		controlP5.show();
	}

	public void hide() {
		isVisible = false;
		controlP5.hide();
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void controlEvent(ControlEvent event) {
		if(isVisible) {
			if(event.controller().name().equals("XbeeManager Scan")) scan();
			else if(event.controller().name().equals("XbeeManager Next")) process();
		}
	}

	void process() {
		if (isScanning()) return;
		parent.changeGameState(GameState.PlayerList);
	}

	public void keyPressed(int keycode) {
		if(isVisible && keycode == parent.ENTER) process();
	}

	public void draw() {
		if (isVisible) {
			
			String msg = "";
			if (isScanning()) msg = "Scanning...";
			else {
				for(String s : xbeeReaders.keySet()) msg += s;
				if(msg.isEmpty()) msg = "No XBees found";
			}

			parent.pushMatrix();
			parent.translate(parent.width / 2, parent.height / 2);
			parent.textFont(Graphics.font, Hud.FONT_SIZE);
			parent.textAlign(PConstants.CENTER, PConstants.CENTER);
			parent.fill(255);
			parent.noStroke();
			parent.text("Detecting XBee modules... ", 0, 0);
			parent.translate(0, 30);
			parent.textFont(Graphics.font, Hud.FONT_SIZE * 0.65f);
			parent.text(msg, 0, 0);
			parent.popMatrix();
		}
	}

}
