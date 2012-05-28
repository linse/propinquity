package propinquity;

import ddf.minim.AudioPlayer;
import processing.core.PConstants;
import propinquity.hardware.*;
public class BopperLevel extends Level {

	static int NUM_ROUNDS = 5;
	static int BOP_TIME = 500;

	AudioPlayer song;
	String songFile;

	int roundInterval;
	int currentRound;

	long lastTime;
	long lastTimeDiff;

	Player defendingPlayer;
	Player scoringPlayer;

	boolean running;

	public BopperLevel(Propinquity parent, Hud hud, Sounds sounds, String songFile, Player[] players) {
		super(parent, hud, sounds, players);

		song = sounds.loadSong(songFile);
		roundInterval = song.length()/(NUM_ROUNDS*players.length+1);

		reset();
	}

	public void pause() {
		song.pause();
		running = false;
		lastTimeDiff = parent.millis()-lastTime;

		for(int i = 0;i < players.length;i++) players[i].pause();
	}

	public void start() {
		for(int i = 0;i < players.length;i++) players[i].start();
		lastTime = parent.millis()-lastTimeDiff;
		running = true;
		song.play();
	}

	public void reset() {
		song.pause();
		running = false;

		for(Player player : players) player.reset(); //Clears all the particles, scores, patches and gloves

		song.rewind();

		roundUpdate(0);
	}

	public void close() {
		song.close();
	}
	
	void roundUpdate(int nextRound) {
		currentRound = nextRound;

		if(currentRound >= NUM_ROUNDS*players.length) {
			for(Player player : players) {
				player.clearPatches();
			}

			defendingPlayer = null;
			scoringPlayer = null;
		} else {
			defendingPlayer = players[currentRound%players.length];
			defendingPlayer.activatePatches();

			for(Player player : players) {
				if(player != defendingPlayer) player.clearPatches();
			}

			scoringPlayer = players[(currentRound+1)%players.length]; //TODO wut hack sorta
		}
	}

	public void proxEvent(Patch patch) {
		return;
	}

	public void update() {
		for(Player player : players) player.update();
	
		long currentTime = parent.millis();
		
		if(defendingPlayer != null && scoringPlayer != null) {
			Patch bestPatch = defendingPlayer.getBestPatch();

			if(bestPatch != null && bestPatch.getZone() > 0) {
				if(currentTime-lastTime > BOP_TIME) {
					scoringPlayer.addPoints(5);

					(new Thread(new Runnable() {
						public void run() {
							scoringPlayer.getGlove().setVibeLevel(255);
							try {
								Thread.sleep(1500);
							} catch(Exception e) {

							}
							scoringPlayer.getGlove().setVibeLevel(0);
						}
					})).start();


					bestPatch.setActive(false);
					
					lastTime = currentTime;
				}
			} else {
				lastTime = currentTime;
			}
		}

		int nextRound = song.position()/roundInterval;
		if(nextRound != currentRound) roundUpdate(nextRound);
	}

	public String getName() {
		return "Bopper";
	}

	public Player getWinner() {
		Player winner = null;
		int highScore = -1;

		for(Player player : players) {

			if(player.getScore() > highScore) {
				winner = player;
				highScore = player.getScore();
			} else if(player.getScore() == highScore) {
				winner = null;
			}
		}

		return winner;
	}

	public int getTotalPoints() {
		int total = 0;
		for(int i = 0; i < players.length; i++) total += players[i].getScore();
		return total;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isDone() {
		return false;
	}
	
	public void keyPressed(char key, int keyCode) {
		if(!isVisible) return;

		switch(key) {
			case BACKSPACE: {
				reset(); //Make sure particles are gone
				if(song.position() == 0) parent.changeGameState(GameState.LevelSelect);
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
				song.cue(song.length()-1000);
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
		for(int i = 0; i < players.length; i++) {
			String score = String.valueOf(players[i].score.getScore());
			String name = players[i].getName();

			while(parent.textWidth(score + name) < 240) name += ' ';

			hud.drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
		}

		//Particles and Liquid
		if(PARTICLES_ABOVE) for(int i = 0; i < players.length; i++) players[i].draw();

		if(isDone()) { //Someone won
			Player winner = getWinner();
			String text = winner != null ? winner.getName() + " won!" : "You Tied!";
			Color color = winner != null ? winner.getColor() : PlayerConstants.NEUTRAL_COLOR;
			hud.drawCenterText("", text, color, hud.getAngle());
			hud.drawCenterImage(hud.hudPlayAgain, hud.getAngle());
		} else if(isRunning()) { //Running
			update();
		} else { //Pause
			hud.drawCenterImage(hud.hudPlay, hud.getAngle());
		}
	}

}
