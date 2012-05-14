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

		parseXML(new XMLElement(parent, levelFile));

		reset();
	}

	public void clear() {
		for(int i = 0; i < players.length; i++) players[i].clear();
	}

	public void reset() {
		for(int i = 0; i < players.length; i++) players[i].reset();
		if(song != null) song.rewind();
		running = false;
	}

	public void pause() {
		running = false;
		song.pause();
	}

	public void start() {
		running = true;
		song.play();
	}

	public void update() {		
		for(int i = 0; i < players.length; i++) players[i].update();
		parent.box2d.setGravity(0, 0);
	}
	
	public void draw() {
		for(int i = 0; i < players.length; i++) players[i].draw();
	}

	public boolean isCoop() {
		return coop;
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

	void parseXML(XMLElement xml) throws XMLException {
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
			System.out.println();
			throw new XMLException("XMLException: XML for level \""+name+"\" has no song tag");
		}

		XMLElement[] step_tags = xml.getChildren("sequence/step");

		if(step_tags.length > 0) {
			for(XML

		} else {
			throw new XMLException("Warning: XML for level \""+name+"\" has no sequence tag and/or no step tags");
		}

		// XMLElement player;
		// XMLElement sequence;
		// XMLElement step;
		// int numPlayers, numSteps;


		// // read coop parameter
		// isCoop = xml.hasAttribute("coop");
		// coopPoints = isCoop ? xml.getIntAttribute("coop") : 0;
		// if(isCoop) {
		// 	players[0].setCoopMode(true);
		// 	players[1].setCoopMode(true);
		// }

		// // read song
		// name = xml.getChild(0).getAttribute("name");
		// songFile = xml.getChild(0).getAttribute("file");
		// songBPM = xml.getChild(0).getIntAttribute("bpm");

		// // calculate step interval
		// stepInterval = (long) (60f / songBPM *  1000);
		// System.out.println("Step Interval: " + stepInterval);

		// // init the number of steps
		// // this makes it possible to do 1 player, but not 3
		// int[] numStepsEach = new int[numPlayers];
		// for(int i = 0; i < numPlayers; i++) {
		// 	player = xml.getChild(i + 1);
		// 	sequence = player.getChild(0);
		// 	numStepsEach[i] = sequence.countChildren();
		// 	if(numStepsEach[i] > MAX_STEPS)
		// 		numStepsEach[i] = MAX_STEPS;
		// }

		// // Now make sure that both players have the same number of steps.
		// // if not, we'll let the other player have free play.
		// numSteps = numStepsEach[0]; // stays this way if there is only one
		// 							// player.
		// if(numPlayers > 1) {
		// 	for(int i = 0; i < numPlayers; i++) {
		// 		// for now this will only go up to 2
		// 		// but let's keep the possibility of increasing the max number
		// 		// of players
		// 		if(numStepsEach[i] > numSteps)
		// 			numSteps = numStepsEach[i];
		// 	}
		// }

		// // save number of steps
		// stepCount = numSteps;

		// // read the steps
		// int readStepsUntil = 0;
		// if(numSteps > 0)
		// 	readStepsUntil = numSteps;
		// else
		// 	readStepsUntil = DEFAULT_STEPS;
		// for(int i = 0; i < numPlayers; i++) {

		// 	// init player steps
		// 	System.out.println("Sending Config for Step Interval " + stepInterval);
		// 	players[i].initializeSteps(numSteps);
		// 	player = xml.getChild(i + 1);
		// 	sequence = player.getChild(0);
		// 	for(int j = 0; j < readStepsUntil; j++) {
		// 		if(j < numStepsEach[i]) {
		// 			step = sequence.getChild(j);
		// 			boolean pad1 = PApplet.parseBoolean(step.getIntAttribute("pad1"));
		// 			boolean pad2 = PApplet.parseBoolean(step.getIntAttribute("pad2"));
		// 			boolean pad3 = PApplet.parseBoolean(step.getIntAttribute("pad3"));
		// 			boolean pad4 = PApplet.parseBoolean(step.getIntAttribute("pad4"));
		// 			boolean free = PApplet.parseBoolean(step.getIntAttribute("free"));
		// 			players[i].addStep(new Step(pad1, pad2, pad3, pad4, free), j);
					
		// 		} else {
		// 			// give them free play. All lights on.
		// 			players[i].addStep(new Step(true, true, true, true, true), j);
		// 		}
		// 	}
		// }

		// song = sounds.loadSong(songFile + ".mp3");

		// // success!
		// System.out.println("Successfully read the level file.");
		// successfullyRead = 1;
		// return;
	}
}
