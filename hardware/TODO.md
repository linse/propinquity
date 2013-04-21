# Hardware TODOs

## V2.0 Hardware

### Known Issues

* Regulator footprint accepts active low EN 3.3V regulators. This seems to be less common than active high especially with for LDOs with >100mA or more of current. This is a problem since is seems that we require at least that much current.
* No pins available for connecting a switch
* Topology of the circuit is not good for charging since everything is connected and running when you try and charge. This means the charger can't tell what's going on
* FTDI +5V connects directly to the +3v3 net, probably not nice for the +3v3 chips (e.g. accel and prox)
* Transistor is uncommon footprint (EBC?)
* The setup is unnecessarily connected to +3v3. It would draw less current and be more power efficient in general to run it off V+

### Current Status and Workarounds

* For regulator Some 100mA regulators are available with the current pinout, but current drive is marginal
* Alternatively use any pin with active high EN, bend up the pin and use a small wire to connect EN to closest V+
* Switches have been added by bending up the V+ pin on the battery Molex connector and putting a switch between the +ve leg of the molex connector and the V+ pad.
* Charging doesn't seem like it's consistently working. Probably have to disconnect batteries for the moment
* Ignoring FTDI issue for now. Pin could be removed from the header if it's an issue
* More common BCE or equivalent MOSFET can be installed upside down easily

## V2.1 Hardware

### Fixes

* Regulator footprint has active high EN polarity. Can accept bigger +3.3V regulators
* Added header for a switch that leave only the charger plugged in when it's in the off position. This should allow on/off and good charging
* +5V from the FTDI connector now connects to V+ via a jumper. This is optional for dev. In general it's probably not good to have the +5V attached to V+ since you risk damaging the battery.
* Flipped transistor footprint
* Step up runs directly off V+
* New routing of the PCB is 10mil tolerant where possible