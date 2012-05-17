package propinquity;

public class Score {

	/**
	 * The amount of time (in ms) that must pass before all new particles are
	 * tallied and added to the player's permanent score.
	 */
	public static final int SCORE_TIME = 5000;

	public static final int MIN_RANGE = 100;
	public static final int MAX_RANGE = 700;
	public static final int MIN_SWEETSPOT = 300;
	public static final int MAX_SWEETSPOT = 500;

	public Liquid liquid;

	Propinquity parent;
	Color color;

	int tempScore;
	int heldScore;

	long lastTime;
	long pauseDifferential;

	public Score(Propinquity parent, Color color) {
		this.parent = parent;
		this.color = color;
		tempScore = 0;
		heldScore = 0;
		lastTime = 0;
		liquid = new Liquid(parent, color);
	}

	public void pause() {
		pauseDifferential = parent.millis()-lastTime;
	}

	public void start() {
		lastTime = parent.millis()-pauseDifferential;
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
		for(int i = 0;i < points;i++) liquid.createParticle();
	}

	public void update() {
		long currentTime = parent.millis();

		if(currentTime - lastTime > Score.SCORE_TIME) {
			liquid.transferParticles();
			heldScore += tempScore;
			tempScore = 0;
			lastTime = currentTime;
		}
		
		liquid.Update();
	}

	public void draw() {
		liquid.draw();
	}

}
