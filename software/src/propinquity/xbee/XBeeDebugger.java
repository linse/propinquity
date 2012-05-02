package propinquity.xbee;

import processing.core.*;
import propinquity.*;
import xbee.*;
import controlP5.*;

public class XBeeDebugger extends PApplet {

	// Unique serialization ID
	private static final long serialVersionUID = 6340508174717159418L;

	static final int[] PATCH_ADDR = new int[] { 1, 2 };
	static final int NUM_PATCHES = PATCH_ADDR.length;

	XBeeManager xbeeManager;
	XBeeReader xbee;

	ControlP5 controlP5;
	boolean show_controls = true;

	boolean[] active;
	int[][] colors;
	int[] vibe;

	public void setup() {
		size(1024, 768);

		active = new boolean[NUM_PATCHES];
		colors = new int[NUM_PATCHES][3];
		vibe = new int[NUM_PATCHES];

		controlP5 = new ControlP5(this);

		for(int i = 0;i < NUM_PATCHES;i++) {
			int x_offset = (width-100)/NUM_PATCHES*i+50;
			int y_offset = 100;
			int local_width = (width-200)/NUM_PATCHES;

			ControlGroup group = controlP5.addGroup("Patch "+i, x_offset, y_offset, local_width);

			Toggle toggle = controlP5.addToggle("Active "+i, 10, 10, 50, 20);
			toggle.setMode(ControlP5.SWITCH);
			toggle.setGroup(group);

			Slider r_slider = controlP5.addSlider("Red "+i, 0, 255, colors[i][0], 10, 90, 15, 200);
			r_slider.setGroup(group);
			Slider g_slider = controlP5.addSlider("Green "+i, 0, 255, colors[i][1], 60, 90, 15, 200);
			g_slider.setGroup(group);
			Slider b_slider = controlP5.addSlider("Blue "+i, 0, 255, colors[i][2], 110, 90, 15, 200);
			b_slider.setGroup(group);

			Slider vibe_slider = controlP5.addSlider("Vibe "+i, 0, 255, vibe[i], 175, 90, 15, 200);
			vibe_slider.setGroup(group);

		}

		if(!show_controls) controlP5.hide();

		xbeeManager = new XBeeManager(this);
	}

	public void controlEvent(ControlEvent theEvent) {
		String name = theEvent.controller().name();
		int value = (int)theEvent.controller().value();
		for(int i = 0;i < NUM_PATCHES;i++) {
			if(name.equals("Active "+i)) {
				if(value != 0) active[i] = true;
				else active[i] = false;
			}
			else if(name.equals("Red "+i)) colors[i][0] = value;
			else if(name.equals("Green "+i)) colors[i][1] = value;
			else if(name.equals("Blue "+i)) colors[i][2] = value;
			else if(name.equals("Vibe "+i)) vibe[i] = value;
		}
	}

	public void draw() {
		background(0);

		if(xbeeManager.isScanning()) {

		} else {

		}
	}
	
	public void keyPressed() {
		if(key == 's') {
			xbeeManager.scan();
		} else if(key == 'h') {
			show_controls = !show_controls;
			if(!show_controls) controlP5.hide();
			else controlP5.show();
		} else if(key == 'p') {
			for(int i = 0;i < NUM_PATCHES;i++) {
				System.out.println(active[i]);
				System.out.println(vibe[i]);
			}
		}

		//  else if(key == '1') {
		// 	System.out.println("Sending 1");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {1, 0});
		// } else if(key == '2') {
		// 	System.out.println("Sending 2");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {1, 1});
		// } else if(key == '3') {
		// 	System.out.println("Sending 3");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {2, 255, 0, 0});
		// } else if(key == '4') {
		// 	System.out.println("Sending 4");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {2, 0, 255, 0});
		// } else if(key == '5') {
		// 	System.out.println("Sending 5");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {2, 0, 0, 255});
		// } else if(key == '6') {
		// 	System.out.println("Sending 6");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {2, 0, 0, 0});
		// } else if(key == '7') {
		// 	System.out.println("Sending 7");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {3, 255});
		// } else if(key == '8') {
		// 	System.out.println("Sending 8");
		// 	xbeeManager.reader("P2_PROX2").sendDataString16(addr, new int[] {3, 0});
		// }
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