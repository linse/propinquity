package propinquity;

import processing.core.PConstants;
import propinquity.hardware.*;
import java.lang.Math;

/**
 * Represents the player and holds his glove, patches and score. It provides some high level mechanisms to clear and set gloves and patches. It also currently provides some elements of gameplay needed for certain levels, it would be nice if this was decoupled in the future.
 *
 */
public class Player implements PConstants {
	
	public static final int SPAWN_DELAY_SHORT = 100;
	public static final int SPAWN_DELAY_MED = 250;
	public static final int SPAWN_DELAY_LONG = 1000;
	public static final double SPAWN_DELAY_TAU = 3000;

	public static final int MAX_HEALTH = 15;
	
	Propinquity parent;

	String name;
	Color color;

	Patch[] patches;
	Glove glove;
	
	Patch bestPatch;
	long bestPatchTime;
	long bestPatchTimePauseDiff;

	long bopTime; //FIXME: There is level elements in player, is there a nice way this could be fixed? The player should be level agnostic.
	long bopTimeDiff;

	Score score;
	
	boolean paused;
	boolean[] pausePatchStates;

	boolean coop;
	
	int[] patchHealth;

	boolean suppressPoints;

	public Player(Propinquity parent, Sounds sounds, String name, Color color, Patch[] patches, Glove glove) {
		this.parent = parent;
		
		this.name = name;
		this.color = color;

		this.patches = patches;
		this.glove = glove;

		patchHealth = new int[patches.length];

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

	public void bump() {
		score.bump();
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

		for(Patch patch : patches) {
			// if(coop) patch.setColor(PlayerConstants.NEUTRAL_COLOR);
			if(coop) patch.setColor(Color.teal());
			else patch.setColor(color);
		}

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
			patches[i].setColor(color);
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
		int totalHealth = 0;
		for(int i = 0;i < patchHealth.length;i++) {
			totalHealth += patchHealth[i];
		}
		return totalHealth;
	}

	public Glove getGlove() {
		return glove;
	}

	public Patch[] getPatches() {
		return patches;
	}
	
	public boolean damage(Patch p, int damage) {
		for(int i = 0;i < patches.length;i++) {
			if(patches[i] == p) {
				patchHealth[i] -= damage;
				if(patchHealth[i] < 0) {
					patchHealth[i] = 0;
					p.setActive(false);
				} else {
					p.setColor(color.r*patchHealth[i]/MAX_HEALTH, color.g*patchHealth[i]/MAX_HEALTH, color.b*patchHealth[i]/MAX_HEALTH);
				}
				return true;
			}
		}
		return false;
	}
	
	public void heal() {
		for(int i = 0;i < patchHealth.length;i++) {
			patchHealth[i] = MAX_HEALTH;
		}
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
		if(!suppressPoints) score.addPoints(points);
	}

	public void addPoints(int points, boolean sound) {
		if(!suppressPoints) score.addPoints(points, sound);
	}

	public void addPoints(int points, Color color) {
		if(!suppressPoints) score.addPoints(points, color, true);
	}
	
	public void addPoints(int points, Color color, boolean sound) {
		if(!suppressPoints) score.addPoints(points, color, sound);
	}
	
	public void suppressPoints(boolean suppressPoints) {
		this.suppressPoints = suppressPoints;
	}
}
