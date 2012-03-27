package propinquity;

import processing.core.*;

/**
 * 
 * 
 * @author Stephane Beniak
 */
public class Graphics {
	
	public static final int FPS = 30;
		
	public static PFont font;
	
	public static PImage hudInnerBoundary, hudOuterBoundary;
	public static PImage hudImgPlay, hudImgLevelComplete, hudImgPlayAgain;
	
	private static PApplet app;
	
	/**
	 * Suppress default constructor to disable instantiability
	 */
	private Graphics () {
		throw new AssertionError();
	}
	
	/**
	 * Setup the PApplet according predefined parameters such as size, frame rate, etc.
	 * 
	 * @param application The PApplet to be initialized.
	 */
	public static void setup(PApplet application) {
		
		app = application;
		
		app.size(1024, 768, PConstants.OPENGL);
		app.frameRate(FPS);
		app.imageMode(PConstants.CENTER);
		app.textureMode(PConstants.NORMAL);
		app.hint(PConstants.ENABLE_OPENGL_4X_SMOOTH);
	}
	
	/**
	 * Load graphics content such as fonts and images.
	 */
	public static void loadContent() {
		
		// Load main font
		font = app.loadFont("hud/Calibri-Bold-32.vlw");
		
		hudInnerBoundary = app.loadImage("hud/innerBoundary.png");
		hudOuterBoundary = app.loadImage("hud/outerBoundary.png");
		
		hudImgPlay = app.loadImage("hud/sbtoplay.png");
		hudImgLevelComplete = app.loadImage("hud/levelcomplete.png");
		hudImgPlayAgain = app.loadImage("hud/sbtoplayagain.png");
	}
	
}
