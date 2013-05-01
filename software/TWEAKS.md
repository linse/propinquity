# Where To Tweak Things in Propinquity

* Enable the hardware simulation IMPORANT MUST BE SET TO FALSE TO PLAY WITH REAL PATCHES
	* Location src/Propinquity.java line 28
	* Name: useSimulator
	* Range: true/false
	* Set to true if you need to use the hardware simulator, set to false if you're using real patches
* Adjust the prox sensor ranges
	* Location: src/hardware/Patch.java line 14-5
	* Name: MIN_RANGE, MAX_RANGE
	* Range: 0-1024 (actual maximum is closer to 1000, depends on prox sensors)
	* Adjust the range of the prox sensor in which you can score. MIN_RANGE, MAX_RANGE sets the range in which normal scoring occurs. Outside this range no scoring occurs
* Player Colors
	* Location src/PlayerConstants.java line 22-27
	* Name: PLAYER_COLORS, NEUTRAL_COLOR
	* Range: see the src/Color.java file for more colors
	* This sets the colors everywhere for neutral and players.
hes/gloves
* ProxLevel (normal levels) length
	* Location: src/ProxLevel.java line 17
	* Name: TOTAL_LEN
	* Range: > 0 (number in milliseconds)
	* Sets the total length of a ProxLevel in millisecond. For example set 180000ms for 180sec/3min
* Background Color Saturation
	* Location: src/LevelConstants.java line 17
	* Name: BACKGROUND_SAT_CAP
	* Range: 0-1
	* This number limits the maximum color saturation of the background when one player is winning. 0 being no saturation and 1 being full saturation
* Blinking speeds and duty cycle
	* Location: src/hardware/HardwareConstants.java line 15-18
	* Name: SLOW_BLINK, FAST_BLINK
	* Range: > 0 (in milliseconds)
	* Name: DEFAULT_DUTY_CYCLE
	* Range: 0-255 (0 -> 0%, 127 -> 50%, 255 -> 100%)
	* Sets the fast and slow blink speeds for the patches/gloves. You can also set how much of each blink period is on/off by adjusting the DEFAULT_DUTY_CYCLE
* Particle Sizes
	* Location: src/Particle.java line 17-20
	* Name: SMALL_SIZE, LARGE_SIZE, METABALL_OVERSIZE_FACTOR
	* Range: > 0
	* Set the size of the small and large particles. The METABALL_OVERSIZE_FACTOR affects how aggressively the particles "melt" into each other
* Particle spawning rates
	* Location: src/Player.java line 13-16
	* Name: SPAWN_DELAY_SHORT
	* Name: SPAWN_DELAY_MED
	* Name: SPAWN_DELAY_LONG (Not used right now, only used with SweetSpot)
	* Name: SPAWN_DELAY_TAU
	* Range: > 0
	* Sets the spawning rate of particles according to an exponential curve. SPAWN_DELAY_MED is the initial rate of spawning (in milliseconds). SPAWN_DELAY_SHORT is the final rate of spawning (in milliseconds). SPAWN_DELAY_TAU adjust the time the exponential curve takes to go from slow to fast (in milliseconds). The equations are on line 275 and 278.
* Patch and glove XBee addresses
	* Location src/PlayerConstants.java line 12-20
	* Name: PATCH_ADDR, GLOVE_ADDR
	* Range: > 0
	* Gives the Xbees addresses for the patches and gloves