Software
-----------------------------
* Visual
	* Merge should work forever by summing particles size and getting bigger and bigger ....
	* Merge should be prox based

* Logger with enablable println and warnings

* Code style/Nitpicks
	* Can HUD be cleaned up

	* Provide more robust behavior for variable quantity objects (Gloves, Patches, Xbees, Levels, etc)
	* Provide meaning full null error messages throughout 

	* if/switch/catch/for/while
	* Standard constructor arg ordering

Firmware/Patch/Glove Control
-----------------------------
* Graceful fail (duty 0)
* alt send mechanism (everything 5 times a sec?)
* "Drop" patch if it hasn't been seen in a certain amount of time (to avoid points when patches spaz out)

Hardware
-----------------------------
* Could the red/prox issue be due to current draw issues
* XBee stress test
* Fix chargers

Playtest 2
-----------------------------
* Better time control of round length

Playtest 3
-----------------------------
* Intro sounds and transistion sounds
* Prox version with coop
	* Intro, sounds/fanfare
	* DONE Transition sounds between coop and versus and/or slience and/or song change
	* Long ambient coop, short slow beat versus, medium medium/slow beat coop, medium high energy versus
	* One patch should always be on across step transitions (same type)
* Cerimonial Bopper
	* Intro, sounds/fanfare, "calibration" for fixed feet
	* DONE Gong between the rounds (acts as a pause) 
	* DONE Vvvvv before score
	* Maybe health
* Bopper Multipop
* Health multiscore

Playtest 4
-----------------------------
* Catch null pointers for wrong file name
* Blinkning is confusing
* All on maybe?
* Coop color is bad
* Gong at round end
* Bigger center holding for rounds