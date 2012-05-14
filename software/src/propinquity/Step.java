package propinquity;

public class Step {

	boolean coop;
	boolean[][] patches;

	public Step(boolean coop, boolean[][] patches) {
		this.coop = coop;
		this.patches = patches;
	}

	public boolean[][] getPatches() {
		return patches;
	}

	public boolean isCoop() {
		return coop;
	}

}
