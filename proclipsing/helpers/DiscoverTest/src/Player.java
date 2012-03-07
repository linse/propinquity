import java.util.ArrayList;
import xbee.*;

import processing.core.PApplet;


public class Player {
    
  // types of xpans
  final int PROX = 0;
  final int ACCEL = 1;
  final int VIBE = 2;
  // number of the xpan's local xbees in prox, accel, vibe xpan of this player
  final int[] XPANS_LOCAL_XBEES = {2,1,1};
  // names of the xpans
  final String[] XPAN_NAMES = {"proximity", "acceleration", "vibration"};
  // the actual xpans of this player
  ArrayList<XPan[]> xpans = new ArrayList<XPan[]>();


  public Player() {
    for (int xpanType = PROX; xpanType <= VIBE; xpanType++)
      xpans.add(new XPan[XPANS_LOCAL_XBEES[xpanType]]);
  }

  void init(ArrayList<String[]> nis) {
    // init all xpans
    for (int xpanType = PROX; xpanType <= VIBE; xpanType++) {
      for (int i = 0; i < XPANS_LOCAL_XBEES[xpanType]; i++) {
        initXPAN(nis.get(xpanType)[i], xpanType, i);
      }
    }
  }
  
  void initXPAN(String localXbeeNI, int xpanType, int xpanNumber) {
    if (localXbeeNI == null) 
      return;

    XBeeReader xbee = XBeeManager.instance().reader(localXbeeNI);
    if (xbee != null) {
      xpans.get(xpanType)[xpanNumber] = new XPan(xbee);
      DiscoverTest.game.println("Xbee network for " + XPAN_NAMES[xpanType] + ": local Xbee " 
      + localXbeeNI + " connected.");
    }
    else {
      System.err.println("No Xbee network for " + XPAN_NAMES[xpanType] + ": " 
      + localXbeeNI + " not there (yet).");
    }
  }

  // detecting which remote xbees respond
  public void discoverRemoteXbees() {
    //println("Discover remote xbees...");
    
    for (int xpanType = PROX; xpanType <= VIBE; xpanType++) {
      for (int i = 0; i < XPANS_LOCAL_XBEES[xpanType]; i++){
        if (xpans.get(xpanType)[i] != null) {
          DiscoverTest.game.println("Discover " + XPAN_NAMES[xpanType]+ " " + (i+1) +" at "+DiscoverTest.game.millis()+":");
          xpans.get(xpanType)[i].nodeDiscover();
        }
      }
    }

  }
}

