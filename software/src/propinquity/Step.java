package propinquity;

/**
 * Represents a step element from the XML level file for the ProxLevel game mechanic.
 *
 */
public class Step {

	StepType type;
	boolean[][] patches;

	public Step(StepType type, boolean[][] patches) {
		this.type = type;
		if(type == StepType.TRANSITION) this.patches = new boolean[2][4];
		else this.patches = patches;
	}

	public boolean[][] getPatches() {
		return patches;
	}

	public StepType getType() {
		return type;
	}

	public boolean isCoop() {
		if(type == StepType.COOP) return true;
		else return false;
	}

	public boolean isTransition() {
		if(type == StepType.TRANSITION) return true;
		else return false;
	}

}
