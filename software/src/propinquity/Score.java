package propinquity;

public class Score {

	/**
	 * The amount of time (in ms) that must pass before all new particles are
	 * tallied and added to the player's permanent score.
	 */
	public static final int SCORE_TIME = 5000;

	public static final int MIN_RANGE = 256;
	public static final int MAX_RANGE = 512;

	public Liquid liquid;

	private Propinquity parent;
	private Color color;
	private int tempScore;
	private int heldScore;

	private long currentTime;
	private long lastTime;

	public Score(Propinquity parent, Color color) {
		this.parent = parent;
		this.color = color;
		tempScore = 0;
		heldScore = 0;
		lastTime = 0;
		liquid = new Liquid(parent, color);
	}

	public void reset() {
		tempScore = 0;
		heldScore = 0;
		lastTime = 0;
		liquid.reset();
	}

	public int getScore() {
		return heldScore;
	}

	public void increment() {
		tempScore++;
		liquid.createParticle();
	}

	public void update() {

		currentTime = parent.millis();

		if(currentTime - lastTime > Score.SCORE_TIME) {
			liquid.transferParticles();
			heldScore += tempScore;
			tempScore = 0;
			lastTime = currentTime;
		}
		
		liquid.update();
		if(color.equals(parent.playerColors[1]))
			liquid.applyReverseGravity();
		else
			liquid.applyGravity();
	}

	public void draw() {
		liquid.draw();
	}

}
