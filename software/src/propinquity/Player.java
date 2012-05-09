package propinquity;

import java.util.ArrayList;

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
	int periodPts; // total number of pts for the period
	int totalPts; // total number of pts
	int killPts; // number of pts to remove

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

	ArrayList<Patch> patches;
	Glove glove;

	// stubs
	ArrayList<String> proxStub = null;
	int proxStubIndex = 0;

	// audio feedback
	AudioPlayer negSoundPlayer = null;
	AudioPlayer coopNegSoundPlayer = null;

	boolean coopMode;
	
	Score score;

	public Player(Propinquity parent, ArrayList<Patch> patches, Glove glove, Color color) {
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
		periodPts = 0;
		totalPts = 0;
		stepProximity = 0;
		stepReadings = 0;
		stepTouched = false;

		hudAngle = 0; // same as coop default angle
		hudVel = 0;

		ri = 0;
		for(int i = 0; i < NUM_PROX_READINGS; i++)
			recentReadings[i] = 0;

		lastVibe = 0;

		// reset stub
		if(proxStub != null)
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
		coopNegSoundPlayer = ap;
	}

	public void playNegativeSound() {
		if(!Sounds.MUTE) {
			if(isInCoopMode()) {
				if(coopNegSoundPlayer != null) {
					coopNegSoundPlayer.play();
					coopNegSoundPlayer.rewind();
				}
			} else {
				if(negSoundPlayer != null) {
					negSoundPlayer.play();
					negSoundPlayer.rewind();
				}
			}
		}
	}

	public void approachHudTo(float targetAngle) {
		if(hudAngle == targetAngle)
			return;

		float diff = targetAngle - hudAngle;
		int dir = diff > 0 ? 1 : -1;

		if(diff * dir > HUD_ROTATION) {
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

	// public void setStubbed(boolean s) { stubbed = s; }
	public void subPeriodPts(int pts) {
		periodPts -= pts;
	}

	public void subKillPts(int pts) {
		killPts -= pts;
	}

	public int getPeriodPts() {
		return periodPts;
	}

	public int getTotalPts() {
		return totalPts;
	}

	public int getKillPts() {
		return killPts;
	}

	public void addPts(int pts) {
		periodPts += pts;
		totalPts += pts;
	}

	public void removePts(int pts) {
		periodPts -= pts;
		if(periodPts < 0)
			periodPts = 0;
		totalPts -= pts;
		if(totalPts < 0)
			totalPts = 0;
		killPts += pts;
	}
	
	public void update() {
		score.update();
	}
	
	public void draw() {
		score.draw();
	}

	public int processStep() {
		int result = 0;

		// if the player touched, then remove penality pts
		if(hasTouched()) {
			System.out.println(name + " TOUCHED ");
			removePts(TOUCH_PENALITY_PTS);
			result = -1;
		}
		// else add pts
		else {
			// System.out.println(name + " scores " +
			if(getProximity() > 180)
				result = 1;
		}

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
		if(!stepTouched && touched) {
			playNegativeSound();
		}
		stepTouched |= touched;

		// filter out the low values
		if(proximity < PROXIMITY_LOW_THRESHOLD)
			return;

		// add up readings and counter
		stepProximity += proximity;
		stepReadings++;

		// avg last 4 readings and send them out to vibes
		recentReadings[ri++] = proximity;
		if(ri >= NUM_PROX_READINGS)
			ri = 0;
		averageReading = recentReadings[0];
		for(int i = 0; i < NUM_PROX_READINGS; i++)
			if(recentReadings[i] > averageReading)
				averageReading = recentReadings[i];

		int total = 0;
		for(int i = 0; i < NUM_PROX_READINGS; i++)
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
		if(patches.indexOf(patch) != -1) {
			
			if(patch.getProx() > Score.MIN_RANGE && patch.getProx() < Score.MAX_RANGE) {
				score.increment();
			}
		}
	}
	
	

	ProxData nextProxStub(long time) {
		if(proxStub == null)
			return null;
		if(proxStubIndex >= proxStub.size())
			return null;

		String[] data = (proxStub.get(proxStubIndex)).split(",");

		// check if we reached the time for this step
		if(Integer.valueOf(data[0]) >= time)
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
		if(data == null || data.length == 0) {
			System.out.println("Error: Proximity stub was empty. I don't think that's right.");
		}

		proxStub = new ArrayList<String>();

		// parse to keep only data for this player
		String[] dataline;
		for(int i = 0; i < data.length; i++) {
			dataline = data[i].split(",");

			if(dataline.length != 5) {
				System.out.println("Warning: Proximity stub line " + i + " (" + data[i]
						+ ") is not formatted correctly");
				continue;
			}

			if(Integer.valueOf(dataline[1]) == index) {
				proxStub.add(data[i]);
			}
		}

		// start the stub at the beginning
		proxStubIndex = 0;

		System.out.println(" Proximity stub... " + proxStub.size());
	}

	void initProxComm(String ni1, String ni2) {
		// TODO load bases using their serial number...?
		if(ni1 != null) {
			// XBeeReader xbee = null;
			// XBeeReader xbee = parent.xbeeManager.reader(ni1);
			// if(xbee != null) {
			// System.out.println("Initialized XBee for proximity #1: " + ni1);
			// } else {
			// System.err
			// .println("Could not initialize XBee for proximity #1: "
			// + ni1);
			// }
		}
		if(ni2 != null) {
			// XBeeReader xbee = null;
			// XBeeReader xbee = parent.xbeeManager.reader(ni2);
			// if(xbee != null) {
			// System.out.println("Initialized XBee for proximity #2: " + ni2);
			// } else {
			// System.err
			// .println("Could not initialize XBee for proximity #2: "
			// + ni2);
			// }
		}

		// create the data packet that requests proximity values
		// for(int i=1; i < outdata.length; i++)
		// outdata[i] = 0;
	}

	void initVibeComm(String ni) {
		if(ni == null)
			return;

		// XBeeReader xbee = null;
		// XBeeReader xbee = parent.xbeeManager.reader(ni);
		// if(xbee != null) {
		// System.out.println("Initialized XBee for vibration: " + ni);
		// } else {
		// System.err
		// .println("Could not initialize XBee for vibration: " + ni);
		// }
	}

	public void sendStep(int stepNum) {
		// System.out.println(name + " sending step: " + stepNum);
		// broadcast step to patches
		// 0/*???*/);
		// Step step1 = stepNum < steps.length ? steps[stepNum] : null;
		// Step step2 = stepNum + 1 < steps.length ? steps[stepNum + 1] : null;
		// Step step3 = stepNum + 2 < steps.length ? steps[stepNum + 2] : null;
		// Step step4 = stepNum + 3 < steps.length ? steps[stepNum + 3] : null;
		// //Does this only broadcast to two of the prox sensors then?
	}

	public void sendVibes(int avgReading) {
		sendVibes(avgReading, false);
	}

	public void sendVibes(int avgReading, boolean override) {
		// make sure the vibe xbee is present

		// if we are not overriding the threshold filter
		if(!override) {
			// then donothing is the value is less than the
			// threshold away from the last sent value
			int diff = lastVibe - avgReading;
			if(diff < 0)
				diff *= -1;
			if(diff < VIBE_DIFF_THRESHOLD)
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
