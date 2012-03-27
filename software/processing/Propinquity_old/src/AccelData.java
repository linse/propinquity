public class AccelData {
	int player;
	int patch;
	int x, y, z;

	public AccelData(int player, int patch, int x, int y, int z) {
		this.player = player;
		this.patch = patch;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return "[player:" + player + " patch:" + patch + " xyz:" + x + "," + y
				+ "," + z + "]";
	}
}
