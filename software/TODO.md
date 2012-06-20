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

* isDone() mechanisms need rework, likely as part of a whole rework of the Transition/Step mechanism or something similar. (Actually maybe it's fine ...)
* Build particles with a specific buffer to draw too, much more agnostic
* CLIPPING ON SCREEN +!+!+!+
* RADIOSSSSZZZ

Minim Gripes
-----------------------------
* Debug granularity
* Rewind behavior
* Mechanism for song ending naturally

Firmware/Patch/Glove Control
-----------------------------
* Graceful fail (duty 0)
* Alternate send mechanism (everything 5 times a sec?)
* "Drop" patch if it hasn't been seen in a certain amount of time (to avoid points when patches spaz out)

Hardware
-----------------------------
* Could the red/prox issue be due to current draw issues
* XBee stress test

Playtest 2
-----------------------------
* Better time control of round length

Playtest 3
-----------------------------
* Bopper multipop
* Health multiscore

Playtest 4
-----------------------------
* Blinking is confusing
* All patches on maybe
* Coop color is bad
* Song doesn't rewind properly