#include <XBee.h>

#define VIBE_IN_PACKET_TYPE 7
#define VIBE_IN_PACKET_LENGTH 3
#define VIBE_STATE_PACKET_TYPE 9
#define VIBE_STATE_PACKET_LENGTH 4
#define THRESHOLD 1500

// THIS IS THE VIBE CODE - BOARD CHOICE IS
// Lilypad Arduino w/ ATmega328

const int vibePin = 3;
const int ledPin = 13;

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

//change for each player

// VIBE1_PLAYER1
//static int myAddress = 5;

// VIBE2_PLAYER1
//static int myAddress = 6;

// VIBE1_PLAYER2
static int myAddress = 13;

// VIBE2_PLAYER2
//static int myAddress = 14;

const int g_inPacketSize = VIBE_STATE_PACKET_LENGTH; // seems right, msg type + one value.
const int listenType = VIBE_STATE_PACKET_TYPE;
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
  Serial.print("glove_playtest_march (addr = ");
  Serial.print(myAddress);
  Serial.println(")");
  pinMode(ledPin, OUTPUT);
  pinMode(vibePin, OUTPUT);

  analogWrite(vibePin, 255);
  digitalWrite(ledPin, true);
  delay(1000);
  analogWrite(vibePin, 0);
  digitalWrite(ledPin, false);
}

void loop()
{
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) // got something
  {
    get_data();
  } 

  blinkVibe();
}


void send_data()
{
  outPacket[0] = outType;
  outPacket[1] = uint8_t(myAddress);
  outPacket[2] = 0;
  tx = Tx16Request(base_address, outPacket, g_outPacketSize);
  xbee.send(tx);
}

void get_data()
{
  prevMsgReceived = millis();
  if (xbee.getResponse().getApiId() == RX_16_RESPONSE)
  {
    int packet_cnt = 0;
    xbee.getResponse().getRx16Response(rx16);
    while (packet_cnt < g_inPacketSize)
    {
      inPacket[packet_cnt] = rx16.getData(packet_cnt++);
    } 
    if (inPacket[0] == listenType)
    {
      digitalWrite(ledPin, true); // Turn on on first received package
      uint16_t period  = inPacket[1] << 8 | inPacket[2];
      uint8_t duty = inPacket[3];
      uint16_t millisOn = 1L * period * duty / 255;
      vibeBlinker.init(millisOn, period - millisOn);
    }
  }
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
