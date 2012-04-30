package propinquity.xbee;

import java.util.*;

import processing.core.*;
import processing.serial.*;
import controlP5.*;

import xbee.*;

import propinquity.*;

/**
 * This class scans for XBees connected to the computer. It then instantiates and holds XBeeReader objects for each such device.
 *
*/
public class XBeeManager implements Runnable, UIElement {

	final int XBEE_BAUDRATE = 115200;
	final int XBEE_RESPONSE_TIMEOUT = 1000;

	PApplet parent;
	Propinquity propinquity;

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
	 * @param parent the parent PApplet object.
	 */
	public XBeeManager(PApplet parent) {
		this(parent, null, false);
	}

	/**
	 * Create a new XBeeManager.
	 *
	 * @param parent the parent PApplet object.
	 * @param xbeeDebug the Xbee xbeeDebug mode.
	 */
	public XBeeManager(PApplet parent, Propinquity propinquity, boolean xbeeDebug) {
		this.parent = parent;
		this.propinquity = propinquity;

		isVisible = true;

		controlP5 = new ControlP5(parent);

		//Button to scan for Xbees
		plScanButton = controlP5.addButton("XbeeManager Scan", 0, parent.width / 2 + 60, parent.height / 2 + 50, 50, 20);
		plScanButton.setCaptionLabel("SCAN");

		//Next button
		plNextButton = controlP5.addButton("XbeeManager Next", 0, parent.width / 2 + 60 + 50 + 10, parent.height / 2 + 50, 50, 20);
		plNextButton.setCaptionLabel("NEXT");

		hide();

		this.xbeeDebug = xbeeDebug;
		xbeePorts = new HashMap<String, Serial>();
		xbeeReaders = new HashMap<String, XBeeReader>();

		scan();
	}

	/**
	 * Get the XBeeReader for the XBee with the matching NodeIdentifier (NI).
	 * 
	 * @param ni the NodeIdentifier of the requested Xbee.
	 * @return the XBeeReader for the Xbee with the matching NodeIdentifier.
	*/
	public XBeeReader reader(String ni) {
		return xbeeReaders.get(ni);
	}

	/**
	 * Get a list of NodeIdentifier (NI) for all available Xbees.
	 *
	 * @return an array of the valid NodeIdentifier for available Xbees.
	*/
	public String[] listXbees() {
		return xbeeReaders.keySet().toArray(new String[0]);
	}

	/**
	 * Checks if the XbeeManager object is currently scanning for Xbees
	 *
	 * @return true if the XbeeManager is currently scanning. False otherwise.
	*/
	public boolean isScanning() {
		if(scanningThread != null && scanningThread.isAlive()) return true;
		else return false;
	}

	/**
	 * Triggers a new scan cycle, unless one is already running. The scan cycle will search all serial ports for available Xbees
	 *
	*/
	public void scan() {
		if(scanningThread != null && scanningThread.isAlive()) return;
		else {
			scanningThread = new Thread(this);
			scanningThread.start();
		}
	}

	/**
	 * Closes/forgets all the Xbee connections that may previously have been established.
	 *
	*/
	public void reset() {
		System.out.print("XbeeManager Reset ");

		for(XBeeReader reader : xbeeReaders.values()) {
			reader.stopXBee();
			while(reader.isAlive()) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException ie) {

				}
				System.out.print(".");
			}
		}


		for(Serial port : xbeePorts.values()) {
			port.stop();
			System.out.print(".");
		}

		xbeeReaders.clear();
		xbeePorts.clear();

		System.out.println("");
	}

	/**
	 * The run method used by the scanning thread.
	 *
	*/
	public void run() {
		reset();

		System.out.println("XbeeManager Scan");

		String[] availablePorts = Serial.list();
		String osName = System.getProperty("os.name");

		for(int portNum = 0; portNum < availablePorts.length; portNum++) {
			if((osName.indexOf("Mac") != -1) && (availablePorts[portNum].indexOf("tty.usbserial") == -1)) {
				System.out.println("\tSkipping port: " + availablePorts[portNum]);
				continue;
			}

			PApplet.print("\tConnecting to port: " + availablePorts[portNum] + " ... ");
			Serial xbeePort = new Serial(parent, availablePorts[portNum], XBEE_BAUDRATE);
			XBeeReader xbeeReader = new XBeeReader(parent, xbeePort);
			xbeeReader.DEBUG = xbeeDebug;

			System.out.println("\t\tStarting XBee");
			xbeeReader.startXBee();

			System.out.println(" \t\tGetting NI");
			xbeeReader.getNI();

			synchronized(this) {
				nodeID = null;

				try {
					this.wait(XBEE_RESPONSE_TIMEOUT);
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

	/**
	 * Recieve and xBeeEvent callback
	 *
	 * @param reader the XBeeReader which has an available event to be processed
	*/
	public void xBeeEvent(XBeeReader reader) {
		XBeeDataFrame data = reader.getXBeeReading();
		data.parseXBeeRX16Frame();

		int[] buffer = data.getBytes();
		nodeID = "";
		for(int i = 0; i < buffer.length; i++) {
			nodeID += (char) buffer[i];
		}

		synchronized(this) {
			this.notify(); //TODO Test
		}
	}

	/* --- GUI Controls --- */

	/**
	 * Receive an event callback from controlP5
	 *
	 * @param event the controlP5 event.
	*/
	public void controlEvent(ControlEvent event) {
		if(isVisible) {
			if(event.controller().name().equals("XbeeManager Scan")) scan();
			else if(event.controller().name().equals("XbeeManager Next")) processUIEvent();
		}
	}

	/**
	 * Receive a keyPressed event.
	 *
	 * @param keycode the keycode of the keyPressed event.
	*/
	public void keyPressed(int keycode) {
		if(isVisible && keycode == PConstants.ENTER) processUIEvent();
	}

	/**
	 * Do the actions for a UI event.
	 *
	*/
	void processUIEvent() {
		if(isScanning()) return;
		else if(propinquity != null) propinquity.changeGameState(GameState.PlayerList); //TODO Fix this is horrid.
	}

	/* --- Graphics --- */

	/**
	 * Shows the GUI.
	 *
	*/
	public void show() {
		isVisible = true;
		controlP5.show();
	}

	/**
	 * Hides the GUI.
	 *
	*/
	public void hide() {
		isVisible = false;
		controlP5.hide();
	}

	/**
	 * Returns true if the GUI is visible.
	 *
	 * @return true is and only if the GUI is visible.
	*/
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Draw method, draws the GUI when called.
	 *
	*/
	public void draw() {
		if(isVisible) {
			
			String msg = "";
			if(isScanning()) msg = "Scanning...";
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

	/**
	 * Handle the dispose when processing window is closed.
	 *
	*/
	public void dispose() {
		reset();
		if(controlP5 != null) {
			controlP5.dispose();
			controlP5 = null;
		}
	}

}
