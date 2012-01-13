public class Player {
  final int XPAN_PROX_BASES = 1; //2;
  final int XPAN_ACCEL_BASES = 1;
  final int XPAN_VIBE_BASES = 1;

  PApplet parent;

  XPan[] xpansProx;
  XPan[] xpansAccel;
  XPan[] xpansVibe;
  int numPatches;

  public Player(PApplet p)
  {
    this.parent = p;
    this.xpansProx = new XPan[XPAN_PROX_BASES];
    this.xpansAccel = new XPan[XPAN_ACCEL_BASES];
    this.xpansVibe = new XPan[XPAN_VIBE_BASES];
    this.numPatches = 0;
  }


  void initProxComm(String ni1, String ni2)
  {
    //TODO load bases using their serial number...?
    if (ni1 != null) {
      XBeeReader xbee = xbeeManager.reader(ni1);
      if (xbee != null) {
        xpansProx[0] = new XPan(xbee, parent);
        println("Initialized Xbee for proximity #1: " + ni1);
      }
      else {
        System.err.println("Could not initialize Xbee for proximity #1: " + ni1);
      }
    }
    if (ni2 != null) {
      XBeeReader xbee = xbeeManager.reader(ni2);
      if (xbee != null) {
        xpansProx[1] = new XPan(xbee, parent);
        println("Initialized Xbee for proximity #2: " + ni2);
      }
      else {
        System.err.println("Could not initialize Xbee for proximity #2: " + ni2);
      }
    }

    //create the data packet that requests proximity values
    //outdata = new int[XPan.PROX_OUT_PACKET_LENGTH];
    //outdata[0] = XPan.PROX_OUT_PACKET_TYPE;
    //for (int i=1; i < outdata.length; i++)
    //  outdata[i] = 0;
  }

  XPan[] getProxXPans() { return xpansProx; }

  void initAccelComm(String ni)
  {
    if (ni == null) return;

    XBeeReader xbee = xbeeManager.reader(ni);
    if (xbee != null) {
      xpansAccel[0] = new XPan(xbee, parent);
      println("Initialized Xbee for acceleration: " + ni);
    }
    else {
      System.err.println("Could not initialize Xbee for acceleration: " + ni);
    }
  }

  void initVibeComm(String ni)
  {
    if (ni == null) return;

    XBeeReader xbee = xbeeManager.reader(ni);
    if (xbee != null) {
      xpansVibe[0] = new XPan(xbee, parent);
      println("Initialized Xbee for vibration: " + ni);
    }
    else {
      System.err.println("Could not initialize Xbee for vibration: " + ni);
    }
  }


  //TODO replace this with configPatches to pass the step length
  //at the same time as detecting which ones respond.
  public void discoverPatches() {
    println("Discover patches...");
    for(int i = 0; i < XPAN_PROX_BASES; i++)
      if (xpansProx[i] != null) {
        println("Discover proximity " + (i+1));
        xpansProx[i].nodeDiscover();
      }

    for(int i = 0; i < XPAN_ACCEL_BASES; i++)
      if (xpansAccel[i] != null) {
        println("Discover acceleration " + (i+1));
        xpansAccel[i].nodeDiscover();
      }

    for(int i = 0; i < XPAN_VIBE_BASES; i++)
      if (xpansVibe[i] != null) {
        println("Discover vibration " + (i+1));
        xpansVibe[i].nodeDiscover();
      }
  }
}

