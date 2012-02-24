package proclipsingpinquity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import com.rapplogic.xbee.api.wpan.*;
import com.rapplogic.xbee.api.wpan.TxRequest64;
import com.rapplogic.xbee.api.wpan.WpanNodeDiscover;
import processing.core.PApplet;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.CollectTerminator;
import java.util.List;

public class Xpan extends PApplet implements Runnable {

	static final int SERIAL_BAUDRATE = 115200;
	static final int VIBE_OUT_PACKET_TYPE = 3;

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
	}

	public void run() {
		discoverRemoteNodes(this.localXbee);
		printRemoteNodes();

		// clear thread
		thread = null;
	}

	// opens serial port
	XBee localXbeeFromPort(String serialPort) {
		XBee localXbee = new XBee();
		try {
			localXbee.open(serialPort, SERIAL_BAUDRATE);
		} catch (XBeeException e) {
			println("** Error opening XBee port " + serialPort + ": " + e
					+ " **");
			println("Is your XBee plugged in to your computer?");
			exit();
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
			}
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

	void broadcastVibe(int value) {
		int[] payload = { VIBE_OUT_PACKET_TYPE, value };
		TxRequest16 request = new TxRequest16(XBeeAddress16.BROADCAST, payload);
		try {
			this.localXbee.sendAsynchronous(request);
		} catch (XBeeException e) {
			System.err.println("Could not broadcast vibe.");
			// e.printStackTrace();
		}
	}

	// receive readings from all remote nodes
	// void receiveProxReadings() {
	// }

	// seems to help clearing serial connection
	public void finalize() {
		localXbee.close();
	}

}
