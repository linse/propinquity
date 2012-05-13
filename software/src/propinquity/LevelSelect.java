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

	String[] names;
	Player[] players;

	int state, selected;

	int radius;

	Particle[] particles;

	PFont font;
	PGraphics pgParticle;
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

	public LevelSelect(Propinquity parent, Sounds sounds) {
		this.parent = parent;
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

		PImage imgParticle = parent.graphics.loadParticle();
		pgParticle = new PGraphics();
		pgParticle = parent.createGraphics(imgParticle.width, imgParticle.height, PApplet.P2D);
		pgParticle.background(imgParticle);
		pgParticle.mask(imgParticle);

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
				Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), pgParticle, PlayerConstants.PLAYER_COLORS[state]);
				p.scale = 1f;
				particles[i] = p;
			}
		} else if(state == players.length) {
			particles = new Particle[levels.size()];

			for (int i = 0; i < particles.length; i++) {
				Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), pgParticle, Color.violet());
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
		parent.graphics.drawInnerBoundary();
		parent.graphics.drawOuterBoundary();
		
		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);

		drawParticles();

		if(state < players.length) {
			drawHUDText("Select Color", players[0].getName());
			drawPlayerName(0);
		} else if(state == players.length) {
			drawHUDText("Select Song");
			drawLevelName();
		}

		parent.popMatrix();
	}

	private void drawParticles() {
		if(particles == null)
			return;

		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);

		for (int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
	}

	void drawHUDText(String line1) {
		drawHUDText(line1, null);
	}

	void drawHUDText(String line1, String line2) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.fill(255);
		parent.pushMatrix();
		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);
		parent.textFont(font, HUD_FONT_SIZE);
		parent.text(line1, 0, 0);
		if(line2 != null) parent.text(line2, 0, -20);
		parent.popMatrix();
	}


	private void drawPlayerName(int player) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.noStroke();
		parent.noFill();

		float angle = PApplet.TWO_PI / players.length * selected;

		parent.pushMatrix();
		parent.translate(PApplet.cos(angle) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET), PApplet.sin(angle)
				* (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
		parent.rotate(angle + PApplet.PI / 2);
		parent.scale(imgPlayers[player].width / 2, imgPlayers[player].height / 2);
		parent.beginShape(PApplet.QUADS);
		parent.texture(imgPlayers[player]);
		parent.vertex(-1, -1, 0, 0, 0);
		parent.vertex(1, -1, 0, 1, 0);
		parent.vertex(1, 1, 0, 1, 1);
		parent.vertex(-1, 1, 0, 0, 1);
		parent.endShape(PApplet.CLOSE);
		parent.popMatrix();

		parent.pushMatrix();
		parent.fill(255);
		parent.noStroke();
		parent.textAlign(PApplet.CENTER, PApplet.BASELINE);
		parent.textFont(font, HUD_FONT_SIZE);
		String name = players[selected].getName().length() > 24 ? players[selected].getName().substring(0, 24)
				: players[selected].getName();
		float offset = (parent.textWidth(name) / 2) / (2 * PApplet.PI * (parent.height / 2 - Hud.SCORE_RADIUS_OFFSET))
				* PApplet.TWO_PI;
		Text.drawArc(name, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset, parent);
		parent.popMatrix();
	}

	private void drawLevelName() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.noStroke();
		parent.noFill();

		float angle = PApplet.TWO_PI / levels.size() * selected;

		parent.pushMatrix();
		parent.translate(PApplet.cos(angle) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET), PApplet.sin(angle)
				* (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
		parent.rotate(angle + PApplet.PI / 2);
		parent.scale(imgLevel.width / 2, imgLevel.height / 2);
		parent.beginShape(PApplet.QUADS);
		parent.texture(imgLevel);
		parent.vertex(-1, -1, 0, 0, 0);
		parent.vertex(1, -1, 0, 1, 0);
		parent.vertex(1, 1, 0, 1, 1);
		parent.vertex(-1, 1, 0, 0, 1);
		parent.endShape(PApplet.CLOSE);
		parent.popMatrix();

		parent.pushMatrix();
		parent.fill(255);
		parent.noStroke();
		parent.textAlign(PApplet.CENTER, PApplet.BASELINE);
		parent.textFont(font, HUD_FONT_SIZE);
		Level level = levels.get(selected);
		String name = level.songName + " (" + level.songDuration + ")";
		name = name.length() > 24 ? name.substring(0, 24) : name;
		float offset = (parent.textWidth(name) / 2) / (2 * PApplet.PI * (parent.height / 2 - Hud.SCORE_RADIUS_OFFSET))
				* PApplet.TWO_PI;
		Text.drawArc(name, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset, parent);
		parent.popMatrix();
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
