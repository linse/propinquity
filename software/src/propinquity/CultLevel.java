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
public class CultLevel extends Level {

	static final int BPM_INTERVAL = 1200;
	static final int CLAIMED_INTERVAL = 2000;
	static final int LEVEL_INTERVAL = 11000;

	String name;
	boolean running, done, win, start;

	Patch heart;
	Patch[] lesions;
	boolean[] lesionsState;

	int bpm;
	int claimedTimer, claimedTimerDiff;
	int bpmTimer, bpmTimerDiff;

	boolean alive, claimed;

	int level;
	int levelTimer, levelTimerDiff;
	
	Beater beater;

	public CultLevel(Propinquity parent, Hud hud, Sounds sounds, Player[] players, Patch[] patches) {
		super(parent, hud, sounds, players);

		name = "Cult Level";

		heart = patches[0];
		lesions = new Patch[patches.length-1];
		lesionsState = new boolean[patches.length-1];
		for(int i = 0;i < lesions.length;i++) {
			lesions[i] = patches[i+1];
			lesionsState[i] = false;
		}

		done = true;
		running = false;

		beater = new Beater();
	}

	public void pause() {
		running = false;

		heart.setActive(false);

		for(int i = 0;i < level;i++) {
			lesions[i].setActive(false);
		}

		levelTimerDiff = parent.millis()-levelTimer;
		if(claimedTimer != -1) claimedTimerDiff = parent.millis()-claimedTimer;
		if(bpmTimer != -1) bpmTimerDiff = parent.millis()-bpmTimer;
	}

	public void start() {
		levelTimer = parent.millis()-levelTimerDiff;
		if(claimedTimer != -1) claimedTimer = parent.millis()-claimedTimerDiff;
		if(bpmTimer != -1) bpmTimer = parent.millis()-bpmTimerDiff;

		heart.setActive(true);

		for(int i = 0;i < level;i++) {
			lesions[i].setActive(true);
		}

		running = true;
	}

	public void reset() {
		running = false;

		start = true;

		done = false;

		claimedTimer = -1;
		claimedTimerDiff = 0;

		bpm = 60;
		bpmTimer = -1;
		bpmTimerDiff = 0;

		alive = true;
		claimed = false;

		level = 0;
		levelTimer = 0;
		levelTimerDiff = 0;

		heart.setColor(255, 0, 0);
		heart.setColorPeriod(255);
		heart.setColorWaveform(1);
		heart.setColorDuty(127);

		heart.setActivationMode(Mode.OFF);
		heart.setActive(false);

		for(int i = 0;i < lesions.length;i++) {
			lesions[i].setColor(100, 100, 100);
			lesions[i].setActivationMode(Mode.PROX);
			lesions[i].setActive(false);
			lesionsState[i] = false;
		}
	}

	public void close() {

	}

	public void proxEvent(Patch patch) {
		if(!isRunning() || isDone()) return;
		if(!patch.getActive()) return;
		//Handle patch feedback
		// patch.setMode(patch.getZone());
	}
	
	public void accelXYZEvent(Patch patch) {
		//Ignore
	}

	public void accelInterrupt0Event(Patch patch) {
		//Ignore 
	}

	public void accelInterrupt1Event(Patch patch) {
		//Ignore
	}

	public void update() {
		if(done) return;

		if(start) {
			sounds.getEKGStart().trigger();
			start =  false;
		}
		int active_lesions = level;

		if(claimed) {
			active_lesions++;
		} else {
			lesions[level].setActive(true);

			if(lesions[level].getZone() != 0) { //Covered
				lesions[level].setMode(0);
				claimed = true;
				claimedTimer = -1;
				sounds.getExhale().trigger();
				lesionsState[level] = true;
			} else { //Not Covered
				lesions[level].setMode(1);
				if(claimedTimer == -1) {
					claimedTimer = parent.millis();
				} else if(parent.millis()-claimedTimer > CLAIMED_INTERVAL) {
					bpm++;
					claimedTimer = -1;
				}

			}
		}

		int num_not_covered = 0;

		for(int i = 0;i < active_lesions;i++) {
			lesions[i].setActive(true);

			if(lesions[i].getZone() != 0) { //Covered
				if(!lesionsState[i]) {
					sounds.getExhale().trigger();
					lesionsState[i] = true;
				}
				lesions[i].setMode(0);
			} else { //Not Covered
				if(lesionsState[i]) {
					sounds.getInhale().trigger();
					lesionsState[i] = false;
				}
				lesions[i].setMode(1);
				num_not_covered++;
			}
		}

		if(num_not_covered > 0) {
			if(bpmTimer == -1) {
				bpmTimer = parent.millis();
			} else if(parent.millis()-bpmTimer > BPM_INTERVAL) {
				// bpm++;
				bpm += num_not_covered;
				heart.setColor(255, 0, 0);
				heart.setColorPeriod((int)PApplet.map(bpm, 60, 150, 255, 30));
				heart.setColorWaveform(1);
				heart.setColorDuty(127);
				bpmTimer = -1;
			}
		}

		if(parent.millis()-levelTimer > LEVEL_INTERVAL) {
			level++;
			levelTimer = parent.millis();

			if(level > lesions.length-1) {
				done = true;
				win = true;

				for(int i = 0;i < active_lesions;i++) {
					lesions[i].setActive(false);
				}

				heart.setActive(true);	

				heart.setColor(255, 0, 0);
				heart.setColorPeriod(255);
				heart.setColorWaveform(1);
				heart.setColorDuty(127);

				return;
			}
		}

		heart.setActive(true);	

		if(bpm > 150) {
			done = true;
			win = false;
			sounds.getFlatline().trigger();
		}

		// System.out.println(bpm);
	}

	public String getName() {
		return name;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isDone() {
		return done;
	}
	
	public void keyPressed(char key, int keyCode) {
		if(!isVisible) return;

		switch(key) {
			case BACKSPACE: {
				reset();
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
		}
	}

	public void draw() {
		if(!isVisible) return;

		//Outlines
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();

		if(isDone()) { //Someone won
			if(win) {
				hud.drawCenterText("", "Win", Color.white(), hud.getAngle());
			} else {
				hud.drawCenterText("", "Loose", Color.white(), hud.getAngle());
			}
			hud.drawCenterImage(hud.hudPlayAgain, hud.getAngle());
		} else if(isRunning()) { //Running
			update();
			//Score Banners
			String score = String.valueOf(bpm);
			hud.drawCenterText("Your Heart is Beating", score +" beats per mintue", Color.white(), hud.getAngle());
		} else { //Pause
			hud.drawCenterImage(hud.hudPlay, hud.getAngle());
		}
	}

	class Beater implements Runnable {
		Thread thread;
		boolean run;

		Beater() {
			run = true;

			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}

		void stop() {
			run = false;
			if(thread != null) while(thread.isAlive()) Thread.yield();
		}

		public void run() {
			while(run) {
				if(!isRunning() || isDone()) {
					Thread.yield();
				} else {
					sounds.getHeartBeat().trigger();
					try {
						Thread.sleep(60*1000/bpm);
					} catch(Exception e) {

					}
				}
			}
		}
	}

}