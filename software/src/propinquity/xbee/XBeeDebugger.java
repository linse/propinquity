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

	Slider prox_sliders[];

	boolean[] active;
	int[][] colors;
	int[] vibe;

	int[] prox;

	public void setup() {
		size(1024, 768);

		active = new boolean[NUM_PATCHES];
		colors = new int[NUM_PATCHES][3];
		vibe = new int[NUM_PATCHES];
		prox = new int[NUM_PATCHES];

		controlP5 = new ControlP5(this);

		prox_sliders = new Slider[NUM_PATCHES];

		controlP5.addButton("Re-Scan", 1, 10, 10, 50, 25);

		for(int i = 0;i < NUM_PATCHES;i++) {
			int x_offset = (width-100)/NUM_PATCHES*i+50;
			int y_offset = 60;
			int local_width = (width-200)/NUM_PATCHES;

			int num = 6;

			ControlGroup group = controlP5.addGroup("Patch "+i, x_offset, y_offset, local_width);

			Toggle toggle = controlP5.addToggle("Active "+i, 10, 10, 30, 30);
			toggle.setGroup(group);

			Slider r_slider = controlP5.addSlider("Red "+i, 0, 255, colors[i][0], (local_width-20)/num*1+10, 10, 15, 200);
			r_slider.setGroup(group);
			Slider g_slider = controlP5.addSlider("Green "+i, 0, 255, colors[i][1], (local_width-20)/num*2+10, 10, 15, 200);
			g_slider.setGroup(group);
			Slider b_slider = controlP5.addSlider("Blue "+i, 0, 255, colors[i][2], (local_width-20)/num*3+10, 10, 15, 200);
			b_slider.setGroup(group);

			Slider vibe_slider = controlP5.addSlider("Vibe "+i, 0, 255, vibe[i], (local_width-20)/num*4+10, 10, 15, 200);
			vibe_slider.setGroup(group);

			prox_sliders[i] = controlP5.addSlider("Prox "+i, 0, 1024, vibe[i], (local_width-20)/num*5+10, 10, 15, 200);
			prox_sliders[i].lock();
			prox_sliders[i].setGroup(group);
		}

		if(!show_controls) controlP5.hide();

		xbeeManager = new XBeeManager(this);
	}

	public void controlEvent(ControlEvent theEvent) {
		String name = theEvent.controller().name();
		int value = (int)theEvent.controller().value();

		if(name.equals("Re-Scan")) {
			xbeeManager.scan();
			return;
		} else {
			for(int i = 0;i < NUM_PATCHES;i++) {
				if(name.equals("Active "+i)) {
					if(value != 0) active[i] = true;
					else active[i] = false;
					sendActive(i, active[i]);
					return;
				} else if(name.equals("Red "+i)) {
					colors[i][0] = value;
					sendColor(i, colors[i][0], colors[i][1], colors[i][2]);
					return;
				} else if(name.equals("Green "+i)) {
					colors[i][1] = value;
					sendColor(i, colors[i][0], colors[i][1], colors[i][2]);
					return;
				} else if(name.equals("Blue "+i)) {
					colors[i][2] = value;
					sendColor(i, colors[i][0], colors[i][1], colors[i][2]);
					return;
				} else if(name.equals("Vibe "+i)) {
					vibe[i] = value;
					sendVibe(i, vibe[i]);
					return;
				}
			}
		}
	}

	void sendActive(int index, boolean active) {
		index = constrain(index, 0, NUM_PATCHES-1);
		xbeeManager.reader("P2_PROX2").sendDataString16(PATCH_ADDR[index], new int[] {1, active?1:0});
	}

	void sendColor(int index, int r, int g, int b) {
		index = constrain(index, 0, NUM_PATCHES-1);
		xbeeManager.reader("P2_PROX2").sendDataString16(PATCH_ADDR[index], new int[] {2, r&0xFF, g&0xFF, b&0xFF});
	}

	void sendVibe(int index, int vibe) {
		index = constrain(index, 0, NUM_PATCHES-1);
		xbeeManager.reader("P2_PROX2").sendDataString16(PATCH_ADDR[index], new int[] {3, vibe&0xFF});
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
		}
	}

	public void xBeeEvent(XBeeReader reader) {
		if(xbeeManager.isScanning()) {
			xbeeManager.xBeeEvent(reader);
		} else {
			XBeeDataFrame data = reader.getXBeeReading();
			if (data.getApiID() == XBeeReader.SERIES1_RX16PACKET) {
				int addr = data.getAddress16();
				int[] packet = data.getBytes();
				for(int i = 0;i < NUM_PATCHES;i++) {
					if(PATCH_ADDR[i] == addr) {
						if(packet.length > 2 && packet[0] == 4) {
							prox[i] = ((packet[1]&0xFF) << 8) | (packet[2]&0xFF);
							prox_sliders[i].setValue(prox[i]);
						}
						return;
					}
				}
			}
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.xbee.XBeeDebugger" });
	}

}