package propinquity.hardware;

import java.util.*;
import processing.serial.*;
import propinquity.*;
import com.rapplogic.xbee.api.*;

/**
 * This class scans for XBees connected to the computer. It then instantiates and holds Xbee objects for each such device.
 *
*/
public class XBeeBaseStation implements Runnable, HardwareInterface {

	final int XBEE_BAUDRATE = 115200;
	final int XBEE_RESPONSE_TIMEOUT = 1000;

	Thread scanningThread;

	HashMap<String, XBee> xbees;

	/**
	 * Create a new XBeeBaseStation.
	 *
	 */
	public XBeeBaseStation() {
		xbees = new HashMap<String, XBee>();
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
			String ni = null;
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
			    	ni = new String(atResponse.getValue(), 0, atResponse.getValue().length);
		        	System.out.println("\t\tGot NI: " + ni);
			    } else {
			        System.out.println("\t\tNI Command was not successful");
			        continue;
			    }
			} else {
				System.out.println("\t\tNI Response was null or wrong type");
				continue;
			}

			xbees.put(ni, xbee); //TODO Check for collision
		}

		System.out.println("Scan Complete");
	}


	public void addPatch(Patch patch) {

	}

	public void removePatch(Patch patch) {

	}

	public void addGlove(Glove glove) {

	}

	public void removeGlove(Glove glove) {

	}

	public void addProxEventListener(ProxEventListener listener) {

	}

	public void removeProxEventListener(ProxEventListener listener) {

	}

	public void sendPacket(Packet packet) {
		// XBeeAddress16 addr = new XBeeAddress16(((packet.getDestAddr() & 0xFF00) >> 8), packet.getDestAddr() & 0x00FF);
		
		// TxRequest16 request = new TxRequest16(addr, new int[] {'H','i'});
		// xbee.sendAsynchronous(new AtCommand("NT"));
	}

}
