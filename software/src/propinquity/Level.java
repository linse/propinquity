package propinquity;

import processing.core.*;
import processing.xml.*;
import propinquity.hardware.*;
import ddf.minim.*;

public class Level implements UIElement, ProxEventListener, LevelConstants {

	Propinquity parent;
	
	Hud hud;
	Sounds sounds;

	Player[] players;
	long[] lastScoreTime;
	long[] lastScoreTimePauseDiff;

	AudioPlayer song;
	String songFile;
	int songBPM;

	Step[] steps;
	long stepInterval;
	int currentStep;

	String name;

	boolean coop;
	boolean running;
	boolean isVisible;

	public Level(Propinquity parent, Hud hud, Sounds sounds, String levelFile, Player[] players) throws XMLException {
		this.parent = parent;
		this.players = players;
		this.hud = hud;
		this.sounds = sounds;

		lastScoreTime = new long[players.length];
		lastScoreTimePauseDiff = new long[players.length];

		XMLElement xml = new XMLElement(parent, levelFile);

		name = xml.getString("name");

		if(name == null) {
			name = "Level";
			System.out.println("Warning: XML contained no level name");
			System.out.println(xml.toString());
		}

		XMLElement[] song_tags = xml.getChildren("song");

		if(song_tags.length > 0) {
			if(song_tags.length > 1)
				System.out.println("Warning: XML contained multiple songs tags for a single Level");

			XMLElement song = song_tags[0];

			songFile = song.getString("file");
			if(songFile.equals(""))
				throw new XMLException("XMLException: XML song tag has empty file attribute");

			songBPM = song.getInt("bpm", DEFAULT_BPM);
		} else {
			throw new XMLException("XMLException: XML for level \"" + name + "\" has no song tag");
		}

		song = sounds.loadSong(songFile);

		XMLElement[] step_tags = xml.getChildren("sequence/step");
		steps = new Step[step_tags.length];
		stepInterval = song.length()/step_tags.length;

		if(step_tags.length > 0) {
			for(int i = 0; i < step_tags.length; i++) {
				String modeString = step_tags[i].getString("mode", "coop");
				boolean coop = true;
				if(!modeString.equals("coop"))
					coop = false;

				XMLElement[] player_tags = step_tags[i].getChildren("player");
				boolean patches[][] = new boolean[player_tags.length][4];
				if(player_tags.length >= players.length) {
					for(int j = 0; j < player_tags.length; j++) {
						patches[j][0] = (player_tags[j].getInt("patch1", 0) != 0);
						patches[j][1] = (player_tags[j].getInt("patch2", 0) != 0);
						patches[j][2] = (player_tags[j].getInt("patch3", 0) != 0);
						patches[j][3] = (player_tags[j].getInt("patch4", 0) != 0);
					}
				} else {
					throw new XMLException("XMLException: XML for level \"" + name + "\", step " + i + " has too few player tags.");
				}

				steps[i] = new Step(coop, patches);
			}

		} else {
			throw new XMLException("Warning: XML for level \"" + name + "\" has no sequence tag and/or no step tags");
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

		for(Player player : players) player.reset(); //Clears all the particles, scores, patches and gloves

		lastScoreTime = new long[players.length];
		lastScoreTimePauseDiff = new long[players.length];

		song.rewind();
		stepUpdate(0); //Load for banner
	}

	public void close() {
		song.close();
	}

	public boolean isCoop() {
		return coop;
	}
	
	void stepUpdate(int nextStep) {
		currentStep = nextStep;

		coop = steps[currentStep].isCoop();
		boolean[][] patchStates = steps[currentStep].getPatches();

		for(int i = 0;i < players.length;i++) {
			if(i < patchStates.length) {
				players[i].step(coop, patchStates[i]);
			} else {
				//TODO warning here, there are too few patchStates, shoudn't happen
				break;
			}
		}
	}

	public void proxEvent(Patch patch) {
		if(!isRunning() || isDone()) return;
		if(!patch.getActive()) return;
		//Handle patch feedback
		patch.setMode(patch.getZone());
	}

	public void update() {
		for(Player player : players) player.update();

		//Handle Glove feedback
		for(int i = 0;i < players.length;i++) {
			Glove glove = players[i].getGlove();
			if(glove.getActive()) {
				Patch bestPatch = players[(i+1)%players.length].getBestPatch();
				if(bestPatch != null) glove.setMode(bestPatch.getZone()); //TODO wut hack
				else glove.setMode(0);
			}
		}

		//Handle score
		long currentTime = parent.millis();
		
		for(int i = 0;i < players.length;i++) {
			Player proxPlayer = players[(i+1)%players.length]; //TODO wut hack
			Player scoringPlayer = players[i];

			Patch bestPatch = proxPlayer.getBestPatch();

			if(bestPatch != null && bestPatch.getZone() > 0) {
				if(currentTime-lastScoreTime[i] > proxPlayer.getSpawnInterval()) {
					scoringPlayer.addPoints(1);
					lastScoreTime[i] = currentTime;
				}
			} else {
				lastScoreTime[i] = currentTime;
			}
		}

		int nextStep = (int)PApplet.constrain(song.position()/stepInterval, 0, steps.length-1);
		if(nextStep != currentStep) stepUpdate(nextStep);
	}

	public String getName() {
		return name;
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
		return (currentStep == steps.length-1); //TODO Crappy ?
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

	/**
	 * Shows the GUI.
	 * 
	 */
	public void show() {
		isVisible = true;
	}

	/**
	 * Hides the GUI.
	 * 
	 */
	public void hide() {
		isVisible = false;
	}

	/**
	 * Returns true if the GUI is visible.
	 * 
	 * @return true is and only if the GUI is visible.
	 */
	public boolean isVisible() {
		return isVisible;
	}

}
