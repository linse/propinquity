/**
 * Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328
 *
*/

#include <XBee.h>
// #include <TimerOne.h>

/* ---- Pin List ---- */

#define RED_LED_PIN    8
#define GREEN_LED_PIN  9
#define BLUE_LED_PIN   10
#define VIBE_PIN       6
#define STATUS_LED_PIN 7
#define PROX_PIN       4

/* ---- Protocol ---- */

#define BASE_ADDR      0

#define CONF_PACKET    1
#define COLOR_PACKET   2
#define VIBE_PACKET    3

#define PROX_PACKET    4

XBee xbee = XBee();
Tx16Request tx;
Rx16Response rx = Rx16Response();

uint8_t out_flag = 0;

/* ---- State ----- */

uint8_t active = 0;

uint8_t rgb[3] = {0, 0, 0};

int vibe_level = 0;

int prox_val = 0;
int prox_adju = 250;

void setup() {
	pinMode(RED_LED_PIN, OUTPUT);
	pinMode(BLUE_LED_PIN, OUTPUT);
	pinMode(GREEN_LED_PIN, OUTPUT);
	pinMode(STATUS_LED_PIN, OUTPUT);
	pinMode(VIBE_PIN, OUTPUT);

	color(0, 0, 0);
	vibe(0);

	// Timer1.initialize(100000);
	// Timer1.attachInterrupt(callback);

	xbee.begin(9600);
}

void callback() {
	out_flag = 1;
}

void loop() {
	for(int i = 0;i < 255;i++) {
		color(i, 0, 0);
		delay(5);
	}

	color(0, 0, 0);

	for(int i = 0;i < 255;i++) {
		color(0, i, 0);
		delay(5);
	}

	color(0, 0, 0);

	for(int i = 0;i < 255;i++) {
		color(0, 0, i);
		delay(5);
	}

	// color(0, 0, 0);

	// xbee.readPacket(); // Read packet, return after 500ms timeout

	// XBeeResponse response = xbee.getResponse();
	// if(response.isAvailable() && response.getApiId() == RX_16_RESPONSE) {
	// 	response.getRx16Response(rx); // Get RX response
	// 	parse_data(rx.getData(), rx.getDataLength());
	// }

	// if(active && out_flag) {
	// 	prox_val = analogRead(PROX_PIN);
	// 	if(prox_val < prox_adju) prox_val = 0;
	// 	else prox_val -= prox_adju;

	// 	send_data();

	// 	out_flag = 0;
	// }

	// updateVibe();
	// updateLEDs();
}

/* ---- Xbee ---- */

void send_data() {
	uint8_t outPacket[3];

	outPacket[0] = PROX_PACKET;
	outPacket[1] = uint8_t(prox_val >> 8);
	outPacket[2] = uint8_t(prox_val);

	tx = Tx16Request(BASE_ADDR, outPacket, 3);

	xbee.send(tx);
}

void parse_data(uint8_t* data, uint8_t len) {
	if(data[0] == CONF_PACKET && len > 1) {
		if(data[1]) active = 1;
		else active = 0;
	} else if(data[0] == COLOR_PACKET && len > 3) {
		rgb[0] = data[1];
		rgb[1] = data[2];
		rgb[2] = data[3];
	} else if(data[0] == VIBE_PACKET && len > 1) {
		vibe_level = data[1];
	}
}

/* ---- Blinking ---- */

void updateVibe() {
	vibe(vibe_level);
}

void updateLEDs() {
	color(rgb[0], rgb[1], rgb[2]); 
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
