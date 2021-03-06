package propinquity;

import propinquity.hardware.*;

/**
 * This abstract class hold common behavior and methods for all type of Level. Each type of Level may have different game mechanics, scoring, etc. However, by implementing this abstract class, the can be easily handled using generics.
 *
 * It would be profitable if more common behavior could be migrated in this abstract class.
 *
 */
public abstract class Level implements UIElement, ProxEventListener, AccelEventListener, LevelConstants {

	Propinquity parent;
	
	Hud hud;
	Sounds sounds;

	Player[] players;

	boolean isVisible;

	public Level(Propinquity parent, Hud hud, Sounds sounds, Player[] players) {
		this.parent = parent;

		this.hud = hud;
		this.sounds = sounds;

		this.players = players;
	}

	public abstract void pause();

	public abstract void start();

	public void startPreview() {};

	public void stopPreview() {};

	public abstract void reset();

	public abstract void close();

	public abstract String getName();

	public int getBPM() {
		return -1;
	}

	public abstract boolean isRunning();
	
	public abstract void keyPressed(char key, int keyCode);

	public void mouseClicked() {} //Empty implementation

	/**
	 * Shows the GUI.
	 * 
	 */
	public void show() {
		isVisible = true;
	}

	/**
	 * Hides the GUI.
	 * 
	 */
	public void hide() {
		isVisible = false;
	}

	/**
	 * Returns true if the GUI is visible.
	 * 
	 * @return true is and only if the GUI is visible.
	 */
	public boolean isVisible() {
		return isVisible;
	}

}
