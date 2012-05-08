package propinquity;

public class Score {

	public static final int MIN_RANGE = 256;
	public static final int MAX_RANGE = 512;
	
	public Liquid liquid;
	
	private Propinquity parent;
	private Colour colour;
	private int score;
	
	public Score(Propinquity parent, Colour colour) {
		this.parent = parent;
		this.colour = colour;
		score = 0;
		liquid = new Liquid(parent, colour);
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
		if (colour.equals(parent.playerColours[1]))
			liquid.applyReverseGravity();
		else 
			liquid.applyGravity();	
	}
	
	public void draw() {
		liquid.draw();
	}
	
}
