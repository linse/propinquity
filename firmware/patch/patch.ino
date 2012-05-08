/**
 * Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328
 *
 */

#include <XBee.h>
#include <Timer.h>

// Timer
Timer t;

/* ---- Pin List ---- */

#define RED_LED_PIN    8
#define GREEN_LED_PIN  9
#define BLUE_LED_PIN   10
#define VIBE_PIN       6
#define STATUS_LED_PIN 7
#define PROX_PIN       4

/* ---- Protocol ---- */
#define BASE_ADDR      0

#define PROX_PACKET    1

#define CONF_PACKET    2

#define COLOR_PACKET   3
#define COLOR_DUTY_PACKET     4
#define COLOR_PERIOD_PACKET   5

#define VIBE_PACKET    6
#define VIBE_DUTY_PACKET      7
#define VIBE_PERIOD_PACKET    8

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

uint16_t time_counter = 0;

boolean led_on = false;
uint8_t led_duty = 255;
uint8_t led_period = 255;

boolean vibe_on = false;
uint8_t vibe_duty = 255;
uint8_t vibe_period = 255;

void setup() {
	pinMode(RED_LED_PIN, OUTPUT);
	pinMode(BLUE_LED_PIN, OUTPUT);
	pinMode(GREEN_LED_PIN, OUTPUT);
	pinMode(STATUS_LED_PIN, OUTPUT);
	pinMode(VIBE_PIN, OUTPUT);

	color(0, 0, 0);
	vibe(0);

	t.every(10, timerCallback);

	xbee.begin(9600);
}

/**
 * Set the data out flag every ~10ms.
 */
void timerCallback() {
	out_flag = 1; // Every 10ms
	
	if(led_period == 0) led_on = true;
	else if((time_counter % led_period) < (uint8_t)(led_duty*led_period/255)) led_on = true;
	else led_on = false;

	if(vibe_period == 0) vibe_on = true;
	else if((time_counter % vibe_period) < (uint8_t)(vibe_duty*vibe_period/255)) vibe_on = true;
	else vibe_on = false;
	
	time_counter++;
}


void loop() {
	t.update();

	xbee.readPacket(); // Read packet, return after 500ms timeout

	XBeeResponse response = xbee.getResponse();
	if(response.isAvailable() && response.getApiId() == RX_16_RESPONSE) {
		response.getRx16Response(rx); // Get RX response
		parse_data(rx.getData(), rx.getDataLength());
	}

	if(active && out_flag) { //This is done with a flag since the send_data won't work from inside an interrupt
		prox_val = analogRead(PROX_PIN);

		if(prox_val < prox_adju) prox_val = 0;
		else prox_val -= prox_adju;

		send_data();

		out_flag = 0;
	} else { // Turn off when inactive? sure
		// reset();
	}

	if(active) {
		updateVibe();
		updateLEDs();
	} else {
		vibe(0);
		color(0, 0, 0);
	}
}

void reset() {
	vibe_level = 0;
	vibe_on = false;
	vibe_duty = 0;
	vibe_period = 0;

	rgb[0] = rgb[1] = rgb[2] = 0;
	led_on = false;
	led_duty = 0;
	led_period = 0;
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
		if(data[1]) {
			active = 1;
			statusLED(1);
		} else {
			active = 0;
			statusLED(0);
		}
	} else if(data[0] == COLOR_PACKET && len > 3) {
		rgb[0] = data[1];
		rgb[1] = data[2];
		rgb[2] = data[3];
	} else if(data[0] == COLOR_DUTY_PACKET && len > 1) {
		led_duty = data[1];
	} else if(data[0] == COLOR_PERIOD_PACKET && len > 1) {
		led_period = data[1];
	} else if(data[0] == VIBE_PACKET && len > 1) {
		vibe_level = data[1];
	} else if(data[0] == VIBE_DUTY_PACKET && len > 1) { 
		vibe_duty = data[1]; 
	} else if(data[0] == VIBE_PERIOD_PACKET && len > 1) { 
		vibe_period = data[1]; 
	} 
}

/* ---- Blinking ---- */

void updateVibe() {
	if(vibe_on) vibe(vibe_level);
	else vibe(0);
}

void updateLEDs() {
	if(led_on) color(rgb[0], rgb[1], rgb[2]);
	else color(0, 0, 0);
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
