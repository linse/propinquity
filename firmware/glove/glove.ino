/**
 * Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328
 *
 */

#include <XBee.h>
#include <Timer.h>

/* ---- Pin List ---- */

#define VIBE_PIN       3
#define STATUS_LED_PIN 13

/* ---- Protocol ---- */

 #define BASE_ADDR      0

 #define PROX_PACKET    1

 #define CONF_PACKET    2
 #define CLEAR_PACKET   3

 #define COLOR_PACKET   4
 #define COLOR_DUTY_PACKET     5
 #define COLOR_PERIOD_PACKET   6

 #define VIBE_PACKET    7
 #define VIBE_DUTY_PACKET      8
 #define VIBE_PERIOD_PACKET    9

/* ---- Timer ---- */

 // Timer
 Timer t;

 uint16_t time_counter = 0;

/* ---- XBee/Wireless ---- */

XBee xbee = XBee();
Tx16Request tx;
Rx16Response rx = Rx16Response();

uint8_t send_flag = 0;
uint8_t send_period = 10;

/* ---- Vibe ----- */

uint8_t active = 0;

int vibe_level = 0;

boolean vibe_on = true;
uint8_t vibe_duty = 0;
uint8_t vibe_period = 0;

void setup() {
	pinMode(VIBE_PIN, OUTPUT);

	setActive(0);
	vibe(0);

	t.every(10, timerCallback);

	xbee.begin(38400);
}

/**
 * Set the data out flag every ~10ms.
 */
void timerCallback() {
	if((time_counter % send_period) == 0) send_flag = 1; // Every 10ms

	if(vibe_period == 0 || vibe_duty == 255) vibe_on = true;
	else if((time_counter % vibe_period) < (uint8_t)(vibe_duty*vibe_period/255)) vibe_on = true;
	else vibe_on = false;
	
	time_counter++;
}


void loop() {
	t.update();

	xbee.readPacket(); // Read packet, return after 500ms timeout

	XBeeResponse response = xbee.getResponse();
	if(response.isAvailable()) {
		if(response.getApiId() == RX_16_RESPONSE) {
			response.getRx16Response(rx); // Get RX response
			parse_data(rx.getData(), rx.getDataLength());
		}
	}
	
	if(active) updateVibe();
	else vibe(0);
}

void reset() {
	vibe_level = 0;

	vibe_on = false;
	vibe_duty = 0;
	vibe_period = 0;

	vibe(0);
}

/* ---- Xbee ---- */

void parse_data(uint8_t* data, uint8_t len) {
	if(data[0] == CONF_PACKET && len > 1) {
		setActive(data[1]);
	} else if(data[0] == CLEAR_PACKET) {
		vibe_level = 0;
		vibe_period = 0;
		vibe_duty = 0;
	} else if(data[0] == VIBE_PACKET && len > 1) {
		vibe_level = data[1];
	} else if(data[0] == VIBE_DUTY_PACKET && len > 1) { 
		vibe_duty = data[1]; 
	} else if(data[0] == VIBE_PERIOD_PACKET && len > 1) { 
		vibe_period = data[1]; 
	} 
}

void setActive(int val) {
	if(val) {
		active = 1;
		statusLED(1);
	} else {
		active = 0;
		statusLED(0);
	}
}

/* ---- Blinking ---- */

void updateVibe() {
	if(vibe_on) vibe(vibe_level);
	else vibe(0);
}

/* ---- Low Level ---- */

void vibe(unsigned char level) {
	analogWrite(VIBE_PIN, level);
}

void statusLED(unsigned char state) {
	digitalWrite(STATUS_LED_PIN, state);
}
