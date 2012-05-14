package propinquity;

import processing.core.PConstants;

import propinquity.hardware.*;

import ddf.minim.AudioPlayer;

public class Player implements PConstants, ProxEventListener {

	Propinquity parent;

	String name;
	Color color;

	Patch[] patches;
	Glove glove;

	AudioPlayer negSoundPlayer;
	AudioPlayer negSoundCoop;

	Score score;

	int distance, currentTime, lastTime;

	boolean coop;

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
		score.reset();
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
		if (isCoop()) {
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

	public void setCoop(boolean coop) {
		this.coop = coop;
	}

	public boolean isCoop() {
		return coop;
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

	public void proxEvent(Patch patch) {
		for(Patch p : patches) {
			if(patch == p) distance = patch.getProx();
		}
	}
}
