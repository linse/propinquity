package propinquity;

import ddf.minim.AudioSample;

/**
 * Hold the player's score and it's liquid representation. This includes the total score and the temporary round score which is held in the middle pool.
 *
 */
public class Score {

	public Liquid liquid;

	Propinquity parent;
	Color color;

	int tempScore;
	int heldScore;

	long pauseDifferential;
	
	AudioSample bubbleSound;

	public Score(Propinquity parent, Color color) {
		this.parent = parent;
		this.color = color;
		tempScore = 0;
		heldScore = 0;
		liquid = new Liquid(parent, color);
		
		if(this.color.equals(PlayerConstants.PLAYER_COLORS[0])) bubbleSound = parent.sounds.getBubbleHigh();
		else bubbleSound = parent.sounds.getBubbleLow();
	}

	public void bump() {
		liquid.bump();
	}

	public void pause() {
		liquid.bump();
	}

	public void start() {

	}

	public void reset() {
		tempScore = 0;
		heldScore = 0;
		pauseDifferential = 0;
		liquid.reset();
	}

	public int getScore() {
		return heldScore+tempScore; //TODO: depending on the score mechanism maybe this needs to be changed
	}

	public void addPoints(int points) {
		addPoints(points, null, true);
	}

	public void addPoints(int points, boolean sound) {
		addPoints(points, null, sound);
	}

	public void addPoints(int points, Color color) {
		addPoints(points, color, true);
	}
	
	public void addPoints(int points, Color color, boolean sound) {
		tempScore += points;
		for(int i = 0; i < points; i++) {
			liquid.createParticle(color);
			if(sound) bubbleSound.trigger();
		}
	}

	public void update() {
		liquid.Update();
	}

	public void transfer() {
		liquid.transferParticles();
		heldScore += tempScore;
		tempScore = 0;
	}

	public void draw() {
		liquid.draw();
	}

}
