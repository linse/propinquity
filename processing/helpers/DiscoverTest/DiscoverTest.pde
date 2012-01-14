import processing.serial.*;
import xbee.*;

int mode = 0;

XBeeManager xbeeManager;

boolean discoversent = false;
Player[] players;
ArrayList foundProxs;
ArrayList foundVibes;
ArrayList foundAccels;
ArrayList foundUndefs;

void setup() {
  foundProxs = new ArrayList();
  foundVibes = new ArrayList();
  foundAccels = new ArrayList();
  foundUndefs = new ArrayList();

  // 1. scan test for local xbees 
  xbeeManager = new XBeeManager(this);

  // 2. discover test for remote xbees
  players = new Player[2];
  players[0] = new Player(this);
  players[1] = new Player(this);
  discoversent = false;
}

//
//
//Boolean discoversent = false;
//Player[] players = null;

void draw() {
  // scan test for local xbees
  if (mode == 0 && xbeeManager.hasAllNIs()) {
    println("Local XBees found: " + xbeeManager.getNodeIDs() + ".");
    mode++;
  }
  // discover test for remote xbees
  else if (mode == 1 && !discoversent) {
      foundProxs.clear();
      foundVibes.clear();
      foundAccels.clear();
      foundUndefs.clear();
      
      initPlayer(0);
      initPlayer(1);
      discoversent = true;
  }
}

void initPlayer(int player) {
  println("Discovering...");
  
  // TODO: hard coded, we want to have this from xbee manager
  final String[] XBEE_PROX_1_NI = {"P1_PROX1", "P2_PROX1"};
  final String[] XBEE_PROX_2_NI = {"P1_PROX2", "P2_PROX2"};
  final String[] XBEE_ACCEL_NI = {"P1_ACCEL", "P2_ACCEL"};
  final String[] XBEE_VIBE_NI = {"P1_VIBE", "P2_VIBE"};

  players[player].initProxComm(XBEE_PROX_1_NI[player], XBEE_PROX_2_NI[player]);
  players[player].initAccelComm(XBEE_ACCEL_NI[player]);
  players[player].initVibeComm(XBEE_VIBE_NI[player]); 
  
  players[player].discoverRemoteXbees();
}

void xBeeDiscoverEvent(XBeeReader xbee) {
    XBeeDataFrame data = xbee.getXBeeReading();
    data.parseXBeeRX16Frame();
    
    int[] buffer = data.getBytes();
    
    if (buffer.length > 11) {
      //check first letter of NI parameter
      String serial = "";
      for(int i = 3; i < 11; i++)
        serial += hex(buffer[i], 2);
      String name = "";
      for(int i = 11; i < buffer.length; i++)
        name += char(buffer[i]);
      
      switch (buffer[11]) {
        case 'P':
          foundProxs.add(serial);
          println(" Found proximity patch: " + name + " (" + serial + ")");
          break;
        case 'V':
          foundVibes.add(serial);
          println(" Found vibration patch: " + name + " (" + serial + ")");
          break;
        case 'A':
          foundAccels.add(serial);
          println(" Found acceleration patch: " + name + " (" + serial + ")");
          break;
        default:
          foundUndefs.add(serial);
          println(" Found undefined patch: " + name + " (" + serial + ")");
          break;
      }
    }
}

void xBeeEvent(XBeeReader xbee) {
  if (mode == 0) {
    xbeeManager.xBeeEvent(xbee);
  }
  else if (mode == 1) {
    xBeeDiscoverEvent(xbee);
  }
}

