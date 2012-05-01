package propinquity.xbee;

import processing.core.*;
import propinquity.*;
import xbee.*;

public class XBeeDebugger extends PApplet {

	// Unique serialization ID
	private static final long serialVersionUID = 6340508174717159418L;

	XBeeManager xbeeManager;
	XBeeReader xbee;

	public void setup() {
		size(200, 200);

		xbeeManager = new XBeeManager(this);
	}

	public void draw() {
		background(0);

		if(xbeeManager.isScanning()) {

		} else {
			stroke(255);
			fill(200, 0, 0);
			rect(10, height/2, 100, 100);
		}
	}

	public void keyPressed() {
		if(key == 's') {
			xbeeManager.scan();
		} else if(key == '1') {
			System.out.println("Sending 1");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {1, 0});
		} else if(key == '2') {
			System.out.println("Sending 2");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {1, 1});
		} else if(key == '3') {
			System.out.println("Sending 3");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {2, 255, 0, 0});
		} else if(key == '4') {
			System.out.println("Sending 4");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {2, 0, 255, 0});
		} else if(key == '5') {
			System.out.println("Sending 5");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {2, 0, 0, 255});
		} else if(key == '6') {
			System.out.println("Sending 6");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {2, 0, 0, 0});
		} else if(key == '7') {
			System.out.println("Sending 7");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {3, 255});
		} else if(key == '8') {
			System.out.println("Sending 8");
			xbeeManager.reader("P2_PROX2").sendDataString16(0xFFFF, new int[] {3, 0});
		}
	}

	public void xBeeEvent(XBeeReader reader) {
		if(xbeeManager.isScanning()) {
			xbeeManager.xBeeEvent(reader);
		} else {
			XBeeDataFrame data = reader.getXBeeReading();
			System.out.println("Got something");
			System.out.println(data.getAddress16());
			if (data.getApiID() == XBeeReader.SERIES1_RX16PACKET) {
				int[] packet = data.getBytes();
				System.out.println("Series 16");
				println(packet);
			}
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.xbee.XBeeDebugger" });
	}

}