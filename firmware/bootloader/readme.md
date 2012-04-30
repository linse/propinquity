Propinquity Arduino Bootloader
==============================

In order to flash the Arduino bootloader onto the propinquity patch, simply run "make". You must make sure the port/programmer in the makefile match the port and programmer you are using.

It is important to use this makefile and NOT the bootloader flashing available in the Arduino IDE. The flashing in the Arduino IDE has the wrong clock rate and it will brick your propinquity patch.