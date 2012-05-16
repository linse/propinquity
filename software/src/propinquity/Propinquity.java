package propinquity;

import controlP5.ControlEvent;

import pbox2d.*;

import java.io.File;

import processing.core.*;
import processing.opengl.*;
import processing.xml.*;

import propinquity.hardware.*;

import java.util.*;

public class Propinquity extends PApplet implements PlayerConstants, LevelConstants {

	/** Unique serialization ID. */
	static final long serialVersionUID = 6340518174717159418L;
	public static final int FPS = 30;

	//General/Util		
	Logger logger;

	GameState gameState;

	Vector<UIElement> uiElements;

	Sounds sounds;
	Hud hud;

	//Xbee/Hardware
	HardwareSimulator simulator; //Testing 

	XBeeBaseStation xbeeBaseStation;
	XBeeManager xbeeManager;

	HardwareInterface hardware;

	//Player/Player List/PlayerSelect
	Player[] players;
	PlayerList playerList;
	PlayerSelect playerSelect;

	//Level/Level Select
	Level[] levels;
	LevelSelect levelSelect;

	//Box 2D
	public float worldSize = 2f;
	PBox2D box2d;
	Fences fences;

	public void setup() {
		size(1024, 768, PConstants.OPENGL);
		frameRate(FPS);
		imageMode(PConstants.CENTER);
		textureMode(PConstants.NORMAL);
		hint(PConstants.ENABLE_OPENGL_4X_SMOOTH);

		//General/Util
		logger = new Logger(this);

		sounds = new Sounds(this);
		hud = new Hud(this);

		//Xbee/Hardware
		simulator = new HardwareSimulator(this);
		
		xbeeBaseStation = new XBeeBaseStation();
		xbeeManager = new XBeeManager(this, xbeeBaseStation);

		hardware = simulator;

		//Player/Player List
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

		playerList = new PlayerList(this, "player.lst");
		playerSelect = new PlayerSelect(this, hud, players);

		//Level/Level Select
		Vector<Level> tmp_levels = new Vector<Level>();
		File file = new File(dataPath(LEVEL_FOLDER));

		if(file.isDirectory()) {
			String names[] = file.list();

			// if extension is specify, parse out the rest
			for(String name : names) {
				if (name.lastIndexOf(".xml") == name.length()-4) {
					try {
						tmp_levels.add(new Level(this, hud, LEVEL_FOLDER+name, players, sounds));
					} catch(XMLException e) {
						System.out.println("Level not built for file \""+name+"\" because of the following XMLException");
						System.out.println(e.getMessage());
					}
				}
			}
		}

		levels = tmp_levels.toArray(new Level[0]);
		if(levels.length == 0) System.out.println("Warning: No valid levels were built");

		levelSelect = new LevelSelect(this, hud, levels);

		//Box 2D
		box2d = new PBox2D(this, (float) height / worldSize);
		box2d.createWorld(-worldSize / 2f, -worldSize / 2f, worldSize, worldSize);
		box2d.setGravity(0.0f, 0.0f);

		fences = new Fences(this, box2d);
		
		//General + Util	
		uiElements = new Vector<UIElement>();
		uiElements.add(xbeeManager);
		uiElements.add(playerList);
		uiElements.add(playerSelect);
		uiElements.add(levelSelect);
		for(Level level : levels) uiElements.add(level);
		
		changeGameState(GameState.XBeeInit);
	}

	public void draw() {
		background(Color.black().toInt(this));

		hud.update(hud.getAngle() + HALF_PI, TWO_PI/10000f, TWO_PI/2000f);

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

			case PlayerSelect: {
				playerSelect.setPlayerNames(playerList.getPlayerNames());
				playerSelect.reset();
				playerSelect.show();
				break;
			}

			case LevelSelect: {
				levelSelect.reset();
				levelSelect.show();
				break;
			}

			case Play: {
				Level level = levelSelect.getSelectedLevel();
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

			case PlayerSelect: {
				playerSelect.keyPressed(key, keyCode);
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
		for(Level level : levels) level.close();
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.Propinquity" });
	}
}
