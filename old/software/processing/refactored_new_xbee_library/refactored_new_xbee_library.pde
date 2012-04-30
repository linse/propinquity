// used for communication via xbee api
import processing.serial.*; 

// xbee api libraries available at http://code.google.com/p/xbee-api/
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import java.util.concurrent.*;
import processing.opengl.*;

String version = "0.9";

static final int NUM_PLAYERS=2;
static final int XPANS_PER_PLAYER=3;
static final int LOCAL_XBEES=6;
static final int SERIAL_BAUDRATE=115200;

// make an array list of thermometer objects for display
//ArrayList thermometers = new ArrayList();
// create a font for display
PFont font;
Player[] players;

void setup() {
  //size(800, 600); // screen size
  size(1024, 768, OPENGL);
  smooth(); // anti-aliasing for graphic display
  font =  loadFont("SansSerif-10.vlw");
  textFont(font); // use the font for text
  players = new Player[NUM_PLAYERS];

  // required by the xbee api library, needs to be in your data folder. 
  PropertyConfigurator.configure(dataPath("")+"log4j.properties"); 
  
  ArrayList serialPorts = getLocalXbeeSerialPorts();
  if (serialPorts.size()==0) {
    println("No local Xbees found. ");
  }
  else {
    println("Xbee serial ports:");
    println(serialPorts);
    
    HashMap niToPortMap = getNIToPortMap(serialPorts);
    if (niToPortMap.size()!=LOCAL_XBEES) {
      println("Not all local xbees found! Are they plugged in?");
      return;
    }
    else {
      println("Local xbees: "+niToPortMap);
      initPlayers(niToPortMap);
    }
    
//    for (int i=0; i<serialPorts.size(); i++) {
//      String port = serialPorts.get(i).toString();
//      xpan = new Xpan(port);
//    }
  }
}



ArrayList getLocalXbeeSerialPorts() {
  
  String[] allSerialPorts = Serial.list();
  String osName = System.getProperty("os.name");
  ArrayList localXbeeSerialPorts = new ArrayList();
  
  for (int i=0; i<allSerialPorts.length; i++) {
    // on mac it _has_ to be an usbserial device
    if (!((osName.indexOf("Mac") != -1) 
    && (allSerialPorts[i].indexOf("tty.usbserial") == -1))) {
      localXbeeSerialPorts.add(allSerialPorts[i]);
    }
  }
  if (localXbeeSerialPorts.size()==0) { // TODO throw exception?
    println("** Error opening serial ports. **");
    println("Are your local XBees plugged in to your computer?");
    exit();
  }
  return localXbeeSerialPorts;
}


// map NI to serial port so that we can assign 
// ports to players!
HashMap getNIToPortMap(ArrayList serialPorts) {
  HashMap niToPortMap = new HashMap();
  try {
    for (int i=0; i<serialPorts.size(); i++) {
      String port = serialPorts.get(i).toString();
      XBee xbee = new XBee();
      xbee.open(port, SERIAL_BAUDRATE);
      // Timeout 70000 if no other devices and not chained.
      AtCommandResponse response = (AtCommandResponse) xbee.sendSynchronous(new AtCommand("NI"), 70000);   
      if (response.isOk()) {
        int[] bytes = response.getValue();
        StringBuffer buffer = new StringBuffer();
        for (int b : bytes) {
          buffer.append((char) b);
        }
        String NI = buffer.toString();
        niToPortMap.put(NI,port);
        println(NI+":"+port);
      }
      xbee.close();
    }
  }
  catch (XBeeException e) {
    //println("Could not open serial port: "+e);
  }
  return niToPortMap;
}

// init players without known port map - makes no sense
//void initPlayers() {
//  for (int p=0; p<NUM_PLAYERS) {
//    players[p] = new Player();
//  }
//}

// init players according to serial port map
// TODO too generic ;-)
void initPlayers(HashMap niToPortMap) {
  Object[] keys = niToPortMap.keySet().toArray();
  Arrays.sort(keys);
  if (keys.length!=NUM_PLAYERS*XPANS_PER_PLAYER) {
    println("Number of local Xbees does not fit to number of players and networks (xpans) per player!");
    exit();
  }
  // make xpans for players from NI / serial port pairs
  int player = 0;
  int xpan = 0;
  players[player] = new Player();
  for (int i = 0; i < keys.length && xpan < XPANS_PER_PLAYER && player < NUM_PLAYERS; i++) {
    players[player].xpans[xpan++] = new Xpan((String)niToPortMap.get(keys[i]));
    if (xpan==XPANS_PER_PLAYER && player!=NUM_PLAYERS-1) {// if player has all xpans and is not last player
      player++;
      players[player] = new Player();
      xpan = 0;
    }
  }
  
}


// defines the data object
class ProximityData {
  int value;
  String address;
}


void draw() {
  ProximityData data = new ProximityData(); // create a data object
  data = getProximityData(); // put data into the data object  
  if (millis() > 10000) {
  players[1].broadcastVibe(); 
  //players[0].broadcastVibe();
  }
}



//// queries the XBee for incoming proximity data frames 
//// and parses them into a data object
ProximityData getProximityData() {

  ProximityData data = new ProximityData();
//  int value = -1;      // returns an impossible value if there's an error
//  String address = ""; // returns a null value if there's an error

  try {		
    //xpan.broadcastVibe(500);
    

    // we wait here until a packet is received.
    //XBeeResponse response = xpan.localXbee.getResponse();
    // uncomment next line for additional debugging information
    //println("Received response " + response.toString());
//    println("got data response from xbee"+response.getApiId());
//
//    // check that this frame is a valid I/O sample, then parse it as such
//    if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE 
//      && !response.isError()) {
//      ZNetRxIoSampleResponse ioSample = 
//        (ZNetRxIoSampleResponse)(XBeeResponse) response;
//
//      // get the sender's 64-bit address
//      int[] addressArray = ioSample.getRemoteAddress64().getAddress();
//      // parse the address int array into a formatted string
//      String[] hexAddress = new String[addressArray.length];
//      for (int i=0; i<addressArray.length;i++) {
//        // format each address byte with leading zeros:
//        hexAddress[i] = String.format("%02x", addressArray[i]);
//      }
//
//      // join the array together with colons for readability:
//      String senderAddress = join(hexAddress, ":"); 
//      print("Sender address: " + senderAddress);
//      data.address = senderAddress;
//      // get the value of the first input pin
//      value = ioSample.getAnalog0();
//      print(" analog value: " + value ); 
//      data.value = value;
//    }
//    else if (!response.isError()) {
//      println("Got error in data frame");
//    }
//    else {
//      println("Got non-i/o data frame");
//    }

//  if (packet.length == XPan.PROX_IN_PACKET_LENGTH 
//       && packet[0] == XPan.PROX_IN_PACKET_TYPE) { 
//    int patch = (packet[1] >> 1);                
//    int player = getPlayerIndexForPatch(patch);
//    
//    if (player != -1) {
//      // TODO packet[1] contains boolean touched, which is obsolete
//      int step = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
//      //println(step);
//      int proximity = ((packet[4] & 0xFF) << 8) | (packet[5] & 0xFF);
//      println("Patch "+patch+" player "+player+" sent prox "+proximity);
//      //players[0].broadcastVibe();
//    }
//    else
//      System.err.println("Error: received a packet from patch '"+ patch + 
//      "', which is not assigned to a player");
//  }

  }
//  catch (XBeeException e) {
//    println("Error receiving response: " + e);
//  }
  finally {
  }
  return data; // sends the data back to the calling function
}



// TODO do we need this? was originally hard coded anyway
public int getPlayerIndexForPatch(int patch) {
  return 0;
}






//// draw loop executes continuously
//void draw() {
//  background(224); // draw a light gray background
//  // report any serial port problems in the main window
//  if (error == 1) {
//    fill(0);
//    text("** Error opening XBee port: **\n"+
//      "Is your XBee plugged in to your computer?\n" +
//      "Did you set your COM port in the code near line 20?", width/3, height/2);
//  }	
//  ProximityData data = new ProximityData(); // create a data object
//  data = getData(); // put data into the data object
//  //data = getSimulatedData(); // uncomment this to use random data for testing
//
//  // check that actual data came in:
//  if (data.value >=0 && data.address != null) { 
//
//    // check to see if a thermometer object already exists for this sensor
//    int i;
//    boolean foundIt = false;
//    for (i=0; i <thermometers.size(); i++) {
//      if ( ((Thermometer) thermometers.get(i)).address.equals(data.address) ) {
//        foundIt = true;
//        break;
//      }
//    }
//
//    // process the data value into a Celsius temperature reading for
//    // LM335 with a 1/3 voltage divider
//    //   (value as a ratio of 1023 times max ADC voltage times 
//    //    3 (voltage divider value) divided by 10mV per degree
//    //    minus zero Celsius in Kevin)
//    float temperatureCelsius = (data.value/1023.0*1.2*3.0*100)-273.15;
//    println(" temp: " + round(temperatureCelsius) + "˚C");
//
//    // update the thermometer if it exists, otherwise create a new one
//    if (foundIt) {
//      ((Thermometer) thermometers.get(i)).temp = temperatureCelsius;
//    }
//    else if (thermometers.size() < 10) {
//      thermometers.add(new Thermometer(data.address,35,450,
//      (thermometers.size()) * 75 + 40, 20));
//      ((Thermometer) thermometers.get(i)).temp = temperatureCelsius;
//    }
//
//    // draw the thermometers on the screen
//    for (int j =0; j<thermometers.size(); j++) {
//      ((Thermometer) thermometers.get(j)).render();
//    }
//  }
//} // end of draw loop
//
//
//
//
//
//// defines the thermometer objects
//class Thermometer {
//  int sizeX, sizeY, posX, posY;
//  int maxTemp = 40; // max of scale in degrees Celsius
//  int minTemp = -10; // min of scale in degress Celcisu
//  float temp; // stores the temperature locally
//  String address; // stores the address locally
//
//  Thermometer(String _address, int _sizeX, int _sizeY, 
//  int _posX, int _posY) { // initialize thermometer object
//    address = _address;
//    sizeX = _sizeX;
//    sizeY = _sizeY;
//    posX = _posX;
//    posY = _posY;
//  }
//
//  void render() { // draw thermometer on screen
//    noStroke(); // remove shape edges
//    ellipseMode(CENTER); // center bulb
//    float bulbSize = sizeX + (sizeX * 0.5); // determine bulb size
//    int stemSize = 30; // stem augments fixed red bulb 
//    // to help separate it from moving mercury
//    // limit display to range
//    float displayTemp = round( temp);
//    if (temp > maxTemp) {
//      displayTemp = maxTemp + 1;
//    }
//    if ((int)temp < minTemp) {
//      displayTemp = minTemp;
//    }
//    // size for variable red area:
//    float mercury = ( 1 - ( (displayTemp-minTemp) / (maxTemp-minTemp) )); 
//    // draw edges of objects in black
//    fill(0); 
//    rect(posX-3,posY-3,sizeX+5,sizeY+5); 
//    ellipse(posX+sizeX/2,posY+sizeY+stemSize, bulbSize+4,bulbSize+4);
//    rect(posX-3, posY+sizeY, sizeX+5,stemSize+5);
//    // draw grey mercury background
//    fill(64); 
//    rect(posX,posY,sizeX,sizeY);
//    // draw red areas
//    fill(255,16,16);
//
//    // draw mercury area:
//    rect(posX,posY+(sizeY * mercury), 
//    sizeX, sizeY-(sizeY * mercury));
//
//    // draw stem area:
//    rect(posX, posY+sizeY, sizeX,stemSize); 
//
//    // draw red bulb:
//    ellipse(posX+sizeX/2,posY+sizeY + stemSize, bulbSize,bulbSize); 
//
//    // show text
//    textAlign(LEFT);
//    fill(0);
//    textSize(10);
//
//    // show sensor address:
//    text(address, posX-10, posY + sizeY + bulbSize + stemSize + 4, 65, 40);
//
//    // show maximum temperature: 
//    text(maxTemp + "˚C", posX+sizeX + 5, posY); 
//
//    // show minimum temperature:
//    text(minTemp + "˚C", posX+sizeX + 5, posY + sizeY); 
//
//    // show temperature:
//    text(round(temp) + " ˚C", posX+2,posY+(sizeY * mercury+ 14));
//  }
//}
//
//// used only if getSimulatedData is uncommented in draw loop
////
//ProximityData getSimulatedData() {
//  ProximityData data = new ProximityData();
//  int value = int(random(750,890));
//  String address = "00:13:A2:00:12:34:AB:C" + str( round(random(0,9)) );
//  data.value = value;
//  data.address = address;
//  delay(200);
//  return data;
//}
//
//// queries the XBee for incoming I/O data frames 
//// and parses them into a data object
//ProximityData getData() {
//
//  ProximityData data = new ProximityData();
//  int value = -1;      // returns an impossible value if there's an error
//  String address = ""; // returns a null value if there's an error
//
//  try {		
//    // we wait here until a packet is received.
//    XBeeResponse response = xbee.getResponse();
//    // uncomment next line for additional debugging information
//    //println("Received response " + response.toString());
//    println("got data response from xbee"+response.getApiId());
//
//    // check that this frame is a valid I/O sample, then parse it as such
//    if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE 
//      && !response.isError()) {
//      ZNetRxIoSampleResponse ioSample = 
//        (ZNetRxIoSampleResponse)(XBeeResponse) response;
//
//      // get the sender's 64-bit address
//      int[] addressArray = ioSample.getRemoteAddress64().getAddress();
//      // parse the address int array into a formatted string
//      String[] hexAddress = new String[addressArray.length];
//      for (int i=0; i<addressArray.length;i++) {
//        // format each address byte with leading zeros:
//        hexAddress[i] = String.format("%02x", addressArray[i]);
//      }
//
//      // join the array together with colons for readability:
//      String senderAddress = join(hexAddress, ":"); 
//      print("Sender address: " + senderAddress);
//      data.address = senderAddress;
//      // get the value of the first input pin
//      value = ioSample.getAnalog0();
//      print(" analog value: " + value ); 
//      data.value = value;
//    }
//    else if (!response.isError()) {
//      println("Got error in data frame");
//    }
//    else {
//      println("Got non-i/o data frame");
//    }
//  }
//  catch (XBeeException e) {
//    println("Error receiving response: " + e);
//  }
//  return data; // sends the data back to the calling function
//}

