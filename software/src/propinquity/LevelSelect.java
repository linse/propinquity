package propinquity;

import java.io.File;
import java.util.ArrayList;

import javax.media.opengl.GL;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import proxml.*;
import xbee.*;

public class LevelSelect implements PConstants {

	final String LEVEL_FOLDER = "levels/";

	final String LEVEL_FONT = "hud/Calibri-Bold-24.vlw";
	final int LEVEL_FONT_SIZE = 24;
	final int PLAYERNAME_FONT_SIZE = 30;

	final String PROX_STUB_FILE = "stubs/sequence4/readings.txt";
	final String ACCEL_STUB_FILE = "stubs/sequence4/accelReadings.txt";
	final boolean[] PROX_STUB = { true, true }; // true = stub, false = live
	final boolean[] ACCEL_STUB = { true, true }; // true = stub, false = live
	final boolean[] SEND_VIBE = { true, true }; // true = vibe, false = don't
												// vibe

	// tmp until we use the xbee's serial
	final String[] XBEE_PROX_1_NI = { "P1_PROX1", "P2_PROX1" };
	final String[] XBEE_PROX_2_NI = { "P1_PROX2", "P2_PROX2" };
	final String[] XBEE_ACCEL_NI = { "P1_ACCEL", "P2_ACCEL" };
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

	LevelSelectParticle[] particles;
	int selected;

	PGraphics[] pgParticle;
	PImage[] imgPlayers;
	PImage[] imgSelectPlayer;
	PImage imgSelectSong;

	Player[] players = null;
	ArrayList<String> foundProxPatches;
	ArrayList<String> foundVibePatches;
	ArrayList<String> foundAccelPatches;
	ArrayList<String> foundUndefPatches;
	int numProxPatches;
	int numConfigAcks;

	XMLElement levelXML;
	String[] levelFiles;
	ArrayList<Level> levels;
	Level loadingLevel;
	PImage imgLevel;
	PGraphics pgLevel;
	String levelFile;

	public LevelSelect(Propinquity p, PlayerList playerList) {
		this.parent = p;
		this.radius = parent.height / 2 - Hud.WIDTH * 2;
		this.playerNames = playerList.getNames();

		this.font = p.loadFont(LEVEL_FONT);

		this.foundProxPatches = new ArrayList<String>();
		this.foundVibePatches = new ArrayList<String>();
		this.foundAccelPatches = new ArrayList<String>();
		this.foundUndefPatches = new ArrayList<String>();
		this.numConfigAcks = 0;

		loadLevels();
		initTextures();

		reset();
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
			loadingLevel = new Level(parent);
			parent.xmlInOut = new XMLInOut(parent, this);
			parent.xmlInOut.loadElement(LEVEL_FOLDER + levelFiles[i]);
			while (true)
				if (loadingLevel.successfullyRead() > -1)
					break;

			if (loadingLevel.successfullyRead() == 0) {
				System.err.println("I had some trouble reading the level file:" + levelFiles[i]);
			}

			levels.add(loadingLevel);
			loadingLevel = null;
		}
	}

	public void xmlEvent(XMLElement p_xmlElement) {
		levelXML = p_xmlElement;

		int l_numPlayers;

		// load number of players
		l_numPlayers = levelXML.countChildren() - 1;

		// check if we have an correct level file
		if (l_numPlayers < 0) {
			PApplet.println("Error: Empty level file");
			loadingLevel.successfullyRead = 0;
			return;
		}

		// read song
		loadingLevel.songName = levelXML.getChild(0).getAttribute("name");
		loadingLevel.songFile = levelXML.getChild(0).getAttribute("file");
		loadingLevel.songDuration = levelXML.getChild(0).getAttribute("duration");
		loadingLevel.tempo = levelXML.getChild(0).getIntAttribute("bpm");
		loadingLevel.multiplier = levelXML.getChild(0).getIntAttribute("multiplier");

		loadingLevel.successfullyRead = 1;
		return;
	}

	void initTextures() {
		PImage[] imgParticle = new PImage[2];
		for (int i = 0; i < imgParticle.length; i++)
			imgParticle[i] = parent.loadImage(parent.dataPath("particles/player" + (i + 1) + ".png"));

		pgParticle = new PGraphics[2];
		for (int i = 0; i < pgParticle.length; i++) {
			pgParticle[i] = parent.createGraphics(imgParticle[i].width, imgParticle[i].height, PApplet.P2D);
			pgParticle[i].background(imgParticle[i]);
			pgParticle[i].mask(imgParticle[i]);
		}

		imgPlayers = new PImage[2];
		for (int i = 0; i < imgPlayers.length; i++)
			imgPlayers[i] = parent.loadImage(parent.dataPath("hud/player" + (i + 1) + "name.png"));

		PImage imgLevelParticle = parent.loadImage(parent.dataPath("hud/levelParticle.png"));
		pgLevel = parent.createGraphics(imgLevelParticle.width, imgLevelParticle.height, PApplet.P2D);
		pgLevel.background(imgLevelParticle);
		pgLevel.mask(imgLevelParticle);

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
		PApplet.println("Initializing player " + player);

		// foundPatches = 0;
		foundProxPatches.clear();
		foundVibePatches.clear();
		foundAccelPatches.clear();
		foundUndefPatches.clear();
		players[player] = new Player(parent, parent.PLAYER_COLORS[player]);

		// init xbee comm or stubs
		// for proximity
		if (PROX_STUB[player])
			players[player].loadProxStub(player, PROX_STUB_FILE);
		else
			players[player].initProxComm(XBEE_PROX_1_NI[player], XBEE_PROX_2_NI[player]);
		// for accelerometer
		if (ACCEL_STUB[player])
			players[player].loadAccelStub(player, ACCEL_STUB_FILE);
		else
			players[player].initAccelComm(XBEE_ACCEL_NI[player]);
		// for vibration
		if (SEND_VIBE[player])
			players[player].initVibeComm(XBEE_VIBE_NI[player]);

		// ping for patches
		players[player].discoverPatches();

		particles = new LevelSelectParticle[playerNames.length];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new LevelSelectParticle(parent, new PVector(PApplet.cos(PApplet.TWO_PI / particles.length
					* i)
					* radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius, 0), 1, pgParticle[player]);
		}
	}

	void initLevels() {
		state = LevelSelectState.Song;

		particles = new LevelSelectParticle[levels.size()];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new LevelSelectParticle(parent, new PVector(PApplet.cos(PApplet.TWO_PI / particles.length
					* i)
					* radius, PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius, 0), 1, pgLevel);
		}

		selected = 0;
	}

	public void draw() {
		// TODO: Fix this too
		
		parent.drawInnerBoundary();
		parent.drawOuterBoundary();
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

		parent.pgl = (PGraphicsOpenGL) parent.g;
		parent.gl = parent.pgl.beginGL();
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		parent.pgl.endGL();

		for (int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
	}

	private void drawSelectPlayerHUD(int player) {
		parent.pgl = (PGraphicsOpenGL) parent.g;
		parent.gl = parent.pgl.beginGL();
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		parent.pgl.endGL();

		parent.fill(255);
		parent.pushMatrix();
		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);
		parent.image(imgSelectPlayer[player], 0, -65);
		parent.textFont(font, LEVEL_FONT_SIZE);
		parent.text(foundProxPatches.size() + " proximity patch" + (foundProxPatches.size() > 1 ? "es" : ""), 0, -20);
		parent.text(foundVibePatches.size() + " vibration patch" + (foundVibePatches.size() > 1 ? "es" : ""), 0, 15);
		parent.text(foundAccelPatches.size() + " acceleration patch" + (foundAccelPatches.size() > 1 ? "es" : ""), 0,
				50);
		if (foundUndefPatches.size() > 0)
			parent.text("found " + foundUndefPatches.size() + " undefined patch"
					+ (foundUndefPatches.size() > 1 ? "es" : ""), 0, 85);
		parent.popMatrix();
	}

	private void drawPlayerName(int player) {
		parent.pgl = (PGraphicsOpenGL) parent.g;
		parent.gl = parent.pgl.beginGL();
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		parent.pgl.endGL();

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
		parent.arctext(name, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset);
		parent.popMatrix();
	}

	private void drawSelectSong() {
		parent.pgl = (PGraphicsOpenGL) parent.g;
		parent.gl = parent.pgl.beginGL();
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		parent.pgl.endGL();

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
		parent.pgl = (PGraphicsOpenGL) parent.g;
		parent.gl = parent.pgl.beginGL();
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		parent.pgl.endGL();

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
		parent.arctext(name, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset);
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
				PApplet.println(" Found proximity patch: " + name + " (" + serial + ")");
				break;

			case 'V':
				foundVibePatches.add(serial);
				PApplet.println(" Found vibration patch: " + name + " (" + serial + ")");
				break;

			case 'A':
				foundAccelPatches.add(serial);
				PApplet.println(" Found acceleration patch: " + name + " (" + serial + ")");
				break;

			default:
				foundUndefPatches.add(serial);
				numProxPatches++; // change this later. should really be prox
									// patches, not undefined.
				PApplet.println(" Found undefined patch: " + name + " (" + serial + ")");
				break;
			}
		}

		else if (buffer.length == XPan.CONFIG_ACK_LENGTH && buffer[0] == XPan.CONFIG_ACK_PACKET_TYPE) {

			int myTurnLength = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
			numConfigAcks++;
			PApplet.println("Config Ack Received in Level Select, Turn Length is " + myTurnLength);
		}

		else if (buffer.length == XPan.VIBE_IN_PACKET_LENGTH && buffer[0] == XPan.VIBE_IN_PACKET_TYPE) {

			int p = buffer[1];
			int direction = buffer[2];
			if (p <= 8 && (state == LevelSelectState.P1 || state == LevelSelectState.Song)) {

				switch (direction) {

				case 1:
					moveLeft();
					break;

				case 2:
					moveRight();
					break;

				default:
					doSelect();
					break;
				}

			} else if (p > 8 && state == LevelSelectState.P2) {

				switch (direction) {

				case 1:
					moveLeft();
					break;

				case 2:
					moveRight();
					break;

				default:
					doSelect();
					break;
				}
			}
		}

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
