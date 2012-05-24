package propinquity;

import ddf.minim.AudioSample;

public class Score {

	/**
	 * The amount of time (in ms) that must pass before all new particles are
	 * tallied and added to the player's permanent score.
	 */
	public static final int SCORE_TIME = 5000;

	public static final int MIN_RANGE = 50;
	public static final int MAX_RANGE = 650;
	public static final int MIN_SWEETSPOT = 450;
	public static final int MAX_SWEETSPOT = 650;

	public Liquid liquid;

	Propinquity parent;
	Color color;

	int tempScore;
	int heldScore;

	long lastTime;
	long pauseDifferential;
	
	AudioSample bubbleSound;

	public Score(Propinquity parent, Color color) {
		this.parent = parent;
		this.color = color;
		tempScore = 0;
		heldScore = 0;
		lastTime = 0;
		liquid = new Liquid(parent, color);
		
		if(this.color.equals(PlayerConstants.PLAYER_COLORS[0]))
			bubbleSound = parent.sounds.bubbleHigh;
		else 
			bubbleSound = parent.sounds.bubbleLow;
	}

	public void pause() {
		pauseDifferential = parent.millis() - lastTime;
	}

	public void start() {
		lastTime = parent.millis() - pauseDifferential;
	}

	public void reset() {
		tempScore = 0;
		heldScore = 0;
		lastTime = 0;
		pauseDifferential = 0;
		liquid.reset();
	}

	public int getScore() {
		return heldScore;
	}

	public void addPoints(int points) {
		tempScore += points;
		for (int i = 0; i < points; i++) {
			liquid.createParticle();
			bubbleSound.trigger();
		}
	}

	public void update() {
		long currentTime = parent.millis();

		if (currentTime - lastTime > Score.SCORE_TIME) {
			transfer();
			lastTime = currentTime;
		}

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
