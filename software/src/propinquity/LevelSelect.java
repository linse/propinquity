package propinquity;

import java.io.File;
import java.util.ArrayList;

import javax.media.opengl.GL;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import proxml.*;
import xbee.*;

public class LevelSelect implements PConstants, UIElement {

	final String LEVEL_FOLDER = "levels/";

	final String LEVEL_FONT = "hud/Calibri-Bold-24.vlw";
	final int LEVEL_FONT_SIZE = 24;
	final int PLAYERNAME_FONT_SIZE = 30;

	final String PROX_STUB_FILE = "stubs/sequence4/readings.txt";
	final boolean[] PROX_STUB = { true, true }; // true = stub, false = live
	final boolean[] SEND_VIBE = { true, true }; // true = vibe, false = don't
												// vibe

	// tmp until we use the xbee's serial
	final String[] XBEE_PROX_1_NI = { "P1_PROX1", "P2_PROX1" };
	final String[] XBEE_PROX_2_NI = { "P1_PROX2", "P2_PROX2" };
	final String[] XBEE_VIBE_NI = { "P1_VIBE", "P2_VIBE" };

	Propinquity parent;
	PFont font;

	// Level selection states
	enum LevelSelectState {
		P1, P2, Song, Done
	}

	LevelSelectState state;

	int radius;
	String[] playerNames;

	Particle[] particles;
	int selected;

	PGraphics pgParticle;
	PImage[] imgPlayers;
	PImage[] imgSelectPlayer;
	PImage imgSelectSong;

	Player[] players = null;
	ArrayList<String> foundProxPatches;
	ArrayList<String> foundVibePatches;
	ArrayList<String> foundUndefPatches;
	int numProxPatches;
	int numConfigAcks;

	String[] levelFiles;
	ArrayList<Level> levels;
	Level loadingLevel;
	PImage imgLevel;
	String levelFile;

	Sounds sounds;

	private boolean isVisible;

	public LevelSelect(Propinquity p, Sounds sounds) {

		isVisible = true;

		this.parent = p;
		this.sounds = sounds;
		this.radius = parent.height / 2 - Hud.WIDTH * 2;

		this.font = p.loadFont(LEVEL_FONT);

		this.foundProxPatches = new ArrayList<String>();
		this.foundVibePatches = new ArrayList<String>();
		this.foundUndefPatches = new ArrayList<String>();
		this.numConfigAcks = 0;

		loadLevels();
		initTextures();
	}

	public void registerPlayers(PlayerList playerList) {
		this.playerNames = playerList.getNames();
	}

	public void reset() {
		clear();
		this.players = new Player[2];
		initP1();
	}

	public void clear() {
		if (this.players != null) {
			for (int i = 0; i < this.players.length; i++) {
				if (this.players[i] != null) {
					this.players[i].clear();
					this.players[i] = null;
				}
			}
		}

		this.players = null;
	}

	void loadLevels() {
		// get the list of level xml files
		levelFiles = listFileNames(parent.dataPath(LEVEL_FOLDER), "xml");

		// load each level to know the song name and duration
		levels = new ArrayList<Level>();
		for (int i = 0; i < levelFiles.length; i++) {
			loadingLevel = new Level(parent, sounds);
			parent.xmlInOut = new XMLInOut(parent, this);
			parent.xmlInOut.loadElement(LEVEL_FOLDER + levelFiles[i]);
			while (true)
				if (loadingLevel.successfullyRead > -1)
					break;

			if (loadingLevel.successfullyRead == 0) {
				System.err.println("I had some trouble reading the level file:" + levelFiles[i]);
			}

			levels.add(loadingLevel);
			loadingLevel = null;
		}
	}

	public void xmlEvent(XMLElement levelXML) {
		loadingLevel.loadSong(levelXML);
	}

	void initTextures() {
		
		PImage imgParticle = parent.graphics.loadParticle();
		pgParticle = new PGraphics();
		pgParticle = parent.createGraphics(imgParticle.width, imgParticle.height, PApplet.P2D);
		pgParticle.background(imgParticle);
		pgParticle.mask(imgParticle);

		imgPlayers = new PImage[2];
		for (int i = 0; i < imgPlayers.length; i++)
			imgPlayers[i] = parent.loadImage(parent.dataPath("hud/player" + (i + 1) + "name.png"));

		imgLevel = parent.loadImage(parent.dataPath("hud/level.png"));

		imgSelectPlayer = new PImage[2];
		imgSelectPlayer[0] = parent.loadImage("hud/selplay1.png");
		imgSelectPlayer[1] = parent.loadImage("hud/selplay2.png");
		imgSelectSong = parent.loadImage("hud/selsong.png");
	}

	void initP1() {
		state = LevelSelectState.P1;
		initPlayer(0);

		selected = 0;
	}

	void initP2() {
		state = LevelSelectState.P2;
		initPlayer(1);

		selected++;
		if (selected >= particles.length)
			selected = 0;
	}

	void initPlayer(int player) {
		System.out.println("Initializing player " + player);

		// foundPatches = 0;
		foundProxPatches.clear();
		foundVibePatches.clear();
		foundUndefPatches.clear();
		players[player] = new Player(parent, parent.playerColours[player]);

		// init xbee comm or stubs
		// for proximity
		if (PROX_STUB[player])
			players[player].loadProxStub(player, PROX_STUB_FILE);
		else
			players[player].initProxComm(XBEE_PROX_1_NI[player], XBEE_PROX_2_NI[player]);
		// for vibration
		if (SEND_VIBE[player])
			players[player].initVibeComm(XBEE_VIBE_NI[player]);

		// ping for patches
		players[player].discoverPatches();

		particles = new Particle[playerNames.length];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle(parent, new PVector(
					PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI
							/ particles.length * i)
							* radius, 0), 1, pgParticle, parent.playerColours[player]);
		}
	}

	void initLevels() {
		state = LevelSelectState.Song;

		particles = new Particle[levels.size()];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle(parent, new PVector(
					PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius, PApplet.sin(PApplet.TWO_PI
							/ particles.length * i)
							* radius, 0), 1, pgParticle, Colour.violet());
		}

		selected = 0;
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

		if (!isVisible)
			return;

		// TODO: Fix this too
		parent.graphics.drawInnerBoundary();
		parent.graphics.drawOuterBoundary();
		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);

		drawParticles();

		switch (state) {
		case P1:
			drawPlayerName(0);
			drawSelectPlayerHUD(0);
			break;

		case P2:
			drawPlayerName(1);
			drawSelectPlayerHUD(1);
			break;

		case Song:
			drawLevelName();
			drawSelectSong();
			break;
		}

		parent.popMatrix();
	}

	private void drawParticles() {
		if (particles == null)
			return;

		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);

		for (int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
	}

	private void drawSelectPlayerHUD(int player) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.fill(255);
		parent.pushMatrix();
		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);
		parent.image(imgSelectPlayer[player], 0, -65);
		parent.textFont(font, LEVEL_FONT_SIZE);
		parent.text(foundProxPatches.size() + " proximity patch" + (foundProxPatches.size() > 1 ? "es" : ""), 0, -20);
		parent.text(foundVibePatches.size() + " vibration patch" + (foundVibePatches.size() > 1 ? "es" : ""), 0, 15);
		if (foundUndefPatches.size() > 0)
			parent.text("found " + foundUndefPatches.size() + " undefined patch"
					+ (foundUndefPatches.size() > 1 ? "es" : ""), 0, 85);
		parent.popMatrix();
	}

	private void drawPlayerName(int player) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.noStroke();
		parent.noFill();

		float angle = PApplet.TWO_PI / playerNames.length * selected;

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
		parent.textFont(font, LEVEL_FONT_SIZE);
		String name = playerNames[selected].length() > 24 ? playerNames[selected].substring(0, 24)
				: playerNames[selected];
		float offset = (parent.textWidth(name) / 2) / (2 * PApplet.PI * (parent.height / 2 - Hud.SCORE_RADIUS_OFFSET))
				* PApplet.TWO_PI;
		Text.drawArc(name, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset, parent);
		parent.popMatrix();
	}

	private void drawSelectSong() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.fill(255);
		parent.pushMatrix();
		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);
		parent.image(imgSelectSong, 0, 20);
		String message = players[0].name + ",";
		parent.textFont(font, PLAYERNAME_FONT_SIZE);
		parent.text(message, 0, -20);
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
		parent.textFont(font, LEVEL_FONT_SIZE);
		Level level = levels.get(selected);
		String name = level.songName + " (" + level.songDuration + ")";
		name = name.length() > 24 ? name.substring(0, 24) : name;
		float offset = (parent.textWidth(name) / 2) / (2 * PApplet.PI * (parent.height / 2 - Hud.SCORE_RADIUS_OFFSET))
				* PApplet.TWO_PI;
		Text.drawArc(name, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset, parent);
		parent.popMatrix();
	}

	/* This is what to refer to for cap sense events from patch buttons */
	public void keyPressed(char key, int keyCode) {
		switch (keyCode) {
		case LEFT:
			moveLeft();
			break;
		case RIGHT:
			moveRight();
			break;
		case ENTER:
		case ' ':
			doSelect();
			break;
		}
	}

	public void moveLeft() {

		selected--;

		switch (state) {

		case P1:
		case P2:
			if (selected < 0)
				selected = particles.length - 1;
			if (playerNames[selected] == players[0].name)
				keyPressed(parent.key, parent.keyCode);
			break;

		case Song:
			if (selected < 0)
				selected = levels.size() - 1;
			break;
		}

	}

	public void moveRight() {

		selected++;

		switch (state) {

		case P1:
		case P2:
			if (selected >= particles.length)
				selected = 0;
			if (playerNames[selected] == players[0].name)
				keyPressed(parent.key, parent.keyCode);
			break;

		case Song:
			if (selected >= levels.size())
				selected = 0;
			break;
		}
	}

	public void doSelect() {

		switch (state) {

		case P1:
			players[0].name = playerNames[selected];
			initP2();
			break;

		case P2:
			if (playerNames[selected] != players[0].name) {
				players[1].name = playerNames[selected];
				initLevels();
			}
			break;

		case Song:
			levelFile = LEVEL_FOLDER + levelFiles[selected];
			state = LevelSelectState.Done;
			break;
		}
	}

	public boolean isDone() {
		return state == LevelSelectState.Done;
	}

	// This function returns all the files in a directory as an array of Strings
	private String[] listFileNames(String dir, String ext) {
		File file = new File(dir);
		if (file.isDirectory()) {
			String names[] = file.list();
			if (ext == null)
				return names;

			// if extension is specify, parse out the rest
			ArrayList<String> parsedNames = new ArrayList<String>();
			for (int i = 0; i < names.length; i++) {
				if (names[i].lastIndexOf("." + ext) == names[i].length() - 4)
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

	public void xBeeEvent(XBeeReader xbee) {
		XBeeDataFrame data = xbee.getXBeeReading();
		data.parseXBeeRX16Frame();

		int[] buffer = data.getBytes();

		if (buffer.length > 11) {
			// check first letter of NI parameter
			String serial = "";
			for (int i = 3; i < 11; i++)
				serial += PApplet.hex(buffer[i], 2);
			String name = "";
			for (int i = 11; i < buffer.length; i++)
				name += PApplet.parseChar(buffer[i]);

			switch (buffer[11]) {

			case 'P':
				foundProxPatches.add(serial);
				System.out.println(" Found proximity patch: " + name + " (" + serial + ")");
				break;

			case 'V':
				foundVibePatches.add(serial);
				System.out.println(" Found vibration patch: " + name + " (" + serial + ")");
				break;

			default:
				foundUndefPatches.add(serial);
				numProxPatches++; // change this later. should really be prox
									// patches, not undefined.
				System.out.println(" Found undefined patch: " + name + " (" + serial + ")");
				break;
			}
		}

		// else if (buffer.length == XPan.CONFIG_ACK_LENGTH && buffer[0] ==
		// XPan.CONFIG_ACK_PACKET_TYPE) {

		// int myTurnLength = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
		// numConfigAcks++;
		// System.out.println("Config Ack Received in Level Select, Turn Length is "
		// + myTurnLength);
		// }

		// else if (buffer.length == XPan.VIBE_IN_PACKET_LENGTH && buffer[0] ==
		// XPan.VIBE_IN_PACKET_TYPE) {

		// int p = buffer[1];
		// int direction = buffer[2];
		// if (p <= 8 && (state == LevelSelectState.P1 || state ==
		// LevelSelectState.Song)) {

		// switch (direction) {

		// case 1:
		// moveLeft();
		// break;

		// case 2:
		// moveRight();
		// break;

		// default:
		// doSelect();
		// break;
		// }

		// } else if (p > 8 && state == LevelSelectState.P2) {

		// switch (direction) {

		// case 1:
		// moveLeft();
		// break;

		// case 2:
		// moveRight();
		// break;

		// default:
		// doSelect();
		// break;
		// }
		// }
		// }

	}

	public boolean allAcksIn() {
		return (numConfigAcks >= numProxPatches);
	}

	public void sendConfigMessages(int turnLength) {
		numConfigAcks = 0;
		players[0].sendConfig(turnLength);
		players[1].sendConfig(turnLength);
	}

}
