package propinquity.hardware;

import java.util.*;
import java.lang.System;
import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.wpan.*;
import gnu.io.*;

/**
 * This class scans for XBees connected to the computer. It then instantiates and holds Xbee objects for each such device.
 *
 */
public class XBeeBaseStation implements Runnable, HardwareInterface, PacketListener {

	final int NUM_XBEES_USED = 1; //In a effort to keep some multi XBee functionality I've left this in and most of the multi-XBee support.

	final int XBEE_BAUDRATE = 57600;
	final int XBEE_RESPONSE_TIMEOUT = 1000;
	final int XBEE_RETRY_TIMOUT = 250;
	final int XBEE_RETRY_COUNT = 0;

	Thread scanningThread;

	HashMap<String, XBee> xbees;
	HashMap<Integer, RequestMonitor> requestMonitors;

	HashMap<Integer, Glove> gloves;
	HashMap<Integer, Patch> patches;
	Vector<ProxEventListener> proxListeners;
	Vector<AccelEventListener> accelListeners;

	Vector<Packet> throttledPackets;
	ThrottleDaemon throttleDaemon;

	int currentFrameID;

	/**
	 * Create a new XBeeBaseStation.
	 *
	 */
	public XBeeBaseStation() {
		xbees = new HashMap<String, XBee>();
		requestMonitors = new HashMap<Integer, RequestMonitor>();

		gloves = new HashMap<Integer, Glove>();
		patches = new HashMap<Integer, Patch>();
		proxListeners = new Vector<ProxEventListener>();
		accelListeners = new Vector<AccelEventListener>();

		throttledPackets = new Vector<Packet>();
		throttleDaemon = new ThrottleDaemon();
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
			scanningThread = new Thread(this);
			scanningThread.start();
		}
	}

	public void scanBlocking() {
		run();
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
			Thread.sleep(500);
		} catch(Exception e) {

		}

		System.out.println("");
	}

	/**
	 * The run method used by the scanning thread.
	 *
	*/
	public void run() {
		System.out.println("XBeeBaseStation Scan");

		String[] availablePorts = getAvailableSerialPorts();

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
				Thread.sleep(500);
			} catch(Exception e) {

			}
			String ni = null;
			XBeeResponse response = null;

			try {
				response = xbee.sendSynchronous(new AtCommand("NI"), XBEE_RESPONSE_TIMEOUT);
			} catch(XBeeTimeoutException e) {
				System.out.println("\t\tTimeout getting NI");
				continue;
			} catch(XBeeException e) {
				System.out.println("\t\tException getting NI");
				continue;
			}

			if(response != null && response.getApiId() == ApiId.AT_RESPONSE) {
				AtCommandResponse atResponse = (AtCommandResponse)response;
				if(atResponse.isOk()) {
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

			xbee.addPacketListener(this);
			XBee returnedXBee = xbees.put(ni, xbee);

			if(returnedXBee != null) System.err.println("Warning: You have at least two XBees flashed with the same NI: \""+ni+"\". One of the duplicate XBees was dropped.");

			if(xbees.size() >= NUM_XBEES_USED) break;
		}

		System.out.println("Scan Complete");
	}

	/**
	 * Scan the COMM port an find the available ones.
	 *
	 * @return a String array containing the port names for all serial ports that are not currently being used.
	 *
	 */
	public static String[] getAvailableSerialPorts() {
		Vector<String> portNames = new Vector<String>();
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		while(ports.hasMoreElements()) {
			CommPortIdentifier com = ports.nextElement();
			switch(com.getPortType()) {
				case CommPortIdentifier.PORT_SERIAL:
				try {
					CommPort port = com.open("CommUtil", 50);
					port.close();
					portNames.add(com.getName());
				} catch(PortInUseException e) {
					System.out.println("Port, " + com.getName() + ", is in use.");
				} catch(Exception e) {
					System.err.println("Failed to open port " + com.getName());
					e.printStackTrace();
				}
			}
		}
		return portNames.toArray(new String[0]);
	}


	public void addPatch(Patch patch) {
		if(patches.put(patch.getAddress(), patch) != null) {
			System.out.println("Warning a patch with duplicate address added to XBeeBaseStation.");
		}

		if(gloves.containsKey(patch.getAddress())) {
			System.out.println("Warning there XBeeBaseStation has a patch and a glove with the same address, no action was taken.");
		}
	}

	public boolean removePatch(Patch patch) {
		if(patches.remove(patch.getAddress()) != null) return true;
		else return false;
	}

	public void addGlove(Glove glove) {
		if(gloves.put(glove.getAddress(), glove) != null) {
			System.out.println("Warning a glove with a duplicate address added to XBeeBaseStation.");
		}

		if(patches.containsKey(glove.getAddress())) {
			System.out.println("Warning there XBeeBaseStation has a patch and a glove with the same address, no action was taken.");
		}
	}

	public boolean removeGlove(Glove glove) {
		if(gloves.remove(glove.getAddress()) != null) return true;
		else return false;
	}

	public void addProxEventListener(ProxEventListener listener) {
		if(proxListeners.indexOf(listener) == -1) proxListeners.add(listener);
	}

	public boolean removeProxEventListener(ProxEventListener listener) {
		return proxListeners.remove(listener);
	}

	public void addAccelEventListener(AccelEventListener listener) {
		if(accelListeners.indexOf(listener) == -1) accelListeners.add(listener);
	}

	public boolean removeAccelEventListener(AccelEventListener listener) {
		return accelListeners.remove(listener);
	}

	public void sendPacket(Packet packet) {
		if(xbees.size() == 0) return;
		// sendPacketAsynchronous(packet);
		sendPacketThrottled(packet);
	}

	int getNextFrameId() {
		currentFrameID++;
		if(currentFrameID > 255) currentFrameID = 1;
		return currentFrameID;
	}

	void removeMonitor(int frameId) {
		RequestMonitor oldRequestMonitor = requestMonitors.remove(frameId);
		if(oldRequestMonitor != null) oldRequestMonitor.ack();
	}

	synchronized void addMonitor(PacketType type, XBeeRequest request) {
		for(Map.Entry<Integer, RequestMonitor> entry : requestMonitors.entrySet()) {
			int id = entry.getKey();
			RequestMonitor monitor = entry.getValue();
			if(monitor.getPacketType() == type) {
				removeMonitor(id);
			}
		}

		RequestMonitor oldRequestMonitor = requestMonitors.put(request.getFrameId(), new RequestMonitor(type, request));
		if(oldRequestMonitor != null) oldRequestMonitor.ack();
	}

	TxRequest16 generateRequest(Packet packet) {
		XBeeAddress16 addr = new XBeeAddress16(((packet.getDestAddr() & 0xFF00) >> 8), packet.getDestAddr() & 0x00FF);
		
		int[] fullPayload = new int[packet.getPayload().length+1];
		fullPayload[0] = packet.getPacketType().getCode();
		System.arraycopy(packet.getPayload(), 0, fullPayload, 1, packet.getPayload().length);
		
		TxRequest16 request = new TxRequest16(addr, getNextFrameId(), fullPayload);

		return request;
	}

	public void sendPacketSynchrous(Packet packet) {
		sendPacketSynchrous(packet, 10*1000);
	}

	public void sendPacketSynchrous(Packet packet, int timeout) {
		TxRequest16 request = generateRequest(packet);

		removeMonitor(request.getFrameId());

		for(XBee xbee : xbees.values()) {
			try {
				// send a request and wait up to timeout milliseconds for the response
				XBeeResponse response = xbee.sendSynchronous(request, timeout);

				if(response.getApiId() == ApiId.TX_STATUS_RESPONSE) {
					TxStatusResponse tx_response = (TxStatusResponse)response;
				}
			} catch(XBeeTimeoutException e) {
				System.out.println("\t\tTimeout sending request");
			} catch(XBeeException e) {
				System.out.println("\t\tException sending request");
			}
		}
	}

	public void sendPacketAsynchronous(Packet packet) {
		TxRequest16 request = generateRequest(packet);
		
		//Request monitor does all the sending
		addMonitor(packet.getPacketType(), request);
	}

	public void sendPacketThrottled(Packet packet) {
		throttledPackets.add(packet);
	}

	public synchronized void processResponse(XBeeResponse response) {
		switch(response.getApiId()) {
			case TX_STATUS_RESPONSE: {
				TxStatusResponse tx_response = (TxStatusResponse)response;
				if(tx_response.isSuccess()) { // ACK
					removeMonitor(tx_response.getFrameId());
				}
				break;
			}
			case RX_16_RESPONSE: {//From remote radio
				RxResponse16 rx_response = (RxResponse16)response;
				int addr = rx_response.getRemoteAddress().get16BitValue();
				Patch patch = patches.get(addr);
				int[] data = rx_response.getData();
				if(patch != null) {
					if(data.length > 1 && data[0] == PacketType.PROX.getCode()) {
						patch.setProx(((data[1] & 0xFF) << 8) | (data[2] & 0xFF));
						for(ProxEventListener listener : proxListeners) listener.proxEvent(patch);
					} else if(data.length > 3 && data[0] == PacketType.ACCEL_XYZ.getCode()) {
						patch.setAccelXYZ((byte)data[1], (byte)data[2], (byte)data[3]);
						for(AccelEventListener listener : accelListeners) listener.accelXYZEvent(patch);
					} else if(data.length > 1 && data[0] == PacketType.ACCEL_INT0.getCode()) {
						System.out.println("Receiver ACCEL_INT0");
					} else if(data.length > 1 && data[0] == PacketType.ACCEL_INT1.getCode()) {
						System.out.println("Receiver ACCEL_INT1");
					} else {
						System.out.println("Packet form address "+addr+" seems malformed. Contains: ");
						for(int i = 0;i < data.length;i++) {
							System.out.println("["+i+"]"+data[i]);
						}
					}
				} else {
					System.out.println("Got reponse from an unregistered patch or glove with address " + addr);
				}

				break;
			}
			default: {
				System.out.println("XBee got something don't know what it is: "+response.getApiId().toString());
				System.out.println(response.toString());
				break;
			}
		}		
	}

	class RequestMonitor implements Runnable {

		boolean ack;
		int retryCount;

		PacketType type;
		XBeeRequest request;

		RequestMonitor(PacketType type, XBeeRequest request) {
			this.request = request;
			ack = false;

			Thread reqThread = new Thread(this);
			reqThread.setDaemon(true);
			reqThread.start();
		}

		int getFrameId() {
			return request.getFrameId();
		}

		PacketType getPacketType() {
			return type;
		}

		void ack() {
			ack = true;
		}

		public void run() {
			while(!ack) {
				for(XBee xbee : xbees.values()) {
					try {
						xbee.sendAsynchronous(request);
					} catch(XBeeException e) {
						System.out.println("\t\tException sending request");
					}
				}

				if(++retryCount > XBEE_RETRY_COUNT) break; //Give up

				try {
					Thread.sleep(XBEE_RETRY_TIMOUT); 
				} catch(Exception e) {

				}
			}
		}

	}

	class ThrottleDaemon implements Runnable {

		Thread thread;
		boolean running;

		ThrottleDaemon() {
			running = true;

			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}

		void stop() {
			running = false;
			if(thread != null) while(thread.isAlive()) Thread.yield();
		}

		public void run() {
			while(running) {
				if(throttledPackets.size() == 0) {
					Thread.yield();
				} else {
					sendPacketAsynchronous(throttledPackets.remove(0));
					try {
						if(throttledPackets.size() < 75) Thread.sleep(1);
						else Thread.sleep(0, 250000);
					} catch(Exception e) {

					}
				}
			}
		}

	}

}
