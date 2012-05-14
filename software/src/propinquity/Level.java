package propinquity;

import processing.core.*;
import processing.xml.*;
import propinquity.hardware.Patch;
import propinquity.hardware.ProxEventListener;
import ddf.minim.*;

public class Level implements ProxEventListener {

	static final int DEFAULT_BPM = 120;

	Propinquity parent;
	
	Hud hud;

	Player[] players;

	Sounds sounds;

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

	int currentTime;
	int[] lastTime;

	public Level(Propinquity parent, Hud hud, String levelFile, Player[] players, Sounds sounds) throws XMLException {
		this.parent = parent;
		this.players = players;
		this.hud = hud;

		this.sounds = sounds;

		XMLElement xml = new XMLElement(parent, levelFile);

		name = xml.getString("name");

		if (name == null) {
			name = "Level";
			System.out.println("Warning: XML contained no level name");
			System.out.println(xml.toString());
		}

		XMLElement[] song_tags = xml.getChildren("song");

		if (song_tags.length > 0) {
			if (song_tags.length > 1)
				System.out.println("Warning: XML contained multiple songs tags for a single Level");

			XMLElement song = song_tags[0];

			songFile = song.getString("file");
			if (songFile.equals(""))
				throw new XMLException("XMLException: XML song tag has empty file attribute");

			songBPM = song.getInt("bpm", DEFAULT_BPM);
		} else {
			throw new XMLException("XMLException: XML for level \"" + name + "\" has no song tag");
		}

		song = sounds.loadSong(songFile);

		XMLElement[] step_tags = xml.getChildren("sequence/step");
		steps = new Step[step_tags.length];
		stepInterval = song.length() / step_tags.length;

		if (step_tags.length > 0) {
			for (int i = 0; i < step_tags.length; i++) {
				String modeString = step_tags[i].getString("mode", "coop");
				boolean coop = true;
				if (!modeString.equals("coop"))
					coop = false;

				XMLElement[] player_tags = step_tags[i].getChildren("player");
				boolean patches[][] = new boolean[player_tags.length][4];
				if (player_tags.length > 1) {
					for (int j = 0; j < player_tags.length; j++) {
						patches[j][0] = (player_tags[j].getInt("patch1", 0) != 0);
						patches[j][1] = (player_tags[j].getInt("patch2", 0) != 0);
						patches[j][2] = (player_tags[j].getInt("patch3", 0) != 0);
						patches[j][3] = (player_tags[j].getInt("patch4", 0) != 0);
					}
				} else {
					throw new XMLException("XMLException: XML for level \"" + name + "\", step " + i
							+ " has less than two player tags.");
				}

				steps[i] = new Step(coop, patches);
			}

		} else {
			throw new XMLException("Warning: XML for level \"" + name + "\" has no sequence tag and/or no step tags");
		}

		lastTime = new int[players.length];

		reset();
	}

	public void pause() {
		running = false;
		song.pause();
	}

	public void start() {
		running = true;
		song.play();
	}

	public void reset() {
		for (int i = 0; i < players.length; i++)
			players[i].reset();
		pause();
		song.rewind();
		stepUpdate();
	}

	public void close() {
		song.close();
	}

	void stepUpdate() {
		coop = steps[currentStep].isCoop();
		//TODO Handle Patches and set player coop
	}

	public boolean isCoop() {
		return coop;
	}

	public void update() {

		currentTime = parent.millis();

		for (int i = 0; i < players.length; i++) {

			for (Patch patch : players[i].patches) {

				int distance = patch.getProx();
				
				if (distance > Score.MIN_SWEETSPOT && distance < Score.MAX_SWEETSPOT) {

					if (currentTime - lastTime[i] > Particle.SPAWN_DELAY) {
						players[i].handleSweetspotRange(patch);
						lastTime[i] = currentTime;
					}

				} else if (distance > Score.MIN_RANGE && distance < Score.MAX_RANGE) {

					if (currentTime - lastTime[i] > Particle.SPAWN_DELAY) {
						players[i].handleScoreRange(patch);
						lastTime[i] = currentTime;
					}
				}

			}

			players[i].update();
		}

		int nextStep = (int)PApplet.constrain(song.position()/stepInterval, 0, steps.length-1);
		if(nextStep != currentStep) {
			currentStep = nextStep;
			stepUpdate();
		}
	}
	
	public String getName() {
		return name;
	}

	public Player getWinner() { // TODO ties
		Player winner = null;

		for (Player player : players) {
			if (winner == null || player.getScore() > winner.getScore()) {
				winner = player; // Ties suck, i'm ignoring them
			}
		}

		return winner;
	}

	public int getTotalPoints() {
		int total = 0;
		for (int i = 0; i < players.length; i++)
			total += players[i].getScore();
		return total;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isDone() {
		return (song.position() >= song.length()); // TODO Crappy
	}

	public void proxEvent(Patch patch) {
		// TODO: interesting things on prox boundaries
	}
	
	public void draw() {
		if(!isVisible) return;

		//Outlines
		hud.drawInnerBoundary();
		hud.drawOuterBoundary();

		//Scores
		if (isCoop()) {
			String score = String.valueOf(getTotalPoints());
			String name = "Coop";

			while (parent.textWidth(score + name) < 240) name += ' ';

			hud.drawBannerCenter(name + score, PlayerConstants.NEUTRAL_COLOR, hud.getAngle());
		} else {
			for (int i = 0; i < players.length; i++) {
				String score = String.valueOf(players[i].score.getScore());
				String name = players[i].getName();

				while (parent.textWidth(score + name) < 240) name += ' ';

				hud.drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
			}
		}

		//Particles and Liquid
		for(int i = 0; i < players.length; i++) players[i].draw();

		if(isRunning()) { //Running
			update();
		} else if(isDone()) { //Someone won
			Player winner = getWinner();
			String text = winner != null ? winner.getName() + " won!" : "You tied!";
			Color color = winner != null ? winner.getColor() : PlayerConstants.NEUTRAL_COLOR;
			hud.drawCenterText(text, color, hud.getAngle());
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
