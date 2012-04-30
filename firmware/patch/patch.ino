// Airplanes

// THIS IS THE PATCH CODE - BOARD CHOICE IS
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328

#include <XBee.h>

/* #define DEBUG */
#define DEBUG_LED

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

/* ---- Pin List ---- */
#define RED_LED_PIN    8 //D11, 15 of 20
#define BLUE_LED_PIN   10 //D10, 14 of 20
#define GREEN_LED_PIN  9 //D9, 13 of 20
#define VIBE_PIN       6 //D6, 7 of 20
#define STATUS_LED_PIN 7 // D8, red LED on seeduino film

/* ---- Protocol ---- */
#define PACKET_TYPE 0
#define PROX_STATE_PACKET_TYPE   8
#define VIBE_STATE_PACKET_TYPE   9

#define PROX_IN_PACKET_TYPE    2 // Sending this
#define CONFIG_OUT_PACKET_TYPE 5 // Listening for this
#define CONFIG_ACK_PACKET_TYPE 6

// Communication variables
#define g_outPacketSize    6
#define g_configPacketSize 3
#define g_configAckSize    4

#define RED_COLOR  1
#define BLUE_COLOR 2

uint8_t outPacket[g_outPacketSize];
uint8_t configPacket[g_configPacketSize];
int packet_cnt;

XBee xbee = XBee();
Rx16Response rx = Rx16Response();
Tx16Request tx;
TxStatusResponse txStatus = TxStatusResponse();
uint16_t base_address = 0;

uint8_t rgb[3] = {0, 0, 0};
bool active = false;

//Communication -- addressing (need to be changed for each patch)
//PROX1_PLAYER1
int myColor = RED_COLOR;
int myAddress = 1;
int initialDelay = 0; //for staggering messages from sensors to avoid packet collision
int ledFilter1 = 0x80; //128, 64, 32, and 16 -- for higher order bits
int ledFilter2 = 0x08; //8, 4, 2, and 1 -- for lower order bits

long dataInterval = 50; // 20 Hz max
long prevDataMillis = 0;

long xCheckInterval = 20; // 50 Hz max
long prevCheckMillis = 0;

long turnLength; // will have to be set by config message

// Sensing
int proxPin = 4; //A4, 18 of 20
int proxBaseline = 250;
int proxReading = 0;
int touchThreshold = 1250;
//might need to establish running average for capsense and look for spikes

uint8_t frameId = 0;

/* struct Blinker { */
/* 	bool _state; */
/* 	uint16_t millisOn; */
/* 	uint16_t millisOff; */
/* 	uint16_t interval; */
/* 	unsigned long prevMillis; */

/* Blinker() : _state(false), millisOn(0), millisOff(0), interval(100), prevMillis(0) {} */

/* 	void init(uint16_t millisOn, uint16_t millisOff) { */
/* 		this->millisOn = millisOn; */
/* 		this->millisOff = millisOff; */
/* 	} */

/* 	bool state() { */
/* 		if (millis() - prevMillis > interval) { */
/* 			if (!_state && millisOn > 0) { */
/* 				_state = true; */
/* 				setInterval(millisOn); */
/* 			} */
/* 			else { */
/* 				_state = false; */
/* 				setInterval(millisOff); */
/* 			} */
/* 		} */
/* 		return _state; */
/* 	} */

/* 	void setInterval(uint16_t newinterval) { */
/* 		prevMillis = millis(); */
/* 		interval = newinterval; */
/* 	} */
/* }; */

void setup() {
	pinMode(RED_LED_PIN, OUTPUT);
	pinMode(BLUE_LED_PIN, OUTPUT);
	pinMode(GREEN_LED_PIN, OUTPUT);
	pinMode(STATUS_LED_PIN, OUTPUT);
	pinMode(VIBE_PIN, OUTPUT);

	color(0, 0, 0);
	vibe(0);

	prevCheckMillis = prevDataMillis = millis();
	packet_cnt = 0;

	xbee.begin(9600);

#ifdef DEBUG
	Serial.begin(9600);//testing*/

	Serial.print("patch_playtest_march (addr = ");
	Serial.print(myAddress);
	Serial.println(")");
#endif
}

/**
 * Blinks the status according the a specific delay to display error statuses.
 */
void debug_blink(uint8_t d) {
	statusLED(1);
	delay(d);
	statusLED(0);
	delay(d);
}

void loop() {
	xbee.readPacket(1000); // Read packet, return after 500ms timeout

	XBeeResponse resp = xbee.getResponse(); // Get the response
	if(resp.isAvailable()) { // Response is valid and readable
		get_data(resp); // Parse
	}
#ifdef DEBUG_LED // Status LED blinking
	else if(resp.isError()) { // Response contains an error
		// Blink StatusLED for debugging.
		uint8_t error_code = resp.getErrorCode();
		switch(error_code) {
		case CHECKSUM_FAILURE:
			debug_blink(200);
			break;
		case PACKET_EXCEEDS_BYTE_ARRAY_LENGTH:
			debug_blink(800);
			break;
		case UNEXPECTED_START_BYTE:
			debug_blink(1000);
			break;
		}
	}
#endif
	else {
		// dot dot dot
	}
	

	if(active) { // transmit
		if(millis() - prevDataMillis > dataInterval) { // Maximum transmition speed
			proxReading = analogRead(proxPin);
			if(proxReading < proxBaseline) proxReading = 0;  // Game logic?
			else proxReading -= proxBaseline;

			send_data();
			prevDataMillis = millis();
		}
	}

	updateVibe();
	updateLEDs();
	/* delay(10); // Ya?  */
}

/* ---- Xbee ---- */
void send_data() {
	outPacket[0] = PROX_IN_PACKET_TYPE;
	outPacket[1] = uint8_t(myAddress << 1);
	outPacket[2] = 0;
	outPacket[3] = 0;
	outPacket[4] = uint8_t(proxReading >> 8);
	outPacket[5] = uint8_t(proxReading);
	tx = Tx16Request(base_address, outPacket, g_outPacketSize);

#ifdef DEBUG
	Serial.print((int)millis()+"\t");
	Serial.print((int)outPacket[1]+"\t");
	Serial.println(proxReading);
#endif
		
	xbee.send(tx);
}

void ack_config() {
	static uint8_t configAck[g_configAckSize];
	configAck[0] = CONFIG_ACK_PACKET_TYPE;
	configAck[1] = uint8_t(myAddress);
	configAck[2] = uint8_t(turnLength >> 8);
	configAck[3] = uint8_t(turnLength);
	tx = Tx16Request(base_address, ACK_OPTION, configAck, g_configAckSize, frameId++);

#ifdef DEBUG	
		Serial.print("Turn length \t");
		Serial.println(turnLength);
		Serial.println("ack_config()");
#endif

	xbee.send(tx);
	statusLED(1);
}

void get_data(XBeeResponse resp) {
#ifdef DEBUG
	Serial.println("get_data()");
#endif
	if(resp.getApiId() == RX_16_RESPONSE) {
#ifdef DEBUG
		Serial.println("RX_16_RESPONSE");
#endif		
				
		resp.getRx16Response(rx); // Get RX response
		uint8_t * pckt = rx.getData(); // Retrive RX data

		if(pckt[PACKET_TYPE] == VIBE_STATE_PACKET_TYPE) { // Vibration motor update packet
			// Vibe: set period (16 bits) and duty cycle
#define VIBE_PERIOD_MSB 1
#define VIBE_PERIOD_LSB 2
#define VIBE_DUTY       3
			statusLED(1); 
			uint16_t period  = pckt[VIBE_PERIOD_MSB] << 8 | pckt[VIBE_PERIOD_LSB];
			uint8_t duty = pckt[VIBE_DUTY];
			uint16_t millisOn = 1L * period * duty / 255;
			/* /\* vibeBlinker.init(millisOn, period - millisOn); *\/ */

		}
		else if(pckt[PACKET_TYPE] == PROX_STATE_PACKET_TYPE) { // Proximity sensor update packet
			// Prox: set active, set color and blinking on LEDs
#define PROX_ACTIVE     1
#define PROX_RED        2
#define PROX_GREEN      3
#define PROX_BLUE       4
#define PROX_PERIOD_MSB 5
#define PROX_PERIOD_LSB 6
#define PROX_DUTY       7

			active = pckt[PROX_ACTIVE];
			rgb[0] = pckt[PROX_RED];
			rgb[1] = pckt[PROX_GREEN];
			rgb[2] = pckt[PROX_BLUE];
			uint16_t period  = pckt[PROX_PERIOD_MSB] << 8 | pckt[PROX_PERIOD_LSB];
			uint8_t duty = pckt[PROX_DUTY];
			uint16_t millisOn = 1L * period * duty / 255;
			/* colorBlinker.init(millisOn, period - millisOn); */
		}
#ifdef DEBUG
		else {
			Serial.print("Unknown packet type: ");
			Serial.println(pckt[PACKET_TYPE], HEX);
		}
#endif
	}
#ifdef DEBUG
	else if(resp.getApiId() != TX_STATUS_RESPONSE) {
		Serial.print("Unknown API ID: ");
		Serial.println(resp.getApiId(), HEX);
	}
#endif
}

/* ---- Blinking ---- */

void updateVibe() {
	/* if (vibeBlinker.state()) { */
	/* 	statusLED(1); */
	/* } else { */
	/* 	vibe(0); */
	/* 	statusLED(0); */
	/* } */
}

void updateLEDs() {
	/* if (colorBlinker.state()) { */
	/* 	color(rgb[0], rgb[1], rgb[2]); */
	/* } else { */
	/* 	color(0, 0, 0); */
	/* } */
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

// James Cameron
