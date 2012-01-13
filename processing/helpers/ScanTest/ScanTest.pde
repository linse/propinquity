import processing.serial.*;
import xbee.*;
import controlP5.*;

XBeeManager xbeeManager;
int mode = 0;

void setup() {
  // build and init xbee manager
  xbeeManager = new XBeeManager(this);
  xbeeManager.init();
}

void draw() {
  // if we are done and did not output result yet
  if (mode == 0 && xbeeManager.isInitialized()) {
    mode++;
    String msg = xbeeManager.getPortsString();
    println("XBee masters found: " + msg);
  }
}

void xBeeEvent(XBeeReader xbee) {
  xbeeManager.xBeeEvent(xbee);
}

