Software
-----------------------------
* Visual
	* Merge should work forever by summing particles size and getting bigger and bigger ....
	* Merge should be prox based

* Logger with enablable println and warnings. Either dump it or start using it.

* Code style/Nitpicks
	* Can HUD be cleaned up

	* Provide more robust behavior for variable quantity objects (Gloves, Patches, Xbees, Levels, etc)
	* Provide meaning full null error messages throughout. (Improved)

	* if/switch/catch/for/while
	* Standard constructor arg ordering

* isDone() mechanisms need rework, likely as part of a whole rework of the Transition/Step mechanism or something similar. (Actually maybe it's fine ...)
* Build particles with a specific buffer to draw too, much more agnostic. Needs better implementation.
* Use new minim as a git submodule
* New broadcast mechanism for radios as alternative.
* Better time control of round length. Load total length from XML file instead of a fixed length.
* New OO system for XML parsing tokens. Provide for easier extension to the current XML files.
* There seems to be a bug where after playing all the songs they no longer play. Maybe also after a song plays through once? Needs to be tested. Probably some sort of rewind minim bug or an issue resetting some sort of sequence timing variable.
* Ambient background color changes were proposed to help players understand who is winning and who is loosing. Should be easy and worth trying.

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

General and Gameplay Notes
-----------------------------
* If players have different sizes, the shorter player should wear his patches low. This causes the taller player to bend over a lot.
* We need to sets of hardware. Complete to avoid downtime. This also means we need two full radio address spaces and a mechanism to switch between them.
* We need music to play during the transition time between rounds just to set the mood and keep the energy up.
* The colors for the patches need to stay on at the end of the levels so that the players can tell who won. There could also be some sort of animation to indicate the winner.

