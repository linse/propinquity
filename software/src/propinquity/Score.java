package propinquity;

public class Score {

	public Liquid liquid;
	
	private int score;
	
	public Score(Propinquity parent, Colour colour) {
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
	
	public void increase(int scoreAdded) {
		score += scoreAdded;
		
		for (int i = 0; i < scoreAdded; i++) {
			liquid.createParticle();
		}
	}
	
	public void update() {
		liquid.update();
	}
	
	public void draw() {
		liquid.draw();
	}
	
}
