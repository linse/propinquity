#include <XBee.h>

//Communication Type Constants
const int PROX_OUT_PACKET_TYPE = 1; //listening for this
const int PROX_IN_PACKET_TYPE = 2; //sending this
const int VIBE_OUT_PACKET_TYPE = 3;
const int CONFIG_OUT_PACKET_TYPE = 5;
const int CONFIG_ACK_PACKET_TYPE = 6;

//Communication variables
const int g_outPacketSize = 6;
const int g_inPacketSize = 5;
const int g_configPacketSize = 3;
const int g_configAckSize = 4;
static byte inPacket[g_inPacketSize];
static byte outPacket[g_outPacketSize];
static byte configPacket[g_configPacketSize];

XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
Rx16Response rx = Rx16Response();
Tx16Request tx;
uint16_t base_address = 0;
TxStatusResponse txStatus = TxStatusResponse();

// PROX2_PLAYER2
static int myAddress = 0x0A;
static int initialDelay = 10; //for staggering messages from sensors to avoid packet collision
static int ledFilter1 = 0x40; //128, 64, 32, and 16 -- for higher order bits
static int ledFilter2 = 0x04; //8, 4, 2, and 1 -- for lower order bits

// Ordering
int turnNum;

// Scheduling
long dataInterval = 20; // 20 Hz
long prevDataMillis = 0;

// Board for SparkJaneBoard: 
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328

// all +1 compared to previous version
int onboardLedPin = 7; 
int redLedPin = 8; 
int greenLedPin = 9; 
int blueLedPin = 10;
int vibePin = 6; 
int proxPin = A4; 

int proxReading = 0;


void setup() {
  prevDataMillis = millis();
  xbee.begin(9600);
  
  pinMode(onboardLedPin, OUTPUT);
  pinMode(redLedPin, OUTPUT);
  pinMode(greenLedPin, OUTPUT);
  pinMode(blueLedPin, OUTPUT);
  
  pinMode(vibePin, OUTPUT);
  
  pinMode(proxPin, INPUT);

  Serial.begin(9600);
  color(0,0,0);
  analogWrite(vibePin, 0);
}

void loop() {
  //blinkRed(1000);
  readAndSendProxViaXbee();
}

void blinkRed(int pause) {
  color(0,0,0);
  delay(pause);
  color(255,0,0);
  delay(pause);
}

void blinkLEDsAndVibe() {
  analogWrite(redLedPin, 255);
  digitalWrite(vibePin, 0);	
  delay(500);
  digitalWrite(redLedPin, 0);
  digitalWrite(vibePin, 1);
  delay(500);
  color(255, 255, 0);
}

void readAndSendProxViaSerial() {
  analogRead(proxPin);
  delay(20);
  proxReading = analogRead(proxPin);
  delay(20);
  Serial.println(proxReading);
  delay(20);
}

void readAndSendProxViaXbee() {
  // TODO calc average over several measurements?
  readProx();
  if (millis() - prevDataMillis > dataInterval) {
    send_data();
    prevDataMillis = millis();
  }
}

void readProx() {
  proxReading = analogRead(proxPin);
}


void send_data() {
  outPacket[0] = PROX_IN_PACKET_TYPE;
  //outPacket[1] = byte(myAddress << 1 | (int)touched);
  outPacket[1] = byte(myAddress << 1);
  outPacket[2] = byte(turnNum >> 8);
  outPacket[3] = byte(turnNum);
  outPacket[4] = byte(proxReading >> 8);
  outPacket[5] = byte(proxReading);
  tx = Tx16Request(base_address, outPacket, g_outPacketSize);
  xbee.send(tx);
}


void color(unsigned char red, unsigned char green, unsigned char blue) {
  analogWrite(redLedPin, 255-red);	
  analogWrite(greenLedPin, 255-green); 
  analogWrite(blueLedPin, 255-blue);
}
