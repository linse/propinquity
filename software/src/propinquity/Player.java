package propinquity;

import processing.core.PConstants;
import propinquity.hardware.*;
import java.lang.Math;

public class Player implements PConstants {
	
	public static final int SPAWN_DELAY_SHORT = 250;
	public static final int SPAWN_DELAY_MED = 250; //TODO Hack to make only one zone
	public static final int SPAWN_DELAY_LONG = 1000;
	public static final double SPAWN_DELAY_TAU = 3000;

	public static final int MAX_HEALTH = 100;
	
	Propinquity parent;

	String name;
	Color color;

	Patch[] patches;
	Glove glove;
	
	Patch bestPatch;
	long bestPatchTime;
	long bestPatchTimePauseDiff;

	long bopTime;
	long bopTimeDiff;

	Score score;
	
	boolean paused;
	boolean[] pausePatchStates;

	boolean coop;
	
	int health;

	public Player(Propinquity parent, Sounds sounds, String name, Color color, Patch[] patches, Glove glove) {
		this.parent = parent;
		
		this.name = name;
		this.color = color;

		this.patches = patches;
		this.glove = glove;

		pausePatchStates = new boolean[patches.length];

		score = new Score(parent, color);

		reset();
	}

	public void reset() {
		score.reset();
		heal();

		bestPatch = null;
		bestPatchTime = 0;
		bestPatchTimePauseDiff = 0;

		clearPatches();
		clearGloves();
		
		paused = true;
	}

	public void clearPatches() {
		for(int i = 0;i < patches.length;i++) {
			pausePatchStates[i] = false;
			patches[i].setActive(false);
			patches[i].clear();
		}

		setPatchesDefaults();
	}

	public void clearGloves() {
		glove.setActive(false);
		glove.clear();

		setGloveDefaults();
	}

	public void setPatchesDefaults() {
		for(Patch patch : patches) {
			patch.setColor(color);
			patch.setColorDuty(HardwareConstants.DEFAULT_DUTY_CYCLE);
			patch.setVibeDuty(HardwareConstants.DEFAULT_DUTY_CYCLE);
		}
	}

	public void setGloveDefaults() {
		glove.setVibeDuty(HardwareConstants.DEFAULT_DUTY_CYCLE);
	}

	public void pause() {
		score.pause();
		
		for(int i = 0;i < patches.length;i++) {
			pausePatchStates[i] = patches[i].getActive();
			patches[i].setActive(false);
		}
		glove.setActive(false);

		bestPatchTimePauseDiff = parent.millis()-bestPatchTime;

		paused = true;
	}

	public void start() {
		for(int i = 0;i < patches.length;i++) {
			patches[i].setActive(pausePatchStates[i]);
		}
		glove.setActive(true);

		score.start();

		bestPatchTime = parent.millis()-bestPatchTimePauseDiff;

		paused = false;
	}

	public void step(boolean coop, boolean[] patchStates) {
		this.coop = coop;

		for(int i = 0;i < patchStates.length;i++) {
			if(i < patches.length) {
				pausePatchStates[i] = patchStates[i];
				if(!paused) patches[i].setActive(patchStates[i]);
			} else {
				break;
			}
		}
	}

	public void activatePatches() {
		for(int i = 0;i < patches.length;i++) {
			pausePatchStates[i] = true;
			if(!paused) patches[i].setActive(true);
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getScore() {
		return score.getScore();
	}

	public Color getColor() {
		return color;
	}
	
	public int getHealth() {
		return health;
	}

	public Glove getGlove() {
		return glove;
	}

	public Patch[] getPatches() {
		return patches;
	}
	
	public void damage(int damage) {
		health -= damage;
	}
	
	public void heal() {
		health = MAX_HEALTH;
	}

	public void transferScore() {
		score.transfer();
	}

	public boolean isPatchOwner(Patch p) {
		for(Patch patch : patches) {
			if(patch == p) return true;
		}
		return false;
	}
	
	public void update() {
		score.update();

		Patch newBestPatch = null;

		for(Patch patch : patches) {
			if(!patch.getActive() || patch.getZone() == 0) continue;
			if(newBestPatch == null || patch.getZone() > newBestPatch.getZone()) newBestPatch = patch;
		}

		if(bestPatch == null) bestPatchTime = parent.millis();
		bestPatch = newBestPatch;
	}

	public long getSpawnInterval() {
		double timediff = parent.millis()-bestPatchTime;
		if(bestPatch != null) {
			if(bestPatch.getZone() == 1) {
				double val = SPAWN_DELAY_MED+(SPAWN_DELAY_LONG-SPAWN_DELAY_MED)*Math.exp(-timediff/SPAWN_DELAY_TAU);
				return Math.round(val);
			} else if(bestPatch.getZone() == 2) {
				double val = SPAWN_DELAY_SHORT+(SPAWN_DELAY_LONG-SPAWN_DELAY_SHORT)*Math.exp(-timediff/SPAWN_DELAY_TAU);
				return Math.round(val);
			}
		}

		return SPAWN_DELAY_LONG;
	}

	public Patch getBestPatch() {
		return bestPatch;
	}

	public void draw() {
		score.draw();
	}

	public void addPoints(int points) {
		score.addPoints(points);
	}
	
	public void addPoints(int points, Color color) {
		score.addPoints(points, color);
	}
	
}
