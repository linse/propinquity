Work Schedule
-----------------------------
My high level plan for the first block of time (24h) looks like this.

 * Review the new hardware. (8-12h)
 	* Note all problem
 	* Assuming it can be done quickly, make a new eagle version with corrections
 	* Make any improvements I can to the current boards (such as changing regulator etc).
 	* Summarize limitation of new boards
 * Review software from the hackathon (4-8h)
 	* Read new code (particularly from marius)
 	* Organize and document a bit so it's a least somewhat maintainable
 * Examine and address what bugs I can such as those in my notes and from Marius' notes (8h+)
 * If time remains start think about the accelerometers

The other block of time (20h) was mainly for working with the accel. I'm thinking something like this.

 * Provide some examples of advanced accelerometer use for future reference
 * Consider some amendments to the Xbee protocol to work better with accel
 * Prototype the often discussed speed limited version of the basic propinquity game
 * Play test this prototype in the TAG lab

Hardware
-----------------------------
* **Done** Regulator footprint should have the EN polarity changed, this would be a more standard regulator package
* **Done via previous** Bigger 3v3 reg
* **Done: Added switch that leave only the charger plugged** Header/footprint for an on/off switch is needed
* **Done: Unsure if safe because connected to battery, put a jumper** FTDI Vcc should connect to V+ and go through the 3v3 reg
* **Done** Flip transistor footprint
* **Done** Stepup should run direclty off the battery
* **Done** Charging topology should be modified so that nothing is connected while charging

XBees
-----------------------------
* XBee configuration should be saved and added to the git repo
* A new correct spreadsheet of the XBee addresses should be added to the repo

Firmware
-----------------------------
* If would be nice if we could consider dumping the arduino IDE for makefiles
* A new state based version would be good for stability and reliability of communication, would require a concerted effort with software changes

Software
-----------------------------
* High level documentation should be produced
	* OO diagram
	* Protocol specifications
* See sofware/TODO.md

Tought Dump
-----------------------------

* Having some patches turn off during the game is confusing. Some game modes do this and I never remember which
* Sensitivity of patches needs fine-tuning, but is hard to do on-site due to time pressure
* Once, the music just stopped and we needed to restart the application
* The background color indicating who is leading is a bit intense sometimes
* After coop/calibration mode is finished, perhaps we should pause the game to have time to brief the players
* Just stopping/starting the music isn't intuitive to everyone - add a "ding-ding"/"gong" to signify start/stop of rounds?
* Have a remote control to at least pause the game, and perhaps also select song. Right now we need two people, one "referee" and one person with the finger on the space bar.
* Have an option to stop early or skip coop mode for more experienced players
* Rename coop to calibration
* Using different projectors is somewhat of a pain. Can we scale the image in software to ensure good use of screen real-estate, as well as ensure a round (not elliptical) circle?
* Show some statistics in the end, e.g. round-to-round scores to give an idea of progress/dynamic
* People are sometimes confused about which color they are when the patches are off. Make sure that all patches are never off.
* It's hard to remember which song is which. Idea: Preview the songs (skip to the middle, low volume) in the selection menu.
* Slow/fast songs: Have an indicator of speed/style in the song menu?
* Circle is cut off

* song selection/preview
* speed
* jousty
* sound by player
* countdown
* Force field joust agress/defend
* different information