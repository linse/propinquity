package propinquity;

import ddf.minim.AudioPlayer;
import processing.core.PConstants;
import propinquity.hardware.*;
public class BopperLevel extends Level {

	static int NUM_ROUNDS = 5;

	AudioPlayer song;
	String songFile;

	int roundInterval;
	int currentRound;

	boolean coop;
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
		for(int i = 0;i < players.length;i++) players[i].pause();
	}

	public void start() {
		for(int i = 0;i < players.length;i++) players[i].start();
		running = true;
		song.play();
	}

	public void reset() {
		song.pause();
		running = false;

		for(Player player : players) player.reset(); //Clears all the particles, scores, patches and gloves

		song.rewind();

		currentRound = 0;
	}

	public void close() {
		song.close();
	}
	
	void roundUpdate(int nextRound) {

	}

	public void proxEvent(Patch patch) {
		if(!isRunning() || isDone()) return;
		if(!patch.getActive()) return;
		//Handle Patch feedback
	}

	public void update() {
		for(Player player : players) player.update();

		for(int i = 0;i < players.length;i++) {
			Glove glove = players[i].getGlove();
			if(glove.getActive()) {
				//Handle Glove feedback
			}
		}

		//Handle score ?

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

		//Outlines
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();

		//Score Banners
		if(coop) {
			String score = String.valueOf(getTotalPoints());
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
		for(int i = 0; i < players.length; i++) players[i].draw();

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

}
