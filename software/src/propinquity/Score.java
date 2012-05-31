package propinquity;

import ddf.minim.AudioSample;

public class Score {

	/**
	 * The amount of time (in ms) that must pass before all new particles are
	 * tallied and added to the player's permanent score.
	 */
	public static final int SCORE_TIME = 5000;

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
		
		if(this.color.equals(PlayerConstants.PLAYER_COLORS[0])) bubbleSound = parent.sounds.getBubbleHigh();
		else bubbleSound = parent.sounds.getBubbleLow();
	}

	public void bump() {
		liquid.bump();
	}

	public void pause() {
		liquid.bump();
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
		long currentTime = parent.millis();

		if(currentTime - lastTime > Score.SCORE_TIME) {
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
