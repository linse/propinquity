package propinquity;

import processing.core.PConstants;

import propinquity.hardware.*;

import ddf.minim.AudioPlayer;

public class Player implements PConstants {

	Propinquity parent;

	String name;
	Color color;

	Patch[] patches;
	Glove glove;

	boolean paused;
	boolean[] pausePatchStates;

	AudioPlayer negSoundPlayer;
	AudioPlayer negSoundCoop;

	Score score;

	boolean coop;

	public Player(Propinquity parent, Sounds sounds, String name, Color color, Patch[] patches, Glove glove) {
		this.parent = parent;
		
		this.name = name;
		this.color = color;

		this.patches = patches;
		this.glove = glove;

		pausePatchStates = new boolean[patches.length];

		negSoundPlayer = sounds.getNegativePlayer(0); //TODO
		negSoundCoop = sounds.getNegativeCoop();

		score = new Score(parent, color);

		reset();
	}

	public void reset() {
		score.reset();

		for(int i = 0;i < patches.length;i++) {
			pausePatchStates[i] = false;
			patches[i].setActive(false);
			patches[i].clear();
		}

		glove.setActive(false);
		glove.clear();

		paused = true;
	}

	public void pause() {
		score.pause();
		
		for(int i = 0;i < patches.length;i++) {
			pausePatchStates[i] = patches[i].getActive();
			patches[i].setActive(false);
		}
		glove.setActive(false);

		paused = true;
	}

	public void start() {
		for(int i = 0;i < patches.length;i++) {
			patches[i].setActive(pausePatchStates[i]);
		}
		glove.setActive(true);

		score.start();

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

	public Glove getGlove() {
		return glove;
	}

	public Patch[] getPatches() {
		return patches;
	}

	public void playNegativeSound() {
		if(isCoop()) {
			if(negSoundCoop != null) {
				negSoundCoop.play();
				negSoundCoop.rewind();
			}
		} else {
			if(negSoundPlayer != null) {
				negSoundPlayer.play();
				negSoundPlayer.rewind();
			}
		}
	}

	public boolean isCoop() {
		return coop;
	}

	public void update() {
		score.update();
	}

	public void draw() {
		score.draw();
	}

	public void handleSweetspotRange(Patch patch) {
		// TODO
		score.increment();
		score.increment();
	}
	
	public void handleScoreRange(Patch patch) {
		// TODO
		score.increment();
	}
	
}
