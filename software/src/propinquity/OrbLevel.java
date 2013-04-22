package propinquity;

import processing.core.*;
import processing.xml.*;
import propinquity.hardware.*;
import ddf.minim.*;
import java.util.*;
import java.lang.Math;

/**
 * The ProxLevel is the "original" game mechanic for Propinquity, players score by being in proximity to the opponent's patches. The opponent's patches may not be on at all times. There are also cooperative and versus rounds and there are pauses between rounds. It supports loading a level from an XML file and can have multiple songs per level.
 *
 * It is currently not in use, but support two scoring zones.
 *
 */
public class OrbLevel extends Level {

	static int ORB_WARNING = 300;
	static int ORB_THRESHOLD = 600;

	static long TIMEOUT = 90000;
	// static long TIMEOUT = 10000;
	static long PROTECTION_TIME = 3000;
	
	boolean orbIsProtected;
	Color orbcolor;
	int orblives;
	long offTime;
	
	long[] lastScoreTime;
	long[] lastScoreTimePauseDiff;

	AudioPlayer[] orbprotected_songs;
	AudioPlayer[] orbcrazy_songs;
	AudioSample crash;
	AudioSample orbwon;
	AudioSample monsterswon;

	int songTransitionCount;
	boolean newSongFlag;
	Vector<AudioPlayer> songs;

	String songFile;
	int songBPM;

	VolumeFader fader;

	String name;

	boolean coop, lastCoop;
	boolean running;
	
	int coopScore;

	long startTime, startTimeDiff;
	long invulnerableUntil;

	boolean useBackgroundColor;
	private Patch orb;
	private Patch glove;
	private boolean timeout;
	private long timeOfOffEvent;
	private long glitchtime;

	public OrbLevel(Propinquity parent, Hud hud, Sounds sounds, String levelfile, Player[] players) {
		super(parent, hud, sounds, players);

		orbcolor = Color.green();
		name = levelfile;
		
		crash = sounds.getCrash();
		orbwon = sounds.getOrbWon();
		monsterswon = sounds.getMonstersWon();
		
		orbprotected_songs = new AudioPlayer[7];
		orbprotected_songs[0] = sounds.loadSong("bg1.mp3");
		orbprotected_songs[1] = sounds.loadSong("bg2.mp3");
		orbprotected_songs[2] = sounds.loadSong("bg3.mp3");
		orbprotected_songs[3] = sounds.loadSong("bg4.mp3");
		orbprotected_songs[4] = sounds.loadSong("bg5.mp3");
		orbprotected_songs[5] = sounds.loadSong("bg6.mp3");
		orbprotected_songs[6] = sounds.loadSong("bg7.mp3");

		orbcrazy_songs = new AudioPlayer[3];
		orbcrazy_songs[0] = sounds.loadSong("noise1.mp3");
		orbcrazy_songs[1] = sounds.loadSong("noise2.mp3");
		orbcrazy_songs[2] = sounds.loadSong("noise3.mp3");
		
		lastScoreTime = new long[players.length];
		lastScoreTimePauseDiff = new long[players.length];
		coopScore = 0;

		startTime = -1;

		useBackgroundColor = true;

		fader = new VolumeFader();

		reset();
	}

	public void pauseOrbProtectedSongs() {
		for (AudioPlayer f : orbprotected_songs) {
			f.pause();
		}
	}

	public void pauseOrbCrazySongs() {
		for (AudioPlayer f : orbcrazy_songs) {
			f.pause();
		}
	}

	public void playOrbProtectedSongs() {
		for (AudioPlayer f : orbprotected_songs) {
			f.play();
			f.loop();
		}
	}

	public void playOrbCrazySongs() {
		for (AudioPlayer f : orbcrazy_songs) {
			f.play();
			f.loop();
		}
	}

	public void useBackgroundColor(boolean useBackgroundColor) {
		this.useBackgroundColor = useBackgroundColor;
	}

	public void pause() {
		// song.pause();
		running = false;
		for(int i = 0;i < players.length;i++) {
			players[i].pause();
			lastScoreTimePauseDiff[i] = parent.millis()-lastScoreTime[i];
		}
		startTimeDiff = parent.millis()-startTime;
	}

	public void start() {

		orbwon.stop();
		monsterswon.stop();
			
		for(int i = 0;i < players.length;i++) {
			players[i].start();
			lastScoreTime[i] = parent.millis()-lastScoreTimePauseDiff[i];
		}
		running = true;
		if(startTime == -1) startTime = parent.millis();
		else startTime = parent.millis()-startTimeDiff;
		offTime = 0;
		playOrbCrazySongs();

		// Orb defender
		this.orb = players[0].patches[0];
//		for(Patch orb : this.players[0].getPatches()) {
			orb.setActivationMode(Mode.PROX | Mode.ACCEL_INT0 | Mode.ACCEL_INT1);
			orb.setActive(true);
			orb.setAccelConfig(0); // Sensitivity
//		}
		orbSetColor(Color.black());
		
		this.glove = players[0].getGlove();
		this.glove.setActivationMode(Mode.OFF);
		this.glove.setColor(Color.black());
		this.glove.setActive(true);

		this.orbIsProtected = false;
		timeout = false;
		this.timeOfOffEvent = this.startTime;

		// Monsters (treat as one single player with multiple patches)
		players[1].configurePatches(Mode.OFF);
		for(Patch p : this.players[1].getPatches()) {
			p.setColor(Color.white());
		}
		players[1].activatePatches();

		this.invulnerableUntil = parent.millis() + PROTECTION_TIME;
		
		System.out.println("start");
	}

	public void reset() {
		System.out.println("reset");
		running = false;
		orblives = 3;
		timeout = false;
		startTime = -1;
		orbcolor = Color.green();

		for(Player player : players) {
			player.configurePatches(Mode.PROX | Mode.ACCEL_XYZ | Mode.ACCEL_INT0 | Mode.ACCEL_INT1);
			player.reset(); //Clears all the particles, scores, patches and gloves
		}

		lastScoreTime = new long[players.length];
		lastScoreTimePauseDiff = new long[players.length];
		coopScore = 0;

		parent.setBackgroundColor(Color.black());
		
		for (AudioPlayer f : orbprotected_songs) {
			f.pause();
			f.rewind();
			f.setGain(0);
		}
		for (AudioPlayer f : orbcrazy_songs) {
			f.pause();
			f.rewind();
			f.setGain(0);
		}
		crash.setGain(10);
		orbwon.setGain(10);
		orbwon.stop();
		monsterswon.setGain(10);
		monsterswon.stop();
	}

	public void close() {
		for (AudioPlayer f : orbprotected_songs) {
			f.close();
		}
		
		for (AudioPlayer f : orbcrazy_songs) {
			f.close();
		}
	}
	

	public void proxEvent(Patch patch) {
		if(!isRunning() || isDone()) return;
		if(!patch.getActive()) return;
		// Handle patch feedback
		// patch.setMode(patch.getZone());

		if(running && parent.millis() < this.invulnerableUntil) return;

		
		// int bestproxval = 0;
		// for (Patch p : this.players[0].patches) {
			// if (p.getProx() > bestproxval) bestproxval = p.getProx();
		// }
		
		// We will never get prox events from player 1, so we know this is an orb event
		// Three zones: Protected (value > THRESHOLD), Warning (THRESHOLD < value < WARNING, Off (WARNING > value)
		// int proxvalue = bestproxval;
		int proxvalue = patch.getProx();
		if(proxvalue > ORB_WARNING) { // Protected
			orbEnableAcceleration(true);
			this.players[1].clearPatches();
			if (!orbIsProtected) {
				offTime += (parent.millis() - timeOfOffEvent);
				this.invulnerableUntil = parent.millis() + PROTECTION_TIME;
				playOrbProtectedSongs();
				pauseOrbCrazySongs();
			}
			orbIsProtected = true;
			if(proxvalue < ORB_THRESHOLD) {
				orbSetColor(new Color(orbcolor.r/2, orbcolor.g/2, orbcolor.b/2));
				orbSetFlash(true);
				this.glove.setVibeLevel(100);
			} else {
				orbSetColor(this.orbcolor);
				orbSetFlash(false);
				this.glove.setVibeLevel(100);
			}
		} else { // Disable orb, enable attackers' patches
			if (orbIsProtected && parent.millis() > glitchtime) {
				glitchtime = -1;
				System.out.println("Penalty!");
				pauseOrbProtectedSongs();
				playOrbCrazySongs();
				// dingding.trigger();
				// decreaseOrbLife();
				this.players[1].activatePatches();
				orbSetColor(Color.black());
				orbEnableAcceleration(false);
				this.glove.setVibeLevel(0);
				timeOfOffEvent = parent.millis();
				orbIsProtected = false;
			} else if (glitchtime == -1) {
				glitchtime = parent.millis() + 500;
			}
		}
	}

	public void orbSetColor(Color col) {
		this.orb.setColor(col);
		// for(Patch p : this.players[0].getPatches()) p.setColor(col);
	}
	
	public void orbSetFlash(boolean flashing) {
		// for(Patch orb : this.players[0].getPatches()) {
			if(flashing) {
				orb.setColorPeriod(5);
				orb.setColorDuty(127);
			} else {
				orb.setColorDuty(255);
			}
		// }
	}
	
	public void orbEnableAcceleration(boolean enabled) {
		// for(Patch orb : this.players[0].getPatches()) {
			if (enabled) orb.setActivationMode(orb.getActivationMode() | Mode.ACCEL_INT0 | Mode.ACCEL_INT1);
			else orb.setActivationMode(orb.getActivationMode() & ~(Mode.ACCEL_INT0|Mode.ACCEL_INT1));
		// }
	}
	
	public void accelXYZEvent(Patch patch) {
	
	}

	public void decreaseOrbLife() {
		this.invulnerableUntil = parent.millis() + 2000;
		
		if (orblives > 0) {
			orblives--;
			switch (orblives) {
			case 2:
				this.orbcolor = Color.yellow();
				break;
			case 1:
				this.orbcolor = Color.red();
				break;
			case 0:
				pauseOrbProtectedSongs();
				pauseOrbCrazySongs();
				monsterswon.trigger();

				this.orbcolor = Color.black();
				running = false;

				for(Patch p : this.players[1].getPatches()) {
					p.setColor(Color.white());
					p.setColorDuty(127);
					p.setColorPeriod(HardwareConstants.SLOW_BLINK);
					p.setActive(true);
				}
				System.out.println("End game");
				break;
			}
			orbSetColor(this.orbcolor);
		}
	}
	
	// This triggers if "some" movement was detected
	public void accelInterrupt0Event(Patch patch) {
		// dingding.trigger();
		System.out.println("Close one...");
	}

	// This triggers if an impact-like movement was detected
	public void accelInterrupt1Event(Patch patch) {
		if(running && parent.millis() > this.invulnerableUntil) {
			System.out.println("Hit!");
			decreaseOrbLife();
			crash.trigger();	
		}
	}

	public void update() {
		for(Player player : players) player.update();

		long runningtime = parent.millis() - startTime - offTime;
		System.out.println("Time: " + runningtime/1000);
		if (orbIsProtected) {
			if(runningtime > TIMEOUT) {
				pauseOrbProtectedSongs();
				orbwon.trigger();
				timeout = true;
				running = false;
				this.orbcolor = Color.white();
				orbSetColor(this.orbcolor);
				// for(Patch orb : this.players[0].getPatches()) {
					orb.setColorDuty(127);
					orb.setColorPeriod(HardwareConstants.SLOW_BLINK);
					this.glove.setVibeLevel(0);
				// }
			}
		}	
		
	}

	public String getName() {
		return name;
	}

	public Player getWinner() {
		Player winner;
		if (orblives > 0) {
			winner = players[0];
		}
		else {
			winner = players[1];
		}
		return winner;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isDone() {
		return (orblives == 0) || timeout;
	}
	
	public void keyPressed(char key, int keyCode) {
		if(!isVisible) return;

		switch(key) {
			case BACKSPACE: {
				reset();
				// if(orbprotected_song.position() == 0 || isDone()) 
				parent.changeGameState(GameState.LevelSelect);
				break;
			}

			case ENTER:
			case ' ': {
				if(isDone()) {
					reset(); //Make sure particles are gone
					parent.changeGameState(GameState.LevelSelect);
				} else {
					if(isRunning()) pause();
					else start();
				}
				break;
			}

			case 'e': { //Force End 
				// song.cue(song.length()-1000);
				startTime = parent.millis()-179000;
				break;
			}
		}
	}

	public void draw() {
		if(!isVisible) return;

		//Particles and Liquid
		if(!PARTICLES_ABOVE) for(int i = 0; i < players.length; i++) players[i].draw();

		//Outlines
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();

		//Score Banners
		if(coop) {
			String score = String.valueOf(coopScore);
			String name = "Coop";

			while(parent.textWidth(score + name) < 240) name += ' ';

			hud.drawBannerCenter(name + score, PlayerConstants.NEUTRAL_COLOR, hud.getAngle());
		} else {
			for(int i = 0; i < players.length; i++) {
				String score = String.valueOf(players[i].score.getScore());
				String name = players[i].getName();

				while(parent.textWidth(score + name) < 240) name += ' ';

				hud.drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
			}
		}

		//Particles and Liquid
		if(PARTICLES_ABOVE) for(int i = 0; i < players.length; i++) players[i].draw();

		if(isDone()) { //Someone won
			Player winner = getWinner();
			String text = winner != null ? winner.getName() + " won!" : "You Tied!";
			Color color = winner != null ? winner.getColor() : PlayerConstants.NEUTRAL_COLOR;
			if(coop) {
				text = "";
				color = PlayerConstants.NEUTRAL_COLOR;
			}
			hud.drawCenterText("", text, color, hud.getAngle());
			hud.drawCenterImage(hud.hudPlayAgain, hud.getAngle());
		} else if(isRunning()) { //Running
			update();
		} else { //Pause
			hud.drawCenterImage(hud.hudPlay, hud.getAngle());
		}
	}

	class VolumeFader implements Runnable {

		Thread thread;

		boolean running, fadeIn;

		public void stop() {
			if(thread != null && thread.isAlive()) {
				running = false;
				thread.interrupt();
				while(thread.isAlive()) Thread.yield();
			}
		}

		public void fadeIn() {
			stop();
			fadeIn = true;
			running = true;
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}

		public void fadeOut() {
			stop();
			fadeIn = false;
			running = true;
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}

		public void run() {
			/* if(fadeIn) {
 
				// dingding.trigger();
				// try {
				// 	Thread.sleep(dingding.length());
				// } catch(Exception e) {
				// }
				for(int i = 100;i >= 0;i--) {
					orbprotected_song.setGain(-(float)i/4);
					orbcrazy_song.setGain(-(float)i/4);
					try {
						Thread.sleep(20);
					} catch(Exception e) {

					}
				}
				orbprotected_song.setGain(0);
				orbcrazy_song.setGain(0);
			} else {
				// gong.trigger();

				for(int i = 0;i < 100;i++) {
					orbprotected_song.setGain(-(float)i/4);
					orbcrazy_song.setGain(-(float)i/4);
					try {
						Thread.sleep(20);
					} catch(Exception e) {

					}
				}
				orbprotected_song.setGain(-100);
				pauseOrbProtectedSongs()
				orbcrazy_song.setGain(-100);
				pauseOrbCrazySongs()
			} */
		}
	}

}
