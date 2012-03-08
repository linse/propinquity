import processing.core.PApplet;
import processing.serial.Serial;
import xbee.XBeeDataFrame;
import xbee.XBeeReader;

public class XPan { 
  //xbee  
  static final int PROX_IN_PACKET_LENGTH = 6;  //length (bytes) of incoming packet for proximity readings
  static final int PROX_OUT_PACKET_LENGTH = 5; //length (bytes) of outgoing packet for proximity steps
  static final int CONFIG_OUT_PACKET_LENGTH = 3; //length (bytes) of outgoing config packet for proximity
  static final int ACCEL_IN_PACKET_LENGTH = 5;  //length (bytes) of incoming packet for proximity readings
  static final int CONFIG_ACK_LENGTH = 4;
  static final int VIBE_IN_PACKET_LENGTH = 3;
  static final int PROX_STATE_PACKET_LENGTH = 8;

  static final int BROADCAST_ADDR = 0xFFFF; 

  static final int PROX_OUT_PACKET_TYPE = 1;
  static final int PROX_IN_PACKET_TYPE = 2;
  static final int VIBE_OUT_PACKET_TYPE = 3;
  static final int ACCEL_IN_PACKET_TYPE = 4;
  static final int CONFIG_OUT_PACKET_TYPE = 5;
  static final int CONFIG_ACK_PACKET_TYPE = 6;
  static final int VIBE_IN_PACKET_TYPE = 7; // THIS IS NEW. For button presses.
  static final int PROX_STATE_PACKET_TYPE = 8;
  static final int VIBE_STATE_PACKET_TYPE = 9;
  
  //Serial g_port;
  XBeeReader xbee;

  public XPan(XBeeReader xbee)
  {
    this.xbee = xbee;
    xbee.startXBee();
  }
  
  public XPan(Serial port, PApplet parent)
  {
    //g_port = port;
    xbee = new XBeeReader(parent, port);
    xbee.startXBee();
  }
  
  void broadcast(int[] data, int turnNum, int baseNum) {
    sendOutgoing(BROADCAST_ADDR, data, turnNum, baseNum);
  }
  
  void sendOutgoing(int adl, int[] data, int turnNum, int baseNum)
  {
   //println("SEND OUTGOING: " + xbee + " " + xbee.getPort());
   int[] myData = data;
   data[1] = turnNum; 
   xbee.sendDataString16(adl, myData);
   //add to output queue
   //printToOutput("SENT at " + millis() + ": turn number " + turnNum + ", base number " + baseNum);
  }

  void broadcast(int[] data) {
    sendOutgoing(BROADCAST_ADDR, data);
  }
  
  void sendOutgoing(int adl, int[] data)
  {
     int[] myData = data;
     xbee.sendDataString16(adl, myData);
  }
  
  void nodeDiscover() { DiscoverTest.game.println("discovering nodes from xpan."); xbee.nodeDiscover(); }
  
  void stop() { xbee.stopXBee(); }
  
  void broadcastProxConfig(int stepLength) { DiscoverTest.game.println("broadcasting prox config"); broadcast(getProxConfigPacket(stepLength)); }
  void broadcastVibe(int period, int duty) { broadcast(getVibePacket(period, duty)); }
  void broadcastStep(int stepNum, Step step1, Step step2, Step step3, Step step4) {
    broadcast(getStepPacket(stepNum, step1, step2, step3, step4));
  }
  
  private int[] getProxConfigPacket(int stepLength) {
    int[] packet = new int[CONFIG_OUT_PACKET_LENGTH];
    packet[0] = CONFIG_OUT_PACKET_TYPE;
    packet[1] = (stepLength >> 8) & 0xFF;
    packet[2] = stepLength & 0xFF;
    return packet;
  }
  
  private int[] getStepPacket(int stepNum, Step step1, Step step2, Step step3, Step step4) {
    int[] packet = new int[PROX_OUT_PACKET_LENGTH];
    packet[0] = PROX_OUT_PACKET_TYPE;
    packet[1] = (stepNum >> 8) & 0xFF;
    packet[2] = stepNum & 0xFF;
    packet[3] = ((step1 == null ? 0 : step1.getPacketComponent()) << 4) | (step2 == null ? 0 : step2.getPacketComponent());
    packet[4] = ((step3 == null ? 0 : step3.getPacketComponent()) << 4) | (step4 == null ? 0 : step4.getPacketComponent());
    return packet;
  }
  
  private int[] getVibePacket(int period, int duty) {
    int[] packet = { VIBE_STATE_PACKET_TYPE, (period >> 8) & 0xff, period & 0xff, duty };
    return packet;
  }

  public int[] getProxStatePacket(Boolean active, int[] color, int color_period, int color_duty) {
	    int[] packet = { PROX_STATE_PACKET_TYPE, active?1:0, color[0], color[1], color[2], (color_period >> 8) & 0xff, color_period & 0xff, color_duty };
	    return packet;
	  }

  public static int[] decodePacket(XBeeReader xbee) {
	  XBeeDataFrame data = xbee.getXBeeReading();  
	  if (data.getApiID() == xbee.SERIES1_RX16PACKET) {
		  return data.getBytes();
	  }
	  else {
		  DiscoverTest.game.println("Got Api ID: " + data.getApiID());
		  return null;
	  }
  }
}

