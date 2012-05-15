package propinquity;

import javax.media.opengl.GL;

import controlP5.ControlEvent;

import pbox2d.*;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;

import propinquity.hardware.*;

import java.util.*;

public class Propinquity extends PApplet implements PlayerConstants {

	/** Unique serialization ID. */
	static final long serialVersionUID = 6340518174717159418L;
	public static final int FPS = 30;

	//General + Util
	GL gl;
	
	Logger logger;

	GameState gameState;

	Vector<UIElement> uiElements;

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
		hud = new Hud(this);

		// Load common artwork and sound

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

			players[i] = new Player(this, null, color, patches, glove, sounds);
		}

		playerList = new PlayerList(this, hardware, "player.lst");
		levelSelect = new LevelSelect(this, hud, players, sounds);

		logger = new Logger(this);

		box2d = new PBox2D(this, (float) height / worldSize);
		box2d.createWorld(-worldSize / 2f, -worldSize / 2f, worldSize, worldSize);
		box2d.setGravity(0.0f, 0.0f);
		fences = new Fences(this);
		
		uiElements = new Vector<UIElement>();
		uiElements.add(xbeeManager);
		uiElements.add(playerList);
		uiElements.add(levelSelect);
		for(Level level : levelSelect.getLevels()) uiElements.add(level);
		
		changeGameState(GameState.XBeeInit);
	}

	public void draw() {
		// clear black
		background(Color.black().toInt(this));

		hud.update(hud.getAngle() + HALF_PI, TWO_PI / 10000f, TWO_PI / 2000f);

		for(UIElement u: uiElements) u.draw();
		if(gameState == GameState.Play) box2d.step();

		pushMatrix();
		translate(100, 100);
		simulator.draw();
		popMatrix();
		
		logger.recordFrame();
	}

	public void changeGameState(GameState newState) {
		for(UIElement u: uiElements) u.hide();

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
				level = levelSelect.getCurrentLevel();
				simulator.addProxEventListener(level);

				level.reset();
				level.show();
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
				level.keyPressed(key, keyCode);
				break;
			}
		}

		switch(key) {
			case 'n': {
				if(simulator.isVisible()) simulator.hide();
				else simulator.show();
				break;
			}
		}
	}

	public void stop() {
		for(Level level : levelSelect.getLevels()) level.close();
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.Propinquity" });
	}
}
