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
* Add smooth color fading for background colors
* Add a build flag for simulation vs real XBees in conjuction with windowed vs normal
* Opponents patches need to turn off when points are suppressed

* Should catch more startup error and give onscreen feedback (sound loading)
* Should give xbee search feedback

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
* Test/Implement a timestamping mechanism to prevent out of order packet reception which is causing erratic behavoir (see for example the PlayerSelect mode)

Reported Issues and Ideas
-----------------------------
* Sensitivity of patches needs fine-tuning, but is hard to do on-site due to time pressure
* Once, the music just stopped and we needed to restart the application
* Show some statistics in the end, e.g. round-to-round scores to give an idea of progress/dynamic
* People are sometimes confused about which color they are when the patches are off. Make sure that all patches are never off.
* Countdown

Fixed
-----------------------------
* **No levels do this** Having some patches turn off during the game is confusing. Some game modes do this and I never remember which
* **Reduced max saturation to 65%, made it a level constant** The background color indicating who is leading is a bit intense sometimes
* **Support pause="true" on steps and added to all XML** After coop/calibration mode is finished, perhaps we should pause the game to have time to brief the players
* **Support mouse clicks** Have a remote control to at least pause the game, and perhaps also select song. Right now we need two people, one "referee" and one person with the finger on the space bar.
* **dingding on unpause and when leaving a transition** Just stopping/starting the music isn't intuitive to everyone - add a "ding-ding"/"gong" to signify start/stop of rounds?
* **Press 's' to skip** Have an option to stop early or skip coop mode for more experienced players
* **Press 'a' to toggle adjustement mode, use arrow keys** Using different projectors is somewhat of a pain. Can we scale the image in software to ensure good use of screen real-estate, as well as ensure a round (not elliptical) circle?
* **Preview built** It's hard to remember which song is which. Idea: Preview the songs (skip to the middle, low volume) in the selection menu.
* **Showing BPM in levelselect** Slow/fast songs: Have an indicator of speed/style in the song menu?
