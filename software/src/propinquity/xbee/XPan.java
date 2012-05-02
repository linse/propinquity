package propinquity.xbee;

import processing.core.PApplet;
import xbee.XBeeReader;
import propinquity.*;

public class XPan {
	// xbee
	static public final int PROX_IN_PACKET_LENGTH = 6; // length (bytes) of incoming
												// packet for proximity readings
	static public final int PROX_OUT_PACKET_LENGTH = 5; // length (bytes) of outgoing
													// packet for proximity
													// steps
	static public final int CONFIG_OUT_PACKET_LENGTH = 3; // length (bytes) of outgoing
													// config packet for
													// proximity
	static public final int ACCEL_IN_PACKET_LENGTH = 5; // length (bytes) of incoming
													// packet for proximity
													// readings
	static public final int CONFIG_ACK_LENGTH = 4;
	static public final int VIBE_IN_PACKET_LENGTH = 3;

	static public final int BROADCAST_ADDR = 0xFFFF;

	static public final int PROX_OUT_PACKET_TYPE = 1;
	static public final int PROX_IN_PACKET_TYPE = 2;
	static public final int VIBE_OUT_PACKET_TYPE = 3;
	static public final int ACCEL_IN_PACKET_TYPE = 4;
	static public final int CONFIG_OUT_PACKET_TYPE = 5;
	static public final int CONFIG_ACK_PACKET_TYPE = 6;
	static public final int VIBE_IN_PACKET_TYPE = 7; // THIS IS NEW. For button
												// presses.

	// Serial g_port;
	XBeeReader xbee;

	public XPan(XBeeReader xbee) {
		this.xbee = xbee;
		// xbee.startXBee();
	}

	void broadcast(int[] data, int turnNum, int baseNum) {
		sendOutgoing(BROADCAST_ADDR, data, turnNum, baseNum);
	}

	void sendOutgoing(int adl, int[] data, int turnNum, int baseNum) {
		// println("SEND OUTGOING: " + xbee + " " + xbee.getPort());
		int[] myData = data;
		// data[1] = turnNum;
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

	public void nodeDiscover() {
		System.out.println("discovering nodes from xpan.");
		xbee.nodeDiscover();
	}

	public void stop() {
		xbee.stopXBee();
	}

	public void broadcastProxConfig(int stepLength) {
		System.out.println("broadcasting prox config");
		broadcast(getProxConfigPacket(stepLength));
	}

	public void broadcastVibe(int value) {
		broadcast(getVibePacket(value));
	}

	public void broadcastStep(int stepNum, Step step1, Step step2, Step step3,
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
