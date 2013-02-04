/**
 * Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328
 *
 */

#include <Timer.h>
#include <Wire.h>
#include <XBee.h>
#include "MMA8452Q.h"
// #include <TimerOne.h>

/* ---- Protocol ---- */

//TODO: Use enum instead and use case statement below

#define BASE_ADDR             0

#define PROX_PACKET           1
#define ACCEL_XYZ_PACKET      2
#define ACCEL_INT0_PACKET     3
#define ACCEL_INT1_PACKET     4

#define MODE_PACKET           5
#define CLEAR_PACKET          6

#define COLOR_PACKET          7
#define COLOR_DUTY_PACKET     8
#define COLOR_PERIOD_PACKET   9

#define VIBE_PACKET           10
#define VIBE_DUTY_PACKET      11
#define VIBE_PERIOD_PACKET    12

/* ---- Pin List ---- */

#define RED_LED_PIN    10
#define GREEN_LED_PIN  9
#define BLUE_LED_PIN   5

#define VIBE_PIN       6

#define PROX_PIN       A7 // Pin 21 Not available on normal arduino

/* ---- Accel ---- */

#define ADDR_DEVICE 0x1C

uint8_t accel_ok = 0;

uint8_t xyz[3];

uint8_t accel_flag = 0;
uint8_t accel_period = 10;

/* ---- Prox ----*/

#define PROX_AVG_LEN 8

uint8_t prox_val_pointer = 0;
uint16_t avg_prox_val = 0;
uint16_t prox_val[PROX_AVG_LEN];

uint8_t prox_flag = 0;
uint8_t prox_period = 10;

/* ---- Vibe/LEDs ----- */

uint8_t rgb[3] = {0, 0, 0};

boolean led_on = true;
uint8_t led_duty = 0;
uint8_t led_period = 0;

uint8_t vibe_level = 0;

boolean vibe_on = true;
uint8_t vibe_duty = 0;
uint8_t vibe_period = 0;

/* ---- Global ---- */

#define EN_MODE 0
#define PROX_MODE 1
#define ACCEL_XYZ_MODE 2

uint8_t mode = 0;

/* ---- Timer ---- */

Timer t;
uint16_t time_counter = 0;

/* ---- XBee/Wireless ---- */

XBee xbee = XBee();
Tx16Request tx;
Rx16Response rx = Rx16Response();

void setup() {
	pinMode(RED_LED_PIN, OUTPUT);
	pinMode(BLUE_LED_PIN, OUTPUT);
	pinMode(GREEN_LED_PIN, OUTPUT);

	pinMode(VIBE_PIN, OUTPUT);

	pinMode(PROX_PIN, INPUT);

	pinMode(SDA, INPUT);
	pinMode(SCL, INPUT);	

	reset();

	Wire.begin(); // join i2c bus

	accel_reset();
	accel_config();

	t.every(10, timerCallback);

	// Timer1.initialize(1000); //1000 microseconds -> 10 milliseconds
	// Timer1.attachInterrupt(timerCallback); // attach the service routine here

	// Serial.begin(9600);
	xbee.begin(38400);
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

	if(!(mode & (1 << EN_MODE))) {
		//Disabled, lights and vibe off
		vibe(0);
		color(0, 0, 0);
	} else {
		if((mode & (1 << PROX_MODE)) && prox_flag) {
			//Prox enable
			prox_val[prox_val_pointer] = analogRead(PROX_PIN);
			prox_val_pointer = (prox_val_pointer+1)%PROX_AVG_LEN;

			avg_prox_val = 0;
			for(uint8_t i = 0;i < PROX_AVG_LEN;i++) {
				avg_prox_val += prox_val[i];
			}

			avg_prox_val = avg_prox_val >> 3; //Divide by 8

			send_prox(avg_prox_val);

			prox_flag = 0;
		}

		if((mode & (1 << ACCEL_XYZ_MODE)) && accel_flag) {
			Wire.beginTransmission(ADDR_DEVICE);
			Wire.write(OUT_X_MSB);
			Wire.endTransmission(false); //Do not send stop condition, this will send a repeat start instead

			Wire.requestFrom(ADDR_DEVICE, 3);

			for(int i = 0;i < 3;i++) {
				xyz[i] = Wire.read();
			}
			
			send_accel_XYZ(xyz[0], xyz[1], xyz[2]);

			accel_flag = 0;
		}

		updateVibe();
		updateLEDs();
	}
}

/**
 * Set the data out flag every ~10ms.
 */
void timerCallback() {
	if((time_counter % prox_period) == 0) prox_flag = 1;
	if((time_counter % accel_period) == 0) accel_flag = 1;

	if(led_period == 0 || led_duty == 255) led_on = true;
	else if((time_counter % led_period) < (uint8_t)(led_duty*led_period/255)) led_on = true;
	else led_on = false;

	if(vibe_period == 0 || vibe_duty == 255) vibe_on = true;
	else if((time_counter % vibe_period) < (uint8_t)(vibe_duty*vibe_period/255)) vibe_on = true;
	else vibe_on = false;
	
	time_counter++;
}

/**
 * Reset patch
 */
void reset() {
	accel_flag = 0;
	prox_flag = 0;

	avg_prox_val = 0;
	for(uint8_t i = 0;i < PROX_AVG_LEN;i++) prox_val[i] = 0;

	vibe_level = 0;

	vibe_on = false;
	vibe_duty = 0;
	vibe_period = 0;

	rgb[0] = rgb[1] = rgb[2] = 0;

	led_on = false;
	led_duty = 0;
	led_period = 0;

	color(0, 0, 0);
	vibe(0);

	setMode(0);
}

/* ---- Accel ---- */

void accel_reset() {
	accel_ok = 0;

	//Reset the accelerometer
	accel_reg_write(CTRL_REG2, (1 << RST));

	//Wait for reset to complete (blink yellow for wait state)
	int timer = 0;

	while(timer < 1000) { //Wait up to 1 second for reset
		if((accel_reg_read(CTRL_REG2) & (1 << RST)) != 0) {
			accel_ok = 1;
			break;
		}

		if(timer % 200 < 100) color(20, 20, 0); //Blink yelow
		else color(0, 0, 0);

		timer++;
		delay(1);
	}

	if(accel_ok) {
		//Green for ok
		color(0, 40, 0);
		delay(250);
	} else {
		//Red for fail
		color(40, 0, 0);
		delay(250);
	}
}

void accel_config() {	
	//IMPORTANT: all registers must be set with the accelerometer in STANDBY

	accel_enable(0);

	accel_reg_write(CTRL_REG1,  (1 << DR1) | (1 << DR0) | (1 << F_READ)); //8 bit mode
	accel_reg_write(XYZ_DATA_CFG, (1 << HPF_OUT) | (0 << FS1) | (1 << FS0)); //Set range +-4g, output high pass data
	accel_reg_write(HP_FILTER_CUTOFF, (1 << SEL1)); //High pass second lowest setting

	//Motion

	//Set the freefall/motin configuration registers to detect motion events on XYZA
	// accel_reg_set(FF_MT_CFG,
	// 	(1 << FF_MT_ELE) |
	// 	(1 << OAE) | //Dection mode 1,1 (see datasheet)
	// 	(1 << ZEFE) |
	// 	(1 << YEFE) |
	// 	(1 << XEFE) //Enable XYZ axis
	// );
	// accel_reg_set(FF_MT_THS, (uint8_t)(
	// 	(0 << DBCNTM) | //Decrement debounce counter when motion falls below threshold
	// 	((MOTION_THRESHOLD) << THS) //Motion threshold (7 bits)
	// ));
	
	// accel_reg_set(FF_MT_COUNT, MOTION_DEBOUNCE_COUNT); //Debounce count

	// // Transient

	// //Set the freefall/motin configuration registers to detect motion events on XYZA
	// accel_reg_set(TRANSIENT_CFG,
	// 	(1 << T_ELE) | //Event latching, clean on read
	// 	(1 << ZTEFE) |
	// 	(1 << YTEFE) |
	// 	(1 << XTEFE) | //Enable XYZ axis
	// 	(0 << HPF_BYP) //Keep the high pass
	// );
	// accel_reg_set(TRANSIENT_THS, (uint8_t)(
	// 	(0 << DBCNTM) | //Decrement debounce counter when motion falls below threshold
	// 	((TRANS_THRESHOLD) << THS) //Motion threshold (7 bits)
	// ));
	
	// accel_reg_set(TRANSIENT_COUNT, TRANS_DEBOUNCE_COUNT); //Debounce count

	accel_enable(1);
}

void accel_enable(uint8_t enable) {
	uint8_t cur_val = accel_reg_read(CTRL_REG1); //TODO: Check that this actually "save" the previous value properly

	if(enable) {
		accel_reg_write(CTRL_REG1, cur_val | (1 << ACTIVE)); //Active mode
	} else {
		accel_reg_write(CTRL_REG1, cur_val & ~(0 << ACTIVE)); //Standby mode
	}
}

void accel_reg_write(uint8_t reg_addr, uint8_t data) {
	Wire.beginTransmission(ADDR_DEVICE);
	Wire.write(reg_addr);
	Wire.write(data);
	Wire.endTransmission();
}

uint8_t accel_reg_read(uint8_t reg_addr) { 
	Wire.beginTransmission(ADDR_DEVICE);
	Wire.write(reg_addr);
	Wire.endTransmission(false); //Do not send stop condition, this will send a repeat start instead
	Wire.requestFrom(ADDR_DEVICE, 1);
	byte status = Wire.read();
}

/* ---- Xbee ---- */

void send_accel_XYZ(uint8_t x, uint8_t y, uint8_t z) {
	uint8_t outPacket[4];

	outPacket[0] = ACCEL_XYZ_PACKET;
	outPacket[1] = x;
	outPacket[1] = y;
	outPacket[1] = z;

	tx = Tx16Request(BASE_ADDR, outPacket, 4);

	xbee.send(tx);
}

void send_prox(uint16_t val) {
	uint8_t outPacket[3];

	outPacket[0] = PROX_PACKET;
	outPacket[1] = uint8_t(val >> 8);
	outPacket[2] = uint8_t(val);

	tx = Tx16Request(BASE_ADDR, outPacket, 3);

	xbee.send(tx);
}

void parse_data(uint8_t* data, uint8_t len) {
	if(data[0] == MODE_PACKET && len > 1) {
		setMode(data[1]);
	} else if(data[0] == CLEAR_PACKET) {
		reset();
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

void setMode(uint8_t new_mode) {
	if(new_mode) {
		mode = new_mode;
	} else {
		mode = 0;
		for(uint8_t i = 0;i < PROX_AVG_LEN;i++) {
			prox_val[i] = 0;
		}
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