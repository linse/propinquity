package propinquity.xbee;

import processing.core.PApplet;
import processing.serial.Serial;
import xbee.XBeeReader;

public class XPan {
	// xbee
	static final int PROX_IN_PACKET_LENGTH = 6; // length (bytes) of incoming
												// packet for proximity readings
	static final int PROX_OUT_PACKET_LENGTH = 5; // length (bytes) of outgoing
													// packet for proximity
													// steps
	static final int CONFIG_OUT_PACKET_LENGTH = 3; // length (bytes) of outgoing
													// config packet for
													// proximity
	static final int ACCEL_IN_PACKET_LENGTH = 5; // length (bytes) of incoming
													// packet for proximity
													// readings
	static final int CONFIG_ACK_LENGTH = 4;
	static final int VIBE_IN_PACKET_LENGTH = 3;

	static final int BROADCAST_ADDR = 0xFFFF;

	static final int PROX_OUT_PACKET_TYPE = 1;
	static final int PROX_IN_PACKET_TYPE = 2;
	static final int VIBE_OUT_PACKET_TYPE = 3;
	static final int ACCEL_IN_PACKET_TYPE = 4;
	static final int CONFIG_OUT_PACKET_TYPE = 5;
	static final int CONFIG_ACK_PACKET_TYPE = 6;
	static final int VIBE_IN_PACKET_TYPE = 7; // THIS IS NEW. For button
												// presses.

	PApplet parent;
	// Serial g_port;
	XBeeReader xbee;

	public XPan(XBeeReader xbee, PApplet parent) {
		this.parent = parent;
		this.xbee = xbee;
		xbee.startXBee();
	}

	public XPan(Serial port, PApplet parent) {
		this.parent = parent;
		// g_port = port;
		xbee = new XBeeReader(parent, port);
		xbee.startXBee();
	}

	void broadcast(int[] data, int turnNum, int baseNum) {
		sendOutgoing(BROADCAST_ADDR, data, turnNum, baseNum);
	}

	void sendOutgoing(int adl, int[] data, int turnNum, int baseNum) {
		// println("SEND OUTGOING: " + xbee + " " + xbee.getPort());
		int[] myData = data;
		data[1] = turnNum;
		xbee.sendDataString16(adl, myData);
		// add to output queue
		// printToOutput("SENT at " + millis() + ": turn number " + turnNum +
		// ", base number " + baseNum);
	}

	void broadcast(int[] data) {
		sendOutgoing(BROADCAST_ADDR, data);
	}

	void sendOutgoing(int adl, int[] data) {
		int[] myData = data;
		xbee.sendDataString16(adl, myData);
	}

	void nodeDiscover() {
		PApplet.println("discovering nodes from xpan.");
		xbee.nodeDiscover();
	}

	void stop() {
		xbee.stopXBee();
	}

	void broadcastProxConfig(int stepLength) {
		PApplet.println("broadcasting prox config");
		broadcast(getProxConfigPacket(stepLength));
	}

	void broadcastVibe(int value) {
		broadcast(getVibePacket(value));
	}

	void broadcastStep(int stepNum, Step step1, Step step2, Step step3,
			Step step4) {
		broadcast(getStepPacket(stepNum, step1, step2, step3, step4));
	}

	private int[] getProxConfigPacket(int stepLength) {
		int[] packet = new int[CONFIG_OUT_PACKET_LENGTH];
		packet[0] = CONFIG_OUT_PACKET_TYPE;
		packet[1] = (stepLength >> 8) & 0xFF;
		packet[2] = stepLength & 0xFF;
		return packet;
	}

	private int[] getStepPacket(int stepNum, Step step1, Step step2,
			Step step3, Step step4) {
		int[] packet = new int[PROX_OUT_PACKET_LENGTH];
		packet[0] = PROX_OUT_PACKET_TYPE;
		packet[1] = (stepNum >> 8) & 0xFF;
		packet[2] = stepNum & 0xFF;
		packet[3] = ((step1 == null ? 0 : step1.getPacketComponent()) << 4)
				| (step2 == null ? 0 : step2.getPacketComponent());
		packet[4] = ((step3 == null ? 0 : step3.getPacketComponent()) << 4)
				| (step4 == null ? 0 : step4.getPacketComponent());
		return packet;
	}

	private int[] getVibePacket(int value) {
		int[] packet = { VIBE_OUT_PACKET_TYPE, value };
		return packet;
	}
}
