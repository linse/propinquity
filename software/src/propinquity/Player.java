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

	AudioPlayer negSoundPlayer;
	AudioPlayer negSoundCoop;

	Score score;

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
