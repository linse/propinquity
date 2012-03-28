package propinquity;

import processing.core.*;

/**
 * Handles graphic content initialization and usage.
 * 
 * @author Stephane Beniak
 */
public class Graphics {
	
	public static final int FPS = 30;
		
	public static PFont font;
	
	public static PImage hudInnerBoundary, hudOuterBoundary;
	public static PImage hudPlay, hudLevelComplete, hudPlayAgain;
	public static PImage hudPlayers[], hudCoop;
	
	private static Propinquity app;
	
	/**
	 * Suppress default constructor to disable instantiability.
	 */
	private Graphics () {
		throw new AssertionError();
	}
	
	/**
	 * Setup the PApplet according predefined parameters such as size, frame rate, etc.
	 * 
	 * @param application The PApplet to be initialized.
	 */
	public static void setup(Propinquity application) {
		
		app = application;
		
		app.size(1024, 768, PConstants.OPENGL);
		app.frameRate(FPS);
		app.imageMode(PConstants.CENTER);
		app.textureMode(PConstants.NORMAL);
		app.hint(PConstants.ENABLE_OPENGL_4X_SMOOTH);
	}
	
	/**
	 * Load common graphics content such as fonts and images.
	 */
	public static void loadCommonContent() {
		
		// Load main font
		font = app.loadFont("hud/Calibri-Bold-32.vlw");
		
		hudInnerBoundary = app.loadImage("hud/innerBoundary.png");
		hudOuterBoundary = app.loadImage("hud/outerBoundary.png");
		
		hudPlay = app.loadImage("hud/sbtoplay.png");
		hudLevelComplete = app.loadImage("hud/levelcomplete.png");
		hudPlayAgain = app.loadImage("hud/sbtoplayagain.png");
	}
	
	/**
	 * Load level-specific graphics content.
	 */
	public static void loadLevelContent() {
		
		// Load player HUDs
		hudPlayers = new PImage[app.level.getNumPlayers()];
		for (int i = 0; i < app.level.getNumPlayers(); i++)
			hudPlayers[i] = app.loadImage("data/hud/player" + (i + 1) + "score.png");
		
		// Load co-op HUD
		hudCoop = app.loadImage("data/hud/level.png");
	}
	
}
