// xbee personal area network of one local and up to two remote xbees

public class XPan { 

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

