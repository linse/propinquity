import processing.serial.*;
import xbee.*;

final int GAME_STATE_INITIAL = 0;
final int GAME_STATE_CHECK_SERIAL = 1;
final int GAME_STATE_CHECK_XPAN = 2;
final int GAME_STATE_SEND_DISCOVER = 3;
final int GAME_STATE_RECEIVE_DISCOVER = 4;
final int GAME_STATE_CHECKS_DONE = 5;
int gameState = GAME_STATE_CHECK_SERIAL;

final int XBEE_DISCOVER_TIMEOUT = 5 * 1000; // 5 sec

XBeeManager xbeeManager;
Player[] players;

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
}


void draw() {
  int startDiscover = 0; // time when discover package is sent
  
  // scan test for local xbees via serial
  if (gameState == GAME_STATE_CHECK_SERIAL && xbeeManager.hasAllNIs()) {
    //println("Local XBees found: " + xbeeManager.getNodeIDs() + ".");
    gameState++;
  }
  // set up networks for all local xbees
  else if (gameState == GAME_STATE_CHECK_XPAN) {
    initPlayers();
    gameState++;
  }
  // discover remote xbees
  else if (gameState == GAME_STATE_SEND_DISCOVER) {
    println("Scanning for remote xbees...");
    println("Player 1:");
    players[0].discoverRemoteXbees();
    println("Player 2:");
    players[1].discoverRemoteXbees();
    startDiscover = millis();
    gameState++;
  }
  else if (gameState == GAME_STATE_RECEIVE_DISCOVER) {
    if (millis()-startDiscover <= XBEE_DISCOVER_TIMEOUT) {
      print(".");
      delay(1000); // 1 sec
    }
    else {
      gameState++;
      // TODO: record - which xbees have we??
    }
  }
  else if (gameState == GAME_STATE_CHECKS_DONE) {
    println("");
    println("Checks done");
    printDiscovered();
    exit();
  }
}

void printDiscovered() {
  println("Discovered proximity patches");
  for(int i=0; i<foundProxs.size() ; i++)
    println(foundProxs.get(i));
  println("Discovered vibration gloves");
  for(int i=0; i<foundVibes.size() ; i++)
    println(foundVibes.get(i));
  println("Discovered accelerometer anklets");
  for(int i=0; i<foundAccels.size() ; i++)
    println(foundAccels.get(i));
  println("Discovered undefined remote xbee senders");
  for(int i=0; i<foundUndefs.size() ; i++)
    println(foundUndefs.get(i));
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
  if (gameState == GAME_STATE_CHECK_SERIAL) {
    xbeeManager.xBeeEvent(xbee);
  }
  else if (gameState == GAME_STATE_RECEIVE_DISCOVER) {
    xBeeDiscoverEvent(xbee);
  }
}

