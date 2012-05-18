Software
-----------------------------

* Gameplay
	* Think about and implement coop scoring. Both players should get points towards their total during coop. How should we handle score display at that time?
	* Negative sounds?
	* The exp timeout should carry from one patch to another so you can keep your multiplier if you "change patch"
	
* Visual
	* Meta balls/Liquid/Coolness
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
* Prox filter
* XBee stress test