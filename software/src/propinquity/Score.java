package propinquity;

public class Score {

	public static final int MIN_RANGE = 256;
	public static final int MAX_RANGE = 512;
	
	public Liquid liquid;
	
	private Propinquity parent;
	private Color color;
	private int score;
	
	public Score(Propinquity parent, Color color) {
		this.parent = parent;
		this.color = color;
		score = 0;
		liquid = new Liquid(parent, color);
	}
	
	public void reset() {
		score = 0;
		liquid.reset();
	}
	
	public int getScore() {
		return score;
	}
	
	public void increment() {
		score++;
		liquid.createParticle();
	}
	
	public void update() {
		liquid.update();
		if (color.equals(parent.playerColors[1]))
			liquid.applyReverseGravity();
		else 
			liquid.applyGravity();	
	}
	
	public void draw() {
		liquid.draw();
	}
	
}
