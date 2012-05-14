package propinquity;

import java.util.Vector;

import javax.media.opengl.GL;

import controlP5.ControlEvent;

import pbox2d.*;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import proxml.*;

import propinquity.hardware.*;

public class Propinquity extends PApplet implements PlayerConstants {

	/** Unique serialization ID. */
	private static final long serialVersionUID = 6340518174717159418L;
	public static final int FPS = 30;

	//General + Util
	GL gl;
	
	Logger logger;
	XMLInOut xmlInOut;

	GameState gameState;

	UIElement[] uiElements;

	Sounds sounds;

	//Xbee + Hardware
	HardwareInterface hardware;

	XBeeBaseStation xbeeBaseStation;
	XBeeManager xbeeManager;

	HardwareSimulator simulator; //Testing 

	//Player/Player List
	Player[] players;
	PlayerList playerList;

	//HUD
	Hud hud;

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
		size(1024, 768, PConstants.OPENGL);
		frameRate(FPS);
		imageMode(PConstants.CENTER);
		textureMode(PConstants.NORMAL);
		hint(PConstants.ENABLE_OPENGL_4X_SMOOTH);

		// Setup sound
		sounds = new Sounds(this);
		hud = new Hud(this, sounds);

		// Load common artwork and sound
		sounds.loadCommonContent();

		// Create resources
		xbeeBaseStation = new XBeeBaseStation();
		// xbeeBaseStation.scan();
		xbeeManager = new XBeeManager(this, xbeeBaseStation);

		simulator = new HardwareSimulator(this);

		hardware = simulator;

		players = new Player[MAX_PLAYERS];

		for(int i = 0;i < MAX_PLAYERS;i++) {
			Patch[] patches = new Patch[PATCH_ADDR[i].length];

			for(int j = 0;j < PATCH_ADDR[i].length;j++) {
				patches[j] = new Patch(PATCH_ADDR[i][j], hardware);
				patches[j].setActive(true);
				hardware.addPatch(patches[j]);
			}

			Glove glove = new Glove(GLOVE_ADDR[i], hardware);

			hardware.addGlove(glove);

			Color color = PLAYER_COLORS[i];

			players[i] = new Player(this, null, color, patches, glove);
		}

		playerList = new PlayerList(this, hardware, "player.lst");
		levelSelect = new LevelSelect(this, hud, players, sounds);

		logger = new Logger(this);

		box2d = new PBox2D(this, (float) height / worldSize);
		box2d.createWorld(-worldSize / 2f, -worldSize / 2f, worldSize, worldSize);
		box2d.setGravity(0.0f, 0.0f);
		fences = new Fences(this);
		
		uiElements = new UIElement[] { xbeeManager, playerList, levelSelect };
		
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
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();

		hud.draw();

		if(level.isDone()) {

			if(endedLevel) {
				Player winner = level.getWinner();

				textAlign(CENTER);
				pushMatrix();
				translate(width / 2, height / 2);
				rotate(frameCount * Hud.PROMPT_ROT_SPEED);
				image(hud.hudLevelComplete, 0, -25);
				textFont(hud.font, Hud.FONT_SIZE);
				textAlign(CENTER, CENTER);
				fill(winner != null ? winner.getColor().toInt(this) : NEUTRAL_COLOR.toInt(this));
				noStroke();
				text(winner != null ? winner.getName() + " won!" : "You tied!", 0, 0);
				image(hud.hudPlayAgain, 0, 30);
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
				if(doneTime != -1 && frameCount > doneTime + FPS * endLevelTime) endedLevel = true;
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
			image(hud.hudPlay, 0, 0);
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
				levelSelect.setPlayerNames(playerList.getPlayerNames());

				levelSelect.reset();
				levelSelect.show();
				break;
			}

			case Play: {
				level = new Level(this, sounds, levelSelect.players, levelSelect.levelFile);

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
						logger.println("Particles: " + (score0 + " " + score1));
						logger.println("Framerate: " + frameRate);
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
