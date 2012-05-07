package propinquity.hardware;

import java.util.*;

import processing.core.*;
import processing.serial.*;
import controlP5.*;

import com.rapplogic.xbee.api.*;

import propinquity.*;

/**
 * This class scans for XBees connected to the computer. It then instantiates and holds Xbee objects for each such device.
 *
*/
public class XBeeBaseStation implements Runnable, UIElement {

	final int XBEE_BAUDRATE = 115200;
	final int XBEE_RESPONSE_TIMEOUT = 1000;

	Propinquity parent;

	boolean isVisible;

	ControlP5 controlP5;
	Button plNextButton;
	Button plScanButton;

	Thread scanningThread;

	boolean xbeeDebug;
	HashMap<String, XBee> xbees;

	/**
	 * Create a new XBeeBaseStation with xbeeDebug turned off.
	 *
	 */
	public XBeeBaseStation() {
		this(null, false);
	}

	/**
	 * Create a new XBeeBaseStation.
	 *
	 * @param xbeeDebug the XBee xbeeDebug mode.
	 */
	public XBeeBaseStation(Propinquity parent, boolean xbeeDebug) {
		this.parent = parent;
		isVisible = true;

		controlP5 = new ControlP5(parent);

		//Button to scan for XBees
		plScanButton = controlP5.addButton("XBeeBaseStation Scan", 0, parent.width / 2 + 60, parent.height / 2 + 50, 50, 20);
		plScanButton.setCaptionLabel("SCAN");

		//Next button
		plNextButton = controlP5.addButton("XBeeBaseStation Next", 0, parent.width / 2 + 60 + 50 + 10, parent.height / 2 + 50, 50, 20);
		plNextButton.setCaptionLabel("NEXT");

		hide();

		this.xbeeDebug = xbeeDebug;
		xbees = new HashMap<String, XBee>();

		scan();
	}

	/**
	 * Get the XBee object for the XBee with the matching NodeIdentifier (NI).
	 * 
	 * @param ni the NodeIdentifier of the requested XBee.
	 * @return the XBee for the XBee with the matching NodeIdentifier.
	*/
	public XBee getXbee(String ni) {
		return xbees.get(ni);
	}

	/**
	 * Get a list of NodeIdentifier (NI) for all available XBees.
	 *
	 * @return an array of the valid NodeIdentifier for available XBees.
	*/
	public String[] listXBees() {
		return xbees.keySet().toArray(new String[0]);
	}

	/**
	 * Checks if the XBeeBaseStation object is currently scanning for XBees
	 *
	 * @return true if the XBeeBaseStation is currently scanning. False otherwise.
	*/
	public boolean isScanning() {
		if(scanningThread != null && scanningThread.isAlive()) return true;
		else return false;
	}

	/**
	 * Triggers a new scan cycle, unless one is already running. The scan cycle will search all serial ports for available XBees
	 *
	*/
	public void scan() {
		if(scanningThread != null && scanningThread.isAlive()) return;
		else {
			//TODO add reset here
			scanningThread = new Thread(this);
			scanningThread.start();
		}
	}

	/**
	 * Closes/forgets all the XBee connections that may previously have been established.
	 *
	*/
	public void reset() {
		System.out.print("XBeeBaseStation Reset");

		for(XBee xbee : xbees.values()) {
			System.out.print(".");
			xbee.close();
		}

		xbees.clear();

		try {
			System.out.print(".");
			Thread.sleep(1000); //TODO is this needed?
		} catch(Exception e) {

		}

		System.out.println("");
	}

	/**
	 * The run method used by the scanning thread.
	 *
	*/
	public void run() {
		reset();

		System.out.println("XBeeBaseStation Scan");

		String[] availablePorts = Serial.list();
		String osName = System.getProperty("os.name");

		for(int portNum = 0;portNum < availablePorts.length;portNum++) {
			if((osName.indexOf("Mac") != -1) && (availablePorts[portNum].indexOf("tty.usbserial") == -1)) {
				System.out.println("\tSkipping port: " + availablePorts[portNum]);
				continue;
			}

			System.out.println("\tConnecting to port: " + availablePorts[portNum] + " ... ");

			XBee xbee = new XBee();

			try {
				xbee.open(availablePorts[portNum], XBEE_BAUDRATE);
			} catch(XBeeException e) {
				System.out.println(e.getMessage());
				System.out.println("Failed to connect to XBee");
				continue;
			}

			System.out.println("\t\tConnected to XBee");

			try {
				Thread.sleep(150);
			} catch(Exception e) {

			}

			XBeeResponse response = null;

			try {
				response = xbee.sendSynchronous(new AtCommand("NI"), XBEE_RESPONSE_TIMEOUT);
			} catch (XBeeTimeoutException e) {
			    System.out.println("\t\tTimeout getting NI");
			    continue;
			} catch (XBeeException e) {
				System.out.println("\t\tException getting NI");
				continue;
			}
	
			if (response != null && response.getApiId() == ApiId.AT_RESPONSE) {
			    AtCommandResponse atResponse = (AtCommandResponse)response;
			    if (atResponse.isOk()) {
		        	System.out.println("\t\tGot NI: "+new String(atResponse.getValue(), 0, atResponse.getValue().length));
			    } else {
			        System.out.println("\t\tNI Command was not successful");
			        continue;
			    }
			} else {
				System.out.println("\t\tNI Response was null or wrong type");
				continue;
			}

			xbees.put(availablePorts[portNum], xbee);
		}

		System.out.println("Scan Complete");
	}

	/* --- GUI Controls --- */

	/**
	 * Receive an event callback from controlP5
	 *
	 * @param event the controlP5 event.
	*/
	public void controlEvent(ControlEvent event) {
		if(isVisible) {
			if(event.controller().name().equals("XBeeBaseStation Scan")) scan();
			else if(event.controller().name().equals("XBeeBaseStation Next")) processUIEvent();
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
		else if(parent != null) parent.changeGameState(GameState.PlayerList); //TODO Fix this is horrid.
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
				for(String s : xbees.keySet()) msg += s;
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
