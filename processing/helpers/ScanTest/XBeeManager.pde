public class XBeeManager implements Runnable {
  
  PApplet parent;
  HashMap ports;
  Thread thread;
  boolean done;
   
  boolean initialized;
  boolean initFound;
  String initNodeId;
  
  final String XBEE_PORTS_FILE = "xbees.lst";
  final int XBEE_BAUDRATE = 115200;
  final int XBEE_RESPONSE_TIMEOUT = 5000;
  
  public XBeeManager(PApplet p) {
    parent = p;
    ports = new HashMap();
    initialized = false;
    done = false;
  }
  
  public void init() {
    //load from file if it exists
    if (new File(dataPath(XBEE_PORTS_FILE)).exists()) {
      load();
    }
    //autodetect
    else {
      scan();
    }
  }
  
  public void scan() {
    if (thread != null) return;
    thread = new Thread(this);
    thread.start();
  }
  
  public boolean isScanning() { 
    return thread != null; 
  }
  
  public void run() {
    ports = new HashMap();
    initialized = false;

    String[] initPorts = Serial.list();
    long initLastCheck;
    println("Initializing XBees...");
    
    String osName = System.getProperty("os.name");
    
    for(int initPortIndex = 0; initPortIndex < initPorts.length; initPortIndex++) {
      
      //if we are on a Mac, then filter out the ports that don't start by tty.usbserial
      if ((osName.indexOf("Mac") != -1) && (initPorts[initPortIndex].indexOf("tty.usbserial") == -1)) {
        //        println(" Skipping port: " + initPorts[initPortIndex]);
        continue;
      }
      
      println(" Connecting to port: " + initPorts[initPortIndex] + " ... ");
      Serial serial = new Serial(parent, initPorts[initPortIndex], XBEE_BAUDRATE);
      XBeeReader xbee = new XBeeReader(parent, serial);
      xbee.startXBee();      
      xbee.getNI();

      initLastCheck = millis();
      initFound = false;
      
      // TODO do with callback or without events?
      // sleep a bit so the xbee event has time to happen  
      try { 
        Thread.sleep(30); // 20 is too short
      }
      catch(InterruptedException ie) {
        ie.printStackTrace(); 
      }  
      
      if (initFound) {
        println(initNodeId);
        ports.put(initNodeId, initPorts[initPortIndex]);
      }
      else {
        println("no XBee found");
      }

      //clean up      
      xbee.stopXBee();
    }
  
    //done
    initialized = true;
    
    //clear thread
    thread = null;
  }
  
  public boolean isInitialized() { return initialized; }
  
  //Get a XBeeReader of the XBee with the matching NodeIdentifier (NI)
  public XBeeReader reader(String ni) {
    String port = (String)ports.get(ni);
    if (port == null) return null;
    XBeeReader xbee = new XBeeReader(parent, new Serial(parent, port, XBEE_BAUDRATE));
    return xbee;
  }
  
  public void xBeeEvent(XBeeReader xbee) {  
    XBeeDataFrame data = xbee.getXBeeReading();
    data.parseXBeeRX16Frame();
    
    int[] buffer = data.getBytes();
    initNodeId = "";
    for(int i = 0; i < buffer.length; i++) {
      initNodeId += (char)buffer[i];
    }
    
    initFound = true;
  }  
  
  public String getPortsString() {
    Set nodes = ports.keySet();
    Iterator it = nodes.iterator();
    
    String nodesString = "";
    while(it.hasNext())    
      nodesString += (String)it.next() + ", ";
    
    if (nodesString.length() < 2) return "";
    return nodesString.substring(0, nodesString.length()-2);
  }
  
  public void save() {
    String[] xbeeList = new String[ports.size()];
    int i = 0;
    Iterator it = ports.keySet().iterator();    
    while(it.hasNext()) {
      String nodeId = (String)it.next();
      xbeeList[i++] = nodeId+"="+(String)ports.get(nodeId);
    }
    saveStrings(dataPath(XBEE_PORTS_FILE), xbeeList);
  }
  
  public void load() {
    println("Loading XBee configuration");
    String[] xbeeList = loadStrings(XBEE_PORTS_FILE);
    for(int i = 0; i < xbeeList.length; i++) {
      int equalIndex = xbeeList[i].indexOf('=');
      if (equalIndex != -1) {
        String nodeId = xbeeList[i].substring(0, equalIndex);
        String port = xbeeList[i].substring(equalIndex+1);
        println(" Using port: " + port + " ... " + nodeId);
        ports.put(nodeId, port);
      }
    }    
    initialized = true;
  }
  
  public void controlEvent(ControlEvent theEvent) {
    switch(theEvent.controller().id()) {
        default:
          println("some event but we have no button any more");
    }
  }
}
