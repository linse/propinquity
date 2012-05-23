package propinquity;

import processing.core.*;
import processing.xml.*;
import propinquity.hardware.*;
import ddf.minim.*;

public abstract class Level implements UIElement, ProxEventListener, LevelConstants {

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

	public abstract void reset();

	public abstract void close();

	public abstract String getName();

	public abstract boolean isRunning();
	
	public abstract void keyPressed(char key, int keyCode);

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
