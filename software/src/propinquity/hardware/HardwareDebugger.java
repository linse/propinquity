package propinquity.hardware;

import processing.core.*;
import controlP5.*;

public class HardwareDebugger extends PApplet implements ProxEventListener {

	// Unique serialization ID
	private static final long serialVersionUID = 6340508174717159418L;

	static final int[] PATCH_ADDR = new int[] { 1, 2 };
	static final int NUM_PATCHES = PATCH_ADDR.length;

	XBeeBaseStation xbeeBaseStation;

	Patch[] patches;

	ControlP5 controlP5;
	boolean show_controls = true;

	Slider prox_sliders[];

	public void setup() {
		size(1024, 768);

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

			Slider r_slider = controlP5.addSlider("Red "+i, 0, 255, 0, (local_width-20)/num*1+10, 10, 15, 200);
			r_slider.setGroup(group);
			Slider g_slider = controlP5.addSlider("Green "+i, 0, 255, 0, (local_width-20)/num*2+10, 10, 15, 200);
			g_slider.setGroup(group);
			Slider b_slider = controlP5.addSlider("Blue "+i, 0, 255, 0, (local_width-20)/num*3+10, 10, 15, 200);
			b_slider.setGroup(group);
			Slider duty_slider = controlP5.addSlider("Color Duty "+i, 0, 255, 0, (local_width-20)/num*1+10, 240, 15, 200);
			duty_slider.setGroup(group);
			Slider period_slider = controlP5.addSlider("Color Period "+i, 0, 255, 0, (local_width-20)/num*2+10, 240, 15, 200);
			period_slider.setGroup(group);

			
			Slider vibe_slider = controlP5.addSlider("Vibe Level "+i, 0, 255, 0, (local_width-20)/num*4+10, 10, 15, 200);
			vibe_slider.setGroup(group);
			Slider vibe_duties_slider = controlP5.addSlider("Vibe Duty "+i, 0, 255, 0, (local_width-20)/num*4+10, 240, 15, 200);
			vibe_duties_slider.setGroup(group);
			Slider vibe_periods_slider = controlP5.addSlider("Vibe Period "+i, 0, 255, 0, (local_width-20)/num*5+10, 240, 15, 200);
			vibe_periods_slider.setGroup(group);

			prox_sliders[i] = controlP5.addSlider("Prox "+i, 0, 1024, 0, (local_width-20)/num*5+10, 10, 15, 200);
			prox_sliders[i].lock();
			prox_sliders[i].setGroup(group);
		}

		if(!show_controls) controlP5.hide();

		xbeeBaseStation = new XBeeBaseStation();
		xbeeBaseStation.addProxEventListener(this);

		patches = new Patch[NUM_PATCHES];
		for(int i = 0;i < NUM_PATCHES;i++) {
			patches[i] = new Patch(PATCH_ADDR[i], xbeeBaseStation);
			xbeeBaseStation.addPatch(patches[i]);
		}
	}

	public void controlEvent(ControlEvent theEvent) {
		String name = theEvent.controller().name();
		int value = (int)theEvent.controller().value();
		if(name.equals("Re-Scan")) {
			xbeeBaseStation.scan();
			return;
		} else {
			for(int i = 0;i < NUM_PATCHES;i++) {
				if(name.equals("Active "+i)) {
					if(value != 0) patches[i].setActive(true);
					else patches[i].setActive(false);
					return;
				} else if(name.equals("Red "+i)) {
					int[] current_color = patches[i].getColor();
					patches[i].setColor(value, current_color[1], current_color[2]);
					return;
				} else if(name.equals("Green "+i)) {
					int[] current_color = patches[i].getColor();
					patches[i].setColor(current_color[0], value, current_color[2]);
					return;
				} else if(name.equals("Blue "+i)) {
					int[] current_color = patches[i].getColor();
					patches[i].setColor(current_color[0], current_color[1], value);
					return;
				} else if(name.equals("Color Duty "+i)) {
					patches[i].setColorDuty(value);
					return;
				} else if(name.equals("Color Period "+i)) {
					patches[i].setColorPeriod(value);
					return;
				} else if(name.equals("Vibe Level "+i)) {
					patches[i].setVibeLevel(value);
					return;
				} else if(name.equals("Vibe Duty "+i)) {
					patches[i].setVibeDuty(value);
					return;
				} else if(name.equals("Vibe Period "+i)) {
					patches[i].setVibePeriod(value);
					return;
				}
			}
		}
	}

	public void draw() {
		background(0);
	}
	
	public void keyPressed() {
		if(key == 's') {
			xbeeBaseStation.scan();
		} else if(key == 'h') {
			show_controls = !show_controls;
			if(!show_controls) controlP5.hide();
			else controlP5.show();
		}
	}

	public void proxEvent(Patch patch) {
		for(int i = 0;i < NUM_PATCHES;i++) {
			if(patch == patches[i]) {
				prox_sliders[i].setValue(patch.getProx());
			}
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.hardware.HardwareDebugger" });
	}

}