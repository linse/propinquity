public class XBeeManager implements Runnable {
  PApplet parent;
  HashMap ports;
  boolean debug;
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
    done = false;
    ports = new HashMap();
    initialized = false;
    debug = false;
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
  
  public boolean isScanning() { return thread != null; }
  
  public void run() {
    ports = new HashMap();
    initialized = false;

    String[] initPorts = Serial.list();
    long initLastCheck;
    println("Initializing XBees...");
    
    String osName = System.getProperty("os.name");
    
    for(int initPortIndex = 0; initPortIndex < initPorts.length; initPortIndex++) {
      
      //if we are on a Mac, then filter out the ports that don't start by tty.us
      if ((osName.indexOf("Mac") != -1) && (initPorts[initPortIndex].indexOf("tty.usbserial") == -1)) {
        println(" Skipping port: " + initPorts[initPortIndex]);
        continue;
      }
      
      print(" Connecting to port: " + initPorts[initPortIndex] + " ... ");
      Serial serial = new Serial(parent, initPorts[initPortIndex], XBEE_BAUDRATE);
      XBeeReader xbee = new XBeeReader(parent, serial);
      xbee.startXBee();

      //take a break to give some time to start
      try { Thread.sleep(250); }
      catch(InterruptedException ie) { }        

      xbee.getNI();

      initLastCheck = millis();
      initFound = false;
        
      while(millis()-initLastCheck < XBEE_RESPONSE_TIMEOUT && !initFound) {
        try { Thread.sleep(100); }
        catch(InterruptedException ie) { }        
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
      while(xbee.isAlive()) {
      try { Thread.sleep(1000); }
      catch(InterruptedException ie) { }  
      }
      try { Thread.sleep(1000); }
      catch(InterruptedException ie) { }  
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
    xbee.DEBUG = debug;
    return xbee;
  }
  
  public void xBeeEvent(XBeeReader xbee) {     
    //println("xbee event: xbee manager");
    XBeeDataFrame data = xbee.getXBeeReading();
    data.parseXBeeRX16Frame();
    
    int[] buffer = data.getBytes();
    initNodeId = "";
    for(int i = 0; i < buffer.length; i++) {
      initNodeId += (char)buffer[i];
    }
    
    initFound = true;
  }  
  
  public String listToString() {
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
  
  public boolean isDone() { return done; }
  
}
