import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import com.rapplogic.xbee.api.wpan.NodeDiscover;
import com.rapplogic.xbee.api.wpan.TxRequest64;
import com.rapplogic.xbee.api.*;
import processing.core.PApplet;
import com.rapplogic.xbee.util.ByteUtils;

public class Xpan extends PApplet {
  
  static final XBeeAddress64 BROADCAST_ADDR = new XBeeAddress64("00 00 00 00 00 00 ff ff");
  static final int VIBE_OUT_PACKET_TYPE = 3;
  
  public XBee localXbee;
  Queue<NodeDiscover> discoveredNodes;


  Xpan(String serialPort) {
    this.localXbee = new XBee();
    this.discoveredNodes = new ConcurrentLinkedQueue<NodeDiscover>();
    this.localXbee = localXbeeFromPort(serialPort);
    discoverRemoteNodes(this.localXbee);
    printRemoteNodes();
  }


  // opens serial port at 9600 baud
  XBee localXbeeFromPort(String serialPort) {
    XBee localXbee = new XBee();
    try {
      localXbee.open(serialPort, 9600);
    }
    catch (XBeeException e) {
      println("** Error opening XBee port " + serialPort + ": " + e + " **");
      println("Is your XBee plugged in to your computer?");
      exit();
    }
    return localXbee;
  }


  // do node discovery
  void discoverRemoteNodes(XBee xbee) {
    try {
      // get the Node discovery timeout
      xbee.sendAsynchronous(new AtCommand("NT"));
      AtCommandResponse nodeTimeout = (AtCommandResponse) xbee.getResponse();
  
      // default is 6 seconds, could be changed here
      long nodeDiscoveryTimeout = ByteUtils.convertMultiByteToInt(nodeTimeout.getValue()) * 100;
      println("Node discovery timeout is " + nodeDiscoveryTimeout + " milliseconds");
  
      PacketListener pl = new PacketListener() {
        
        public void processResponse(XBeeResponse response) {
          if (response.getApiId() == ApiId.AT_RESPONSE) {
            NodeDiscover nd = NodeDiscover.parse((AtCommandResponse)response);
            //println("Node discover response is: " + nd);
            //XBeeAddress64 a2 = nd.getNodeAddress64();
            discoveredNodes.offer(nd);
          } 
          else {
            println("Ignoring unexpected response: " + response);	
          }					
        }	
      };
      xbee.addPacketListener(pl);			
  
      println("Sending node discover command");
      xbee.sendAsynchronous(new AtCommand("ND"));
  
      // wait for nodeDiscoveryTimeout milliseconds
      Thread.sleep(nodeDiscoveryTimeout);
      xbee.removePacketListener(pl);
    } 
    catch (XBeeException e) {
      println(e);
    }
    catch (InterruptedException e) {
      println(e);
    }
    finally {
        xbee.close();
    }
  }


  void printRemoteNodes() {
      println("Discovered nodes:");
      NodeDiscover nd;
      while ((nd = discoveredNodes.poll()) != null) {
        println (nd);
      }
  }
  

  void broadcastVibe(int value) { 
    int[] payload = { VIBE_OUT_PACKET_TYPE, value };
    TxRequest64 request = new TxRequest64(BROADCAST_ADDR, payload);
    try { 
      this.localXbee.sendAsynchronous(request);
    }
    catch (XBeeException e) {
      println("Could not broadcast vibe.");
    }
  }

  void receiveProxReading() {
  }
//  // do something to a remote xbee
//  void remoteAction(XBee xbee) {
//    
//    try {
//      // SH + SL of end device
//      // SH 13A200
//      // SL 4065772B
//      XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x65, 0x77, 0x2B);
//      
//      // turn on end device (pin 20) D0 (Digital output high = 5) 
//      //RemoteAtRequest request = new RemoteAtRequest(addr64, "D0", new int[] {5});
//      RemoteAtRequest request = new RemoteAtRequest(addr64, "P0", new int[] {1});
//      RemoteAtResponse response = (RemoteAtResponse) xbee.sendSynchronous(request, 10000);
//      
//      if (response.isOk()) {
//        println("successfully turned on pin 20 (D0)");
//      } 
//      else {
//        throw new RuntimeException("failed to turn on pin 20.  status is " + response.getStatus());
//      }
//      
//      System.exit(0);
//      // wait a bit
//      Thread.sleep(5000);                      
//      // now turn off end device D0
//      request.setValue(new int[] {4});
//  
//      response = (RemoteAtResponse) xbee.sendSynchronous(request, 10000);
//  
//      if (response.isOk()) {
//        println("successfully turned off pin 20 (D0)");
//      } 
//      else {
//        throw new RuntimeException("failed to turn off pin 20.  status is " + response.getStatus());
//      }
//  
//    } catch (XBeeTimeoutException e) {
//            println("request timed out. make sure you remote XBee is configured and powered on");
//    } 
//    catch (Exception e) {
//            println("unexpected error"+ e);
//    } 
//    finally {
//            xbee.close();
//    }
//    
//  }

}
