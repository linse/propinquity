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

	static final long HEART_INTERVAL = 1000;
	static final long COOL_DOWN_BLACK = 8000;
	static final long COOL_DOWN_WARMUP = 8000;

	String name;
	boolean running;

	long heartTimers[];
	long headTimersPauseDiff[];

	long coolDownTimer, coolDownTimerDiff;

	Player winner;
	
	public CultLevel(Propinquity parent, Hud hud, Sounds sounds, Player[] players) {
		super(parent, hud, sounds, players);

		heartTimers = new long[players.length];
		headTimersPauseDiff = new long[players.length];

		for(int i = 0;i < heartTimers.length;i++) {
			heartTimers[i] = -1;
		}

		coolDownTimer = -1;

		name = "Cult Level";
	}

	public void pause() {
		running = false;
		for(int i = 0;i < players.length;i++) {
			players[i].pause();
			if(heartTimers[i] != -1) headTimersPauseDiff[i] = parent.millis()-heartTimers[i];
		}
		if(coolDownTimer != -1) coolDownTimerDiff = parent.millis()-coolDownTimer;
	}

	public void start() {
		for(int i = 0;i < players.length;i++) {
			players[i].start();
			if(heartTimers[i] != -1) heartTimers[i] = parent.millis()-headTimersPauseDiff[i];
		}
		if(coolDownTimer != -1) coolDownTimer = parent.millis()-coolDownTimerDiff;
		running = true;
	}

	public void reset() {
		running = false;
		
		for(Player player : players) {
			player.configurePatches(Mode.PROX);
			player.reset(); //Clears all the particles, scores, patches and gloves

			Patch[] patches = player.getPatches();

			patches[0].setColor(255, 0, 0);
			patches[0].setActivationMode(Mode.PROX);
			for(int i = 1;i < patches.length;i++) {
				patches[i].setColor(100, 100, 100);
			}
		}

		for(int i = 0;i < heartTimers.length;i++) {
			heartTimers[i] = -1;
		}

		coolDownTimer = -1;

		winner = null;
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
		// for(Player player : players) player.update();

		if(coolDownTimer != -1) {
			if(parent.millis()-coolDownTimer < COOL_DOWN_BLACK) {
				for(int i = 0;i < players.length;i++) {
					for(Patch patch : players[i].getPatches()) {
						patch.setActive(false);
					}
				}
				return;
			} else if(parent.millis()-coolDownTimer < COOL_DOWN_WARMUP+COOL_DOWN_BLACK) {
				for(int i = 0;i < players.length;i++) {
					players[i].getPatches()[0].setActive(false);
				}
			} else {
				coolDownTimer = -1;
			}
		}

		boolean[] player_covered = {true, true};

		for(int i = 0;i < players.length;i++) {
			Player player = players[i];
			Patch[] patches = player.getPatches();
			Patch heart = patches[0];

			for(int j = 1;j < patches.length;j++) {
				if(j < players[i].getScore()+1) {
					patches[j].setActive(true);
					if(patches[j].getZone() != 0) {
						//Patch is covered
						patches[j].setMode(0);
					} else {
						patches[j].setMode(1);
						player_covered[i] = false;
					}
				} else {
					patches[j].setActive(false);
				}
			}

			if(player_covered[i]) {
				heart.setColor(255, 0, 0);
			} else {
				heart.setColor(0, 0, 255);
			}
		}

		if(coolDownTimer != -1) return;

		for(int i = 0;i < players.length;i++) {
			Player opponent = players[(i+1)%2];
			Player player = players[i];
			Patch heart = player.getPatches()[0];
			heart.setActive(true);

			if(heart.getZone() != 0 && player_covered[(i+1)%2]) {
				if(heartTimers[i] == -1) {
					heartTimers[i] = parent.millis();
				} else if(parent.millis()-heartTimers[i] > HEART_INTERVAL) {
					opponent.addPoints(1);
					heartTimers[i] = -1;

					if(opponent.getScore() == opponent.getPatches().length) {
						win(opponent);
					} else {
						coolDownTimer = parent.millis();
					}
				}
			} else {
				heartTimers[i] = -1;
			}
		}
	}

	public String getName() {
		return name;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isDone() {
		if(winner != null) return true;
		else return false;
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

	void win(Player p) {
		winner = p;
		Patch[] winnerPatches = p.getPatches();
		Patch winnerHeart = winnerPatches[0];

		winnerHeart.setColor(0, 255, 0);

		for(int i = 1;i < winnerPatches.length;i++) {
			winnerPatches[i].setActive(false);
		}

		for(Player player : players) {
			if(player == winner) continue;
			for(Patch patch : player.getPatches()) {
				patch.setActive(false);
			}
		}

	}

	public void draw() {
		if(!isVisible) return;

		//Outlines
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();

		//Score Banners
		for(int i = 0; i < players.length; i++) {
			String score = String.valueOf(players[i].score.getScore());
			String name = players[i].getName();

			while(parent.textWidth(score + name) < 240) name += ' ';

			hud.drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
		}

		if(isDone()) { //Someone won
			hud.drawCenterText("", "Only one cult leader remains", Color.white(), hud.getAngle());
			hud.drawCenterImage(hud.hudPlayAgain, hud.getAngle());
		} else if(isRunning()) { //Running
			update();
		} else { //Pause
			hud.drawCenterImage(hud.hudPlay, hud.getAngle());
		}
	}

}
