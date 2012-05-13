package propinquity;

import java.io.File;
import java.util.Vector;

import javax.media.opengl.GL;

import org.jbox2d.common.Vec2;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import xbee.*;
import proxml.*;

public class LevelSelect implements PConstants, UIElement {

	final String LEVEL_FOLDER = "levels/";

	Propinquity parent;

	Sounds sounds;

	Hud hud;
	Particle[] particles;

	String[] names;

	String[] playerNames;
	Player[] players;

	String[] levelFiles;
	Level[] levels;
	Level loadingLevel;
	String levelFile;

	int state, selected;

	boolean isVisible;

	public LevelSelect(Propinquity parent, Hud hud, Sounds sounds) {
		this.parent = parent;
		this.hud = hud;
		this.sounds = sounds;

		isVisible = true;

		loadLevels();
	}

	void loadLevels() {
		// get the list of level xml files
		levelFiles = listFileNames(parent.dataPath(LEVEL_FOLDER), "xml");

		// load each level to know the song name and duration
		Vector<Level> tmp_levels = new Vector<Level>();
		for (int i = 0; i < levelFiles.length; i++) {
			loadingLevel = new Level(parent, sounds);
			parent.xmlInOut = new XMLInOut(parent, this);
			parent.xmlInOut.loadElement(LEVEL_FOLDER + levelFiles[i]);
			while (true)
				if(loadingLevel.successfullyRead > -1)
					break;

			if(loadingLevel.successfullyRead == 0) {
				System.err.println("I had some trouble reading the level file:" + levelFiles[i]);
			}

			tmp_levels.add(loadingLevel);
			loadingLevel = null;
		}

		levels = tmp_levels.toArray(new Level[0]);
	}


	// This function returns all the files in a directory as an array of Strings
	private String[] listFileNames(String dir, String ext) {
		File file = new File(dir);
		if(file.isDirectory()) {
			String names[] = file.list();
			if(ext == null)
				return names;

			// if extension is specify, parse out the rest
			Vector<String> parsedNames = new Vector<String>();
			for (int i = 0; i < names.length; i++) {
				if(names[i].lastIndexOf("." + ext) == names[i].length() - 4)
					parsedNames.add(names[i]);
			}

			String[] namesWithExt = new String[parsedNames.size()];
			for (int i = 0; i < namesWithExt.length; i++)
				namesWithExt[i] = parsedNames.get(i);

			return namesWithExt;
		} else {
			// If it's not a directory
			return null;
		}
	}

	public void xmlEvent(XMLElement levelXML) {
		loadingLevel.loadSong(levelXML);
	}

	public void setPlayers(String[] playerNames, Player[] players) {
		this.playerNames = playerNames;
		this.players = players;
	}

	public void reset() {
		stateChange(0);
	}

	void stateChange(int state) {
		this.state = state;
		selected = 0;

		killParticles();

		if(state < players.length) {
			//TODO Turn on patches here
			createParticles(playerNames.length, players[state].getColor());
		} else if(state == players.length) {
			createParticles(levels.length, PlayerConstants.NEUTRAL_COLOR);
		} else if(state == players.length+1) {
			parent.changeGameState(GameState.Play);
		} else {
			System.out.println("Unknown Level Select State");
		}
	}

	void createParticles(int num, Color color) {
		int radius = parent.height / 2 - Hud.WIDTH * 2;

		particles = new Particle[num];

		for (int i = 0; i < num; i++) {
			Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), color);
			p.scale = 1f;
			particles[i] = p;
		}
	}

	void killParticles() {
		if(particles == null) return;
		for(Particle particle : particles) particle.kill();
	}


	public void show() {
		isVisible = true;
	}

	public void hide() {
		isVisible = false;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void draw() {
		if(!isVisible) return;

		// TODO: Fix this too
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();
		
		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);

		drawParticles();

		if(state < players.length) {
			hud.drawCenterText("Select Player "+(state+1), players[state].getName());
			hud.drawBannerCenter(playerNames[selected], players[state].getColor(), PApplet.TWO_PI/playerNames.length*selected);
		} else if(state == players.length) {
			hud.drawCenterText("Select Song");
			hud.drawBannerCenter(levels[selected].songName, PlayerConstants.NEUTRAL_COLOR, PApplet.TWO_PI/levels.length*selected);
		}

		parent.popMatrix();
	}

	private void drawParticles() {
		if(particles == null) return;

		for (int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
	}

	/**
	 * Receive a keyPressed event.
	 * 
	 * @param key the char of the keyPressed event.
	 * @param keycode the keycode of the keyPressed event.
	 */
	public void keyPressed(char key, int keycode) {
		switch(keycode) {
			case BACKSPACE: {
				reset();
				parent.changeGameState(GameState.PlayerList);
				break;
			}
			case LEFT: {
				left();
				break;
			}
			case RIGHT: {
				right();
				break;
			}
			case ENTER:
			case ' ': {
				select();
				break;
			}
		}
	}

	public void left() {
		if(state < players.length) {
			selected = (selected+playerNames.length-1)%playerNames.length;
		} else if(state == players.length) {
			selected = (selected+levels.length-1)%levels.length;
		}
	}

	public void right() {
		if(state < players.length) {
			selected = (selected+1)%playerNames.length;
		} else if(state == players.length) {
			selected = (selected+1)%levels.length;
		}
	}

	public void select() {
		if(state < players.length) {
			players[state].setName(playerNames[selected]);
			String[] remainingNames = new String[playerNames.length-1];
			int j = 0;
			for(int i = 0;i < playerNames.length;i++) {
				if(i != selected) {
					remainingNames[j] = playerNames[i];
					j++;
				}
			}
			
			playerNames = remainingNames; //TODO Ugly
		} else if(state == players.length) {
			levelFile = LEVEL_FOLDER + levelFiles[selected];
		}

		stateChange(state+1);
	}
}
