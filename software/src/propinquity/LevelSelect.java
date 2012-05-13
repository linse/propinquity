package propinquity;

import java.io.File;
import java.util.Vector;

import javax.media.opengl.GL;

import org.jbox2d.common.Vec2;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import xbee.*;

public class LevelSelect implements PConstants, UIElement {

	final String LEVEL_FOLDER = "levels/";
	final String LEVEL_FONT = "hud/Calibri-Bold-24.vlw";

	final int HUD_FONT_SIZE = 24;

	Propinquity parent;

	Hud hud;

	String[] names;
	Player[] players;

	int state, selected;

	int radius;

	Particle[] particles;

	PFont font;
	PImage[] imgPlayers;
	PImage[] imgSelectPlayer;
	PImage imgSelectSong;

	String[] levelFiles;
	Vector<Level> levels;
	Level loadingLevel;
	PImage imgLevel;
	String levelFile;

	Sounds sounds;

	boolean isVisible;

	public LevelSelect(Propinquity parent, Hud hud, Sounds sounds) {
		this.parent = parent;
		this.hud = hud;
		this.sounds = sounds;

		players = new Player[0];

		isVisible = true;

		this.radius = parent.height / 2 - Hud.WIDTH * 2;

		this.font = parent.loadFont(LEVEL_FONT);

		levelFiles = listFileNames(parent.dataPath(LEVEL_FOLDER), "xml");

		// load each level to know the song name and duration
		levels = new Vector<Level>();
		
		for (int i = 0; i < levelFiles.length; i++) {
			levels.add(new Level(parent, sounds));
		}

		imgPlayers = new PImage[2];
		for (int i = 0; i < imgPlayers.length; i++)
			imgPlayers[i] = parent.loadImage(parent.dataPath("hud/player" + (i + 1) + "name.png"));

		imgLevel = parent.loadImage(parent.dataPath("hud/level.png"));
	}

	public void setPlayers(String[] names, Player[] players) {
		this.names = names;
		this.players = players;
	}

	public void reset() {
		stateChange(0);
	}

	void stateChange(int state) {
		this.state = state;
		selected = 0;

		if(state < players.length) {
			particles = new Particle[players.length];

			for (int i = 0; i < particles.length; i++) {
				Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), PlayerConstants.PLAYER_COLORS[state]);
				p.scale = 1f;
				particles[i] = p;
			}
		} else if(state == players.length) {
			particles = new Particle[levels.size()];

			for (int i = 0; i < particles.length; i++) {
				Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), Color.violet());
				p.scale = 1f;
				particles[i] = p;
			}
		} else {
			System.out.println("Unknown Level Select State");
		}
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
			hud.drawCenterText("Select Color", players[state].getName());
			hud.drawBannerCenter(players[state].getName(), players[state].getColor(), PApplet.TWO_PI/players.length*selected);
		} else if(state == players.length) {
			hud.drawCenterText("Select Song");
			hud.drawBannerCenter(levels.get(selected).songName, PlayerConstants.NEUTRAL_COLOR, PApplet.TWO_PI/levels.size()*selected);
		}

		parent.popMatrix();
	}

	private void drawParticles() {
		if(particles == null) return;

		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);

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
		selected = PApplet.constrain(selected-1, 0, 1);

		switch(state) {

		case 0:
		case 1:
			// if(selected < 0)
			// 	selected = particles.length - 1;
			// if(players[selected].getName() == players[0].name)
			// 	keyPressed(parent.key, parent.keyCode);
			break;

		case 2:
			if(selected < 0)
				selected = levels.size() - 1;
			break;
		}
	}

	public void right() {
		selected = PApplet.constrain(selected+1, 0, 1);

		switch(state) {

		case 0:
		case 1:
			// if(selected >= particles.length)
			// 	selected = 0;
			// if(players[selected].getName() == players[0].getName())
			// 	keyPressed(parent.key, parent.keyCode);
			break;

		case 2:
			if(selected >= levels.size())
				selected = 0;
			break;
		}
	}

	public void select() {
		switch(state) {

		case 0:
			players[0].name = players[selected].getName();
			stateChange(1);
			break;

		case 1:
			if(players[selected].getName() != players[0].name) {
				players[1].name = players[selected].getName();
				stateChange(players.length);
			}
			stateChange(2);
			break;

		case 2:
			levelFile = LEVEL_FOLDER + levelFiles[selected];
			state = 3;
			parent.changeGameState(GameState.Play);
			break;
		}
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
}
