import processing.serial.*;
import xbee.*;
import controlP5.*;

XBeeManager xbeeManager;

ArrayList foundProxPatches;
ArrayList foundVibePatches;
ArrayList foundAccelPatches;
ArrayList foundUndefPatches;

void setup() {
  this.foundProxPatches = new ArrayList();
  this.foundVibePatches = new ArrayList();
  this.foundAccelPatches = new ArrayList();
  this.foundUndefPatches = new ArrayList();

  xbeeManager = new XBeeManager(this);
  xbeeManager.debug = false;
  xbeeManager.init();
  String msg = xbeeManager.listToString();
  if (msg.isEmpty()) {
    if (xbeeManager.isScanning()) msg = "Scanning...";
    else msg = "No Xbee found.";
  }
  else if (!xbeeManager.isScanning())
    msg += ".";

  println(msg);
}

int mode = 0;

final String[] XBEE_PROX_1_NI = {"P1_PROX1", "P2_PROX1"};
final String[] XBEE_PROX_2_NI = {"P1_PROX2", "P2_PROX2"};
final String[] XBEE_ACCEL_NI = {"P1_ACCEL", "P2_ACCEL"};
final String[] XBEE_VIBE_NI = {"P1_VIBE", "P2_VIBE"};

Boolean discoversent = false;

Player[] players = null;

void draw()
{
  if (mode == 0 && xbeeManager.isInitialized()) {
    mode++;
    String msg = xbeeManager.listToString();
    println("XBee masters found: " + msg);
    players = new Player[2];
    players[0] = new Player(this);
    players[1] = new Player(this);
  }
  else if (mode == 1) {
    if (!discoversent) {
      foundProxPatches.clear();
      foundVibePatches.clear();
      foundAccelPatches.clear();
      foundUndefPatches.clear();
      
      initPlayer(0);
      initPlayer(1);
      discoversent = true;
    }
  }
}

void initPlayer(int player)
{
  println("Discovering...");
  
  players[player].initProxComm(XBEE_PROX_1_NI[player], XBEE_PROX_2_NI[player]);
  players[player].initAccelComm(XBEE_ACCEL_NI[player]);
  players[player].initVibeComm(XBEE_VIBE_NI[player]); 
  
  players[player].discoverPatches();
}

void xBeeDiscoverEvent(XBeeReader xbee)
{
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
          foundProxPatches.add(serial);
          println(" Found proximity patch: " + name + " (" + serial + ")");
          break;
        case 'V':
          foundVibePatches.add(serial);
          println(" Found vibration patch: " + name + " (" + serial + ")");
          break;
        case 'A':
          foundAccelPatches.add(serial);
          println(" Found acceleration patch: " + name + " (" + serial + ")");
          break;
        default:
          foundUndefPatches.add(serial);
          println(" Found undefined patch: " + name + " (" + serial + ")");
          break;
      }
    }
      
    // else if (buffer.length == XPan.CONFIG_ACK_LENGTH && buffer[0] == XPan.CONFIG_ACK_PACKET_TYPE)
    // {
    //   int myTurnLength = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF); 
    //   numConfigAcks++;
    //   println("Config Ack Received in Level Select, Turn Length is " + myTurnLength);
    // }
    
    // else if (buffer.length == XPan.VIBE_IN_PACKET_LENGTH && buffer[0] == XPan.VIBE_IN_PACKET_TYPE)
    // {
    //   int p = buffer[1];
    //   int direction = buffer[2];
    //   if (p<=8 && (state == LEVEL_SELECT_P1 || state == LEVEL_SELECT_SONG)) {
    //     switch (direction) {
    //       case 1:
    //         moveLeft();
    //         break;
    //       case 2:
    //         moveRight();
    //         break;
    //       default:
    //         doSelect();
    //         break;
    //     }
    //   }
    //   else if (p>8 && state == LEVEL_SELECT_P2) {
    //    switch (direction) {
    //       case 1:
    //         moveLeft();
    //         break;
    //       case 2:
    //         moveRight();
    //         break;
    //       default:
    //         doSelect();
    //         break;
    //    } 
    //   }
    // }
}

void xBeeEvent(XBeeReader xbee)
{
  if (mode == 0) {
    xbeeManager.xBeeEvent(xbee);
  }
  else if (mode == 1) {
    xBeeDiscoverEvent(xbee);
  }
}

