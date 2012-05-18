TODO for Propinquity Software
=============================

* Gameplay
	* Think about and implement coop scoring. Both players should get points towards their total during coop. How should we handle score display at that time?
	* Multiple scoring patch handling?
	* Meta balls/Liquid/Coolness
	* Merge should work forever by summing particles size and getting bigger and bigger ....
	* Merge should be prox based

* Negative sounds?

* Hardware control
	* Handle actual behavior for vibe and patches
	* Prox events
	* Add clear all command (diff from active)
	* Prox data = 0 when inactive !!!
	
* XBee stress test

* Logger with enablable println and warnings

* Code style/Nitpicks
	* Can HUD be cleaned up

	* Provide more robust behavior for variable quantity objects (Gloves, Patches, Xbees, Levels, etc)
	* Provide meaning full null error messages throughout 

	* if/switch/catch/for/while
	* Standard constructor arg ordering

* Clear vibe for glove at the last step !
* Graceful fail (duty 0)
* "Drop" patch if it hasn't been seen in a certain amount of time (to avoid points when patches spaz out)
* alt send mechanism (everything 5 times a sec?)
* Could the red/prox issue be due to current draw issues