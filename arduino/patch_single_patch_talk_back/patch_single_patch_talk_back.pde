#include <XBee.h>

// Board for Seeeduino: 
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 168
// Take out xbee for programming!

//Communication Type Constants
const int PROX_OUT_PACKET_TYPE = 1; //listening for this
const int PROX_IN_PACKET_TYPE = 2; //sending this
const int VIBE_OUT_PACKET_TYPE = 3;
//const int ACCEL_IN_PACKET_TYPE = 4;
const int CONFIG_OUT_PACKET_TYPE = 5; //listening for this
const int CONFIG_ACK_PACKET_TYPE = 6;
//const int VIBE_IN_PACKET_TYPE = 7; // THIS IS NEW. For button presses.

//Communication variables
const int g_outPacketSize = 6;
const int g_inPacketSize = 5;
const int g_configPacketSize = 3;
const int g_configAckSize = 4;
static uint8_t inPacket[g_inPacketSize];
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
// int myColor = BLUE_COLOR;
// static int myAddress = 9;
// static int initialDelay = 0; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x80; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x08; //8, 4, 2, and 1 -- for lower order bits

// PROX2_PLAYER2
int myColor = BLUE_COLOR;
static int myAddress = 0x0A;
static int initialDelay = 10; //for staggering messages from sensors to avoid packet collision
static int ledFilter1 = 0x40; //128, 64, 32, and 16 -- for higher order bits
static int ledFilter2 = 0x04; //8, 4, 2, and 1 -- for lower order bits

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

int maxProx = 0; // MIN analog read
int minProx = 1023; // MAX analog read

// Scheduling
const int DEFAULT_TURN_LENGTH = 1900;

long dataInterval = 50; // 20 Hz
long prevDataMillis = 0;
long xCheckInterval = 20; // 50 Hz
long prevCheckMillis = 0;
long blinkInterval = 100; // 10 Hz
long prevBlinkVibeMillis = 0;
int blinkVibeInterval = 0;
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
int redLedPin = 9; //D11, 15 of 20
int blueLedPin = 10; //D10, 14 of 20
int greenLedPin = 11; //D9, 13 of 20
int vibePin = 6; //D6, 7 of 20 // ohh no, pin 5 and 6 are controlled by timer 0 => side effects on millis()
int onboardLEDPin = 8; // D8, red LED on seeduino film

//LED handling
int ledState;
boolean vibeState = LOW;

//debug
boolean testing = true; // actually it's specifically testing sensors
boolean testingLights = false;

void setup() {
  initOutputs();
  // blink every color, buzz
  //debugCycle();

  prevCheckMillis = prevTurnMillis = prevDataMillis = prevBlinkVibeMillis = millis();

  turnLength = DEFAULT_TURN_LENGTH;
  xbee.begin(9600);
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
  pinMode(onboardLEDPin, OUTPUT);
  pinMode(vibePin, OUTPUT);
  color(0, 0, 0);
  analogWrite(vibePin, 0);
  ledState = LOW;
}

void debugCycle() {
  // Red + debug
  color(255,0,0);
  digitalWrite(onboardLEDPin, 1);
  delay(500);
  digitalWrite(onboardLEDPin, 0);
  delay(500);

  // Green + debug
  color(0,255,0);
  digitalWrite(onboardLEDPin, 1);
  delay(500);
  digitalWrite(onboardLEDPin, 0);
  delay(500);

  // Blue + debug
  color(0,0,255);
  digitalWrite(onboardLEDPin, 1);
  delay(500);
  digitalWrite(onboardLEDPin, 0);
  delay(500);
  color(0,0,0);

  // Vibe + debug
  analogWrite(vibePin, 255); 
  digitalWrite(onboardLEDPin, 1);
  delay(500);
  digitalWrite(onboardLEDPin, 0);
  delay(500);
  analogWrite(vibePin, 0); 
}

void loop() {
//  //currentMillis = millis();
//  if (millis() - prevCheckMillis > xCheckInterval) {
//    checkXbee();
//    prevCheckMillis = millis();
//  }
//  //if (currentMillis - prevTurnMillis > turnLength) running = false;
//  if (waiting) {
//   //check delay and if it's past, start running. 
//   if (millis() - prevTurnMillis > initialDelay) {
//     running = true;
//     blinking = true;
//     waiting = false;
//   }
//  }
//  if (running) {
//    // Turned off capacitive sensing. kintel 20111013.
//    //    readCapSense();


    // TODO calc average over several measurements
    dataInterval=20;
    readProx();
    if (millis() - prevDataMillis > dataInterval) {
      send_data();  
      if (testing) {
//        Serial.println("Min");
//        Serial.println((int) minProx);
//        Serial.println("Max");
//        Serial.println((int) maxProx);
      }
      prevDataMillis = millis();
    }
    
//    if (blinking) {
//      if (millis() - prevBlinkVibeMillis > blinkInterval) {
//        doBlink(); 
//        prevBlinkVibeMillis = millis();
//      }
//    }
//    if (millis() - prevTurnMillis > turnLength) {
//      running = false;
//      stopVibe();
//      if (testing) {
//        turnNum++;
//        running = true; 
//      }
//      if (blinking) {
//        color(0,0,0);
//        blinking = false; 
//      }
//    }
//  }
}

void blinkVibe(int *onOffSequence, int len) {
  if (len % 2 != 0) {
    Serial.println("We need an even number of on/off time intervals.");
    debugCycle();
  }
  else {
    for(int i = 0; i < len; i+=2) {
      int millisOff = onOffSequence[i];
      int millisOn = onOffSequence[i+1];
      blinkVibe(millisOff, millisOn);
    }
  }
}

void blinkVibe(int millisOff, int millisOn) {
  if (millis() - prevBlinkVibeMillis > blinkVibeInterval) {
    prevBlinkVibeMillis = millis();
    if (vibeState == HIGH) {
      vibeState = LOW;
      analogWrite(vibePin, 0);
      blinkVibeInterval = millisOff;
    } 
    else if (vibeState == LOW) {
      vibeState = HIGH;
      analogWrite(vibePin, 255);
      blinkVibeInterval = millisOn;
    } 
  }  
}
  
void checkXbee() {
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) {
     get_data(); 
  }
}

void readProx() {
  proxReading = analogRead(proxPin);
  if (proxReading < minProx) minProx = proxReading; // is between 0 and 1023
  if (proxReading > maxProx) maxProx = proxReading;
  
  if (proxReading < proxBaseline) proxReading = 0;
  else proxReading -= proxBaseline;
  doVibe();
  if (testing) {
//    Serial.println("Prox reading");
//    Serial.println((int) proxReading);
  }
}

// TODO: rescale?
// prox readings go from approx 60 - 720
void doVibe() {
  if (proxReading > 400) {
    //analogWrite(vibePin, 255);
    int len = 6;
    int onOffSequence[] = { 100, 100, 300, 300, 500, 500 };
    blinkVibe(onOffSequence, 6);
    //blinkVibe(250, 250); // the motor cannot do faster than 300 
  }
  else if (proxReading > 300) {;
    int len = 6;
    int onOffSequence[] = { 100, 100, 300, 300, 700, 400 };
    blinkVibe(onOffSequence, 6);
    //analogWrite(vibePin, 200);
  }
  else if (proxReading > 200) {;
    int len = 6;
    int onOffSequence[] = { 100, 100, 300, 300, 800, 300 };
    blinkVibe(onOffSequence, 6);
    //analogWrite(vibePin, 150);
  }
  else if (proxReading > 100) {;
    int len = 6;
    int onOffSequence[] = { 100, 100, 300, 300, 900, 200 };
    blinkVibe(onOffSequence, 6);
    //analogWrite(vibePin, 100); 
  }
  else {
    analogWrite(vibePin, 0); 
  }
}

void stopVibe() {
  analogWrite(vibePin, 0); 
}

void doBlink() {
  if (ledState == LOW) {
    color(255, 255, 255);
    ledState = HIGH;
  }
  else if (ledState == HIGH) {
    color(0, 0, 0);
    ledState = LOW;
  }
}

void setLeds(int firstbyte, int secondbyte) {
  if (firstbyte & ledFilter1) {
    //start blinking and sensing.
    color(255, 255, 255);
    ledState = HIGH;
    blinking = true;
    waiting = true;
    //if (testingLights) Serial.println("sensing now.");
  }
  // Temporarily disabled "warning" lights for patches becoming active 
  // next turns. kintel 20111013
  else {
    //nothing anytime soon
    color(0,0,0);
    ledState = LOW;
    blinking = false;
    running = false;
    waiting = false;
    //if (testingLights) Serial.println("sensing in three or more turns.");
  }
  if (testing) running = true;
}

void send_data() {
  outPacket[0] = PROX_IN_PACKET_TYPE;
  //outPacket[1] = uint8_t(myAddress << 1 | (int)touched);
  outPacket[1] = uint8_t(myAddress << 1);
  outPacket[2] = uint8_t(turnNum >> 8);
  outPacket[3] = uint8_t(turnNum);
  outPacket[4] = uint8_t(proxReading >> 8);
  outPacket[5] = uint8_t(proxReading);
  tx = Tx16Request(base_address, outPacket, g_outPacketSize);
  if (testing) {
//    Serial.print((int)millis()+"\t");
//    Serial.print(turnNum+"\t");
//    Serial.print((int)outPacket[1]+"\t");
//    Serial.print(touched+"\t");
//    Serial.println("Prox reading");
//    Serial.println((int)proxReading);
  }
  xbee.send(tx);
}

void ack_config() {
  static uint8_t configAck[g_configAckSize];
  configAck[0] = CONFIG_ACK_PACKET_TYPE;
  configAck[1] = uint8_t(myAddress);
  configAck[2] = uint8_t(turnLength >> 8);
  configAck[3] = uint8_t(turnLength);
  tx = Tx16Request(base_address, configAck, g_configAckSize);
  if (testing) {
    //Serial.print("Turn length \t");
    //Serial.println(turnLength); 
  }
  xbee.send(tx);
  digitalWrite(onboardLEDPin, 1);
}

void get_data() {
  if (xbee.getResponse().getApiId() == RX_16_RESPONSE) {
    int packet_cnt = 0;
    xbee.getResponse().getRx16Response(rx);
    if (rx.getData(0) == PROX_OUT_PACKET_TYPE) {
      while (packet_cnt < g_inPacketSize) {
        inPacket[packet_cnt] = rx.getData(packet_cnt++);
      }
      setLeds(inPacket[3], inPacket[4]);
      turnNum = inPacket[1] << 8 | inPacket[2];
      turnSeqNum = 0;
      prevTurnMillis = millis();
    }
    else if (rx.getData(0) == CONFIG_OUT_PACKET_TYPE) {
      while (packet_cnt < g_configPacketSize) {
        configPacket[packet_cnt] = rx.getData(packet_cnt++);
      }
      int stepLength = configPacket[1] << 8 | configPacket[2];
      turnLength = stepLength-10;
      ack_config();
    }
  }
}

void color(unsigned char red, unsigned char green, unsigned char blue) {
  analogWrite(redLedPin, 255-red);	 
  analogWrite(blueLedPin, 255-blue);
  analogWrite(greenLedPin, 255-green);
}
