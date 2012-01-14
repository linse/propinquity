import processing.serial.*;
import xbee.*;

XBeeManager xbeeManager;

void setup() {
  // build and init xbee manager
  xbeeManager = new XBeeManager(this);
}

void draw() {
}

// hand over xbee events to xbee manager
void xBeeEvent(XBeeReader xbee) {
  xbeeManager.xBeeEvent(xbee);
}

