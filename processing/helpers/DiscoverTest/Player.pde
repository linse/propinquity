
public class Player {
  
  final int PROX = 0;
  final int ACCEL = 1;
  final int VIBE = 2;
  
  // number of local xbees in prox, accel, vibe xpan of this player
  final int[] XPANS_LOCAL_XBEES = {1,1,1};
  final String[] XPAN_NAMES = {"proximity", "acceleration", "vibration"};

  PApplet parent;

  XPan[] xpansProx;
  XPan[] xpansAccel;
  XPan[] xpansVibe;

  public Player(PApplet p) {
    parent = p;
    xpansProx = new XPan[XPANS_LOCAL_XBEES[PROX]];
    xpansAccel = new XPan[XPANS_LOCAL_XBEES[ACCEL]];
    xpansVibe = new XPan[XPANS_LOCAL_XBEES[VIBE]];
  }


  void initProxComm(String ni1, String ni2) {
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
  }

  void initAccelComm(String ni) {
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

  void initVibeComm(String ni) {
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
  public void discoverRemoteXbees() {
    println("Discover patches...");
    for (int i = 0; i < XPANS_LOCAL_XBEES[PROX]; i++)
      if (xpansProx[i] != null) {
        println("Discover proximity " + (i+1));
        xpansProx[i].nodeDiscover();
      }

    for (int i = 0; i < XPANS_LOCAL_XBEES[ACCEL]; i++)
      if (xpansAccel[i] != null) {
        println("Discover acceleration " + (i+1));
        xpansAccel[i].nodeDiscover();
      }

    for (int i = 0; i < XPANS_LOCAL_XBEES[VIBE]; i++)
      if (xpansVibe[i] != null) {
        println("Discover vibration " + (i+1));
        xpansVibe[i].nodeDiscover();
      }
  }
}

