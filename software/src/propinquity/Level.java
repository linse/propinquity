package propinquity;

import processing.core.*;
import processing.xml.*;
import ddf.minim.*;

public class Level {

	static final int DEFAULT_BPM = 120;

	Propinquity parent;

	Player[] players;

	Sounds sounds;

	AudioPlayer song;
	public String songFile;
	public int songBPM;

	Step[] steps;
	long stepInterval;
	public int currentStep;
	
	public String name;

	boolean coop;
	boolean running;

	public Level(Propinquity parent, String levelFile, Player[] players, Sounds sounds) throws XMLException {
		this.parent = parent;
		this.players = players;

		this.sounds = sounds;

		XMLElement xml = new XMLElement(parent, levelFile);

		name = xml.getString("name");

		if(name == null) {
			name = "Level";
			System.out.println("Warning: XML contained no level name");
			System.out.println(xml.toString());
		}


		XMLElement[] song_tags = xml.getChildren("song");

		if(song_tags.length > 0) {
			if(song_tags.length > 1) System.out.println("Warning: XML contained multiple songs tags for a single Level");

			XMLElement song = song_tags[0];

			songFile = song.getString("file");
			if(songFile.equals("")) throw new XMLException("XMLException: XML song tag has empty file attribute");

			songBPM = song.getInt("bpm", DEFAULT_BPM);
		} else {
			throw new XMLException("XMLException: XML for level \""+name+"\" has no song tag");
		}

		song = sounds.loadSong(songFile);

		XMLElement[] step_tags = xml.getChildren("sequence/step");
		steps = new Step[step_tags.length];
		stepInterval = song.length()/step_tags.length;

		if(step_tags.length > 0) {
			for(int i = 0;i < step_tags.length;i++) {
				String modeString = step_tags[i].getString("mode", "coop");
				boolean coop = true;
				if(!modeString.equals("coop")) coop = false;

				XMLElement[] player_tags = xml.getChildren("player");
				boolean patches[][] = new boolean[player_tags.length][4];
				if(player_tags.length > 1) {
					for(int j = 0;j < player_tags.length;j++) {
						patches[j][0] = (player_tags[j].getInt("patch1", 0) != 0);
						patches[j][1] = (player_tags[j].getInt("patch2", 0) != 0);
						patches[j][2] = (player_tags[j].getInt("patch3", 0) != 0);
						patches[j][3] = (player_tags[j].getInt("patch4", 0) != 0);
					}
				} else {
					throw new XMLException("XMLException: XML for level \""+name+"\", step "+i+" has less than two player tags.");
				}

				steps[i] = new Step(coop, patches);
			}

		} else {
			throw new XMLException("Warning: XML for level \""+name+"\" has no sequence tag and/or no step tags");
		}

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
		for(int i = 0; i < players.length; i++) players[i].reset();
		pause();
		song.rewind();
		stepUpdate();
	}

	public void close() {
		song.close();
	}

	void stepUpdate() {
		int nextStep = (int)PApplet.constrain(song.position()/stepInterval, 0, steps.length);
		if(nextStep != currentStep) {
			currentStep = nextStep;
			coop = steps[currentStep].isCoop();
			//TODO Handle Patches
		}
	}

	public boolean isCoop() {
		return coop;
	}

	public void update() {
		for(int i = 0; i < players.length; i++) players[i].update();
		stepUpdate();
	}
	
	public void draw() {
		for(int i = 0; i < players.length; i++) players[i].draw();
	}

	public String getName() {
		return name;
	}

	public Player getWinner() {
		int maxScore = -1;
		int winner = -1;

		// TODO: This "loop" looks a little fishy...
		for(int i = 0; i < players.length; i++)
			// check if we have a winner
			if(players[i].score.getScore() > maxScore) {
				maxScore = players[i].score.getScore();
				winner = i;
			}
			// if not check if we have a tie
			else if(players[i].score.getScore() == maxScore) {
				winner = -1;
			}

		if(winner == -1)
			return null;
		else
			return players[winner];
	}

	public int getTotalPoints() {
		int total = 0;
		for(int i = 0; i < players.length; i++) total += players[i].score.getScore();
		return total;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isDone() {
		return (currentStep > steps.length);
	} // extra step to make sure the last one got in

}
