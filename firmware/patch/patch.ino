// THIS IS THE PATCH CODE - BOARD CHOICE IS
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328

#include <XBee.h>

/* ---- Pin List ---- */

#define RED_LED_PIN 8 //D11, 15 of 20
#define BLUE_LED_PIN 10 //D10, 14 of 20
#define GREEN_LED_PIN 9 //D9, 13 of 20
#define VIBE_PIN 6 //D6, 7 of 20
#define STATUS_LED_PIN 7 // D8, red LED on seeduino film

/* ---- Communication defines ---- */

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

Blinker vibeBlinker;
Blinker colorBlinker;
uint8_t rgb[3] = {0, 0, 0};
bool active = false;

//Communication -- addressing (need to be changed for each patch)
//PROX1_PLAYER1
int myColor = RED_COLOR;
static int myAddress = 1;
static int initialDelay = 0; //for staggering messages from sensors to avoid packet collision
static int ledFilter1 = 0x80; //128, 64, 32, and 16 -- for higher order bits
static int ledFilter2 = 0x08; //8, 4, 2, and 1 -- for lower order bits

// PROX2_PLAYER1
// int myColor = RED_COLOR;
// static int myAddress = 2;
// static int initialDelay = 10; //for staggering messages from sensors to avoid packet collision
// static int ledFilter1 = 0x40; //128, 64, 32, and 16 -- for higher order bits
// static int ledFilter2 = 0x04; //8, 4, 2, and 1 -- for lower order bits

// PROX3_PLAYER1
//int myColor = RED_COLOR;
//static int myAddress = 3;
//static int initialDelay = 20; //for staggering messages from sensors to avoid packet collision
//static int ledFilter1 = 0x20; //128, 64, 32, and 16 -- for higher order bits
//static int ledFilter2 = 0x02; //8, 4, 2, and 1 -- for lower order bits

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
boolean waiting = false;

// Sensing
int proxPin = 4; //A4, 18 of 20
int proxBaseline = 250;
int proxReading = 0;
int touchThreshold = 1250;
//might need to establish running average for capsense and look for spikes

//debug
boolean testing = false; // actually it's specifically testing sensors
boolean testingLights = false;

uint8_t frameId = 0;

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

void setup() {
	pinMode(RED_LED_PIN, OUTPUT);
	pinMode(BLUE_LED_PIN, OUTPUT);
	pinMode(GREEN_LED_PIN, OUTPUT);
	pinMode(STATUS_LED_PIN, OUTPUT);
	pinMode(VIBE_PIN, OUTPUT);

	color(0, 0, 0);
	vibe(0);

	prevCheckMillis = prevTurnMillis = prevDataMillis = prevBlinkMillis = millis();
	packet_cnt = 0;

	xbee.begin(9600);

	if (testing) Serial.begin(9600);//testing*/

	Serial.print("patch_playtest_march (addr = ");
	Serial.print(myAddress);
	Serial.println(")");
}

void loop() {
	if (millis() - prevCheckMillis > xCheckInterval) {
		checkXbee();
		prevCheckMillis = millis();
	}

	if (active) {
		if (millis() - prevDataMillis > dataInterval) {
			readProx();
			send_data();
			prevDataMillis = millis();
		}
	}

	blinkVibe();
	blinkColor();
}

/* ---- Xbee ---- */

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
	outPacket[2] = 0;
	outPacket[3] = 0;
	outPacket[4] = uint8_t(proxReading >> 8);
	outPacket[5] = uint8_t(proxReading);
	tx = Tx16Request(base_address, outPacket, g_outPacketSize);
	if (testing) {
		Serial.print((int)millis()+"\t");
		Serial.print((int)outPacket[1]+"\t");
		Serial.println(proxReading);
	}

	xbee.send(tx);
}

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
	statusLED(1);
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
			statusLED(1);
			uint16_t period  = vibeStatePacket[1] << 8 | vibeStatePacket[2];
			uint8_t duty = vibeStatePacket[3];
			uint16_t millisOn = 1L * period * duty / 255;
			vibeBlinker.init(millisOn, period - millisOn);
		} else if (rx.getData(0) == PROX_STATE_PACKET_TYPE) {
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
		} else {
			Serial.print("Unknown packet type: ");
			Serial.println(rx.getData(0), HEX);
		}
	} else if (xbee.getResponse().getApiId() != TX_STATUS_RESPONSE) {
		Serial.print("Unknown API ID: ");
		Serial.println(xbee.getResponse().getApiId(), HEX);
	}
}

/* ---- Blinking ---- */

void blinkVibe() {
	if (vibeBlinker.state()) {
		statusLED(1);
	} else {
		vibe(0);
		statusLED(0);
	}
}

void blinkColor() {
	if (colorBlinker.state()) {
		color(rgb[0], rgb[1], rgb[2]);
	} else {
		color(0, 0, 0);
	}
}

/* ---- Low Level ---- */

void color(unsigned char red, unsigned char green, unsigned char blue) {
	analogWrite(RED_LED_PIN, 255-red);	 
	analogWrite(BLUE_LED_PIN, 255-blue);
	analogWrite(GREEN_LED_PIN, 255-green);
}

void vibe(unsigned char level) {
	analogWrite(VIBE_PIN, level);
}

void statusLED(unsigned char state) {
	digitalWrite(STATUS_LED_PIN, state);
}