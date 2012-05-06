package propinquity;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import xbee.*;
import propinquity.hardware.*;

import ddf.minim.AudioPlayer;

public class Player implements PConstants {

	final int PROXIMITY_LOW_THRESHOLD = 80;

	// xbee
	final int XPAN_PROX_BASES = 1; // 2;
	final int XPAN_VIBE_BASES = 1;


	final int VIBE_DIFF_THRESHOLD = 15;

	final int TOUCH_PENALITY_PTS = 100;

	final float HUD_ROTATION = PI / 256f;
	final float HUD_FRICTION = 0.94f;

	Propinquity parent;

	String name;
	int color;

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

	// xbee
	XPan[] xpansProx;
	XPan[] xpansVibe;
	// int[] outdata;
	int numPatches;

	// stubs
	ArrayList<String> proxStub = null;
	int proxStubIndex = 0;

	// audio feedback
	AudioPlayer negSoundPlayer = null;
	AudioPlayer coopNegSoundPlayer = null;

	boolean coopMode;

	// public Player(PApplet p, String n, color c)
	public Player(Propinquity p, int c) {
		this.parent = p;
		this.name = "noname";
		this.color = c;
		this.xpansProx = new XPan[XPAN_PROX_BASES];
		this.xpansVibe = new XPan[XPAN_VIBE_BASES];
		this.numPatches = 0;
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
		for (int i = 0; i < NUM_PROX_READINGS; i++)
			recentReadings[i] = 0;

		lastVibe = 0;

		// reset stub
		if (proxStub != null)
			proxStubIndex = 0;
	}

	public void clear() {
		// clear vibration
		sendVibes(0);

		// close xbee comm
		for (int i = 0; i < xpansProx.length; i++)
			if (xpansProx[i] != null)
				xpansProx[i].stop();
		for (int i = 0; i < xpansVibe.length; i++)
			if (xpansVibe[i] != null)
				xpansVibe[i].stop();
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}

	public void setNumPatches(int num) {
		numPatches = num;
	}

	public void registerNegativePlayerSound(AudioPlayer ap) {
		negSoundPlayer = ap;
	}

	public void registerNegativeCoopSound(AudioPlayer ap) {
		coopNegSoundPlayer = ap;
	}

	public void playNegativeSound() {
		if (!Sounds.MUTE) {
			if (isInCoopMode()) {
				if (coopNegSoundPlayer != null) {
					coopNegSoundPlayer.play();
					coopNegSoundPlayer.rewind();
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
		if (periodPts < 0)
			periodPts = 0;
		totalPts -= pts;
		if (totalPts < 0)
			totalPts = 0;
		killPts += pts;
	}

	public int processStep() {
		int result = 0;

		// if the player touched, then remove penality pts
		if (hasTouched()) {
			System.out.println(name + " TOUCHED ");
			removePts(TOUCH_PENALITY_PTS);
			result = -1;
		}
		// else add pts
		else {
			// System.out.println(name + " scores " +
			if (getProximity() > 180)
				result = 1;
		}

		stepTouched = false;
		stepProximity = 0;
		stepReadings = 0;

		return result;
	}

	public void processConfigAck(int patch, int turnLength) {

	}

	public void processProxReading(int patch, int step, boolean touched,
			int proximity) {
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

		return new ProxData(Integer.valueOf(data[1]), Integer.valueOf(data[2]),
				proxStubIndex++, Integer.valueOf(data[3]) == 1,
				Integer.valueOf(data[4]));
	}

	void loadProxStub(int index, String stubFile) {
		// proximity data stub
		String[] data = parent.loadStrings(stubFile);
		if (data == null || data.length == 0) {
			System.out.println("Error: Proximity stub was empty. I don't think that's right.");
		}

		proxStub = new ArrayList<String>();

		// parse to keep only data for this player
		String[] dataline;
		for (int i = 0; i < data.length; i++) {
			dataline = data[i].split(",");

			if (dataline.length != 5) {
				System.out.println("Warning: Proximity stub line " + i + " ("
						+ data[i] + ") is not formatted correctly");
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

	void initProxComm(String ni1, String ni2) {
		// TODO load bases using their serial number...?
		if (ni1 != null) {
			XBeeReader xbee = parent.xbeeManager.reader(ni1);
			if (xbee != null) {
				xpansProx[0] = new XPan(xbee);
				System.out.println("Initialized XBee for proximity #1: " + ni1);
			} else {
				System.err
						.println("Could not initialize XBee for proximity #1: "
								+ ni1);
			}
		}
		if (ni2 != null) {
			XBeeReader xbee = parent.xbeeManager.reader(ni2);
			if (xbee != null) {
				xpansProx[1] = new XPan(xbee);
				System.out.println("Initialized XBee for proximity #2: " + ni2);
			} else {
				System.err
						.println("Could not initialize XBee for proximity #2: "
								+ ni2);
			}
		}

		// create the data packet that requests proximity values
		// outdata = new int[XPan.PROX_OUT_PACKET_LENGTH];
		// outdata[0] = XPan.PROX_OUT_PACKET_TYPE;
		// for (int i=1; i < outdata.length; i++)
		// outdata[i] = 0;
	}

	XPan[] getProxXPans() {
		return xpansProx;
	}


	void initVibeComm(String ni) {
		if (ni == null)
			return;

		XBeeReader xbee = parent.xbeeManager.reader(ni);
		if (xbee != null) {
			xpansVibe[0] = new XPan(xbee);
			System.out.println("Initialized XBee for vibration: " + ni);
		} else {
			System.err
					.println("Could not initialize XBee for vibration: " + ni);
		}
	}

	public void sendStep(int stepNum) {
		// System.out.println(name + " sending step: " + stepNum);
		if (xpansProx[0] == null)
			return;

		// broadcast step to patches
		// xpansProx[0].sendOutgoing(XPan.BROADCAST_ADDR, outdata, stepNum,
		// 0/*???*/);
		Step step1 = stepNum < steps.length ? steps[stepNum] : null;
		Step step2 = stepNum + 1 < steps.length ? steps[stepNum + 1] : null;
		Step step3 = stepNum + 2 < steps.length ? steps[stepNum + 2] : null;
		Step step4 = stepNum + 3 < steps.length ? steps[stepNum + 3] : null;
		// xpansProx[0].broadcastStep(stepNum, step1, step2, step3, step4);
		// //Does this only broadcast to two of the prox sensors then?
		for (int i = 0; i < xpansProx.length; i++)
			if (xpansProx[i] != null)
				xpansProx[i].broadcastStep(stepNum, step1, step2, step3, step4);
	}

	public void sendVibes(int avgReading) {
		sendVibes(avgReading, false);
	}

	public void sendVibes(int avgReading, boolean override) {
		// make sure the vibe xbee is present
		if (xpansVibe[0] == null)
			return;

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
		// int[] myData = { XPan.VIBE_OUT_PACKET_TYPE, avgReading };
		// xpansVibe[0].sendOutgoing(XPan.BROADCAST_ADDR, myData);
		xpansVibe[0].broadcastVibe(avgReading);

		// keep track of last sent value
		lastVibe = avgReading;
	}

	public void sendConfig(int stepLength) {
		for (int i = 0; i < xpansProx.length; i++)
			if (xpansProx[i] != null)
				xpansProx[i].broadcastProxConfig(stepLength);
	}

	// TODO replace this with configPatches to pass the step length
	// at the same time as detecting which ones respond.
	public void discoverPatches() {
		System.out.println("Discover patches...");
		for (int i = 0; i < XPAN_PROX_BASES; i++)
			if (xpansProx[i] != null) {
				System.out.println("Discover proximity " + (i + 1));
				xpansProx[i].nodeDiscover();
			}

		for (int i = 0; i < XPAN_VIBE_BASES; i++)
			if (xpansVibe[i] != null) {
				System.out.println("Discover vibration " + (i + 1));
				xpansVibe[i].nodeDiscover();
			}
	}
}
