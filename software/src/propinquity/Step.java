package propinquity;

public class Step {
	boolean _led0; // patch 1
	boolean _led1; // patch 2
	boolean _led2; // patch 3
	boolean _led3; // patch 4
	boolean _free; // don't need to use this now

	public Step(boolean led0, boolean led1, boolean led2, boolean led3, boolean free) {
		_led0 = led0;
		_led1 = led1;
		_led2 = led2;
		_led3 = led3;
		_free = free;
	}

	public boolean isFree() {
		return _free;
	}

	public int getPacketComponent() {
		int p = 0;
		if (_led0)
			p |= 8;
		if (_led1)
			p |= 4;
		if (_led2)
			p |= 2;
		if (_led3)
			p |= 1;
		return p;
	}

	public int[] getCurrentPatches() {
		int[] patches = { -1, -1, -1, -1 };
		int num = 0;
		if (_led0)
			patches[num++] = 0;
		if (_led1)
			patches[num++] = 1;
		if (_led2)
			patches[num++] = 2;
		if (_led3)
			patches[num] = 3;
		return patches;
	}
}
