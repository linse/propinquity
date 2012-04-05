package propinquity;

import processing.core.PApplet;
import proxml.XMLElement;
import xbee.XBeeDataFrame;
import xbee.XBeeReader;

public class Level {

	/*
	 * Here's how the data packet is constructed.
	 * 
	 * Byte 1: __ type (2 bits) ___ destination address (3 bits) _ free (1 bit)
	 * xx not used (2 bits)
	 * 
	 * Byte 2: sequence number
	 * 
	 * Byte 3: ____ current LED states ____ next LED states
	 * 
	 * Byte 4: ____ next+1 LED states ____ next+2 LED states
	 */

	// if there is nothing in the file, we want to default to 2 min of lights-on
	// free play.
	final int FREE = 1;
	final int MAX_PLAYERS = 2;
	final int MAX_STEPS = 256;
	final int DEFAULT_PLAYERS = 2;
	final int DEFAULT_TEMPO = 120;
	final int DEFAULT_MULTIPLIER = 4;
	final int DEFAULT_STEPS = 80;
	final int DEFAULT_COOP_PTS = 5000;

	// parent applet
	Propinquity parent;
	Sounds sounds;

	// the players
	// String[] playerNames;
	Player[] players = null;

	// level parameters
	boolean isCcoop;
	boolean lastCoopDone;
	int coopPts;
	String songName;
	String songFile;
	String songDuration;
	int tempo;
	int multiplier;
	int numSteps;
	int currentStep;
	int packetType;
	long stepInterval;

	boolean isRunning;
	long time;
	long lastUpdate;
	long lastStep;

	// stub parameters
	// String[] stubReadings;
	// int stubReading = 0;

	// the XML
	XMLElement levelXML;
	int successfullyRead = -1; // -1 means not read yet. 0 --> false. 1-->true

	// Only used for loading level data
	public Level(Propinquity parent, Sounds sounds) {
		Player[] players = new Player[2];
		players[0] = new Player(parent, parent.PLAYER_COLORS[0]);
		players[0].name = "Player 1";
		players[1] = new Player(parent, parent.PLAYER_COLORS[1]);
		players[1].name = "Player 2";
		init(parent, sounds, players);
	}

	public Level(Propinquity parent, Sounds sounds, Player[] players) {
		init(parent, sounds, players);
	}

	public void init(Propinquity parent, Sounds sounds, Player[] plyrs) {		
		this.parent = parent;
		this.sounds = sounds;
		players = plyrs;
		players[0].registerNegativePlayerSound(sounds.negativeP1);
		players[0].registerNegativeCoopSound(sounds.negativeCoop);
		players[1].registerNegativePlayerSound(sounds.negativeP2);
		players[1].registerNegativeCoopSound(sounds.negativeCoop);
		
		lastCoopDone = false;
		reset();
	}

	public boolean isCoop() {
		return isCcoop;
	}

	public boolean isInCoopMode() {
		return (isCcoop && (coopPts == 0 || getTotalPts() < coopPts * 2));
	}

	public boolean isCoopDone() {
		return (coopPts != 0) && (getTotalPts() / 2) >= coopPts;
	}

	public boolean getLastCoopDone() {
		return lastCoopDone;
	}

	public void setLastCoopDone(boolean b) {
		lastCoopDone = b;
		if (b) {
			players[0].setCoopMode(!b);
			players[1].setCoopMode(!b);
		}
	}

	public Player getWinner() {
		int maxScore = -1;
		int winner = -1;
		for (int i = 0; i < players.length; i++)
			// check if we have a winner
			if (players[i].getTotalPts() > maxScore) {
				maxScore = players[i].getTotalPts();
				winner = i;
			}
			// if not check if we have a tie
			else if (players[i].getTotalPts() == maxScore) {
				winner = -1;
			}

		if (winner == -1)
			return null;
		else
			return players[winner];
	}

	public int getTotalPts() {
		int total = 0;
		for (int i = 0; i < players.length; i++)
			total += players[i].getTotalPts();
		return total;
	}

	void reset() {
		if (players != null) {
			for (int i = 0; i < players.length; i++)
				players[i].reset();
			// players[0].setStubbed(false);
			// println("set stubbed to false");
		}

		// For March 28 playtest, setting one player to take prox data
		// one is faked
		currentStep = 0;
		packetType = 0;
		tempo = DEFAULT_TEMPO;
		multiplier = DEFAULT_MULTIPLIER;
		stepInterval = (long) (60f / tempo * multiplier * 1000);
		// stubReading = 0;
		time = 0;
		lastStep = -PApplet.MAX_INT;
		isRunning = false;
		lastUpdate = 0;

		// rewind song
		if (sounds.song != null)
			sounds.song.rewind();
	}

	void update() {
		long now = parent.millis();
		long dt = now - lastUpdate;
		if (dt > 0)
			time += dt;
		lastUpdate = now;

		// process stubs
		for (int i = 0; i < players.length; i++) {
			// players[i].stub(time);
			ProxData psd;
			while ((psd = players[i].nextProxStub(time)) != null) {
				processProxReading(psd);
				// proxStubIndex++;
			}

			AccelData asd;
			while ((asd = players[i].nextAccelStub(time)) != null) {
				processAccelReading(asd);
				// accelStubIndex++;
			}
		}

		// process step
		if (time - lastStep > stepInterval)
			step();
	}

	void clear() {
		for (int i = 0; i < players.length; i++)
			players[i].clear();
	}

	void step() {
		// println("Step " + currentStep + " ("+time+")");

		// keep track of time
		lastStep = time;

		// process each player step
		if (currentStep < numSteps)
			for (int i = 0; i < players.length; i++)
				players[i].sendStep(currentStep);

		// increment
		currentStep++;
	}

	void pause() {
		isRunning = false;
		sounds.song.pause();
	}

	void start() {
		isRunning = true;
		lastUpdate = parent.millis();

		if (!Sounds.MUTE)
			sounds.song.play();
	}

	boolean isPaused() {
		return !isRunning;
	}

	boolean isRunning() {
		return isRunning;
	}

	int successfullyRead() {
		return successfullyRead;
	}

	int getTempo() {
		return tempo;
	}

	int getMultiplier() {
		return multiplier;
	}

	int getNumPlayers() {
		return players.length;
	}

	int getNumSteps() {
		return numSteps;
	}

	boolean isDone() {
		return (currentStep > numSteps);
	} // extra step to make sure the last one got in

	int getCurrentStep() {
		return currentStep;
	}

	Player getPlayer(int i) {
		return players[i];
	}

	long getTime() {
		return time;
	}

	void loadDefaults() {
		tempo = DEFAULT_TEMPO;
		multiplier = DEFAULT_MULTIPLIER;
		numSteps = DEFAULT_STEPS;
		stepInterval = (long) (60f / tempo * multiplier * 1000);

		for (int i = 0; i < DEFAULT_PLAYERS; i++) {
			players[i].initializeSteps(numSteps);
			for (int j = 0; j < numSteps; j++) {
				Step s = new Step(true, true, true, true, true);
				players[i].addStep(s, j);
			}
		}
	}

	public void xmlEvent(proxml.XMLElement p_xmlElement) {
		levelXML = p_xmlElement;
		// levelXML.printElementTree();
		proxml.XMLElement l_player;
		proxml.XMLElement l_sequence;
		proxml.XMLElement l_step;
		int l_numPlayers, l_numSteps, i;

		// load number of players
		l_numPlayers = levelXML.countChildren() - 1;

		// check if we have an correct level file
		if (l_numPlayers < 2) {
			System.out.println("Error: Bad level file. We need data for 2 players.");
			successfullyRead = 0;
			return;
		}
		// limit number of players to default (?)
		else if (l_numPlayers > MAX_PLAYERS)
			l_numPlayers = MAX_PLAYERS;

		// read coop parameter
		isCcoop = levelXML.hasAttribute("coop");
		coopPts = isCcoop ? levelXML.getIntAttribute("coop") : 0;
		if (isCcoop) {
			players[0].setCoopMode(true);
			players[1].setCoopMode(true);
		}

		// read song
		songName = levelXML.getChild(0).getAttribute("name");
		songFile = levelXML.getChild(0).getAttribute("file");
		songDuration = levelXML.getChild(0).getAttribute("duration");
		tempo = levelXML.getChild(0).getIntAttribute("bpm");
		multiplier = levelXML.getChild(0).getIntAttribute("multiplier");

		// calculate step interval
		stepInterval = (long) (60f / tempo * multiplier * 1000);
		System.out.println("Step Interval: " + stepInterval);

		// init the number of steps
		// this makes it possible to do 1 player, but not 3
		int[] l_numStepsEach = new int[l_numPlayers];
		for (i = 0; i < l_numPlayers; i++) {
			l_player = levelXML.getChild(i + 1);
			l_sequence = l_player.getChild(0);
			l_numStepsEach[i] = l_sequence.countChildren();
			if (l_numStepsEach[i] > MAX_STEPS)
				l_numStepsEach[i] = MAX_STEPS;
			// max out at 256
		}

		// Now make sure that both players have the same number of steps.
		// if not, we'll let the other player have free play.
		l_numSteps = l_numStepsEach[0]; // stays this way if there is only one
										// player.
		if (l_numPlayers > 1) {
			for (i = 0; i < l_numPlayers; i++) {
				// for now this will only go up to 2
				// but let's keep the possibility of increasing the max number
				// of players
				if (l_numStepsEach[i] > l_numSteps)
					l_numSteps = l_numStepsEach[i];
			}
		}

		// save number of steps
		numSteps = l_numSteps;

		// init players' steps
		// players = new Player[l_numPlayers];

		// read the steps
		int readStepsUntil = 0;
		if (l_numSteps > 0)
			readStepsUntil = l_numSteps;
		else
			readStepsUntil = DEFAULT_STEPS;
		for (i = 0; i < l_numPlayers; i++) {
			// create player
			// players[i] = new Player(parent, playerNames[i], COLORS[i]);

			// activate stubs if needed
			// if (PROX_STUB[i]) players[i].loadProxStub(i, PROX_STUB_FILE);
			// else players[i].initProxComm(XPAN_PROX_1_PORT[i]);

			// if (ACCEL_STUB[i]) players[i].loadAccelStub(i, ACCEL_STUB_FILE);
			// else players[i].initAccelComm();

			// if (SEND_VIBE[i]) players[i].initVibeComm(XPAN_VIBE_PORT[i]);

			// init player steps
			System.out.println("Sending Config for Step Interval " + stepInterval);
			players[i].sendConfig((int) stepInterval);
			players[i].initializeSteps(l_numSteps);
			l_player = levelXML.getChild(i + 1);
			l_sequence = l_player.getChild(0);
			for (int j = 0; j < readStepsUntil; j++) {
				if (j < l_numStepsEach[i]) {
					l_step = l_sequence.getChild(j);
					players[i].addStep(
							new Step(PApplet.parseBoolean(l_step
									.getIntAttribute("pad1")), PApplet
									.parseBoolean(l_step
											.getIntAttribute("pad2")), PApplet
									.parseBoolean(l_step
											.getIntAttribute("pad3")), PApplet
									.parseBoolean(l_step
											.getIntAttribute("pad4")), PApplet
									.parseBoolean(l_step
											.getIntAttribute("free"))), j);
				} else {
					// give them free play. All lights on.
					players[i].addStep(new Step(true, true, true, true, true),
							j);
				}
			}
		}

		sounds.loadSong(songFile + ".mp3");

		// success!
		System.out.println("Successfully read the level file.");
		successfullyRead = 1;
		return;
	}

	public void xBeeEvent(XBeeReader xbee) {

		XBeeDataFrame data = xbee.getXBeeReading();

		if (data.getApiID() == XBeeReader.SERIES1_RX16PACKET) {
			int[] packet = data.getBytes();
			if (packet.length == XPan.PROX_IN_PACKET_LENGTH
					&& packet[0] == XPan.PROX_IN_PACKET_TYPE) {
				// println("prox message received");
				int patch = (packet[1] >> 1);
				int player = getPlayerIndexForPatch(patch);

				if (player != -1) {
					boolean touched = (packet[1] & 1) == 1;
					// println(packet[1]);
					// println(touched);
					int step = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
					// println(step);
					int proximity = ((packet[4] & 0xFF) << 8)
							| (packet[5] & 0xFF);
					;
					// println(proximity);
					// player.processProxReading(patch, step, touched,
					// proximity);
					processProxReading(new ProxData(player, patch, step,
							touched, proximity));
				} else
					System.err
							.println("Trouble in paradise, we received a packet from patch '"
									+ patch
									+ "', which is not assigned to a player");
			} else if (packet.length == XPan.ACCEL_IN_PACKET_LENGTH
					&& packet[0] == XPan.ACCEL_IN_PACKET_TYPE) {
				// TODO
				int patch = packet[1];
				int x = packet[2];
				int y = packet[3];
				int z = packet[4];
				int player = getPlayerIndexForPatch(patch);
				processAccelReading(new AccelData(player, patch, x, y, z));
			} else if (packet.length == XPan.CONFIG_ACK_LENGTH
					&& packet[0] == XPan.CONFIG_ACK_PACKET_TYPE) {
				int myTurnLength = ((packet[2] & 0xFF) << 8)
						| (packet[3] & 0xFF);
				int patch = packet[1];
				int player = getPlayerIndexForPatch(patch);
				players[player].processConfigAck(patch, myTurnLength);
				System.out.println("Config Ack Received in Level, Turn Length is "
						+ myTurnLength);
			} else if (packet.length == XPan.VIBE_IN_PACKET_LENGTH
					&& packet[0] == XPan.VIBE_IN_PACKET_TYPE) {
				if (packet[2] == 4)
					doPause();
			} else {
				System.err.println("Level received a bad packet.");
			}
		}
	}

	// TODO make this work with patch detection in LevelSelect
	public Player getPlayerForPatch(int patch) {
		if (patch >= 1 && patch <= 4)
			return players[0];
		else if (patch >= 9 && patch <= 16)
			return players[1];
		else
			return null;
	}

	public int getPlayerIndexForPatch(int patch) {
		if (patch >= 1 && patch <= 4)
			return 0;
		else if (patch >= 9 && patch <= 16)
			return 1;
		else
			return -1;
	}

	public void processProxReading(ProxData data) {
		// check if we are in coop mode
		if (isCcoop && (coopPts == 0 || getTotalPts() < coopPts * 2)) {
			for (int i = 0; i < players.length; i++)
				players[i].processProxReading(data.patch, data.step,
						data.touched, data.proximity);
			// println("Proximity reading: " + data + " (coop)");
		}
		// if not only need to process for incoming player
		else {
			players[data.player].processProxReading(data.patch, data.step,
					data.touched, data.proximity);
			// println("Proximity reading: " + data);
		}
	}

	public void processAccelReading(AccelData data) {
		// check if we are in coop mode
		if (isCcoop && (coopPts == 0 || getTotalPts() < coopPts * 2)) {
			for (int i = 0; i < players.length; i++)
				players[i].processAccelReading(data.patch, data.x, data.y,
						data.z);
			// println("Acceleration reading: " + data + " (coop)");
		}
		// if not only need to process for incoming player
		else {
			players[data.player].processAccelReading(data.patch, data.x,
					data.y, data.z);
			// println("Acceleration reading: " + data);
		}
	}

	public int getStepInterval() {
		return (int) stepInterval;
	}

	public void doPause() {
		if (!isDone() && isPaused())
			start();
		else if (!isDone())
			pause();
	}
}// end class

