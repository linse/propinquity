// extra class to use multithreading:
// stop thread to wait for xbee response via serial
// so far deals only with _local_ xbees connected via serial

public class XBeeManager implements Runnable {

  final int XBEE_SERIAL_BAUDRATE = 115200; // bits/s
  final int XBEE_SERIAL_RESPONSE_TIMEOUT = 5000; // 5 sec
  
  Thread thread;
  String nodeID;
  boolean hasNI;
  boolean hasAllLocalNIs;
  
  PApplet parent;
  HashMap nodeIDAndSerialPort;
  
  public XBeeManager(PApplet p) {
    parent = p;
    nodeIDAndSerialPort = new HashMap();
    
    if (thread != null) 
      return;
    thread = new Thread(this);
    thread.start();
  }
  
  public void run() {

    String[] serialPortList = Serial.list();
    String osName = System.getProperty("os.name");
    
    // for all serial ports
    for (int i=0; i<serialPortList.length; i++) {
      
      // on a Mac, filter out the ports that don't start by tty.usbserial
      if ((osName.indexOf("Mac") != -1) && (serialPortList[i].indexOf("tty.usbserial") == -1))
        continue;
      readSerialPort(serialPortList[i]);

    }
    hasAllLocalNIs = true;
    
    //clear thread
    thread = null; 
  }
  
  public boolean hasAllLocalNIs() {
    return hasAllLocalNIs;
  }
  
  public void readSerialPort(String port) {
      println(" Connecting to port: " + port);
      Serial serial = new Serial(parent, port, XBEE_SERIAL_BAUDRATE); 
      
      // get node identifier from local xbee
      hasNI = false;  
      XBeeReader localXbee = new XBeeReader(parent, serial);
      // the following lines give us debugging output of the xbee library - TODO
      localXbee.startXBee();      
      localXbee.getNI();
      
      // wait for xbee event until timeout, break if we got it
      int start = millis();
      while (!hasNI && millis() < start+XBEE_SERIAL_RESPONSE_TIMEOUT) { 
        try { 
          Thread.sleep(1);
          //print(".");
        }
        catch(InterruptedException ie) {
          ie.printStackTrace(); 
        }  
        if (hasNI) {
          println(nodeID);
          nodeIDAndSerialPort.put(nodeID, port);
          break;
        }
      }
      
      //clean up      
      localXbee.stopXBee();
      
      // Stop program if we still has no xbee after timeout
      if (!hasNI) {
        println("Serial timeout and no local XBee found.");
        //exit();
      }
  }  
  
  //Get a XBeeReader of the XBee with the matching NodeIdentifier (NI)
  public XBeeReader reader(String ni) {
    String serialPort = (String) nodeIDAndSerialPort.get(ni);
    if (serialPort == null) 
      return null;
    XBeeReader xbee = new XBeeReader(parent, new Serial(parent, serialPort, XBEE_SERIAL_BAUDRATE));
    return xbee;
  }
  
  public void foundLocalXbeeEvent(XBeeReader xbee) { 
    XBeeDataFrame data = xbee.getXBeeReading();
    data.parseXBeeRX16Frame();
    
    int[] buffer = data.getBytes();
    nodeID = "";
    for(int i = 0; i < buffer.length; i++) {
      nodeID += (char) buffer[i];
    }
    
    hasNI = true;
  }  
  
  
void foundRemoteXbeeEvent(XBeeReader xbee) {
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
   
  public String getNodeIDs() {
    Set nodeIDs = nodeIDAndSerialPort.keySet();
    Iterator it = nodeIDs.iterator();
    String nodeIDString = "";
    
    while (it.hasNext())    
      nodeIDString += (String)it.next() + ", ";
    
    if (nodeIDString.length() < 2) 
      return "";
    return nodeIDString.substring(0, nodeIDString.length()-2);
  }
    
}
