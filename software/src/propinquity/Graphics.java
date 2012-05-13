package propinquity;

import javax.media.opengl.GL;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;

/**
 * Handles graphic content initialization and usage.
 * 
 * @author Stephane Beniak
 */
public class Graphics {

	/**
	 * Application frame rate.
	 */
	public static final int FPS = 30;

	public static PFont font;

	public PImage hudInnerBoundary, hudOuterBoundary;
	public PImage hudPlay, hudLevelComplete, hudPlayAgain;
	public PImage hudPlayers[], hudCoop;

	Propinquity parent;

	/**
	 * Setup the PApplet according predefined parameters such as size, frame
	 * rate, etc.
	 * 
	 * @param parent The Propinquity to be initialized.
	 */
	public Graphics(Propinquity parent) {
		this.parent = parent;

		parent.size(1024, 768, PConstants.OPENGL);
		parent.frameRate(FPS);
		parent.imageMode(PConstants.CENTER);
		parent.textureMode(PConstants.NORMAL);
		parent.hint(PConstants.ENABLE_OPENGL_4X_SMOOTH);
	}

	/**
	 * Load common graphics content such as fonts and images.
	 */
	public void loadCommonContent() {

		// Load main font
		font = parent.loadFont("hud/Calibri-Bold-32.vlw");

		hudInnerBoundary = parent.loadImage("hud/innerBoundary.png");
		hudOuterBoundary = parent.loadImage("hud/outerBoundary.png");

		hudPlay = parent.loadImage("hud/sbtoplay.png");
		hudLevelComplete = parent.loadImage("hud/levelcomplete.png");
		hudPlayAgain = parent.loadImage("hud/sbtoplayagain.png");
	}

	/**
	 * Load level-specific graphics content.
	 */
	public void loadLevelContent() {

		// Load player HUDs
		hudPlayers = new PImage[parent.level.getNumberOfPlayers()];
		for(int i = 0; i < parent.level.getNumberOfPlayers(); i++)
			hudPlayers[i] = parent.loadImage("data/hud/player" + (i + 1) + "score.png");

		// Load co-op HUD
		hudCoop = parent.loadImage("data/hud/level.png");
	}

	/**
	 * Loads the common particle image.
	 * 
	 * @return The particle image used for all players.
	 */
	public PImage loadParticle() {
		return parent.loadImage("data/particles/particle.png");
	}

	public void drawInnerBoundary() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.pushMatrix();
		parent.translate(parent.width / 2 - 1, parent.height / 2);
		parent.image(parent.graphics.hudInnerBoundary, 0, 0);
		parent.popMatrix();
	}

	public void drawOuterBoundary() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.pushMatrix();
		parent.translate(parent.width / 2 - 1, parent.height / 2);
		parent.image(parent.graphics.hudOuterBoundary, 0, 0);
		parent.popMatrix();
	}

}
