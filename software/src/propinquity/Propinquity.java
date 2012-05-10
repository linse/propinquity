package propinquity;

import java.util.Vector;

import javax.media.opengl.GL;

import controlP5.ControlEvent;

import pbox2d.*;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import proxml.*;

import propinquity.hardware.*;

public class Propinquity extends PApplet {

	/** Unique serialization ID. */
	private static final long serialVersionUID = 6340518174717159418L;

	//General + Util
	GL gl;
	
	Logger logger;
	XMLInOut xmlInOut;

	GameState gameState;

	UIElement[] uiElements;

	boolean debugGraphics = false;
	Graphics graphics;
	Sounds sounds;

	//Xbee + Hardware
	XBeeBaseStation xbeeBaseStation;
	XBeeManager xbeeManager;

	HardwareSimulator simulator; //Testing 
	public Vector<Patch> patches = new Vector<Patch>(1);
	Glove glove;

	//Player/Player List
	PlayerList playerList;

	//HUD
	Hud hud;
	Color[] playerColors = { Color.blue(), Color.red() };
	Color neutralColor = Color.violet();

	//Level Select
	LevelSelect levelSelect;

	//Level
	boolean endedLevel = false;
	int endLevelTime = 6;
	long doneTime = -1;

	Level level;

	//Box 2D
	public float worldSize = 2f;
	Fences fences;
	PBox2D box2d;

	public void setup() {
		// Setup graphics and sound
		sounds = new Sounds(this);
		graphics = new Graphics(this); //Size is here

		// Load common artwork and sound
		graphics.loadCommonContent();
		sounds.loadCommonContent();

		// Create resources
		xbeeBaseStation = new XBeeBaseStation();
		// xbeeBaseStation.scan();
		xbeeManager = new XBeeManager(this, xbeeBaseStation);
		playerList = new PlayerList(this, "player.lst");
		levelSelect = new LevelSelect(this, sounds);

		hud = new Hud(this, sounds, graphics);
		logger = new Logger(this);

		box2d = new PBox2D(this, (float) height / worldSize);
		box2d.createWorld(-worldSize / 2f, -worldSize / 2f, worldSize, worldSize);
		box2d.setGravity(0.0f, 0.0f);
		fences = new Fences(this);
		
		uiElements = new UIElement[] { xbeeManager, playerList, levelSelect };

		simulator = new HardwareSimulator(this);
		patches.add(new Patch(0, simulator));
		patches.get(0).setActive(true);
		glove = new Glove(1, simulator);
		simulator.addPatch(patches.get(0));
		
		changeGameState(GameState.XBeeInit);
	}
	
	void resetLevel() {
		level.reset();

		endedLevel = false;
		doneTime = -1;

		hud.reset();
		levelSelect.reset();

		changeGameState(GameState.LevelSelect);
	}

	public void stop() {
		if(gameState == GameState.Play) level.clear();
	}

	public void draw() {
		// clear black
		background(Color.black().toInt(this));

		for(int i = 0; i < uiElements.length; i++) uiElements[i].draw();

		if(gameState == GameState.Play) drawPlay();

		pushMatrix();
		translate(100, 100);
		simulator.draw();
		popMatrix();
		
		logger.recordFrame();
	}

	void drawPlay() {
		graphics.drawInnerBoundary();
		graphics.drawOuterBoundary();

		if(debugGraphics) graphics.drawDebugFence();

		hud.draw();

		if(level.isDone()) {

			if(endedLevel) {
				Player winner = level.getWinner();

				textAlign(CENTER);
				pushMatrix();
				translate(width / 2, height / 2);
				rotate(frameCount * Hud.PROMPT_ROT_SPEED);
				image(graphics.hudLevelComplete, 0, -25);
				textFont(Graphics.font, Hud.FONT_SIZE);
				textAlign(CENTER, CENTER);
				fill(winner != null ? winner.getColor().toInt(this) : neutralColor.toInt(this));
				noStroke();
				text(winner != null ? winner.getName() + " won!" : "You tied!", 0, 0);
				image(graphics.hudPlayAgain, 0, 30);
				popMatrix();
			} else {
				// keep track of done time
				if(doneTime == -1) {
					level.clear();
					doneTime = frameCount;
				}

				// TODO: physics and things
				box2d.step();

				// snap score in final position
				hud.snap();

				// flag as ended
				if(doneTime != -1 && frameCount > doneTime + Graphics.FPS * endLevelTime) endedLevel = true;
			}

		} else if(level.isRunning()) {

			hud.update(hud.getAngle() + HALF_PI, TWO_PI / 10000f, TWO_PI / 2000f);

			// TODO: physics and things
			box2d.step();

			level.update();
			level.draw();

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

	public void changeGameState(GameState newState) {
		for(int i = 0; i < uiElements.length; i++) uiElements[i].hide();

		switch(newState) {
			case XBeeInit: {
				xbeeManager.show();
				break;
			}

			case PlayerList: {
				playerList.show();
				break;
			}

			case LevelSelect: {
				levelSelect.registerPlayers(playerList.getNames());
				levelSelect.reset();
				levelSelect.show();
				break;
			}

			case Play: {
				level = new Level(this, sounds, levelSelect.players, levelSelect.levelFile);
				graphics.loadLevelContent();

				level.load();
				break;
			}
		}

		gameState = newState;

		logger.println("gamestate = " + gameState);
	}

	public void controlEvent(ControlEvent event) {
		switch(gameState) {
			case XBeeInit: {
				xbeeManager.controlEvent(event);
				break;
			}

			case PlayerList: {
				playerList.controlEvent(event);
				break;
			}
		}
	}

	public void keyPressed() {
		switch(gameState) {
			case XBeeInit: {
				xbeeManager.keyPressed(key, keyCode);
				break;
			}

			case PlayerList: {
				playerList.keyPressed(key, keyCode);
				break;
			}

			case LevelSelect: {
				levelSelect.keyPressed(key, keyCode);
				break;
			}

			case Play: {
				switch(key) {
					case ESC: {
						level.clear();
						exit();
						break;
					}

					case ENTER:
					case ' ': {
						if(level.isDone() && endedLevel) resetLevel();
						else if(!level.isDone() && !level.isRunning()) level.start();
						else if(!level.isDone()) level.pause();
						break;
					}

					case BACKSPACE: {
						if(!level.isRunning()) resetLevel();
						break;
					}

					case 'i': { // info
						int score0 = level.getPlayer(0).score.liquid.particlesHeld.size();
						int score1 = level.getPlayer(1).score.liquid.particlesHeld.size();
						println("Particles: " + (score0 + " " + score1));
						println("Framerate: " + frameRate);
						break;
					}

					case 'e': {// play stub
						level.currentStep = level.stepCount;
						break;
					}

					case 'n': {
						simulator.show();
						break;
					}

					case 'm': {
						simulator.hide();
						break;
					}
				}
				break;
			}
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.Propinquity" });
	}
}
