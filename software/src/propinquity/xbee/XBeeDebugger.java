package propinquity.xbee;

import processing.core.*;
import propinquity.*;
import xbee.*;

public class XBeeDebugger extends PApplet {

	// Unique serialization ID
	private static final long serialVersionUID = 6340508174717159418L;

	XBeeManager xbeeManager;
	XBeeReader xbee;
	XPan xpan;

	public void setup() {
		size(1024, 768);

		xbeeManager = new XBeeManager(this);
		while(xbeeManager.isScanning());
		xbee = xbeeManager.reader("P1_PROX1");
		xpan = new XPan(xbee);
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
		}
	}

	public void xBeeEvent(XBeeReader reader) {
		if(xbeeManager.isScanning()) {
			xbeeManager.xBeeEvent(reader);
		} else {
			XBeeDataFrame data = reader.getXBeeReading();

			if (data.getApiID() == XBeeReader.SERIES1_RX16PACKET) {
				int[] packet = data.getBytes();
				System.out.println(packet);
			}
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.xbee.XBeeDebugger" });
	}

}