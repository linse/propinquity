public class ProxData {
	int player;
	int patch;
	int step;
	boolean touched;
	int proximity;

	public ProxData(int player, int patch, int step, boolean touched,
			int proximity) {
		this.player = player;
		this.patch = patch;
		this.step = step;
		this.touched = touched;
		this.proximity = proximity;
	}

	public String toString() {
		return "[player:" + player + " patch:" + patch + " step:" + step
				+ " touched:" + touched + " proximity:" + proximity + "]";
	}
}
