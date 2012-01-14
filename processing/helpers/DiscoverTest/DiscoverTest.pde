import processing.serial.*;
import xbee.*;

final int MODE_CHECK_SERIAL = 0;//,
final int MODE_CHECK_XPAN = 1;
final int MODE_CHECK_REMOTE = 2;
int mode = MODE_CHECK_SERIAL;

XBeeManager xbeeManager;

boolean initPlayers = false;
Player[] players;

boolean discoversent = false;
ArrayList foundProxs;
ArrayList foundVibes;
ArrayList foundAccels;
ArrayList foundUndefs;

void setup() {

  // 1. scan test for local xbees 
  xbeeManager = new XBeeManager(this);

  // 2. discover test for remote xbees
  foundProxs = new ArrayList();
  foundVibes = new ArrayList();
  foundAccels = new ArrayList();
  foundUndefs = new ArrayList();
  initPlayers = false;
  discoversent = false;
}


void draw() {
  // scan test for local xbees via serial
  if (mode == MODE_CHECK_SERIAL && xbeeManager.hasAllNIs()) {
    //println("Local XBees found: " + xbeeManager.getNodeIDs() + ".");
    mode++;
  }
  // set up networks for all local xbees
  else if (mode == MODE_CHECK_XPAN && !initPlayers && !discoversent) {
    initPlayers();
    initPlayers = true;
    println("hallo");
    mode++;
  }
  // discover remote xbees
  else if (mode == MODE_CHECK_REMOTE && !discoversent) {
    println("Scanning for remote xbees...");
    println("Player 1:");
    players[0].discoverRemoteXbees();
    println("Player 2:");
    players[1].discoverRemoteXbees();
    discoversent = true;
  }
}

void initPlayers() {
  players = new Player[2];
  players[0] = new Player(this);
  players[1] = new Player(this);
  
  // TODO: Node IDs are hard coded, we want this from the scan!!
  // node identifyers of local xbees for player 1 
  ArrayList<String[]> NIS_PLAYER1 = new ArrayList<String[]>();
  String[] proxNIs = {"P1_PROX1","P1_PROX2"};
  String[] accelNI = {"P1_ACCEL"};
  String[] vibeNI = {"P1_VIBE"};
  NIS_PLAYER1.add(proxNIs);
  NIS_PLAYER1.add(accelNI);
  NIS_PLAYER1.add(vibeNI);
  // player 2
  ArrayList<String[]> NIS_PLAYER2 = new ArrayList<String[]>();
  String[] proxNIs2 = {"P2_PROX1","P2_PROX2"};
  String[] accelNI2 = {"P2_ACCEL"};
  String[] vibeNI2 = {"P2_VIBE"};
  NIS_PLAYER2.add(proxNIs2);
  NIS_PLAYER2.add(accelNI2);
  NIS_PLAYER2.add(vibeNI2);
  
  players[0].init(NIS_PLAYER1);
  players[1].init(NIS_PLAYER2);
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
          println(" Found proximity patch: " + name + " (" + serial + ") at "+millis());
          break;
        case 'V':
          foundVibes.add(serial);
          println(" Found vibration patch: " + name + " (" + serial + ") at "+millis());
          break;
        case 'A':
          foundAccels.add(serial);
          println(" Found acceleration patch: " + name + " (" + serial + ") at "+millis());
          break;
        default:
          foundUndefs.add(serial);
          println(" Found undefined patch: " + name + " (" + serial + ") at "+millis());
          break;
      }
    }
}

void xBeeEvent(XBeeReader xbee) {
  if (mode == MODE_CHECK_SERIAL) {
    xbeeManager.xBeeEvent(xbee);
  }
  else if (mode == MODE_CHECK_REMOTE) {
    xBeeDiscoverEvent(xbee);
  }
}

