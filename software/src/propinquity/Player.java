package propinquity;

import processing.core.PConstants;

import propinquity.hardware.*;

import ddf.minim.AudioPlayer;

public class Player implements PConstants, ProxEventListener {

	final int PROXIMITY_LOW_THRESHOLD = 80;

	// xbee

	final int VIBE_DIFF_THRESHOLD = 15;

	final int TOUCH_PENALITY_PTS = 100;

	final float HUD_ROTATION = PI / 256f;
	final float HUD_FRICTION = 0.94f;

	Propinquity parent;

	String name;
	Color color;

	float hudAngle;
	float hudVel;

	Step[] steps;

	int stepProximity;
	boolean stepTouched;
	int stepReadings;

	final int NUM_PROX_READINGS = 4;
	int[] recentReadings = new int[NUM_PROX_READINGS];
	int ri = 0;
	int averageReading;
	int lastVibe;
	// int totalVibe;

	Patch[] patches;
	Glove glove;

	AudioPlayer negSoundPlayer;
	AudioPlayer negSoundCoop;

	boolean coopMode;

	Score score;
	int distance;
	private long currentTime;
	private long lastTime;

	public Player(Propinquity parent, String name, Color color, Patch[] patches, Glove glove, Sounds sounds) {
		this.parent = parent;
		
		this.name = name;
		this.color = color;

		this.patches = patches;
		this.glove = glove;

		negSoundPlayer = sounds.getNegativePlayer(0); //TODO
		negSoundCoop = sounds.getNegativeCoop();

		score = new Score(parent, color);

		reset();
	}

	public void reset() {
		steps = null;
		stepProximity = 0;
		stepReadings = 0;
		stepTouched = false;
		lastTime = 0;

		hudAngle = 0; // same as coop default angle
		hudVel = 0;

		ri = 0;
		for (int i = 0; i < NUM_PROX_READINGS; i++)
			recentReadings[i] = 0;

		lastVibe = 0;

		score.reset();
	}

	public void clear() {
		// clear vibration
		sendVibes(0);

		// close xbee comm
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public Glove getGlove() {
		return glove;
	}

	public Patch[] getPatches() {
		return patches;
	}

	public void playNegativeSound() {
		if (isInCoopMode()) {
			if (negSoundCoop != null) {
				negSoundCoop.play();
				negSoundCoop.rewind();
			}
		} else {
			if (negSoundPlayer != null) {
				negSoundPlayer.play();
				negSoundPlayer.rewind();
			}
		}
	}

	public void approachHudTo(float targetAngle) {
		if (hudAngle == targetAngle)
			return;

		float diff = targetAngle - hudAngle;
		int dir = diff > 0 ? 1 : -1;

		if (diff * dir > HUD_ROTATION) {
			hudVel += HUD_ROTATION * dir;
		} else {
			hudAngle = targetAngle;
		}

		hudVel *= HUD_FRICTION;

		hudAngle += hudVel;
	}

	public void initializeSteps(int stepLength) {
		steps = new Step[stepLength];
	}

	public void addStep(Step s, int i) {
		steps[i] = s;
	}

	public Step getStep(int i) {
		return steps[i];
	}

	public void update() {

		currentTime = parent.millis();

		score.update();

		if (currentTime - lastTime > Particle.SPAWN_DELAY) {
			if (distance > Score.MIN_RANGE && distance < Score.MAX_RANGE) {
				score.increment();
				lastTime = currentTime;
			}
		}
	}

	public void draw() {
		score.draw();
	}

	public int processStep() {
		int result = 0;

		if (getProximity() > 180)
			result = 1;

		stepTouched = false;
		stepProximity = 0;
		stepReadings = 0;

		return result;
	}

	public void processConfigAck(int patch, int turnLength) {

	}

	public void processProxReading(int patch, int step, boolean touched, int proximity) {
		// check if the reading is for the current step or if it's too late
		// TODO

		// keep track of the step touched
		// and give negative audio feedback immediately
		if (!stepTouched && touched) {
			playNegativeSound();
		}
		stepTouched |= touched;

		// filter out the low values
		if (proximity < PROXIMITY_LOW_THRESHOLD)
			return;

		// add up readings and counter
		stepProximity += proximity;
		stepReadings++;

		// avg last 4 readings and send them out to vibes
		recentReadings[ri++] = proximity;
		if (ri >= NUM_PROX_READINGS)
			ri = 0;
		averageReading = recentReadings[0];
		for (int i = 0; i < NUM_PROX_READINGS; i++)
			if (recentReadings[i] > averageReading)
				averageReading = recentReadings[i];

		int total = 0;
		for (int i = 0; i < NUM_PROX_READINGS; i++)
			total += recentReadings[i];

		averageReading = total / NUM_PROX_READINGS;
		sendVibes(averageReading);
	}

	public boolean hasTouched() {
		return stepTouched;
	}

	public int getProximity() {
		return stepReadings == 0 ? 0 : stepProximity / stepReadings;
	}

	public void setCoopMode(boolean b) {
		coopMode = b;
	}

	public boolean isInCoopMode() {
		return coopMode;
	}

	public void proxEvent(Patch patch) {
		for(Patch p : patches) {
			if(patch == p) distance = patch.getProx();
		}
	}

	public void sendVibes(int avgReading) {
		sendVibes(avgReading, false);
	}

	public void sendVibes(int avgReading, boolean override) {
		// make sure the vibe xbee is present

		// if we are not overriding the threshold filter
		if (!override) {
			// then donothing is the value is less than the
			// threshold away from the last sent value
			int diff = lastVibe - avgReading;
			if (diff < 0)
				diff *= -1;
			if (diff < VIBE_DIFF_THRESHOLD)
				return;
		}

		// broadcast vibe paket

		// keep track of last sent value
		lastVibe = avgReading;
	}
}
