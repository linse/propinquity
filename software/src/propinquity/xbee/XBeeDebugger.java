package propinquity.xbee;

import processing.core.*;

import propinquity.*;

public class XBeeDebugger extends PApplet {
	// Unique serialization ID
	private static final long serialVersionUID = 6340508174717159418L;

	int i = 0;

	public void setup() {
		size(1024, 768);
	}

	public void draw() {
		background(0);
		stroke(255);
		fill(200, 0, 0);
		rect(10, i%height, 100, 100);
		i++;
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.xbee.XBeeDebugger" });
	}

}