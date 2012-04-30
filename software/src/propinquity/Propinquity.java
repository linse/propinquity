package propinquity;

import javax.media.opengl.GL;

import org.jbox2d.testbed.TestSettings;

import controlP5.ControlEvent;

import pbox2d.*;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import proxml.*;
import xbee.XBeeReader;

import propinquity.xbee.*;

public class Propinquity extends PApplet {

	// Unique serialization ID
	private static final long serialVersionUID = 6340518174717159418L;

	// debug constants
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_XBEE = false;
	public static final boolean DRAW_SHADOWS = false;
	public static final boolean DRAW_PARTICLES = true;
	public static final int FULL_SCREEN_ID = 0;

	// game constants
	public static final float WORLD_SIZE = 2f;
	final int END_LEVEL_TIME = 6;
	final int BOUNDARY_WIDTH = 5;
	final int[] PLAYER_COLORS = { color(55, 137, 254), color(255, 25, 0) };
	final int NEUTRAL_COLOR = color(142, 20, 252);

	GameState gameState;

	// level select controller
	LevelSelect levelSelect = null;

	// player list controller
	PlayerList playerList = null;

	// OpenGL
	GL gl;

	Liquid liquid;
	PBox2D box2d;
	TestSettings settings;

	// Level parameters
	Level level;
	boolean endedLevel = false;
	long doneTime = -1;

	XBeeManager xbeeManager;
	XMLInOut xmlInOut;
	Hud hud;

	Logger logger;
	Sounds sounds;
	Graphics graphics;

	UIElement[] ui_elements;

	public void setup() {

		// Setup graphics and sound
		sounds = new Sounds(this);
		graphics = new Graphics(this);

		// initial opengl setup
		gl = ((PGraphicsOpenGL) g).gl;
		gl.glDisable(GL.GL_DEPTH_TEST);

		// Load common artwork and sound
		graphics.loadCommonContent();
		sounds.loadCommonContent();

		// Create resources
		xbeeManager = new XBeeManager(this, this, DEBUG_XBEE);

		playerList = new PlayerList(this);

		hud = new Hud(this, sounds, graphics);

		// init logging
		logger = new Logger(this);

		ui_elements = new UIElement[] { xbeeManager, playerList };

		changeGameState(GameState.XBeeInit);
	}

	public void draw() {
		// clear
		background(0);

		for (int i = 0; i < ui_elements.length; i++)
			ui_elements[i].draw();

		switch (gameState) {
		case LevelSelect:
			// TODO: To fix next.

			// init level select UI
			if (levelSelect == null) {
				playerList.dispose();
				levelSelect = new LevelSelect(this, sounds, playerList);
			}
			levelSelect.draw();
			break;

		case Play:
			drawPlay();
			break;
		}

		logger.recordFrame();
	}

	public void stop() {
		if (gameState == GameState.Play)
			level.clear();
	}

	void drawPlay() {
		graphics.drawInnerBoundary();
		if (DRAW_PARTICLES)
			liquid.draw();
		drawMask();
		graphics.drawOuterBoundary();

		if (DEBUG)
			graphics.drawDebugFence();

		hud.draw();

		if (level.isDone()) {

			if (endedLevel) {
				Player winner = level.getWinner();

				textAlign(CENTER);
				pushMatrix();
				translate(width / 2, height / 2);
				rotate(frameCount * Hud.PROMPT_ROT_SPEED);
				image(graphics.hudLevelComplete, 0, -25);
				textFont(Graphics.font, Hud.FONT_SIZE);
				textAlign(CENTER, CENTER);
				fill(winner != null ? winner.getColor() : NEUTRAL_COLOR);
				noStroke();
				text(winner != null ? winner.getName() + " won!" : "You tied!", 0, 0);
				image(graphics.hudPlayAgain, 0, 30);
				popMatrix();
			} else {
				// keep track of done time
				if (doneTime == -1) {
					level.clear();
					doneTime = frameCount;
				}

				// give the last push
				liquid.pushPeriod(true);

				// step through time
				box2d.step();

				// liquify
				liquid.liquify();

				// snap score in final position
				hud.snap();

				// pull particles into groups
				liquid.groupParticles();

				// flag as ended
				if (doneTime != -1 && frameCount > doneTime + Graphics.FPS * END_LEVEL_TIME)
					endedLevel = true;
			}

		} else if (level.isRunning()) {
			// update hud
			hud.update(hud.getAngle() + HALF_PI, TWO_PI / 10000f, TWO_PI / 2000f);

			// push particles of current period out
			liquid.pushPeriod();

			// release balls
			liquid.updateParticles();

			// step through time
			box2d.step();

			// liquify
			liquid.liquify();

			// process level
			level.update();

			// read level data stub
			// if (USE_STUB) level.processStub();

		} else {
			gl = ((PGraphicsOpenGL) g).gl;
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

			fill(255);
			textAlign(CENTER);
			pushMatrix();
			translate(width / 2, height / 2);
			rotate(frameCount * Hud.PROMPT_ROT_SPEED);
			image(graphics.hudPlay, 0, 0);
			popMatrix();
		}
	}

	void resetLevel() {
		liquid.resetLiquid();
		level.reset();
		endedLevel = false;
		doneTime = -1;
		liquid.groupedParticles = false;
		liquid.lastPeriodParticle = new Particle[level.getNumPlayers()];

		hud.reset();

		levelSelect.reset();
		gameState = GameState.LevelSelect;
		println("gamestate = " + gameState);
	}

	void drawMask() {
		gl = ((PGraphicsOpenGL) g).gl;
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_ZERO);

		pushMatrix();
		translate(width / 2, height / 2);
		scale(width / 2, height / 2);
		beginShape(QUADS);
		texture(hud.hudMask);
		vertex(-1, -1, 0, 0, 0);
		vertex(1, -1, 0, 1, 0);
		vertex(1, 1, 0, 1, 1);
		vertex(-1, 1, 0, 0, 1);
		endShape(CLOSE);
		popMatrix();
	}

	public void changeGameState(GameState new_state) {
		for (int i = 0; i < ui_elements.length; i++)
			ui_elements[i].hide();

		switch (new_state) {
		case XBeeInit:
			xbeeManager.show();
			break;
		case PlayerList:
			playerList.show();
			break;
		case LevelSelect:

			break;
		case Play:

			break;
		}

		gameState = new_state;

		println("gamestate = " + gameState);
	}

	public void xBeeEvent(XBeeReader xbee) {

		switch (gameState) {

		case XBeeInit:
			xbeeManager.xBeeEvent(xbee);
			break;

		case LevelSelect:
			println("xBeeEvent(): sending to level select");
			levelSelect.xBeeEvent(xbee);
			break;

		case Play:
			level.xBeeEvent(xbee);
			break;
		}

	}

	public void controlEvent(ControlEvent theEvent) {
		switch (gameState) {
		case XBeeInit:
			xbeeManager.controlEvent(theEvent);
			break;
		case PlayerList:
			playerList.controlEvent(theEvent);
			break;
		}
	}

	public void keyPressed() {
		switch (gameState) {
		case XBeeInit:
			xbeeManager.keyPressed(keyCode);
			break;
		case PlayerList:
			playerList.keyPressed(keyCode);
			break;

		case LevelSelect:
			switch (key) {
			case BACKSPACE:
				levelSelect.clear();
				levelSelect = null;
				playerList = null;
				// initPlayerListCtrl();
				changeGameState(GameState.PlayerList);
				break;

			default:
				if (levelSelect != null) {
					// pass the key to the level select controller
					levelSelect.keyPressed(key, keyCode);

					// check if the level select controller is done
					// and ready to play
					if (levelSelect.isDone()) {
						// init level
						level = new Level(this, sounds, levelSelect.players, levelSelect.levelFile);
						graphics.loadLevelContent();

						while (true)
							if (level.successfullyRead() > -1)
								break;

						if (level.successfullyRead() == 0) {
							level.loadDefaults();
							System.err.println("I had some trouble reading the level file.");
							System.err.println("Defaulting to 2 minutes of free play instead.");
						}

						// send configuration message here
						// TODO: send step length to proximity patches

						delay(50);
						while (!levelSelect.allAcksIn()) {
							println("sending again");
							levelSelect.sendConfigMessages(level.getStepInterval());
							delay(50);
						}

						// init liquid particles
						liquid = new Liquid(this);

						// play
						gameState = GameState.Play;
						println("gamestate = " + gameState);
					}
				}
				break;
			}
			break;

		case Play:
			switch (key) {

			case ESC:
				level.clear();
				exit();
				break;

			case ENTER:
			case ' ':
				if (level.isDone() && endedLevel)
					resetLevel();
				else if (!level.isDone() && level.isPaused())
					level.start();
				else if (!level.isDone())
					level.pause();
				break;

			case BACKSPACE:
				if (level.isPaused())
					resetLevel();
				break;

			case 'i': // info
				println("Particles: " + (liquid.particles[0].size() + liquid.particles[1].size()));
				println("Framerate: " + frameRate);
				println("Radius: " + liquid.particleRadius);
				println("Viscosity: " + liquid.particleViscosity);
				break;

			case '8':
				liquid.particleRadius += 0.01;
				break;

			case '2':
				liquid.particleRadius -= 0.01;
				if (liquid.particleRadius < 0)
					liquid.particleViscosity = 0;
				break;

			case '4':
				liquid.particleViscosity -= 0.001;
				if (liquid.particleViscosity < 0)
					liquid.particleViscosity = 0;
				break;

			case '6':
				liquid.particleViscosity += 0.001;
				break;

			case 'e': // play stub
				level.currentStep = level.numSteps;
				break;

			case 'f': // flush output and close
				logger.close();
				exit();
				break;
			}
			break;
		}

	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "--bgcolor=#FFFFFF", "propinquity.Propinquity" });
	}
}
