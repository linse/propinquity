package proclipsingpinquity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import com.rapplogic.xbee.api.wpan.*;
import processing.core.PApplet;
import com.rapplogic.xbee.api.*;

import java.util.List;

public class Xpan extends PApplet implements Runnable {

	static final int SERIAL_BAUDRATE = 115200;
	static final int PROX_OUT_PACKET_TYPE = 1;
	static final int VIBE_OUT_PACKET_TYPE = 3;
	static final int CONFIG_OUT_PACKET_TYPE = 5;
	// length (bytes) of outgoing
	// packet for proximity
	// steps
	static final int PROX_OUT_PACKET_LENGTH = 5; 
	// length (bytes) of outgoing
	// config packet for
	// proximity
	static final int CONFIG_OUT_PACKET_LENGTH = 3; 

	Thread thread;
	XBee localXbee;
	Queue<WpanNodeDiscover> discoveredNodes;

	Xpan(String serialPort) {
		this.localXbee = new XBee();
		this.discoveredNodes = new ConcurrentLinkedQueue<WpanNodeDiscover>();
		this.localXbee = localXbeeFromPort(serialPort);

		if (thread != null)
			return;
		thread = new Thread(this);
		thread.start();
		try {
			thread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace(); 
		}

	}

	public void run() {
		discoverRemoteNodes(this.localXbee);

		// clear thread
		thread = null;
	}

	// opens serial port
	XBee localXbeeFromPort(String serialPort) {
		XBee localXbee = new XBee();
		try {
			localXbee.open(serialPort, Settings.SERIAL_BAUDRATE);
		} catch (XBeeException e) {
			println("** Error opening XBee port " + serialPort + ": " + e
					+ " **");
			println("Is your XBee plugged in to your computer?");
			System.exit(1);
		}
		return localXbee;
	}

	void discoverRemoteNodes(XBee xbee) {
		try {
			// default is 2.5 seconds for series 1
			int nodeDiscoveryTimeout = 5000;
			xbee.sendAsynchronous(new AtCommand("ND"));

			// collect responses up to the timeout or until the terminating
			// response is received, whichever occurs first
			List<? extends XBeeResponse> responses = xbee.collectResponses(
					nodeDiscoveryTimeout, new CollectTerminator() {
						public boolean stop(XBeeResponse response) {
							if (response instanceof AtCommandResponse) {
								AtCommandResponse at = (AtCommandResponse) response;
								if (at.getCommand().equals("ND")
										&& at.getValue() != null
										&& at.getValue().length == 0) {
									// println("Found terminating response");
									return true;
								}
							}
							return false;
						}
					});

			for (XBeeResponse response : responses) {
				if (response instanceof AtCommandResponse) {
					AtCommandResponse atResponse = (AtCommandResponse) response;

					if (atResponse.getCommand().equals("ND")
							&& atResponse.getValue() != null
							&& atResponse.getValue().length > 0) {
						WpanNodeDiscover nd = WpanNodeDiscover.parse((AtCommandResponse) response);
						this.discoveredNodes.add(nd);
					}
				}
				else {
					//println("Other response "+response);
					// we also get some rx16 responses already
				}
			}

			this.localXbee.addPacketListener(new PacketListener() {
			    public void processResponse(XBeeResponse response) {
					println(response);
			    }
			});

		} catch (XBeeException e) {
			System.err.println("Error during node discovery: " + e);
		}
	}

	void printRemoteNodes() {
		println("Discovered nodes:");
		WpanNodeDiscover nd;
		while ((nd = discoveredNodes.poll()) != null) {
			println(nd);
		}
	}

	void sendOutgoing(XBeeAddress16 address, int[] data, int turnNum,
			int baseNum) {
		// println("SEND OUTGOING: " + xbee + " " + xbee.getPort());
		int[] payload = data;
		payload[1] = turnNum;
		TxRequest16 request = new TxRequest16(address, payload);
		try {
			this.localXbee.sendAsynchronous(request);
		} catch (XBeeException e) {
			System.err.println("Could not send outgoing package " + data
					+ "in turn " + turnNum + ".");
			// e.printStackTrace();
		}
		// add to output queue
		// printToOutput("SENT at " + millis() + ": turn number " + turnNum +
		// ", base number " + baseNum);
	}

	void broadcast(int[] payload) {
		TxRequest16 request = new TxRequest16(XBeeAddress16.BROADCAST, payload);
		try {
			this.localXbee.sendAsynchronous(request);
		} catch (XBeeException e) {
			System.err.println("Could not broadcast vibe.");
			// e.printStackTrace();
		}
	}

	// receive readings from all remote nodes
	void receiveProxReadings() {
	    try {
			XBeeResponse response = this.localXbee.getResponse(1000);
	    	println("try to read");
			if (response!=null) {
				println(response);
			}
		} catch (XBeeTimeoutException e) {
			println("timeout");
		} catch (XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // TODO check if prox reading
	}
	

	// seems to help clearing serial connection
	public void finalize() {
		localXbee.close();
	}

	void broadcastProxConfig(int stepLength) {
		println("broadcasting prox config");
		broadcast(getProxConfigPacket(stepLength));
	}

	void broadcastVibe(int value) {
		broadcast(getVibePacket(value));
	}

	 void broadcastStep() {
	 broadcast(getStepPacket());
	 }

	private int[] getProxConfigPacket(int stepLength) {
		int[] packet = new int[CONFIG_OUT_PACKET_LENGTH];
		packet[0] = CONFIG_OUT_PACKET_TYPE;
		packet[1] = (stepLength >> 8) & 0xFF;
		packet[2] = stepLength & 0xFF;
		return packet;
	}

	 private int[] getStepPacket() {
	 int[] packet = new int[PROX_OUT_PACKET_LENGTH];
	 packet[0] = PROX_OUT_PACKET_TYPE;
	 packet[1] = 0;
	 packet[2] = 0;
	 packet[3] = 0xff;
	 packet[4] = 0xff;
	 return packet;
	 }

	private int[] getVibePacket(int value) {
		int[] packet = { VIBE_OUT_PACKET_TYPE, value };
		return packet;
	}
}
