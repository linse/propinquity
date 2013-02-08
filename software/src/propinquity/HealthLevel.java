package propinquity;

import ddf.minim.AudioPlayer;
import processing.core.PConstants;
import propinquity.hardware.*;

/**
 * <p>In the HealthLevel, each patch represents a quantity of health, being in proximity to a patch causes it to "bleed out" loosing health. When a player has no more health left on any patches he/she has lost.</p>
 *
 * <p>If neither player has run out of health after a certain amount of time, the player with the highest health wins.</p>
 *
 * <p>This implementation was only a rough sketch done for playtesting and should be reworked if it is to be used permanently.</p>
 *
 */
public class HealthLevel extends Level {

	/**
	 * The amount of time (in ms) that must pass before all new particles are
	 * tallied and added to the player's permanent score.
	 */
	public static final int SCORE_TIME = 300;

	long[] lastScoreTime;
	long[] lastScoreTimePauseDiff;
	long lastTransferTime;

	AudioPlayer song;
	String songFile;
	
	boolean running, ended;
	
	public HealthLevel(Propinquity parent, Hud hud, Sounds sounds, String songFile, Player[] players) {
		super(parent, hud, sounds, players);
		
		lastScoreTime = new long[players.length];
		lastScoreTimePauseDiff = new long[players.length];

		try {
			song = sounds.loadSong(songFile);
		} catch(Exception e) {
			throw new NullPointerException("Loading song file failed. Likely file name invalid or file missing for HealthLevel. Given file name was \""+songFile+"\".");
		}
		
		reset();
	}

	public void pause() {
		song.pause();
		running = false;
		for(int i = 0;i < players.length;i++) {
			players[i].pause();
			lastScoreTimePauseDiff[i] = parent.millis()-lastScoreTime[i];
		}
	}

	public void start() {
		for(int i = 0;i < players.length;i++) {
			players[i].start();
			lastScoreTime[i] = parent.millis()-lastScoreTimePauseDiff[i];
		}
		running = true;
		song.play();
	}

	public void reset() {
		song.pause();
		running = false;
		ended = false;

		for(Player player : players) {
		  player.configurePatches(Mode.PROX);
			player.reset(); //Clears all the particles, scores, patches and gloves
			player.activatePatches();
		}

		lastScoreTime = new long[players.length];
		lastScoreTimePauseDiff = new long[players.length];
		
		song.rewind();
	}

	public void close() {
		song.close();
	}

	public void proxEvent(Patch patch) {
		if(!isRunning() || isDone()) return;
		if(!patch.getActive()) return;
	}
	
    public void accelXYZEvent(Patch patch) {
      
    }

    public void accelInterrupt0Event(Patch patch) {
      
    }

    public void accelInterrupt1Event(Patch patch) {
      
    }

    public void update() {
		long currentTime = parent.millis();

		if(currentTime - lastTransferTime > SCORE_TIME) {
			for(Player player : players) player.transferScore();
			lastTransferTime = currentTime;
		}
		
		for(Player player : players) player.update();

		//Handle Glove feedback
		for(int i = 0;i < players.length;i++) {
			Glove glove = players[i].getGlove();
			if(glove.getActive()) {
				Patch bestPatch = players[(i+1)%players.length].getBestPatch(); //TODO: Hack being use to get opponent. Nothing significantly better can be done with this hardware.
				if(bestPatch != null) glove.setMode(bestPatch.getZone());
				else glove.setMode(0);
			}
		}
				
		for(int i = 0;i < players.length;i++) {
			Player proxPlayer = players[(i+1)%players.length]; //TODO: Hack being use to get opponent. Nothing significantly better can be done with this hardware.
			Player scoringPlayer = players[i];

			Patch bestPatch = proxPlayer.getBestPatch();

			if(bestPatch != null && bestPatch.getZone() > 0) {
				if(currentTime-lastScoreTime[i] > proxPlayer.getSpawnInterval()) {
					proxPlayer.damage(bestPatch, 1);
					proxPlayer.addPoints(1);
					lastScoreTime[i] = currentTime;
				}
			} else {
				// lastScoreTime[i] = currentTime;
			}
		}

		for(int i = 0;i < players.length;i++) {
			players[i].bufPatch();
		}
	}

	public String getName() {
		return "Health Level";
	}
	
	public Player getWinner() {
		Player winner = null;
		int highestHealth = -1;
		
		for(Player player : players) {
			if(player.getHealth() > highestHealth) {
				winner = player;
				highestHealth = player.getScore();
			} else if(player.getHealth() == highestHealth) {
				winner = null;
			}
		}
		
		return winner;
	}

	public boolean isRunning() {
		return running;
	}
	
	public boolean isDone() {
		int playersAlive = players.length;
		
		for(Player player : players) {
			if(player.getHealth() <= 0) playersAlive--;
		}
		
		if(playersAlive <= 1) {
			if(!ended) {
				for(Player player : players) {
					player.transferScore();
					player.clearGloves();
					player.clearPatches();
					player.bump();
				}
				ended = true;
			}
			return true;
		} else {
			return false;
		}
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

		//Health Banners
		for(int i = 0; i < players.length; i++) {
			String health = String.valueOf(players[i].getHealth());
			String name = players[i].getName();

			while(parent.textWidth(health + name) < 240) name += ' ';

			hud.drawBannerSide(name + health, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
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
