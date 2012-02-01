// Board for SparkJaneBoard: 
// Arduino Pro or Pro Mini (3.3V, 8 mHz) with Atmega 328

// all +1 compared to previous version
int onboardLedPin = 7; 
int redLedPin = 8; 
int greenLedPin = 9; 
int blueLedPin = 10;

int vibePin = 6; 

int proxPin = A4; // analog 4
int proxReading = 0;

// d 0 rx
// d 1 tx


void setup() {
  pinMode(onboardLedPin, OUTPUT);
  pinMode(redLedPin, OUTPUT);
  pinMode(greenLedPin, OUTPUT);
  pinMode(blueLedPin, OUTPUT);
  
  pinMode(vibePin, OUTPUT);
  
  pinMode(proxPin, INPUT);

  Serial.begin(9600);
  color(0,0,0);
}

void loop() {
  readAndOutputProx();
}

void blinkLEDsAndVibe() {
  analogWrite(redLedPin, 255);
  digitalWrite(vibePin, 0);	
  delay(500);
  digitalWrite(redLedPin, 0);
  digitalWrite(vibePin, 1);
  delay(500);
  color(255, 255, 0);
}

void readAndOutputProx() {
  analogRead(proxPin);
  delay(20);
  proxReading = analogRead(proxPin);
  delay(20);
  Serial.println(proxReading);
  delay(20);
}

void color(unsigned char red, unsigned char green, unsigned char blue) {
  analogWrite(redLedPin, 255-red);	
  analogWrite(greenLedPin, 255-green); 
  analogWrite(blueLedPin, 255-blue);
}
