/** 
 * XBee ACD API Reader
 * by Rob Faludi and Daniel Shiffman and Tom Igoe
 * http://www.faludi.com
 * http://www.shiffman.net
 * http://tigoe.net 
 * v 1.2  (added multiple samples)
 * v 1.3  (added Znet IO and AT support
 * v 1.4  (added file input support)
 * v 1.5 added support for rx and tx packets
 */

package xbee;

import java.lang.reflect.Method;
import processing.core.*;
import processing.serial.*;

public class XBeeReader extends Thread {
  public static final int SERIES1_TX16PACKET = 0x01;
  public static final int ATCOMMAND = 0x08;
  public static final int AT_CMD_QUEUE_PARAM_REQ = 0x09;
  public static final int TRANSMIT_REQUEST = 0x10;
  public static final int EXPLICIT_ADDR_ZIGBEE_CMD_FRAME = 0x11;
  public static final int REMOTE_ATCOMMAND_REQUEST = 0x17;
  public static final int SERIES1_RX16PACKET = 0x81;              // 0x80 is for 64bit, but we usually us 16bit, 0x81.
  public static final int SERIES1_IOPACKET = 0x83;    // 0x82 is for 64bit
  public static final int ATCOMMAND_RESPONSE = 0x88;
  public static final int TRANSMITSTATUS = 0x89;
  public static final int MODEMSTATUS = 0x8A;
  public static final int ZIGBEE_TX_STATUS =  0x8B;
  public static final int ZIGBEE_RX_PACKET = 0x90;
  public static final int ZIGBEE_EXPLICIT_RX_INDICATOR = 0x91;
  public static final int ZNET_IOPACKET = 0x92;   
  public static final int XBEE_SENSOR_READ_INDICATOR = 0x94;
  public static final int XBEE_NODEID_INDICATOR = 0x95;
  public static final int REMOTE_ATCOMMAND_RESPONSE = 0x97;


  PApplet parent;                         // the sketch that's calling this class
  Serial port = null;                     // the serial port
  int[] dataArray;                        // the array returned for the data frame
  public XBeeDataFrame dataFrame;         // the dataArray object
  public boolean DEBUG = false;                  // whether or not we're debugging; turns off System.out.printlns()
  Method xBeeMethod;                      // for the xBeeEvent

  private boolean running;                // Is the thread running?  Yes or no?
  boolean available = false;              // any data available
  private String versionNumber = "1.5";       // version of the library

  /*
    New variables in 1.4:
   */
  String dataFile = null;                  // for reading from a file rather than the serial port
  byte[] fileArray;                        // array to store the file in
  int fileIndex;                           // pointer to where you are in the array
  long sleepRate = 1;                      // how long the thread sleeps for
  public boolean fileEmpty = false;        // whether or not you've read to the end of the file

    public XBeeReader(PApplet p, Serial thisPort) {
    running = false;
    port = thisPort;
    parent = p;
    sleepRate = 1;
    
    try {
      xBeeMethod = parent.getClass().getMethod("xBeeEvent", new Class[] { 
        XBeeReader.class                                                                                                             }
      );
    } 
    catch (Exception e) {
      System.out.println("You forgot to implement the xBeeEvent() method.");
    }
  }

  /*
    New overloaded constructor for 1.4
   */

  public XBeeReader(PApplet p, String thisFile, long sleepTime) {
    running = false;
    dataFile = thisFile;
    parent = p;
    fileArray = readFile(dataFile);
    fileIndex = 0;
    sleepRate = sleepTime;

    try {
      xBeeMethod = parent.getClass().getMethod("xBeeEvent", new Class[] { 
        XBeeReader.class                                                                                                             }
      );
    } 
    catch (Exception e) {
      System.out.println("You forgot to implement the xBeeEvent() method.");
    }
  }

  // Starts the thread running
  public void start() {
    running = true;
    super.start();
  }

  public void startXBee() {
    running = true;
    super.start();
  }

  public void stopXBee() {
    running = false;
  } 

  // runs the thread. this is the thread's loop
  public void run() {
    // do this as long as the thread is running
    while (running) {
      try {
        // read bytes in from the serial port, and get a dataArray back:
        /*
          New in 1.4: checks to see if the port has been initialized.
         If not, assumes it's to read from a file and tries to do so.
         */
        if (port != null) {
          dataArray = getPacket();
        } 

        else {
          if (bytesAvailable() > 0) {
            dataArray = getPacket(fileArray);
          } 
          else {
            running = false; 
          }
        }

        if (dataArray != null) {
          dataFrame = new XBeeDataFrame(dataArray);

          switch (dataFrame.apiId) {
          case ZNET_IOPACKET: 
            // process a ZNet IO packet
            dataFrame.parseZNetFrame();
            break;
          case SERIES1_RX16PACKET: 
            dataFrame.parseXBeeRX16Frame();
            // process a series 1 packet
            break;
          case SERIES1_IOPACKET: 
            dataFrame.parseXBeeIOFrame();
            // process a series 1 packet
            break;
          }
        }
        
        // when we have a dataArray, set available = true:
        available = true; 
        
        // not being used at the moment, since XBeeEvent is not getting triggered:
        // if we have a valid data frame, generate an event:
        if (xBeeMethod != null && dataArray != null) { 
          // generate an XBeeEvent:
          try {
            xBeeMethod.invoke(parent, new Object[] { this }
            );
          } 
          catch (Exception e) {
            System.out.println("Problem with XBeeEvent()");
            e.printStackTrace();
            xBeeMethod = null;
          }
        }

        // gives the main sketch back the processor
        try {
          sleep(sleepRate);  // Should we sleep?
        } 
        catch (Exception e) {
          // Nothing for now. We'll get here if interrupt() is called
        }
      } catch (Exception e) {
      	System.out.println("Exception in XBeeReader.run():");
        e.printStackTrace();
      }
    }
    running = false;
    fileEmpty = true;
    
    if (port != null) {
    	port.clear();
    	port.stop();
    }
  }

  public XBeeDataFrame getXBeeReading() {
    available = false;
    return dataFrame;
  }

  // reads in a packet, returns the API-specific data frame, or null
  // if the checksum doesn't check

  public int[] getPacket() {
    boolean gotAPacket = false;    // whether the first byte was 0x7E
    int packetLength = -1;         // length of the dataArray
    int[] thisdataArray = null;    // the dataArray to return
    int checksum = -1;             // the checksum as received
    int localChecksum = -1;        // the chacksum as we calculate it

    // read bytes until you get a 0x7E
    //port.clear(); // flush the buffer so that no old data comes in

    //while (port.available() < 1) {
    //  ; // do nothing while we wait for the buffer to fill
    //}

    // this is a good header. Get ready to read a packet
    while(port.available() > 0) {
    	if (port.read() == 0x7E) {
    		gotAPacket = true;
    		break;
    	}
    }

    // if the header byte is good, try the rest:
    if (gotAPacket) {
      // read two bytes
      while (port.available() < 2) {
        ; // wait until you get two bytes of length
      }
      int lengthMSB = port.read(); // high byte for length of packet
      int lengthLSB = port.read(); // low byte for length of packet

      // convert to length value
      packetLength = (lengthLSB + (lengthMSB << 8));
      // sanity check: if the packet is too long, bail.
      if (packetLength > 200) {
      	System.out.println("Length (" + packetLength + ") too long, discarding packet.");
        return null;
      }

      if (DEBUG) System.out.print("> [" + packetLength + "]: ");
      
      // read bytes until you've reached the length
      while (port.available() < packetLength) {
        ; // do nothing while we wait for the buffer to fill
      }
      // make an array to hold the data frame:
      thisdataArray = new int[packetLength];

      // read all the bytes except the last one into the dataArray array:
      for (int thisByte = 0; thisByte < packetLength; thisByte++) {
        thisdataArray[thisByte] = port.read(); 
        if (DEBUG) System.out.print(parent.hex(thisdataArray[thisByte], 2) + " ");
      }

      while (port.available() < 1) {
        ; // do nothing while we wait for the buffer to fill
      }
      // get the checksum:
      checksum = port.read();

      // calculate the checksum of the received dataArray:
      localChecksum = checkSum(thisdataArray);
      // if they're not the same, we have bad data:
      if ( localChecksum != checksum) {
    	if (DEBUG) System.out.println();
        System.out.print("Bad checksum, discarding packet. Local: " + parent.hex(localChecksum%256));
        System.out.println(", Remote: " + parent.hex(checksum) + ", Length: " + packetLength);
        // if the checksums don't add up, clear the dataArray array:
        thisdataArray = null;
      } 
    }
    // makes a nice printing for debugging:
    //    if (DEBUG) System.out.println("!");

    // return the data frame.  If it's null, you got a bad packet.
    return thisdataArray;
  }

  // reads in a packet from a byte array 
  // if the checksum doesn't check

  public int[] getPacket(byte[] someFileArray) {
    boolean gotAPacket = false;    // whether the first byte was 0x7E
    int packetLength = -1;         // length of the dataArray
    int[] thisdataArray = null;    // the dataArray to return
    int checksum = -1;             // the checksum as received
    int localChecksum = -1;        // the chacksum as we calculate it

    if (DEBUG) System.out.println("fileIndex: " + fileIndex);
    if (DEBUG) System.out.println("file length: " + fileArray.length);


    // make sure you have a 0x7E and at least three bytes
    if (getNextByte() == 0x7E && fileArray.length > 2) {
      gotAPacket = true;
    }

    // if the header byte is good, try the rest:
    if (gotAPacket) {
      // read two bytes
      if (bytesAvailable() >= 2) {
        int lengthMSB = getNextByte(); // high byte for length of packet
        int lengthLSB = getNextByte(); // low byte for length of packet

        // convert to length value
        packetLength = (lengthLSB + (lengthMSB << 8));
        if (DEBUG) System.out.println("length: " + packetLength);
      }
      // read bytes until you've reached the length
      if (bytesAvailable()  >= packetLength && packetLength > 0) {
        // make an array to hold the data frame:
        thisdataArray = new int[packetLength];

        // read all the bytes except the last one into the dataArray array:
        for (int thisByte = 0; thisByte < packetLength; thisByte++) {
          thisdataArray[thisByte] = getNextByte(); 
          if (DEBUG) System.out.print(parent.hex(thisdataArray[thisByte], 2) + " ");
        }


        if (bytesAvailable() >= 1) {
          // get the checksum:
          checksum = getNextByte();
        }
        // calculate the checksum of the received dataArray:

        localChecksum = checkSum(thisdataArray);
        // if they're not the same, we have bad data:
        if ( localChecksum != checksum) {
          if (DEBUG) System.out.println("bad checksum. Local: " + parent.hex(localChecksum%256));
          if (DEBUG) System.out.println("  remote: " + parent.hex(checksum));
          // if the checksums don't add up, clear the dataArray array:
          thisdataArray = null;
        }
      } 
    }
    // makes a nice printing for debugging:
    if (DEBUG) System.out.println();

    // return the data frame.  If it's null, you got a bad packet.
    return thisdataArray;
  }

  // File reader for when you don't want to use the serial port:
  public byte[] readFile(String fileName) {
    byte[] byteData = parent.loadBytes(fileName);
    fileIndex = 0;
    return byteData;
  }
  public int getNextByte() {
    int byteToReturn = -1;
    try {
      byteToReturn = fileArray[fileIndex];
      if (byteToReturn != 0) {
        byteToReturn = byteToReturn  & 0xff;
      } 
      fileIndex++;
    }  
    catch (Exception e) {
      System.out.println("Problem with getNextByte()");
      e.printStackTrace();
      fileIndex = 0;
    }
    return byteToReturn;
  }

  public int bytesAvailable() {
    return fileArray.length - fileIndex; 
  }

  public void setSleepTime(long thisSleepTime) {
    sleepRate = thisSleepTime; 
  }

  public long getSleepTime() {
    return sleepRate; 
  }


  /*-------------------------------------------------------------*/

  public void sendPacket(int[] thisFrame) {
    // calculate the length of the total array
    // header byte + length (2 bytes) + command + checksum:
    int packetLength = thisFrame.length + 4;

    // set up the array you'll actually send:
    int[] packet = new int[packetLength];
    packet[0] = 0x7E;

    // get the high byte and the low byte of the length of the frame:
    packet[1] = thisFrame.length / 256;
    packet[2] = thisFrame.length % 256;

    // add the frame to the packet:
    for (int b = 0; b < thisFrame.length; b++) {
      packet[b+3] = thisFrame[b]; 
    }

    // checksum the command:  
    packet[packetLength - 1]  = checkSum(thisFrame);

    if (DEBUG) System.out.print("< ");
    // send it out the serial port:
    for (int c = 0; c < packet.length; c++) {
      port.write(packet[c]);
      if (DEBUG) System.out.print(parent.hex(packet[c], 2) + " " );
    }
    if (DEBUG) System.out.println();
  } 

  // calculate the checksum
  private int checkSum(int[] thisArray) {
    int ck = 0;
    // add all the bytes:
    for (int i = 0; i < thisArray.length; i++) {
      ck += thisArray[i];
    } 
    // subtract the result from 0xFF.

    ck = (0xFF & ck);
    ck = (0xFF - ck);
    return (int)(ck);
  }

  // generic structure for AT commands
  public void sendATCommand(String thisCommand, int apiID, int frameID) {

    int[] myCommand = new int[2];    // array that you'll pass to sendATCommand()
    myCommand[0] = apiID;        // frame identifier
    myCommand[1] = frameID;             // frame type

    // parent.append the cmdString to the array
    for (int i = 0; i < thisCommand.length(); i++) {
      myCommand = parent.append(myCommand, (int)(thisCommand.charAt(i)));
    }
    // send it out:
    sendPacket(myCommand);
  }


  public void sendRemoteCommand(int addressH, int addressL, int address16, String remoteCommand, int option) {
    // not correct, I think the frame identifier is wrong
    String cmdString;
    if (option>0) {
      cmdString = remoteCommand + (char)(option);
    } 
    else {
      cmdString = remoteCommand;
    }
    int[] myCommand = new int[2];
    myCommand[0] = REMOTE_ATCOMMAND_REQUEST;  // frame identifier
    myCommand[1] = 0x01;    // frame ID
    // break up the remote address to send to:
    byte thisByte = 0;
    // get each byte from the 4-byte int:
    for (int b = 3 ; b > -1; b--) {
      thisByte = (byte)(addressH >> (8*b));
      myCommand = parent.append(myCommand, thisByte);
    }
    // same with addressL:
    for (int b = 3 ; b > -1; b--) {
      thisByte = (byte)(addressL >> (8*b));
      myCommand = parent.append(myCommand, thisByte);
    }

    // 16 bit address:
    for (int b = 1 ; b > -1; b--) {
      thisByte = (byte)(address16 >> (8*b));
      myCommand = parent.append(myCommand, thisByte);
    }

    myCommand = parent.append(myCommand, 0x02);  // apply changes on remote

    // append the cmdString to the array
    for (int i = 0; i < cmdString.length(); i++) {
      myCommand = parent.append(myCommand, (int)(cmdString.charAt(i)));
    }

    sendPacket(myCommand);
  }

  public void sendDataString(int addressH, int addressL, String data) {
    if (DEBUG) System.out.println("executing sendString");
    String cmdString = data;
    int[] myCommand = new int[2];
    myCommand[0] = TRANSMIT_REQUEST;  // frame identifier
    myCommand[1] = 0x01;    // frame type

    // break up the address and append the bytes of it:
    byte thisByte = 0;
    // get each byte from the 4-byte int:
    for (int b = 3 ; b > -1; b--) {
      thisByte = (byte)(addressH >> (8*b));
      myCommand = parent.append(myCommand, thisByte);
    }
    // same for addressL:
    for (int b = 3 ; b > -1; b--) {
      thisByte = (byte)(addressL >> (8*b));
      myCommand = parent.append(myCommand, thisByte);
    }
    // 16-bit address:
    myCommand = parent.append(myCommand, 0xFF);
    myCommand = parent.append(myCommand, 0xFE);

    myCommand = parent.append(myCommand, 0x00);    // broadcast radius
    myCommand = parent.append(myCommand, 0x00);    // options
    // append the cmdString to the array
    for (int i = 0; i < cmdString.length(); i++) {
      myCommand = parent.append(myCommand, (int)(cmdString.charAt(i)));
    }
    sendPacket(myCommand);
  }

  // use address 0xFFFF to broadcast
  public void sendDataString16(int addr, int[] buf) {
    int[] myCommand = new int[5 + buf.length];
    myCommand[0] = SERIES1_TX16PACKET;
    myCommand[1] = 0; // UART data frame (disable response frame)
    myCommand[2] = (addr >> 8) & 0xFF; // MSB
    myCommand[3] = addr & 0xFF; // LSB
    myCommand[4] = 0x00; // options
    System.arraycopy(buf, 0, myCommand, 5, buf.length);
    sendPacket(myCommand);
  }


  /*******************************************************************
   * The AT commands we want to send:
   */

  public void nodeDiscover() {
    sendATCommand("ND", ATCOMMAND, 0x01);
  }


  public void getDL() {
    sendATCommand("DL", ATCOMMAND, 0x01);
  }

  public void getDH() {
    sendATCommand("DH", ATCOMMAND, 0x01);
  }

  public void getID() {
    sendATCommand("ID", ATCOMMAND, 0x01);
  }

  public void getCH() {
    sendATCommand("CH", ATCOMMAND, 0x01);
  }

  public void setDestinationNode(String nodeIdentifier) {
    sendATCommand("DN"+nodeIdentifier, ATCOMMAND, 0x01);
  }

  public void getIOPin(int pinNumber) {
    sendATCommand("DN"+pinNumber, ATCOMMAND, 0x01);
  }

  public void setIOPin(int pinNumber, int pinState) {

    // not correct, I think the frame identifier is wrong
    String cmdString = "D" + pinNumber + (char)(pinState);
    sendATCommand(cmdString, ATCOMMAND, 0x01);
  }

  public void getNI() {
    sendATCommand("NI", ATCOMMAND, 0x01);
  }

  public String getVersion() {
    return versionNumber;
  }
}
