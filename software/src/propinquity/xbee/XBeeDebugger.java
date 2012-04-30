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
		size(1024, 768);

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
		} else if(key == 'm') {
			xbee = xbeeManager.reader("P1_PROX1");
		} else if(key == 't') {
			System.out.println("Sending");
			xbee.sendDataString16(0xFFFF, new int[] {8, 1, 255});
		}
	}

	public void xBeeEvent(XBeeReader reader) {
		if(xbeeManager.isScanning()) {
			xbeeManager.xBeeEvent(reader);
		} else {
			XBeeDataFrame data = reader.getXBeeReading();
			System.out.println("Got something");
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