/**
 * Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328
 *
 */

#include <Timer.h>
#include <Wire.h>
#include <XBee.h>
#include "MMA8452Q.h"
// #include <TimerOne.h>

// #define DEBUG

/* ---- Protocol ---- */

 enum PacketType {
 	MODE_PACKET          = 0,
 	CLEAR_PACKET         = 1,

	PROX_PACKET          = 2,

	ACCEL_XYZ_PACKET     = 3,
	ACCEL_INT0_PACKET    = 4,
	ACCEL_INT1_PACKET    = 5,

	ACCEL_CONF_PACKET    = 6,

	COLOR_PACKET         = 7,
	COLOR_DUTY_PACKET    = 8,
	COLOR_PERIOD_PACKET  = 9,
	VIBE_PACKET          = 10,
	VIBE_DUTY_PACKET     = 11,
	VIBE_PERIOD_PACKET   = 12
 };

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
#define PROX_AVG_SHIFT 3

uint8_t prox_val_pointer = 0;
uint16_t avg_prox_val = 0;
uint16_t prox_val[PROX_AVG_LEN];

uint8_t prox_flag = 0;
uint8_t prox_send_period = 10;
uint8_t prox_read_period = 1;

/* ---- Vibe/LEDs ----- */

uint8_t rgb[3] = {0, 0, 0};

uint8_t led_on = 1;
uint8_t led_duty = 0;
uint8_t led_period = 0;

uint8_t vibe_level = 0;

uint8_t vibe_on = 1;
uint8_t vibe_duty = 0;
uint8_t vibe_period = 0;

/* ---- Global ---- */

#define ACTIVE_MODE 0
#define PROX_MODE 1
#define ACCEL_XYZ_MODE 2
#define ACCEL_INT0_MODE 3
#define ACCEL_INT1_MODE 4

uint8_t mode = 0;

/* ---- Timer ---- */

Timer t;
uint16_t time_counter = 0;

/* ---- XBee/Wireless ---- */

#define XBEE_BASE_ADDR 0

XBee xbee = XBee();
Tx16Request tx;
Rx16Response rx = Rx16Response();

void setup(void) {
	pinMode(RED_LED_PIN, OUTPUT);
	pinMode(BLUE_LED_PIN, OUTPUT);
	pinMode(GREEN_LED_PIN, OUTPUT);

	pinMode(VIBE_PIN, OUTPUT);

	pinMode(PROX_PIN, INPUT);

	pinMode(SDA, INPUT);
	pinMode(SCL, INPUT);	

	reset();

	#ifdef DEBUG
	Serial.begin(9600);
	Serial.println("Boot");
	setMode((1 << ACTIVE_MODE) | (1 << PROX_MODE) | (1 << ACCEL_XYZ_MODE));
	#endif

	Wire.begin(); // join i2c bus

	accel_reset();
	accel_config();

	attachInterrupt(0, accel_int0, RISING);
	attachInterrupt(0, accel_int1, RISING);

	t.every(10, timerCallback);

	// Timer1.initialize(10000); //1000 microseconds -> 10 milliseconds
	// Timer1.attachInterrupt(timerCallback); // attach the service routine here


	#ifndef DEBUG
	xbee.begin(38400);	
	#endif
}

void loop(void) {
	t.update();

	#ifndef DEBUG
	xbee.readPacket(); // Read packet, return after 500ms timeout

	XBeeResponse response = xbee.getResponse();
	if(response.isAvailable()) {
		if(response.getApiId() == RX_16_RESPONSE) {
			response.getRx16Response(rx); // Get RX response
			parse_data(rx.getData(), rx.getDataLength());
		}
	}
	#endif

	if(!(mode & (1 << ACTIVE_MODE))) {
		//Disabled, lights and vibe off
		vibe(0);
		color(0, 0, 0);
	} else {
		if((mode & (1 << PROX_MODE)) && prox_flag) {
			send_prox(avg_prox_val);
			prox_flag = 0;
		}

		if(accel_ok) {
			if((mode & (1 << ACCEL_XYZ_MODE)) && accel_flag) {
				accel_reg_multi_read(OUT_X_MSB, xyz, 3);				
				send_accel_XYZ(xyz[0], xyz[1], xyz[2]);

				accel_flag = 0;
			}
		}

		updateVibe();
		updateLEDs();
	}
}

/**
 * Set the data out flag every ~10ms.
 */
void timerCallback(void) {
	if((time_counter % prox_read_period) == 0) {
		if(mode & (1 << PROX_MODE)) {
			//Prox enable
			prox_val[prox_val_pointer] = analogRead(PROX_PIN);
			prox_val_pointer = (prox_val_pointer+1)%PROX_AVG_LEN;

			avg_prox_val = 0;
			for(uint8_t i = 0;i < PROX_AVG_LEN;i++) {
				avg_prox_val += prox_val[i];
			}

			avg_prox_val = avg_prox_val >> PROX_AVG_SHIFT; //Divide by 8
		}
	}
	if((time_counter % prox_send_period) == 0) prox_flag = 1;
	if((time_counter % accel_period) == 0) accel_flag = 1;

	if(led_period == 0 || led_duty == 255) led_on = 1;
	else if((time_counter % led_period) < (uint8_t)(led_duty*led_period/255)) led_on = 1;
	else led_on = 0;

	if(vibe_period == 0 || vibe_duty == 255) vibe_on = 1;
	else if((time_counter % vibe_period) < (uint8_t)(vibe_duty*vibe_period/255)) vibe_on = 1;
	else vibe_on = 0;
	
	time_counter++;
}

/**
 * Reset patch
 */
void reset(void) {
	accel_flag = 0;
	prox_flag = 0;

	avg_prox_val = 0;
	for(uint8_t i = 0;i < PROX_AVG_LEN;i++) prox_val[i] = 0;

	vibe_level = 0;

	vibe_on = 0;
	vibe_duty = 0;
	vibe_period = 0;

	rgb[0] = rgb[1] = rgb[2] = 0;

	led_on = 0;
	led_duty = 0;
	led_period = 0;

	color(0, 0, 0);
	vibe(0);

	setMode(0);
}

/* ---- Accel ---- */

void accel_reset(void) {
	#ifdef DEBUG
	Serial.println("Accel Reset Started");
	#endif

	accel_ok = 1;
	uint8_t tmp_accel_ok = 0;

	//Reset the accelerometer
	accel_reg_write(CTRL_REG2, (1 << RST));

	//Wait for reset to complete (blink yellow for wait state)
	int timer = 0;

	while(timer < 1000) { //Wait up to 1 second for reset
		if((accel_reg_read(CTRL_REG2) & (1 << RST)) == 0 && accel_reg_read(WHO_AM_I) == 0x2A) {
			tmp_accel_ok = 1;
			break;
		}

		if(timer % 200 < 100) color(20, 20, 0); //Blink yelow
		else color(0, 0, 0);

		timer++;
		delay(1);
	}

	accel_ok = tmp_accel_ok;

	if(accel_ok) {
		//Green for ok
		#ifdef DEBUG
		Serial.println("Reset Complete, WHO_AM_I Ok");
		#endif
		color(0, 40, 0);
		delay(500);
	} else {
		//Red for fail
		#ifdef DEBUG
		Serial.println("Reset Failed, Accel disabled");
		#endif
		color(40, 0, 0);
		delay(500);
	}
}

void accel_config(void) {	
	//IMPORTANT: all registers must be set with the accelerometer in STANDBY

	accel_enable(0);

	accel_reg_write(CTRL_REG1,  (1 << DR1) | (1 << DR0) | (1 << F_READ)); //8 bit mode
	// accel_reg_write(XYZ_DATA_CFG, (0 << FS1) | (1 << FS0)); //Set range +-4g, output high pass data
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
	accel_reg_multi_write(reg_addr, &data, 1);
}

void accel_reg_multi_write(uint8_t reg_addr, uint8_t* data, int len) {
	if(!accel_ok) return;
	Wire.beginTransmission(ADDR_DEVICE);
	Wire.write(reg_addr);
	for(uint8_t i = 0;i < len;i++) {
		Wire.write(data[i]);
	}
	if(Wire.endTransmission() > 0) {
		accel_error();
	}
}

uint8_t accel_reg_read(uint8_t reg_addr) { 
	if(!accel_ok) return 0;
	uint8_t result = 0;
	accel_reg_multi_read(reg_addr, &result, 1);
	return result;
}

void accel_reg_multi_read(uint8_t reg_addr, uint8_t* data, int len) { 
	if(!accel_ok) return;
	Wire.beginTransmission(ADDR_DEVICE);
	Wire.write(reg_addr);
	if(Wire.endTransmission(false) > 0) { //Do not send stop condition, this will send a repeat start instead
		accel_error();
	}
	Wire.requestFrom(ADDR_DEVICE, len);
	//TODO: Implement OnReceive for less delay?
	for(uint8_t i = 0;i < len;i++) {
		if(Wire.available() > 0) {
			data[i] = Wire.read();
		} else {
			data[i] = 0;
			accel_error();
		}
	}
}

void accel_int0(void) {
	//Nothing yet handle interrupt 2
}

void accel_int1(void) {
	//Nothing yet handle interrupt 1
}

void accel_error() {
	//TODO: Some sort of error_count, try reseting device or eventually disabling it
}

/* ---- Xbee ---- */

void send_accel_XYZ(uint8_t x, uint8_t y, uint8_t z) {
	uint8_t outPacket[4];

	outPacket[0] = ACCEL_XYZ_PACKET;
	outPacket[1] = x;
	outPacket[2] = y;
	outPacket[3] = z;

	#ifdef DEBUG
	Serial.print(int8_t(x));
	Serial.print(" - ");
	Serial.print(int8_t(y));
	Serial.print(" - ");
	Serial.println(int8_t(z));
	#else
	tx = Tx16Request(XBEE_BASE_ADDR, outPacket, 4);

	xbee.send(tx);
	#endif
}

void send_prox(uint16_t val) {
	uint8_t outPacket[3];

	outPacket[0] = PROX_PACKET;
	outPacket[1] = uint8_t(val >> 8);
	outPacket[2] = uint8_t(val);

	#ifdef DEBUG
	Serial.print("\t\t\t");
	Serial.println(val);
	#else
	tx = Tx16Request(XBEE_BASE_ADDR, outPacket, 3);

	xbee.send(tx);
	#endif
}

void parse_data(uint8_t* data, uint8_t len) {
	switch(data[0]) {
		case MODE_PACKET: {
			if(len == 2) setMode(data[1]);
			break;
		}
		case CLEAR_PACKET: {
			reset();
			break;
		}
		case ACCEL_CONF_PACKET: {
			if(len >= 3) accel_reg_multi_write(data[1], &data[2], len-2);
			break;
		}
		case COLOR_PACKET: {
			if(len == 4) {
				rgb[0] = data[1];
				rgb[1] = data[2];
				rgb[2] = data[3];
			}
			break;
		}
		case COLOR_DUTY_PACKET: {
			if(len == 2) led_duty = data[1];
			break;
		}
		case COLOR_PERIOD_PACKET: {
			if(len == 2) led_period = data[1];
			break;
		}
		case VIBE_PACKET: {
			if(len == 2) vibe_level = data[1];
			break;
		}
		case VIBE_DUTY_PACKET: {
			if(len == 2) vibe_duty = data[1]; 
			break;
		}
		case VIBE_PERIOD_PACKET: {
			if(len == 2) vibe_period = data[1]; 
			break;
		}
		default: {
			//Hmm invalid packet
			break;
		}
	}
}

void setMode(uint8_t new_mode) {
	mode = new_mode;

	if(!(new_mode & (1 << PROX_MODE))) {
		for(uint8_t i = 0;i < PROX_AVG_LEN;i++) {
			prox_val[i] = 0;
		}
	}
}

/* ---- Blinking ---- */

void updateVibe(void) {
	if(vibe_on) vibe(vibe_level);
	else vibe(0);
}

void updateLEDs(void) {
	if(led_on) color(rgb[0], rgb[1], rgb[2]);
	else color(0, 0, 0);
}

/* ---- Low Level ---- */

void color(uint8_t red, uint8_t green, uint8_t blue) {
	if(red < 5) digitalWrite(RED_LED_PIN, HIGH);
	else analogWrite(RED_LED_PIN, 255-red);
	if(green < 5) digitalWrite(GREEN_LED_PIN, HIGH);
	else analogWrite(GREEN_LED_PIN, 255-green);
	if(blue < 5) digitalWrite(BLUE_LED_PIN, HIGH);
	else analogWrite(BLUE_LED_PIN, 255-blue);
}

void vibe(uint8_t level) {
	analogWrite(VIBE_PIN, level);
}