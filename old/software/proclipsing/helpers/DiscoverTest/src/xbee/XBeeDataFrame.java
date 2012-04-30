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

import java.util.ArrayList;

import processing.core.*;
import processing.serial.*;

public class XBeeDataFrame {
  boolean DEBUG = false;                   // whether or not we're debugging; turns off printlns()

  public int[] frameData;
  public int apiId = -1;
  public int   sourceAddress16 = -1;
  public long  sourceAddress64 = -1;

  public int rssi = -1;

  public boolean addressBroadcast;
  public boolean panBroadcast;

  int totalSamples = -1;

  ArrayList digitalSamples;
  ArrayList analogSamples;
  
  int[] bytes;

  public XBeeDataFrame(int[] thisXBeeDataFrame) {
    digitalSamples = new ArrayList();
    analogSamples = new ArrayList();
    frameData = thisXBeeDataFrame;
    apiId = frameData[0];
  }
  
  private void parseXBeeFrame() {
    if (DEBUG) System.out.print("  API ID: " + (int)apiId);
    if (DEBUG) System.out.println("   Frame length: " + frameData.length);
    // Get the address
    int addrMSB = frameData[1];  // high byte of sender's 16-bit address
    int addrLSB = frameData[2];  // low byte of sender's 16-bit address
    int addr = (addrMSB << 8) + addrLSB;
    setAddress16(addr);

    // Get RSSI
    int rssi = frameData[3];     // Received Signal Strength Indicator shows how strong a signal we received
    setRSSI(rssi);

    // Reading the options
    int options = frameData[4];
    boolean addressbroadcast = ((options >> 1) & 0x01) == 1;
    setAddressBroadcast(addressbroadcast);

    boolean pan = ((options >> 2) & 0x01) == 1;
    setPanBroadcast(pan);
  }
  
  public void parseXBeeRX16Frame() {
    parseXBeeFrame();
    bytes = new int[frameData.length - 5];
    System.arraycopy(frameData, 5, bytes, 0, bytes.length);
  }

  // if you get an XBee XBeeDataFrame, pull the parts out:
  public void parseXBeeIOFrame() {
    parseXBeeFrame();
  
    // now we get to the ADC data itself
    int totalSamples = frameData[5]; // this is the number of sample packages that we're receiving

    setTotalSamples(totalSamples);

    /*if (totalSamples > 1) {
     System.out.println("This preliminary version of the XBee API library only works with a sample size of 1.");
     System.out.println("Set ATIT to 1 on your transmitting radio(s).");
     quit();
     return null;
     }*/

    if (DEBUG) System.out.print("  Total Samples: " + (int) totalSamples);
    int channelIndicatorHigh = frameData[6]; // this tells us which analog channels (pins) are in use (and one digital channel)
    if (DEBUG) System.out.print("  CI High: " + PApplet.binary(channelIndicatorHigh, 8));
    int channelIndicatorLow = frameData[7];  // this tells us which digital channels (pins) are in use.
    if (DEBUG) System.out.print("  CI Low: " + PApplet.binary(channelIndicatorLow, 8));

    // Process Digital

    for (int n = 0; n < totalSamples; n++) {

      int[] dataD = {
        -1,-1,-1,-1,-1,-1,-1,-1,-1                  };
      int digitalChannels = channelIndicatorLow;
      boolean digital = false;

      // Is Digital on in any of the 8 bits?
      for (int i=0; i < dataD.length-1; i++) { // add up the active channel indicators
        if ((digitalChannels & 1) == 1) { // by masking so we only see the last bit
          dataD[i] = 0;
          digital = true;
        }
        digitalChannels = digitalChannels >> 1;
      }

      // Is Digital on in the last weird extra bit?
      if ((channelIndicatorHigh & 1) == 1) { // by masking so we only see the last bit
        dataD[8] = 0;
        digital = true;
      }

      //System.out.println(digital);

      if (digital) {
        int digMSB = frameData[8];
        int digLSB = frameData[9];
        int dig = (int)((digMSB << 8) + digLSB);
        for (int i = 0; i < dataD.length; i++) {
          if (dataD[i] == 0) {
            dataD[i] = dig & 1;
          }
          dig = dig >> 1;
        }
      }

      // Put Digital Data in object
      addDigital(dataD);
      if (DEBUG) System.out.print("  DataD ");
      if (DEBUG) {
        for (int x = 0; x < dataD.length; x++) {
          System.out.print(dataD[x]+ " ");
        } 
        System.out.println();
      }


      // Process Analog 
      int[] dataADC = {
        -1,-1,-1,-1,-1,-1                  };
      int analogChannels = (channelIndicatorHigh >> 1); // shift out the one digital channel indicator that doesn't interest us now
      int nextByte = 10; // next frameData byte to read
      for (int i=0; i < dataADC.length; i++) { // add up the active channel indicators
        if ((analogChannels & 1) == 1) { // by masking so we only see the last bit
          if (nextByte+1 < frameData.length) {  // make sure you don't exceed the frame length
            int dataADCMSB = frameData[nextByte];
            int dataADCLSB = frameData[nextByte+1];
            nextByte+=2;  // skip one byte, because every ADC is two bytes
            dataADC[i] = (int)((dataADCMSB << 8) + dataADCLSB);
          }
        }
        analogChannels = analogChannels >> 1; // then shifting over one bit at a time as we go
      }
      // Put Analog in object
      addAnalog(dataADC);

      if (DEBUG) System.out.print("  analogD ");
      if (DEBUG) {
        for (int x = 0; x < dataADC.length; x++) {
          System.out.print(dataADC[x]+ " ");
        } 
        System.out.println();
      }
    }

  }
  // if you get a ZNet XBeeDataFrame, pull the parts out:

  public void parseZNetFrame() {

    if (DEBUG) System.out.print("  API ID: " + (int) apiId);

    // Looping to get 64-bit address
    long address = 0;
    for (int i = 8; i >= 1; i--) {
      long currentByte = frameData[i];
      address += currentByte << i*8;
    }
    setAddress64(address);

    // Get the 16 BIT address
    int addrMSB = frameData[9];  // high byte of sender's 16-bit address
    int addrLSB = frameData[10];  // low byte of sender's 16-bit address
    int addr = (addrMSB << 8) + addrLSB;
    setAddress16(addr);

    // Reading the options
    int options = frameData[11];
    // DO NOTHING WITH OPTIONS FOR NOW

    // now we get to the ADC data itself
    int totalSamples = frameData[12]; // this is the number of sample packages that we're receiving
    setTotalSamples(totalSamples);

    if (totalSamples > 1) {
      System.out.println("This preliminary version of the XBee API library only works with a sample size of 1.");
      System.out.println("Set ATIT to 1 on your transmitting radio(s).");

      // you need to quit out here. Figure out how.
      //quit();
      //return null;
    }

    if (DEBUG) System.out.print("  Total Samples: " + (int) totalSamples);
    int digitalChannelIndicatorHigh = frameData[13]; // this tells us which analog channels (pins) are in use (and one digital channel)
    int digitalChannelIndicatorLow = frameData[14];  // this tells us which digital channels (pins) are in use.
    int analogChannelIndicator = frameData[15];


    for (int n = 0; n < totalSamples; n++) {
      // Process Digital
      int[] dataD = {
        -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1                              };
      int digitalChannels = (digitalChannelIndicatorHigh << 8) + digitalChannelIndicatorLow;
      boolean digital = false;

      // Is Digital on in any of the 8 bits?
      for (int i=0; i < dataD.length; i++) { // add up the active channel indicators
        if ((digitalChannels & 1) == 1) { // by masking so we only see the last bit
          dataD[i] = 0;  // 0 means it's on, unlike -1 it's off
          digital = true;
        }
        digitalChannels = digitalChannels >> 1;
      }

      //System.out.println(digital);

      if (digital) {
        int digMSB = frameData[16];
        int digLSB = frameData[17];
        // Rob: what is int dig all about?
        int dig = (int)((digMSB << 8) + digLSB);
        for (int i = 0; i < dataD.length; i++) {
          if (dataD[i] == 0) {
            dataD[i] = dig & 1;
          }
          dig = dig >> 1;
        }
      }

      // Put Digital Data in object
      addDigital(dataD);

      // Process Analog 
      int[] dataADC = {
        -1,-1,-1,-1                              };
      int analogChannels = analogChannelIndicator; // Before, we shifted out a bit so just renaming var here
      for (int i=0; i < dataADC.length; i++) { // add up the active channel indicators
        if ((analogChannels & 1) == 1) { // by masking so we only see the last bit
          int dataADCMSB = frameData[18];
          int dataADCLSB = frameData[19];
          dataADC[i] = (int)((dataADCMSB << 8) + dataADCLSB);
        }
        analogChannels = analogChannels >> 1; // then shifting over one bit at a time as we go
      }

      // Put Analog in object
      addAnalog(dataADC);
    }
  }
  public void setAddress16(int address) {
    sourceAddress16 = address;
  }

  public void setAddress64(long address) {
    sourceAddress64 = address;
  }


  public void setRSSI(int r) {
    rssi = -r;
  }

  public void setAddressBroadcast(boolean a) {
    addressBroadcast = a;
  }

  public void setPanBroadcast(boolean pan) {
    panBroadcast = pan;
  }

  public void setTotalSamples(int ts) {
    totalSamples = ts;
  }

  public void addDigital(int[] d) {
    digitalSamples.add(d);
    //digital = d;
  }

  public void addAnalog(int[] a) {
    analogSamples.add(a);
    //analog = a;
  }

  public void setApiID(int api_id) {
    apiId = api_id;
  }

  public int getApiID() {
    return apiId;
  }

  public int getAddress16() {
    // TODO Auto-generated method stub
    return sourceAddress16;
  }

  public long getAddress64() {
    // TODO Auto-generated method stub
    return sourceAddress64;
  }

  public int getRSSI() {
    return rssi;
  }

  public int[] getDigital() {
    return getDigital(0);
  }

  public int[] getAnalog() {
    return getAnalog(0);
  }

  public int[] getDigital(int index) {
    if (index < digitalSamples.size()) {
      return (int[]) digitalSamples.get(index);
    } 
    else {
      return null;
    }
  }

  public int[] getAnalog(int index) {
    if (index < analogSamples.size()) {
      return (int[]) analogSamples.get(index);
    } 
    else {
      return null;
    }
  }
  public int getTotalSamples() {
    return totalSamples;
  }
  
  public int[] getBytes() {
    return bytes;
  }

}



