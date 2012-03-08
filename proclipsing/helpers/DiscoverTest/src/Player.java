import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xbee.*;

import processing.core.PApplet;


public class Player {
    
  // types of xpans
  static final int PROX = 0;
  static final int ACCEL = 1;
  static final int VIBE = 2;
  // number of the xpan's local xbees in prox, accel, vibe xpan of this player
  final int[] XPANS_LOCAL_XBEES = {2,1,1};
  // names of the xpans
  final String[] XPAN_NAMES = {"proximity", "acceleration", "vibration"};
  // the actual xpans of this player
  XPan vibeXpan;
public XPan proxXpan;
  
  // map from patch address to XPan
  static public Map<Integer, XPan> address_to_xpan;
  // Map from NI to patch address
  static public Map<String, Integer> ni_to_address;
  
  public Player() {
	  if (ni_to_address == null) {
		  ni_to_address = new HashMap<String, Integer>();
		  ni_to_address.put("PROX1_PLAYER1", 1);
		  ni_to_address.put("PROX2_PLAYER1", 2);
		  ni_to_address.put("PROX3_PLAYER1", 3);
		  ni_to_address.put("PROX4_PLAYER1", 4);
		  ni_to_address.put("VIBE1_PLAYER1", 5);
		  ni_to_address.put("VIBE2_PLAYER1", 6);
		  ni_to_address.put("PROX1_PLAYER2", 9);
		  ni_to_address.put("PROX2_PLAYER2", 10);
		  ni_to_address.put("PROX3_PLAYER2", 11);
		  ni_to_address.put("PROX4_PLAYER2", 12);
		  ni_to_address.put("VIBE1_PLAYER2", 13);
		  ni_to_address.put("VIBE2_PLAYER2", 14);
	  }
  }

  static void init()
  {
	  address_to_xpan = new HashMap<Integer, XPan>();
	  String localXbeeNI = "P1_PROX1";
	  XBeeReader xbee = XBeeManager.instance().reader(localXbeeNI);
	  XPan xpan;
	  if (xbee != null) {
		   xpan = new XPan(xbee);
		  address_to_xpan.put(1, xpan);
		  address_to_xpan.put(2, xpan);
	  }
	  localXbeeNI = "P1_PROX2";
	  xbee = XBeeManager.instance().reader(localXbeeNI);
	  if (xbee != null) {
	   xpan = new XPan(xbee);
	  address_to_xpan.put(3, xpan);
	  address_to_xpan.put(4, xpan);
	  }
	   localXbeeNI = "P1_VIBE";
	   xbee = XBeeManager.instance().reader(localXbeeNI);
		  if (xbee != null) {
	   xpan = new XPan(xbee);
	  address_to_xpan.put(5, xpan);
	  address_to_xpan.put(6, xpan);
		  }
	   localXbeeNI = "P2_PROX1";
	   xbee = XBeeManager.instance().reader(localXbeeNI);
		  if (xbee != null) {
	   xpan = new XPan(xbee);
	  address_to_xpan.put(9, xpan);
	  address_to_xpan.put(10, xpan);
		  }
	   localXbeeNI = "P2_PROX2";
	   xbee = XBeeManager.instance().reader(localXbeeNI);
		  if (xbee != null) {
	   xpan = new XPan(xbee);
	  address_to_xpan.put(11, xpan);
	  address_to_xpan.put(12, xpan);
		  }
	   localXbeeNI = "P2_VIBE";
	   xbee = XBeeManager.instance().reader(localXbeeNI);
		  if (xbee != null) {
	   xpan = new XPan(xbee);
	  address_to_xpan.put(13, xpan);
	  address_to_xpan.put(14, xpan);
		  }
	  
  }
  
  // detecting which remote xbees respond
  public void discoverRemoteXbees() {
    //println("Discover remote xbees...");
    
	if (proxXpan != null) proxXpan.nodeDiscover();
	if (vibeXpan != null) vibeXpan.nodeDiscover();

  }

public XPan getProxXpan() {
	// TODO Auto-generated method stub
	return proxXpan;
}

public XPan getVibeXpan() {
	return vibeXpan;
}
}

