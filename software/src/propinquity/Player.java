package propinquity;

import java.util.Vector;

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

	// boolean stubbed;
	final int NUM_PROX_READINGS = 4;
	int[] recentReadings = new int[NUM_PROX_READINGS];
	int ri = 0;
	int averageReading;
	int lastVibe;
	// int totalVibe;

	Vector<Patch> patches;
	Glove glove;

	// stubs
	Vector<String> proxStub = null;
	int proxStubIndex = 0;

	AudioPlayer negSoundPlayer;
	AudioPlayer negSoundCoop;

	boolean coopMode;

	Score score;
	int distance;
	private long currentTime;
	private long lastTime;

	public Player(Propinquity parent, Vector<Patch> patches, Glove glove, Color color) {
		this.parent = parent;
		this.name = "noname";
		this.color = color;
		this.patches = patches;
		this.glove = glove;
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

		// reset stub
		if (proxStub != null)
			proxStubIndex = 0;

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

	public Color getColor() {
		return color;
	}

	public void registerNegativePlayerSound(AudioPlayer ap) {
		negSoundPlayer = ap;
	}

	public void registerNegativeCoopSound(AudioPlayer ap) {
		negSoundCoop = ap;
	}

	public void playNegativeSound() {
		if (!Sounds.MUTE) {
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
		if (patches.indexOf(patch) != -1) {
			distance = patch.getProx();
		}
	}

	ProxData nextProxStub(long time) {
		if (proxStub == null)
			return null;
		if (proxStubIndex >= proxStub.size())
			return null;

		String[] data = (proxStub.get(proxStubIndex)).split(",");

		// check if we reached the time for this step
		if (Integer.valueOf(data[0]) >= time)
			return null;

		// System.out.println(time + ": " + Integer.valueOf(data[2]) + " " +
		// (Integer.valueOf(data[3])==1) + " " + Integer.valueOf(data[4]));

		/*
		 * processProxReading(Integer.valueOf(data[2]), proxStubIndex,
		 * Integer.valueOf(data[3])==1, Integer.valueOf(data[4]));
		 */

		return new ProxData(Integer.valueOf(data[1]), Integer.valueOf(data[2]), proxStubIndex++,
				Integer.valueOf(data[3]) == 1, Integer.valueOf(data[4]));
	}

	void loadProxStub(int index, String stubFile) {
		// proximity data stub
		String[] data = parent.loadStrings(stubFile);
		if (data == null || data.length == 0) {
			System.out.println("Error: Proximity stub was empty. I don't think that's right.");
		}

		proxStub = new Vector<String>();

		// parse to keep only data for this player
		String[] dataline;
		for (int i = 0; i < data.length; i++) {
			dataline = data[i].split(",");

			if (dataline.length != 5) {
				System.out.println("Warning: Proximity stub line " + i + " (" + data[i]
						+ ") is not formatted correctly");
				continue;
			}

			if (Integer.valueOf(dataline[1]) == index) {
				proxStub.add(data[i]);
			}
		}

		// start the stub at the beginning
		proxStubIndex = 0;

		System.out.println(" Proximity stub... " + proxStub.size());
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

	public void sendConfig(int stepLength) {
	}

	// TODO replace this with configPatches to pass the step length
	// at the same time as detecting which ones respond.
	public void discoverPatches() {
		System.out.println("Discover patches...");
	}
}
