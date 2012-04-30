// Board for SparkJaneBoard: 
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328

int onboardLedPin = 7; 
int redLedPin = 8; 
int greenLedPin = 9; 
int blueLedPin = 10; 

int vibePin = 6; 
// TODO test prox 
int proxPin = 4;

void setup() {
pinMode(onboardLedPin, OUTPUT);
pinMode(redLedPin, OUTPUT);
pinMode(greenLedPin, OUTPUT);
pinMode(blueLedPin, OUTPUT);

pinMode(vibePin, OUTPUT);

//pinMode(prox, INPUT);
}

void loop() {
	delay(500);
	color(255, 0, 0);
	delay(500);
	color(0, 255, 0);
	delay(500);
	color(0, 0, 255);
	delay(500);
	color(0, 0, 0);
}


void color(unsigned char red, unsigned char green, unsigned char blue) {
analogWrite(redLedPin, 255-red);	
analogWrite(greenLedPin, 255-green); 
analogWrite(blueLedPin, 255-blue);
}
