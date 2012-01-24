// xbee personal area network of one local and up to two remote xbees

public class XPan { 
  
  //length (bytes) of incoming packet for proximity readings
  static final int PROX_IN_PACKET_LENGTH = 6; 
  //length (bytes) of outgoing packet for proximity steps
  static final int PROX_OUT_PACKET_LENGTH = 5; 
  
  static final int BROADCAST_ADDR = 0xFFFF; 

  static final int PROX_OUT_PACKET_TYPE = 1;
  static final int PROX_IN_PACKET_TYPE  = 2;
  static final int VIBE_OUT_PACKET_TYPE = 3;
  
  PApplet parent;
  XBeeReader xbee;

  public XPan(XBeeReader xbee, PApplet parent) {
    this.parent = parent;
    this.xbee = xbee;
    xbee.startXBee();
  }
  
  public XPan(Serial port, PApplet parent) {
    this.parent = parent;
    xbee = new XBeeReader(parent, port);
    xbee.startXBee();
  }
  
  void nodeDiscover() { 
    xbee.nodeDiscover(); 
  }
  
  void stop() { 
    xbee.stopXBee(); 
  }
  
}

