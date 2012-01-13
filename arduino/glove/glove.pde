#include <XBee.h>
#include <CapSense.h>

#define VIBE_OUT_PACKET_LENGTH 2
#define VIBE_IN_PACKET_TYPE 7
#define VIBE_OUT_PACKET_TYPE 3
#define VIBE_IN_PACKET_LENGTH 3
#define THRESHOLD 1500

// THIS IS THE VIBE CODE - BOARD CHOICE IS
// Lilypad Arduino w/ ATmega328

const int vibepin = 3; // for right handed gloves
//if lefthanded use pin 6
//const int vibepin = 6;
int val = 0;

CapSense cs_4_2 = CapSense(4,2);
CapSense cs_4_5 = CapSense(4,5);
CapSense cs_4_6 = CapSense(4,6);
long total1;
long total2;
long total3;
boolean touched1;
boolean touched2;
boolean touched3;

//const int ledpin = 13;
//int ledState = LOW;

//change for each player

// VIBE1_PLAYER1
//static int myAddress = 5;

// VIBE2_PLAYER1
static int myAddress = 6;

// VIBE1_PLAYER2
//static int myAddress = 13;

// VIBE2_PLAYER2
//static int myAddress = 14;

const int g_inPacketSize = VIBE_OUT_PACKET_LENGTH; // seems right, msg type + one value.
const int listenType = VIBE_OUT_PACKET_TYPE;
static uint8_t inPacket[g_inPacketSize];

const int g_outPacketSize = VIBE_IN_PACKET_LENGTH;
const int outType = VIBE_IN_PACKET_TYPE;
static uint8_t outPacket[g_outPacketSize];

XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
Rx16Response rx16 = Rx16Response();
Tx16Request tx;
uint16_t base_address = 0;
TxStatusResponse txStatus = TxStatusResponse();

const unsigned long timeoutInterval = 250;
unsigned long prevMsgReceived;

void setup()
{
  xbee.begin(9600);
  //pinMode(ledpin, OUTPUT);
  pinMode(vibepin, OUTPUT);
  initCapValues();

  analogWrite(vibepin, 255);
  delay(1000);
  analogWrite(vibepin, 0);
}

void loop()
{
  // Turned off capacitive sensing. kintel 20111013
  // total1 = cs_4_2.capSense(10);
  // if (total1 > THRESHOLD) touched1=true;
  // total2 = cs_4_5.capSense(10);
  // if (total2 > THRESHOLD) touched2=true;
  // total3 = cs_4_6.capSense(10);
  // if (total3 > THRESHOLD) touched3=true;
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) // got something
  {
    get_data();
  } 
  if (val > 255) val = 255;
  analogWrite(vibepin, val);
  if (millis() - prevMsgReceived > timeoutInterval) analogWrite(vibepin, 0);
}

void initCapValues()
{
  total1=0;
  total2=0;
  total2=0; 
  touched1=false;
  touched2=false;
  touched3=false;
}

void send_data()
{
  outPacket[0] = outType;
  outPacket[1] = uint8_t(myAddress);
  if (touched1 && touched2 && touched3) outPacket[2] = 4; //all touched
  else if (touched1 && !touched2 && !touched3) outPacket[2] =  2; //right touched
  else if (!touched1 && touched2 && !touched3) outPacket[2] = 3; //center touched
  else if (!touched1 && !touched2 && touched3) outPacket[2] = 1; //left touched
  else outPacket[2] = 0;
  tx = Tx16Request(base_address, outPacket, g_outPacketSize);
  xbee.send(tx);
  initCapValues(); 
}

void get_data()
{
  prevMsgReceived = millis();
  if (xbee.getResponse().getApiId() == RX_16_RESPONSE)
  {
    //if (ledState == LOW) ledState = HIGH;
    //else ledState = LOW;
    int packet_cnt = 0;
    xbee.getResponse().getRx16Response(rx16);
    while (packet_cnt < g_inPacketSize)
    {
      inPacket[packet_cnt] = rx16.getData(packet_cnt++);
    } 
    if (inPacket[0] == listenType)
    {
      val = inPacket[1];
    }
  }
}
