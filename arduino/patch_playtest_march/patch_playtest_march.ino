// THIS IS THE PATCH CODE - BOARD CHOICE IS
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328

#include <XBee.h>

//Communication Type Constants
//listening for these
#define PROX_STATE_PACKET_TYPE 8
#define PROX_STATE_PACKET_LENGTH 8
#define VIBE_STATE_PACKET_TYPE 9
#define VIBE_STATE_PACKET_LENGTH 4

const int PROX_IN_PACKET_TYPE = 2; //sending this
const int CONFIG_OUT_PACKET_TYPE = 5; //listening for this
const int CONFIG_ACK_PACKET_TYPE = 6;

//Communication variables
const int g_outPacketSize = 6;
const int g_configPacketSize = 3;
const int g_configAckSize = 4;
static uint8_t vibeStatePacket[VIBE_STATE_PACKET_LENGTH];
static uint8_t proxStatePacket[PROX_STATE_PACKET_LENGTH];
static uint8_t outPacket[g_outPacketSize];
static uint8_t configPacket[g_configPacketSize];
static int packet_cnt;

XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
Rx16Response rx = Rx16Response();
Tx16Request tx;
uint16_t base_address = 0;
TxStatusResponse txStatus = TxStatusResponse();
const int RED_COLOR = 1;
const int BLUE_COLOR = 2;

struct Blinker {
  bool _state;
  uint16_t millisOn;
  uint16_t millisOff;
  uint16_t interval;
  unsigned long prevMillis;

  Blinker() : _state(false), millisOn(0), millisOff(0), interval(100), prevMillis(0) {}

  void init(uint16_t millisOn, uint16_t millisOff) {
    this->millisOn = millisOn;
    this->millisOff = millisOff;
  }

  bool state() {
    if (millis() - prevMillis > interval) {
      if (!_state && millisOn > 0) {
          _state = true;
          setInterval(millisOn);
      }
      else {
        _state = false;
        setInterval(millisOff);
      }
    }
    return _state;
  }

  void setInterval(uint16_t newinterval) {
    prevMillis = millis();
    interval = newinterval;
  }
};

Blinker vibeBlinker;
Blinker colorBlinker;
uint8_t rgb[3] = {0, 0, 0};
bool active = false;

//Communication -- addressing (need to be changed for each patch)
//PROX1_PLAYER1
 // int myColor = RED_COLOR;
 // static int myAddress = 1;
 // static int initialDelay = 0; //for staggering messages from sensors to avoid packet collision
 // static int ledFilter1 = 0x80; //128, 64, 32, and 16 -- for higher order bits
 // static int ledFilter2 = 0x08; //8, 4, 2, and 1 -- for lower order bits

// PROX2_PLAYER1
// int myColor = RED_COLOR;
// static int myAddress = 2;
// static int initialDelay = 10; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x40; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x04; //8, 4, 2, and 1 -- for lower order bits

// PROX3_PLAYER1
// int myColor = RED_COLOR;
// static int myAddress = 3;
// static int initialDelay = 20; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x20; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x02; //8, 4, 2, and 1 -- for lower order bits

// PROX4_PLAYER1
// int myColor = RED_COLOR;
// static int myAddress = 4;
// static int initialDelay = 30; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x10; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x01; //8, 4, 2, and 1 -- for lower order bits

// PROX1_PLAYER2
int myColor = BLUE_COLOR;
static int myAddress = 9;
static int initialDelay = 0; //for staggering messages from sensors to avoid packet collision
static int ledFilter1 = 0x80; //128, 64, 32, and 16 -- for higher order bits
static int ledFilter2 = 0x08; //8, 4, 2, and 1 -- for lower order bits

// PROX2_PLAYER2
//int myColor = BLUE_COLOR;
//static int myAddress = 0x0A;
//static int initialDelay = 10; //for staggering messages from sensors to avoid packet collision
//static int ledFilter1 = 0x40; //128, 64, 32, and 16 -- for higher order bits
//static int ledFilter2 = 0x04; //8, 4, 2, and 1 -- for lower order bits

// PROX3_PLAYER2
// int myColor = BLUE_COLOR;
// static int myAddress = 0x0B;
// static int initialDelay = 20; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x20; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x02; //8, 4, 2, and 1 -- for lower order bits

// PROX4_PLAYER2
// int myColor = BLUE_COLOR;
// static int myAddress = 0x0C;
// static int initialDelay = 30; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x10; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x01; //8, 4, 2, and 1 -- for lower order bits


// Ordering
int seqNum;
int turnSeqNum;
int turnNum;

// Scheduling
const int DEFAULT_TURN_LENGTH = 1900;

long dataInterval = 50; // 20 Hz
long prevDataMillis = 0;
long xCheckInterval = 20; // 50 Hz
long prevCheckMillis = 0;
long blinkInterval = 100; // 10 Hz
long prevBlinkMillis = 0;
long turnLength; // will have to be set by config message
long prevTurnMillis = 0;
unsigned long currentMillis = 0;

// State variables
boolean running = false;
boolean blinking = false;
boolean waiting = false;

// Sensing
int proxPin = 4; //A4, 18 of 20
int proxBaseline = 250;
int proxReading = 0;
int touchThreshold = 1250;
//might need to establish running average for capsense and look for spikes

//Feedback
int redLedPin = 8; //D11, 15 of 20
int blueLedPin = 10; //D10, 14 of 20
int greenLedPin = 9; //D9, 13 of 20
int vibePin = 6; //D6, 7 of 20
int ledPin = 7; // D8, red LED on seeduino film

//LED handling
int ledState;

//debug
boolean testing = false; // actually it's specifically testing sensors
boolean testingLights = false;

void setup() {
  initOutputs();
  // blink every color, buzz
  debugCycle();
  prevCheckMillis = prevTurnMillis = prevDataMillis = prevBlinkMillis = millis();

  turnLength = DEFAULT_TURN_LENGTH;
  xbee.begin(9600);
  Serial.print("patch_newboard_oldcode (addr = ");
  Serial.print(myAddress);
  Serial.println(")");
  packet_cnt = 0;
  seqNum = 0;
  turnSeqNum = 0;
  turnNum = 0;
  if (testing) {
    running = true;//testing
    blinking = true;//testing
    Serial.begin(9600);//testing*/
  }

}

void initOutputs() {
  pinMode(redLedPin, OUTPUT);
  pinMode(blueLedPin, OUTPUT);
  pinMode(greenLedPin, OUTPUT);
  pinMode(ledPin, OUTPUT);
  pinMode(vibePin, OUTPUT);
  color(0, 0, 0);
  analogWrite(vibePin, 0);
  ledState = LOW;
}

void debugCycle() {
  // Red + debug
  color(255,0,0);
  digitalWrite(ledPin, 1);
  delay(500);
  digitalWrite(ledPin, 0);
  delay(500);

  // Green + debug
  color(0,255,0);
  digitalWrite(ledPin, 1);
  delay(500);
  digitalWrite(ledPin, 0);
  delay(500);

  // Blue + debug
  color(0,0,255);
  digitalWrite(ledPin, 1);
  delay(500);
  digitalWrite(ledPin, 0);
  delay(500);
  color(0,0,0);

  // Vibe + debug
  analogWrite(vibePin, 255); 
  digitalWrite(ledPin, 1);
  delay(500);
  digitalWrite(ledPin, 0);
  delay(500);
  analogWrite(vibePin, 0); 
}

void loop() {
  if (millis() - prevCheckMillis > xCheckInterval) {
    checkXbee();
    prevCheckMillis = millis();
  }

  if (active) {
    if (millis() - prevDataMillis > dataInterval) {
      Serial.println("ReadProx");
      readProx();
      send_data();
      prevDataMillis = millis();
    }
  }
  blinkVibe();
  blinkColor();
}

void checkXbee() {
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) {
     get_data(); 
  }
}

void readProx() {
  proxReading = analogRead(proxPin);
  if (proxReading < proxBaseline) proxReading = 0;
  else proxReading -= proxBaseline;
}

void send_data() {
  outPacket[0] = PROX_IN_PACKET_TYPE;
  outPacket[1] = uint8_t(myAddress << 1);
  outPacket[2] = uint8_t(turnNum >> 8);
  outPacket[3] = uint8_t(turnNum);
  outPacket[4] = uint8_t(proxReading >> 8);
  outPacket[5] = uint8_t(proxReading);
  tx = Tx16Request(base_address, outPacket, g_outPacketSize);
  if (testing) {
    Serial.print((int)millis()+"\t");
    Serial.print(turnNum+"\t");
    Serial.print((int)outPacket[1]+"\t");
    Serial.println(proxReading);
  }
  xbee.send(tx);
}

uint8_t frameId = 0;

void ack_config() {
  static uint8_t configAck[g_configAckSize];
  configAck[0] = CONFIG_ACK_PACKET_TYPE;
  configAck[1] = uint8_t(myAddress);
  configAck[2] = uint8_t(turnLength >> 8);
  configAck[3] = uint8_t(turnLength);
  tx = Tx16Request(base_address, ACK_OPTION, configAck, g_configAckSize, frameId++);
  if (testing) {
    Serial.print("Turn length \t");
    Serial.println(turnLength); 
  }
  xbee.send(tx);
  Serial.println("ack_config()");
  digitalWrite(ledPin, 1);
}

void get_data() {
  Serial.println("get_data()");
  if (xbee.getResponse().getApiId() == RX_16_RESPONSE) {
    Serial.println("RX_16_RESPONSE");
    int packet_cnt = 0;
    xbee.getResponse().getRx16Response(rx);
    if (rx.getData(0) == VIBE_STATE_PACKET_TYPE) {
      Serial.println("VIBE_STATE_PACKET_TYPE");
      while (packet_cnt < VIBE_STATE_PACKET_LENGTH) {
        vibeStatePacket[packet_cnt] = rx.getData(packet_cnt++);
      }
      digitalWrite(ledPin, true); // Turn on on first received package
      uint16_t period  = vibeStatePacket[1] << 8 | vibeStatePacket[2];
      uint8_t duty = vibeStatePacket[3];
      uint16_t millisOn = 1L * period * duty / 255;
      vibeBlinker.init(millisOn, period - millisOn);
    }
    else if (rx.getData(0) == PROX_STATE_PACKET_TYPE) {
      Serial.println("PROX_STATE_PACKET_TYPE");
      while (packet_cnt < PROX_STATE_PACKET_LENGTH) {
        proxStatePacket[packet_cnt] = rx.getData(packet_cnt++);
      }
      active = proxStatePacket[1];
      Serial.print("Active: "); Serial.println(active);
      rgb[0] = proxStatePacket[2];
      rgb[1] = proxStatePacket[3];
      rgb[2] = proxStatePacket[4];
      
      uint16_t period  = proxStatePacket[5] << 8 | proxStatePacket[6];
      uint8_t duty = proxStatePacket[7];
      uint16_t millisOn = 1L * period * duty / 255;
      colorBlinker.init(millisOn, period - millisOn);
    }
    // else if (rx.getData(0) == PROX_OUT_PACKET_TYPE) {
    //   Serial.println("PROX_OUT_PACKET_TYPE");
    //   while (packet_cnt < g_inPacketSize) {
    //     inPacket[packet_cnt] = rx.getData(packet_cnt++);
    //   }
    //   setLeds(inPacket[3], inPacket[4]);
    //   turnNum = inPacket[1] << 8 | inPacket[2];
    //   turnSeqNum = 0;
    //   prevTurnMillis = millis();
    // }
    // else if (rx.getData(0) == CONFIG_OUT_PACKET_TYPE) {
    //   Serial.println("CONFIG_OUT_PACKET_TYPE");
    //   while (packet_cnt < g_configPacketSize) {
    //     configPacket[packet_cnt] = rx.getData(packet_cnt++);
    //   }
    //   int stepLength = configPacket[1] << 8 | configPacket[2];
    //   turnLength = stepLength-10;
    //   ack_config();
    // }
    else {
      Serial.print("Unknown packet type: ");
      Serial.println(rx.getData(0), HEX);
    }
  }
  else if (xbee.getResponse().getApiId() != TX_STATUS_RESPONSE) {
    Serial.print("Unknown API ID: ");
    Serial.println(xbee.getResponse().getApiId(), HEX);
  }
}

void color(unsigned char red, unsigned char green, unsigned char blue) {
  analogWrite(redLedPin, 255-red);	 
  analogWrite(blueLedPin, 255-blue);
  analogWrite(greenLedPin, 255-green);
}

void blinkVibe() {
  if (vibeBlinker.state()) {
    analogWrite(vibePin, 255);
    digitalWrite(ledPin, 1);
  }
  else {
    analogWrite(vibePin, 0);
    digitalWrite(ledPin, 0);
  }
}

void blinkColor() {
  if (colorBlinker.state()) {
    color(rgb[0], rgb[1], rgb[2]);
  }
  else {
    color(0, 0, 0);
  }
}
