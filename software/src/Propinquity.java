import java.io.PrintWriter;
import java.util.*;

import javax.media.opengl.GL;

import org.jbox2d.collision.FilterData;
import org.jbox2d.collision.MassData;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.testbed.TestSettings;

import controlP5.ControlEvent;

import pbox2d.*;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import processing.video.MovieMaker;
import proxml.*;
import xbee.XBeeReader;
import ddf.minim.*;

public class Propinquity extends PApplet {

	// Unique serialization ID
	private static final long serialVersionUID = 6340518174717159418L;

	// debug constants
	final boolean DEBUG = false;
	final boolean DEBUG_XBEE = false;
	final boolean DRAW_SHADOWS = false;
	final boolean DRAW_PARTICLES = true;
	final int FULL_SCREEN_ID = 0;
	final public boolean MUTE = false;

	// liquid constants
	final float PARTICLE_SCALE = 0.8f;
	final float PARTICLE_SCALE_RANGE = 0.5f;
	final int TEXTURE_HALF = 32;
	final int AVG_PTS_PER_STEP = 250;
	final int AVG_PARTICLE_PER_STEP = 50;
	final int APPROX_MAX_PARTICLES = 1600;
	final int MAX_PARTICLES_PER_FRAME = 5;
	final Integer LIQUID_MAGIC = new Integer(12345);
	final int FENCE_SECTIONS = 24;
	final int INNER_FENCE_RADIUS = 100;
	final float EMITTER_RADIUS = 0.14f;
	final int SHADOW_X = 8;
	final int SHADOW_Y = 8;
	final float MIN_RELEASE_FORCE = 0.4f;
	final float MAX_RELEASE_FORCE = 0.6f;
	final float WORLD_SIZE = 2f;
	final float EMITTER_ANGULAR_VELOCITY = 4 * TWO_PI;
	final int INNER_FENCE_MASK = 0x4;
	final int OUTER_FENCE_MASK = 0x8;
	final int PLAYERS_MASK = 0x1 | 0x2;
	// final int NUM_STEP_PER_PERIOD = 4;
	final float PUSH_PERIOD_ROT_SPEED = 1f;
	final float PUSH_DAMPENING = 0.98f;

	// game constants
	final int HUD_WIDTH = 50;
	final int HUD_OFFSET = 4;
	final float HUD_SCORE_ROT_SPEED = 0.0001f;
	final float HUD_PROMPT_ROT_SPEED = 0.002f;
	final int HUD_FONT_SIZE = 32;
	final float HUD_SCORE_ANGLE_OFFSET = 0.35f;
	final int HUD_SCORE_RADIUS_OFFSET = 40;
	// final String LEVEL_FILE = "levels/sequence4.xml";
	final int END_LEVEL_TIME = 6;
	final int BOUNDARY_WIDTH = 5;
	final int[] PLAYER_COLORS = { color(55, 137, 254), color(255, 25, 0) };
	final int NEUTRAL_COLOR = color(142, 20, 252);

	// game states
	final int STATE_XBEE_INIT = 0;
	final int STATE_PLAYER_LIST = 1;
	final int STATE_LEVEL_SELECT = 2;
	final int STATE_PLAY = 3;
	final int STATE_HIGHSCORE = 4;
	int gameState = STATE_XBEE_INIT;

	// level select controller
	LevelSelect levelSelect = null;

	// player list controller
	PlayerList playerList = null;

	// Minim
	Minim minim;

	// OpenGL
	public PGraphicsOpenGL pgl;
	GL gl;

	// Box2D
	PBox2D box2d;
	TestSettings settings;

	// Liquid parameters
	ArrayList<Integer>[][] hash;
	int hashWidth, hashHeight;
	float totalMass = 100.0f;
	float particleRadius = 0.11f; // 0.11
	float particleViscosity = 0.005f;// 0.027f;
	float damp = 0.7f; // 0.095
	float fluidMinX = -WORLD_SIZE / 2f;
	float fluidMaxX = WORLD_SIZE / 2f;
	float fluidMinY = -WORLD_SIZE / 2f;
	float fluidMaxY = WORLD_SIZE / 2f;

	// Particle graphics
	PImage[] imgParticle;
	PImage imgShadow;
	PGraphics[] pgParticle;

	// Level parameters
	Level level;
	int ptsPerParticle = 0;
	LinkedList<Particle>[] particles;
	int lastPeriodStep = 0;
	Particle[] lastPeriodParticle;
	boolean endedLevel = false;
	long doneTime = -1;
	boolean groupedParticles = false;
	int numStepsPerPeriod;

	// XML
	XMLInOut xmlInOut;

	// HUD (Heads Up Display) -- shows the score.
	
	
	PImage[] hudImgPlayers;
	PImage hudCoopPlayer;
	PGraphics hudMask;
	float hudAngle = 0;// random(0, TWO_PI);
	float hudVelocity = -TWO_PI / 500f;
	boolean hudSnapped = false;
	AudioPlayer compSound;

	// Video output
	MovieMaker mm;
	boolean mmOutput = false;

	// logging
	String filename = "messages.txt";
	PrintWriter output;

	// XBees
	public XBeeManager xbeeManager;

	public void setup() {
		
		Graphics.setup(this);

		// init minim
		minim = new Minim(this);

		// initial opengl setup
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.beginGL();
		gl.glDisable(GL.GL_DEPTH_TEST);
		pgl.endGL();

		// Load artwork
		Graphics.loadContent();
		
		// init logging
		initLogging();
	}
	
	void initLogging() {
		output = createWriter(filename);
		output.println("Starting Logging of Propinquity Test.");
	}

	void initLevel(Player[] players, String levelFile) {
		level = new Level(this, players);
		xmlInOut = new XMLInOut(this, level);
		xmlInOut.loadElement(levelFile);
		while (true)
			if (level.successfullyRead() > -1)
				break;

		if (level.successfullyRead() == 0) {
			level.loadDefaults();
			System.err.println("I had some trouble reading the level file.");
			println("Defaulting to 2 minutes of free play instead.");
		}

		// send configuration message here
		// TODO (send step length to proximity patches)
	}

	void initHUD() {
		compSound = minim.loadFile("sounds/comp.mp3", 2048);
		compSound.setGain(5);

		hudImgPlayers = new PImage[level.getNumPlayers()];
		for (int i = 0; i < level.getNumPlayers(); i++)
			hudImgPlayers[i] = loadImage("data/hud/player" + (i + 1)
					+ "score.png");
		hudCoopPlayer = loadImage("data/hud/level.png");

		hudMask = createGraphics(width, height, P2D);
		hudMask.background(0);
		hudMask.beginDraw();
		hudMask.noStroke();
		hudMask.fill(255);
		hudMask.ellipse(width / 2, height / 2, height - HUD_WIDTH * 2
				+ BOUNDARY_WIDTH, height - HUD_WIDTH * 2 + BOUNDARY_WIDTH);
		hudMask.endDraw();
	}

	void initBox2D() {
		// initialize box2d physics and create the world
		box2d = new PBox2D(this, (float) height / WORLD_SIZE);
		box2d.createWorld(-WORLD_SIZE / 2f, -WORLD_SIZE / 2f, WORLD_SIZE,
				WORLD_SIZE);
		box2d.setGravity(0.0f, 0.0f);

		// load default jbox2d settings
		settings = new TestSettings();
	}

	void initTextures() {
		imgParticle = new PImage[level.getNumPlayers()];
		for (int i = 0; i < level.getNumPlayers(); i++)
			imgParticle[i] = loadImage("data/particles/player" + (i + 1)
					+ ".png");

		if (DRAW_SHADOWS)
			imgShadow = loadImage("data/particles/shadow.png");

		pgParticle = new PGraphics[level.getNumPlayers()];
		for (int i = 0; i < level.getNumPlayers(); i++) {
			pgParticle[i] = createGraphics(imgParticle[i].width,
					imgParticle[i].height, P2D);
			pgParticle[i].background(imgParticle[i]);
			pgParticle[i].mask(imgParticle[i]);
		}
	}

	void initFence() {
		Body innerFence = null;
		{
			BodyDef bd = new BodyDef();
			bd.position.set(0.0f, 0.0f);
			innerFence = box2d.createBody(bd);

			PolygonDef sd = new PolygonDef();
			// sd.filter.groupIndex = 1;
			sd.filter.categoryBits = INNER_FENCE_MASK;
			sd.filter.maskBits = PLAYERS_MASK;

			float fenceDepth = 0.2f;
			float worldScale = height / WORLD_SIZE;
			float radius = INNER_FENCE_RADIUS / worldScale + fenceDepth;
			float perimeter = 2 * PI * radius;

			for (int i = 0; i < FENCE_SECTIONS; i++) {
				float angle = 2 * PI / FENCE_SECTIONS * i;
				sd.setAsBox(perimeter / FENCE_SECTIONS, fenceDepth, new Vec2(
						cos(angle) * radius, sin(angle) * radius), angle + PI
						/ 2);
				innerFence.createShape(sd);
			}
		}

		Body outerFence = null;
		{
			BodyDef bd = new BodyDef();
			bd.position.set(0.0f, 0.0f);
			outerFence = box2d.createBody(bd);

			PolygonDef sd = new PolygonDef();
			// sd.filter.groupIndex = 1;
			sd.filter.categoryBits = OUTER_FENCE_MASK;
			sd.filter.maskBits = PLAYERS_MASK;

			float fenceDepth = 0.2f;
			float worldScale = height / WORLD_SIZE;
			float radius = (WORLD_SIZE - (HUD_WIDTH / worldScale)) / 2f
					+ fenceDepth / 2;
			float perimeter = 2 * PI * radius;

			for (int i = 0; i < FENCE_SECTIONS; i++) {
				float angle = 2 * PI / FENCE_SECTIONS * i;
				sd.setAsBox(perimeter / FENCE_SECTIONS, fenceDepth, new Vec2(
						cos(angle) * radius, sin(angle) * radius), angle + PI
						/ 2);
				outerFence.createShape(sd);
			}
		}
	}

	void initParticles() {
		// init box2d
		initBox2D();

		// load textures
		initTextures();

		// create the boundary fence
		initFence();

		// init hash to space sort particles
		hashWidth = 40;
		hashHeight = 40;
		hash = new ArrayList[hashHeight][hashWidth];
		for (int i = 0; i < hashHeight; ++i) {
			for (int j = 0; j < hashWidth; ++j) {
				hash[i][j] = new ArrayList<Integer>();
			}
		}

		// init particles
		// particles = new Particle[level.getNumPlayers()][MAX_PARTICLES /
		// level.getNumPlayers()];
		particles = new LinkedList[level.getNumPlayers()];
		for (int i = 0; i < particles.length; i++)
			particles[i] = new LinkedList<Particle>();

		ptsPerParticle = (level.getNumSteps() * AVG_PTS_PER_STEP * level
				.getNumPlayers()) / APPROX_MAX_PARTICLES;
		// pCount = new int[level.getNumPlayers()];
		numStepsPerPeriod = round(AVG_PARTICLE_PER_STEP * ptsPerParticle
				/ AVG_PTS_PER_STEP);
		if (numStepsPerPeriod == 0)
			++numStepsPerPeriod;
		lastPeriodParticle = new Particle[level.getNumPlayers()];

		println("Points per particle: " + ptsPerParticle);
	}

	public void draw() {
		// clear
		background(0);

		switch (gameState) {
		case STATE_XBEE_INIT:
			drawXBeeManager();
			break;
		case STATE_PLAYER_LIST:
			updatePlayerList();
			break;
		case STATE_LEVEL_SELECT:
			drawLevelSelect();
			break;
		case STATE_PLAY:
			drawPlay();
			break;
		case STATE_HIGHSCORE:
			drawHighscore();
			break;
		}

		// record frame to video
		if (mmOutput)
			mm.addFrame();
	}

	public void stop() {
		if (gameState == STATE_PLAY)
			level.clear();
	}

	void drawXBeeManager() {
		if (xbeeManager == null) {
			xbeeManager = new XBeeManager(this);
			xbeeManager.debug = DEBUG_XBEE;
			// if port list file exists
			// then init with the list
			// else autodetect
			xbeeManager.init();
		}

		// if (xbeeManager.isInitialized()) {
		// xbeeManager.save();
		// initPlayerListCtrl();
		// gameState = STATE_PLAYER_LIST;
		// }
		// else {
		String msg = xbeeManager.foundPortIds();
		if (msg.isEmpty()) {
			if (xbeeManager.isScanning())
				msg = "Scanning...";
			else
				msg = "No Xbee found.";
		} else if (!xbeeManager.isScanning())
			msg += ".";

		pushMatrix();
		translate(width / 2, height / 2);
		textFont(Graphics.font, HUD_FONT_SIZE);
		textAlign(CENTER, CENTER);
		fill(255);
		noStroke();
		text("Detecting XBee modules... ", 0, 0);
		translate(0, 30);
		textFont(Graphics.font, HUD_FONT_SIZE * 0.65f);
		text(msg, 0, 0);
		popMatrix();
	}

	void updatePlayerList() {
		if (playerList == null) {
			xbeeManager.dispose();
			playerList = new PlayerList(this);
		}
		playerList.update();
	}

	void drawLevelSelect() {
		// init level select UI
		if (levelSelect == null) {
			playerList.dispose();
			levelSelect = new LevelSelect(this, playerList);
		}

		drawInnerBoundary();
		drawOuterBoundary();
		pushMatrix();
		translate(width / 2, height / 2);
		levelSelect.draw();
		popMatrix();
	}

	void drawPlay() {
		drawInnerBoundary();
		if (DRAW_PARTICLES)
			drawParticles();
		drawMask();
		drawOuterBoundary();

		// drawOuterFence();
		if (DEBUG)
			drawDebugFence();

		drawHUD();

		if (level.isDone()) {

			if (endedLevel) {
				Player winner = level.getWinner();

				textAlign(CENTER);
				pushMatrix();
				translate(width / 2, height / 2);
				rotate(frameCount * HUD_PROMPT_ROT_SPEED);
				image(Graphics.hudImgLevelComplete, 0, -25);
				textFont(Graphics.font, HUD_FONT_SIZE);
				textAlign(CENTER, CENTER);
				fill(winner != null ? winner.getColor() : NEUTRAL_COLOR);
				noStroke();
				text(winner != null ? winner.getName() + " won!" : "You tied!",
						0, 0);
				image(Graphics.hudImgPlayAgain, 0, 30);
				popMatrix();
			} else {
				// keep track of done time
				if (doneTime == -1) {
					level.clear();
					doneTime = frameCount;
				}

				// give the last push
				pushPeriod(true);

				// step through time
				box2d.step();

				// liquify
				liquify();

				// snap score in final position
				snapHUD();

				// pull particles into groups
				groupParticles();

				// flag as ended
				if (doneTime != -1
						&& frameCount > doneTime + Graphics.FPS * END_LEVEL_TIME)
					endedLevel = true;
			}

		} else if (level.isRunning()) {
			// update hud
			updateHUD(hudAngle + HALF_PI, TWO_PI / 10000f, TWO_PI / 2000f);

			// push particles of current period out
			pushPeriod();

			// release balls
			updateParticles();

			// step through time
			box2d.step();

			// liquify
			liquify();

			// process level
			level.update();

			// read level data stub
			// if (USE_STUB) level.processStub();

		} else {
			pgl = (PGraphicsOpenGL) g;
			gl = pgl.beginGL();
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			pgl.endGL();

			fill(255);
			textAlign(CENTER);
			pushMatrix();
			translate(width / 2, height / 2);
			rotate(frameCount * HUD_PROMPT_ROT_SPEED);
			image(Graphics.hudImgPlay, 0, 0);
			popMatrix();
		}
	}

	void drawHighscore() {
	}

	void resetLevel() {
		resetLiquid();
		level.reset();
		endedLevel = false;
		doneTime = -1;
		groupedParticles = false;
		lastPeriodParticle = new Particle[level.getNumPlayers()];
		hudSnapped = false;
		hudVelocity = -TWO_PI / 500f;

		levelSelect.reset();
		gameState = STATE_LEVEL_SELECT;
		println("gamestate = " + gameState);
	}

	void drawInnerBoundary() {
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.beginGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		pgl.endGL();

		pushMatrix();
		translate(width / 2 - 1, height / 2);
		image(Graphics.hudInnerBoundary, 0, 0);
		popMatrix();
	}

	void drawOuterBoundary() {
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.beginGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		pgl.endGL();

		pushMatrix();
		translate(width / 2 - 1, height / 2);
		image(Graphics.hudOuterBoundary, 0, 0);
		popMatrix();
	}

	void drawDebugFence() {
		noFill();
		stroke(0, 255, 0);
		strokeWeight(1);

		rectMode(CENTER);
		float radius = height / 2 - HUD_WIDTH;
		float perimeter = 2 * PI * radius;
		float w = perimeter / FENCE_SECTIONS;
		float h = 5f;
		float angle = 0;
		for (int i = 0; i < FENCE_SECTIONS; i++) {
			angle = 2f * PI / FENCE_SECTIONS * i;
			pushMatrix();
			translate(width / 2 + cos(angle) * radius, height / 2 + sin(angle)
					* radius);
			rotate(angle + PI / 2);
			rect(0, 0, w, h);
			popMatrix();
		}

		radius = INNER_FENCE_RADIUS;
		perimeter = 2 * PI * radius;
		w = perimeter / FENCE_SECTIONS;
		h = 5f;
		angle = 0;
		for (int i = 0; i < FENCE_SECTIONS; i++) {
			angle = 2f * PI / FENCE_SECTIONS * i;
			pushMatrix();
			translate(width / 2 + cos(angle) * radius, height / 2 + sin(angle)
					* radius);
			rotate(angle + PI / 2);
			rect(0, 0, w, h);
			popMatrix();
		}
	}

	void drawParticles() {
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.beginGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		pgl.endGL();

		for (int i = 0; i < level.getNumPlayers(); i++)
			drawParticles(i);
	}

	void drawParticles(int p) {
		// draw balls
		noStroke();
		noFill();

		ListIterator<Particle> it = particles[p].listIterator();
		while (it.hasNext())
			((Particle) it.next()).draw();
	}

	void drawMask() {
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.beginGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_ZERO);
		pgl.endGL();

		pushMatrix();
		translate(width / 2, height / 2);
		scale(width / 2, height / 2);
		beginShape(QUADS);
		texture(hudMask);
		vertex(-1, -1, 0, 0, 0);
		vertex(1, -1, 0, 1, 0);
		vertex(1, 1, 0, 1, 1);
		vertex(-1, 1, 0, 0, 1);
		endShape(CLOSE);
		popMatrix();
	}

	void drawHUD() {
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.beginGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		pgl.endGL();

		noStroke();
		noFill();

		/*
		 * if ((level.isCoop() && !level.isCoopDone()) != lastCoopDone)
		 * playsound lastCoopDone = level.isCoop() && !level.isCoopDone();
		 */

		if (level.isCoop() && !level.isCoopDone()) {
			float angle = hudAngle - PI / 2;
			pushMatrix();
			translate(width / 2 + cos(angle)
					* (height / 2 - HUD_WIDTH + HUD_OFFSET), height / 2
					+ sin(angle) * (height / 2 - HUD_WIDTH + HUD_OFFSET));
			rotate(angle + PI / 2);
			scale(hudCoopPlayer.width / 2, hudCoopPlayer.height / 2);
			beginShape(QUADS);
			texture(hudCoopPlayer);
			vertex(-1, -1, 0, 0, 0);
			vertex(1, -1, 0, 1, 0);
			vertex(1, 1, 0, 1, 1);
			vertex(-1, 1, 0, 0, 1);
			endShape(CLOSE);
			popMatrix();
			pushMatrix();
			translate(width / 2, height / 2);
			fill(255);
			noStroke();
			textAlign(CENTER, BASELINE);
			textFont(Graphics.font, HUD_FONT_SIZE);
			String score = String.valueOf(level.getTotalPts() / 2);
			String name = "Coop";
			while (textWidth(score + name) < 240)
				name += ' ';

			arctext(name + score, height / 2 - HUD_SCORE_RADIUS_OFFSET, angle
					- HUD_SCORE_ANGLE_OFFSET);

			popMatrix();
		} else {
			if (!level.getLastCoopDone()) {
				compSound.play();
				compSound.rewind();
				level.setLastCoopDone(true);
			}
			for (int i = 0; i < level.getNumPlayers(); i++) {
				Player player = level.getPlayer(i);
				player.approachHudTo(-PI / 2 + TWO_PI / level.getNumPlayers()
						* i);
				float angle = hudAngle - PI / 2 + player.hudAngle;
				pushMatrix();
				translate(width / 2 + cos(angle)
						* (height / 2 - HUD_WIDTH + HUD_OFFSET), height / 2
						+ sin(angle) * (height / 2 - HUD_WIDTH + HUD_OFFSET));
				rotate(angle + PI / 2);
				scale(hudImgPlayers[i].width / 2, hudImgPlayers[i].height / 2);
				beginShape(QUADS);
				texture(hudImgPlayers[i]);
				vertex(-1, -1, 0, 0, 0);
				vertex(1, -1, 0, 1, 0);
				vertex(1, 1, 0, 1, 1);
				vertex(-1, 1, 0, 0, 1);
				endShape(CLOSE);
				popMatrix();
				pushMatrix();
				translate(width / 2, height / 2);
				fill(255);
				noStroke();
				textAlign(CENTER, BASELINE);
				textFont(Graphics.font, HUD_FONT_SIZE);
				String score = String.valueOf(player.getTotalPts());
				String name = player.getName().length() > 12 ? player.getName()
						.substring(0, 12) : player.getName();
				while (textWidth(score + name) < 240)
					name += ' ';

				arctext(name + score, height / 2 - HUD_SCORE_RADIUS_OFFSET,
						angle - HUD_SCORE_ANGLE_OFFSET);

				popMatrix();
			}
		}
	}

	void arctext(String message, float radius, float startAngle) {
		// We must keep track of our position along the curve
		float arclength = 0;

		// For every box
		for (int i = 0; i < message.length(); i++) {
			// Instead of a constant width, we check the width of each
			// character.
			char currentChar = message.charAt(i);
			float w = textWidth(currentChar);

			// Each box is centered so we move half the width
			arclength += w / 2;
			// Angle in radians is the arclength divided by the radius
			// Starting on the left side of the circle by adding PI
			float theta = startAngle + arclength / radius;

			pushMatrix();
			// Polar to cartesian coordinate conversion
			translate(radius * cos(theta), radius * sin(theta));
			// Rotate the box
			rotate(theta + PI / 2); // rotation is offset by 90 degrees
			// Display the character
			// fill(0);
			text(currentChar, 0, 0);
			popMatrix();
			// Move halfway again
			arclength += w / 2;
		}
	}

	void updateParticles() {
		for (int i = 0; i < level.getNumPlayers(); i++)
			updateParticles(i);
	}

	void updateParticles(int p) {
		Player player = level.getPlayer(p);
		int nParticles;

		// release particles if the player has accumulated period pts
		nParticles = min(player.getPeriodPts() / ptsPerParticle,
				MAX_PARTICLES_PER_FRAME);
		if (nParticles > 0) {
			// if (pCount[p]+nParticles > MAX_PARTICLES/2)
			// nParticles = MAX_PARTICLES/2-pCount[p];

			releaseParticles(p, nParticles);
		}

		// kill particles if the player touched
		nParticles = min(player.getKillPts() / ptsPerParticle,
				MAX_PARTICLES_PER_FRAME);
		if (nParticles > 0) {
			killParticles(p, nParticles);
		}
	}

	void releaseParticles(int p, int nParticles) {
		Player player = level.getPlayer(p);

		float releaseAngle = level.getTime() * HUD_SCORE_ROT_SPEED
				/ EMITTER_ANGULAR_VELOCITY;
		if (p % 2 == 1)
			releaseAngle += PI;

		float massPerParticle = totalMass / APPROX_MAX_PARTICLES;

		CircleDef pd = new CircleDef();
		pd.filter.categoryBits = p + 1;
		pd.filter.maskBits = INNER_FENCE_MASK | OUTER_FENCE_MASK | PLAYERS_MASK;
		pd.filter.groupIndex = -(p + 1);
		// pd.filter.groupIndex = -1;
		pd.density = 1.0f;
		// pd.radius = 0.020f;
		pd.radius = 0.040f;
		pd.restitution = 0.1f;
		pd.friction = 0.0f;
		// float radiusMult = random(0.5, 1);
		// float cx = cos(releaseAngle)*(EMITTER_RADIUS*radiusMult);
		// float cy = sin(releaseAngle)*(EMITTER_RADIUS*radiusMult);

		for (int i = 0; i < nParticles; ++i) {
			BodyDef bd = new BodyDef();
			bd.position = new Vec2(cos(releaseAngle)
					* (EMITTER_RADIUS * random(0.8f, 1)), sin(releaseAngle)
					* (EMITTER_RADIUS * random(0.8f, 1)));
			// bd.position = new Vec2(cx, cy);
			bd.fixedRotation = true;
			Body b = box2d.createBody(bd);
			Shape sh = b.createShape(pd);
			sh.setUserData(LIQUID_MAGIC);
			MassData md = new MassData();
			md.mass = massPerParticle;
			md.I = 1.0f;
			b.setMass(md);
			b.allowSleeping(false);

			// particles[p][i] = new Particle(b, sh,
			// PARTICLE_SCALE*random(1.0-PARTICLE_SCALE_RANGE, 1.0),
			// pgParticle[p]);
			particles[p]
					.add(new Particle(this, b, sh, PARTICLE_SCALE
							* random(1.0f - PARTICLE_SCALE_RANGE, 1.0f),
							pgParticle[p]));
		}

		// keep track of the released particles
		player.subPeriodPts(nParticles * ptsPerParticle);
		// pCount[p] += nParticles;
		// totalParticles += nParticles;
	}

	void killParticles(int p, int nParticles) {
		// get player
		Player player = level.getPlayer(p);

		// clear kill pts
		player.subKillPts(nParticles * ptsPerParticle);

		Particle particle;
		boolean killedLastPeriodParticle = false;
		while (nParticles > 0 && particles[p].size() > 0) {
			particle = (Particle) particles[p].removeFirst();
			if (particle == lastPeriodParticle[p])
				killedLastPeriodParticle = true;
			box2d.destroyBody(particle.body);
			nParticles--;
		}

		// adjust the last period particle push in case
		// we killed some particles that were within the
		// inner fence. if we don't do that the new particles
		// will get trapped in.
		if (killedLastPeriodParticle) {
			if (particles[p].isEmpty())
				lastPeriodParticle[p] = null;
			else
				lastPeriodParticle[p] = (Particle) particles[p].getLast();
		}
	}

	void liquify() {
		for (int i = 0; i < level.getNumPlayers(); i++)
			liquify(i);
	}

	void liquify(int p) {
		float dt = 1.0f / this.settings.hz;

		hashLocations(p);
		applyLiquidConstraint(p, dt);
		dampenLiquid(p);
	}

	void hashLocations(int p) {
		for (int a = 0; a < hashWidth; a++) {
			for (int b = 0; b < hashHeight; b++) {
				hash[a][b].clear();
			}
		}

		Particle particle;
		// for(int a = 0; a < particles[p].size(); a++)
		// {
		// particle = (Particle)particles[p].get(a);
		int i = 0;
		ListIterator<Particle> it = particles[p].listIterator();
		while (it.hasNext()) {
			particle = (Particle) it.next();
			int hcell = hashX(particle.body.m_sweep.c.x);
			int vcell = hashY(particle.body.m_sweep.c.y);
			if (hcell > -1 && hcell < hashWidth && vcell > -1
					&& vcell < hashHeight)
				hash[hcell][vcell].add(new Integer(i));
			i++;
		}
	}

	int hashX(float x) {
		float f = PApplet.map(x, fluidMinX, fluidMaxX, 0, hashWidth - .001f);
		return (int) f;
	}

	int hashY(float y) {
		float f = PApplet.map(y, fluidMinY, fluidMaxY, 0, hashHeight - .001f);
		return (int) f;
	}

	void applyLiquidConstraint(int p, float deltaT) {
		//
		// Unfortunately, this simulation method is not actually scale
		// invariant, and it breaks down for rad < ~3 or so. So we need
		// to scale everything to an ideal rad and then scale it back after.
		//
		final float idealRad = 50.0f;
		float multiplier = idealRad / particleRadius;

		int count = particles[p].size();
		float[] xchange = new float[count];
		float[] ychange = new float[count];
		Arrays.fill(xchange, 0.0f);
		Arrays.fill(ychange, 0.0f);

		float[] xs = new float[count];
		float[] ys = new float[count];
		float[] vxs = new float[count];
		float[] vys = new float[count];

		// for (int i=0; i<count; ++i) {
		// particle = particles[p][i];
		Particle particle;
		ListIterator<Particle> it;

		it = particles[p].listIterator();
		int i = 0;
		while (it.hasNext()) {
			particle = (Particle) it.next();
			xs[i] = multiplier * particle.body.m_sweep.c.x;
			ys[i] = multiplier * particle.body.m_sweep.c.y;
			vxs[i] = multiplier * particle.body.m_linearVelocity.x;
			vys[i] = multiplier * particle.body.m_linearVelocity.y;
			i++;
		}

		it = particles[p].listIterator();
		i = 0;
		while (it.hasNext()) {
			particle = (Particle) it.next();
			// Populate the neighbor list from the 9 proximate cells
			ArrayList<Integer> neighbors = new ArrayList<Integer>();
			int hcell = hashX(particle.body.m_sweep.c.x);
			int vcell = hashY(particle.body.m_sweep.c.y);
			for (int nx = -1; nx < 2; nx++) {
				for (int ny = -1; ny < 2; ny++) {
					int xc = hcell + nx;
					int yc = vcell + ny;
					if (xc > -1 && xc < hashWidth && yc > -1 && yc < hashHeight
							&& hash[xc][yc].size() > 0) {
						for (int a = 0; a < hash[xc][yc].size(); a++) {
							Integer ne = (Integer) hash[xc][yc].get(a);
							if (ne != null && ne.intValue() != i)
								neighbors.add(ne);
						}
					}
				}
			}

			// Particle pressure calculated by particle proximity
			// Pressures = 0 iff all particles within range are idealRad
			// distance away
			float[] vlen = new float[neighbors.size()];
			float pres = 0.0f;
			float pnear = 0.0f;
			for (int a = 0; a < neighbors.size(); a++) {
				Integer n = (Integer) neighbors.get(a);
				int j = n.intValue();
				float vx = xs[j] - xs[i];// liquid[j].m_sweep.c.x -
											// liquid[i].m_sweep.c.x;
				float vy = ys[j] - ys[i];// liquid[j].m_sweep.c.y -
											// liquid[i].m_sweep.c.y;

				// early exit check
				if (vx > -idealRad && vx < idealRad && vy > -idealRad
						&& vy < idealRad) {
					float vlensqr = (vx * vx + vy * vy);
					// within idealRad check
					if (vlensqr < idealRad * idealRad) {
						vlen[a] = (float) Math.sqrt(vlensqr);
						if (vlen[a] < Settings.EPSILON)
							vlen[a] = idealRad - .01f;
						float oneminusq = 1.0f - (vlen[a] / idealRad);
						pres = (pres + oneminusq * oneminusq);
						pnear = (pnear + oneminusq * oneminusq * oneminusq);
					} else {
						vlen[a] = Float.MAX_VALUE;
					}
				}
			}

			// Now actually apply the forces
			// System.out.println(p);
			float pressure = (pres - 5F) / 2.0F; // normal pressure term
			float presnear = pnear / 2.0F; // near particles term
			float changex = 0.0F;
			float changey = 0.0F;
			for (int a = 0; a < neighbors.size(); a++) {
				Integer n = (Integer) neighbors.get(a);
				int j = n.intValue();
				float vx = xs[j] - xs[i];// liquid[j].m_sweep.c.x -
											// liquid[i].m_sweep.c.x;
				float vy = ys[j] - ys[i];// liquid[j].m_sweep.c.y -
											// liquid[i].m_sweep.c.y;
				if (vx > -idealRad && vx < idealRad && vy > -idealRad
						&& vy < idealRad) {
					if (vlen[a] < idealRad) {
						float q = vlen[a] / idealRad;
						float oneminusq = 1.0f - q;
						float factor = oneminusq
								* (pressure + presnear * oneminusq)
								/ (2.0F * vlen[a]);
						float dx = vx * factor;
						float dy = vy * factor;
						float relvx = vxs[j] - vxs[i];
						float relvy = vys[j] - vys[i];
						factor = particleViscosity * oneminusq * deltaT;
						dx -= relvx * factor;
						dy -= relvy * factor;
						// liquid[j].m_xf.position.x += dx;//*deltaT*deltaT;
						// liquid[j].m_xf.position.y += dy;//*deltaT*deltaT;
						// liquid[j].m_linearVelocity.x +=
						// dx;///deltaT;//deltaT;
						// liquid[j].m_linearVelocity.y +=
						// dy;///deltaT;//deltaT;
						xchange[j] += dx;
						ychange[j] += dy;
						changex -= dx;
						changey -= dy;
					}
				}
			}
			// liquid[i].m_xf.position.x += changex;//*deltaT*deltaT;
			// liquid[i].m_xf.position.y += changey;//*deltaT*deltaT;
			// liquid[i].m_linearVelocity.x += changex;///deltaT;//deltaT;
			// liquid[i].m_linearVelocity.y += changey;///deltaT;//deltaT;
			xchange[i] += changex;
			ychange[i] += changey;
			i++;
		}
		// multiplier *= deltaT;
		it = particles[p].listIterator();
		i = 0;
		while (it.hasNext()) {
			particle = (Particle) it.next();
			particle.body.m_xf.position.x += xchange[i] / multiplier;
			particle.body.m_xf.position.y += ychange[i] / multiplier;
			particle.body.m_linearVelocity.x += xchange[i]
					/ (multiplier * deltaT);
			particle.body.m_linearVelocity.y += ychange[i]
					/ (multiplier * deltaT);
			i++;
		}
	}

	void dampenLiquid(int p) {
		Particle particle;
		ListIterator<Particle> it = particles[p].listIterator();
		while (it.hasNext()) {
			particle = (Particle) it.next();
			particle.body.setLinearVelocity(particle.body.getLinearVelocity()
					.mul(damp));
		}
	}

	void resetLiquid() {
		for (int p = 0; p < level.getNumPlayers(); p++) {
			// for (int i=0; i<particles[p].size(); ++i) {
			// box2d.destroyBody(((Particle)particles[p].removeFirst()).body);
			// }
			Particle particle;
			ListIterator<Particle> it = particles[p].listIterator();
			while (it.hasNext()) {
				particle = (Particle) it.next();
				box2d.destroyBody(particle.body);
				it.remove();
			}
		}
	}

	void pushPeriod() {
		pushPeriod(false);
	}

	void pushPeriod(boolean override) {
		int cStep = level.getCurrentStep();

		// go through particles
		// apply the force from the previous push
		for (int p = 0; p < level.getNumPlayers(); p++) {
			// for(int i = 0; i < lastPeriodParticle[p]; i++) {
			// if (particles[p][i] == null) continue;
			Particle particle = null;
			ListIterator<Particle> it = particles[p].listIterator();
			while (it.hasNext() && particle != lastPeriodParticle[p]) {
				particle = (Particle) it.next();

				particle.body.m_linearVelocity.x += particle.push.x;
				particle.body.m_linearVelocity.y += particle.push.y;
				particle.push.x *= PUSH_DAMPENING;
				particle.push.y *= PUSH_DAMPENING;
			}
			// println("last period step " + lastPeriodStep);
			// println("num steps per period: " + numStepsPerPeriod);

			if (!override
					&& (lastPeriodStep == cStep || cStep % numStepsPerPeriod != 0))
				continue;

			// go through particles
			// remove collision with inner fence
			// and apply push outward
			FilterData filter = new FilterData();
			filter.groupIndex = -(p + 1);
			filter.categoryBits = p + 1;
			filter.maskBits = OUTER_FENCE_MASK | PLAYERS_MASK;

			float angle = level.getTime() * PUSH_PERIOD_ROT_SPEED + TWO_PI
					/ level.getNumPlayers() * p;
			float force = random(MIN_RELEASE_FORCE, MAX_RELEASE_FORCE);

			// int i;
			// for(i = lastPeriodParticle[p]; i < pCount[p]; i++) {
			// if (particles[p][i] == null) continue;
			while (it.hasNext()) {
				particle = (Particle) it.next();

				particle.shape.setFilterData(filter);
				box2d.world.refilter(particle.shape);

				// particles[p][i].body.m_linearVelocity.x -= cos(angle)*force;
				// particles[p][i].body.m_linearVelocity.y -= sin(angle)*force;
				particle.push.x -= cos(angle) * force;
				particle.push.y -= sin(angle) * force;
			}

			lastPeriodParticle[p] = particle;
		}

		lastPeriodStep = cStep;
	}

	void updateHUD(float targetAngle, float targetAcceleration,
			float maxVelocity) {
		float diff = targetAngle - hudAngle;
		int dir = diff < 0 ? -1 : 1;

		if (diff * dir < TWO_PI / 5000f) {
			hudAngle = targetAngle;
			return;
		}

		hudVelocity += dir * targetAcceleration;
		if (hudVelocity / dir > maxVelocity)
			hudVelocity = dir * maxVelocity;

		hudVelocity *= 0.85;

		hudAngle += hudVelocity;
	}

	void snapHUD() {
		if (!hudSnapped) {
			hudAngle = hudAngle - ((int) (hudAngle / TWO_PI)) * TWO_PI;
			hudSnapped = true;
		}

		updateHUD(TWO_PI, TWO_PI / 500f, TWO_PI / 10f);
	}

	void groupParticles() {
		Particle particle;

		if (!groupedParticles) {
			for (int p = 0; p < level.getNumPlayers(); p++) {
				FilterData filter = new FilterData();
				filter.groupIndex = -1;
				filter.categoryBits = p + 1;
				filter.maskBits = OUTER_FENCE_MASK;

				ListIterator<Particle> it = particles[p].listIterator();
				while (it.hasNext()) {
					particle = (Particle) it.next();
					particle.shape.setFilterData(filter);
					box2d.world.refilter(particle.shape);
				}
			}

			groupedParticles = true;
		}

		for (int p = 0; p < level.getNumPlayers(); p++) {
			ListIterator<Particle> it = particles[p].listIterator();
			while (it.hasNext()) {
				particle = (Particle) it.next();

				Body b = particle.body;

				// if (p == 0 && pos.x < width/2)
				if (p == 0)
					b.m_linearVelocity.x += 0.20f;
				else if (p == 1)
					b.m_linearVelocity.x -= 0.20f;
			}
		}
	}

	public void printToOutput(String op) {
		output.println(op);
	}

	void xBeeEvent(XBeeReader xbee) {
		if (gameState == STATE_XBEE_INIT) {
			xbeeManager.xBeeEvent(xbee);
		} else if (gameState == STATE_PLAYER_LIST) {
			// println("xbee event: player list");
		} else if (gameState == STATE_LEVEL_SELECT) {
			println("xBeeEvent(): sending to level select");
			levelSelect.xBeeEvent(xbee);
		} else if (gameState == STATE_PLAY) {
			// println("sending to level");
			level.xBeeEvent(xbee);
		}
	}

	public void controlEvent(ControlEvent theEvent) {
		if (gameState == STATE_XBEE_INIT) {
			xbeeManager.controlEvent(theEvent);
			if (xbeeManager.isDone()) {
				// xbeeManager.dispose();
				// initPlayerListCtrl();
				gameState++;
				println("gamestate = " + gameState);
			}
		} else if (gameState == STATE_PLAYER_LIST) {
			playerList.controlEvent(theEvent);
			if (playerList.isDone()) {
				gameState++;
				println("gamestate = " + gameState);
			}
		}
	}

	public void keyPressed() {
		/*
		 * switch (key) { case 'ENTER': if (!mmOutput) {
		 * println("Start recording..."); mm = new MovieMaker(this, width,
		 * height, "Propinquity.mov", FPS, MovieMaker.ANIMATION,
		 * MovieMaker.LOSSLESS); mmOutput = true; } else {
		 * println("End recording."); mm.finish(); mmOutput = false; } break; }
		 */
		if (gameState == STATE_XBEE_INIT) {
			switch (key) {
			case ENTER:
				xbeeManager.save();
				// xbeeManager.dispose();
				// initPlayerListCtrl();
				gameState++;
				println("gamestate = " + gameState);
				break;
			}
		} else if (gameState == STATE_PLAYER_LIST) {
			switch (key) {
			case ENTER:
				playerList.process();
				if (playerList.isDone()) {
					gameState++;
					println("gamestate = " + gameState);
				}
				break;
			}
		} else if (gameState == STATE_LEVEL_SELECT) {
			switch (key) {
			case BACKSPACE:
				levelSelect.clear();
				levelSelect = null;
				playerList = null;
				// initPlayerListCtrl();
				gameState = STATE_PLAYER_LIST;
				println("gamestate = " + gameState);
				break;
			default:
				if (levelSelect != null) {
					// pass the key to the level select controller
					levelSelect.keyPressed(key, keyCode);

					// check if the level select controller is done
					// and ready to play
					if (levelSelect.isDone()) {
						// init level
						initLevel(levelSelect.players, levelSelect.levelFile);
						delay(50);
						while (!levelSelect.allAcksIn()) {
							println("sending again");
							levelSelect.sendConfigMessages(level
									.getStepInterval());
							delay(50);
						}
						// init hud
						initHUD();

						// init liquid particles
						initParticles();

						// play
						gameState = STATE_PLAY;
						println("gamestate = " + gameState);
					}
				}
				break;
			}
		} else if (gameState == STATE_PLAY) {
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
				println("Particles: "
						+ (particles[0].size() + particles[1].size()));
				println("Framerate: " + frameRate);
				println("Radius: " + particleRadius);
				println("Viscosity: " + particleViscosity);
				break;
			case '8':
				particleRadius += 0.01;
				break;
			case '2':
				particleRadius -= 0.01;
				if (particleRadius < 0)
					particleViscosity = 0;
				break;
			case '4':
				particleViscosity -= 0.001;
				if (particleViscosity < 0)
					particleViscosity = 0;
				break;
			case '6':
				particleViscosity += 0.001;
			case 'e': // play stub
				level.currentStep = level.numSteps;
				break;
			case 'f':// flush output and close
				output.flush();
				output.close();
				exit();
				break;
			}
		} else if (gameState == STATE_HIGHSCORE) {
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "--bgcolor=#FFFFFF", "Propinquity" });
	}
}
