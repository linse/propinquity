package propinquity;

import processing.core.*;
import propinquity.hardware.*;
import java.util.*;
import controlP5.*;

public class PlayerList implements PlayerConstants, UIElement {

	final int WIDTH = 200;
	final int PLAYER_HEIGHT = 20;
	final int VERT_SPACER = 20;

	Propinquity parent;

	HardwareInterface hardware;

	Player[] players;
	Vector<Textfield> playerFields;

	String plistFile;

	ControlP5 controlP5;

	Button nextButton;
	Button addButton;
	Button removeButton;

	boolean isVisible;

	public PlayerList(Propinquity parent, HardwareInterface hardware) {
		this(parent, hardware, null);
	}

	public PlayerList(Propinquity parent, HardwareInterface hardware, String plistFile) {
		this.parent = parent;
		this.hardware = hardware;
		this.plistFile = plistFile;

		players = new Player[0];

		isVisible = true;
		controlP5 = new ControlP5(parent);

		// create text fields for each
		playerFields = new Vector<Textfield>();

		// create button to add new players
		addButton = controlP5.addButton("ADD", 0, parent.width / 2 - WIDTH / 2, parent.height / 2, 50, 20);
		removeButton = controlP5.addButton("REMOVE", 0, parent.width / 2 - WIDTH / 2 + WIDTH / 3, parent.height / 2, 50, 20);

		// create next button
		nextButton = controlP5.addButton("NEXT", 0, parent.width / 2 - WIDTH / 2 + 2 * WIDTH / 3, parent.height / 2, 50, 20);

		// load the player list
		// playerList = new Vector();
		String[] xml_lines = parent.loadStrings(plistFile);
		if(xml_lines != null) {
			for(int i = 0; i < xml_lines.length; i++) {
				if(PApplet.trim(xml_lines[i]).length() > 0) {
					addPlayer(xml_lines[i]);
				}
			}
		}

		if(playerFields.size() < 1) addPlayer("Player 1");
		if(playerFields.size() < 2) addPlayer("Player 2");
	}

	public Player[] getPlayers() {
		return players;
	}

	public void draw() {

	}

	void addPlayer(String name) {
		// add a new text field to the list
		Textfield tf = controlP5.addTextfield("Player " + playerFields.size(), parent.width / 2 - WIDTH / 2, parent.height / 2, WIDTH, PLAYER_HEIGHT);
		tf.setAutoClear(false);
		tf.setText(name);
		tf.setCaptionLabel("Player " + (playerFields.size() + 1));
		tf.setFocus(true);

		playerFields.add(tf);

		layout();
	}

	void removePlayer() {
		Textfield tf = playerFields.remove(playerFields.size()-1);
		controlP5.remove(tf.name());
		layout();
	}

	public void layout() {
		float y = parent.height / 2 - (PLAYER_HEIGHT + VERT_SPACER) * (playerFields.size() + 1) / 2;

		// move existing player fields up
		for(int i = 0; i < playerFields.size();i++) {
			Textfield tf = playerFields.get(i);
			tf.setPosition(tf.position().x, y);

			y += PLAYER_HEIGHT + VERT_SPACER;
		}

		y += VERT_SPACER;

		// move buttons down
		addButton.setPosition(addButton.position().x, y);
		removeButton.setPosition(removeButton.position().x, y);
		nextButton.setPosition(nextButton.position().x, y);

		if(playerFields.size() >= MAX_PLAYERS) addButton.hide();
		else addButton.show();

		if(playerFields.size() <= MIN_PLAYERS) removeButton.hide();
		else removeButton.show();
	}

	public void reset() {
		while(playerFields.size() > 0) removePlayer();
		addPlayer("Player 1");
		addPlayer("Player 2");
		process();
	}

	public void process() {
		System.out.println("a");
		for(Player player : players) {
			hardware.removeGlove(player.getGlove());
			for(Patch patch : player.getPatches()) {
				hardware.removePatch(patch);
			}

			player.reset();
		}

		System.out.println("b");
		players = new Player[playerFields.size()];
		String[] names = new String[playerFields.size()];

		System.out.println("c");

		for(int i = 0;i < playerFields.size();i++) {
			names[i] = PApplet.trim(playerFields.get(i).getText());
			System.out.println("1");
			Patch[] patches;

			System.out.println("2");
			if(i < PATCH_ADDR.length) {
				patches = new Patch[PATCH_ADDR[i].length];
				for(int j = 0;j < PATCH_ADDR[i].length;j++) patches[j] = new Patch(PATCH_ADDR[i][j], hardware);
			} else {
				patches = new Patch[] { new Patch(-1, hardware) };
			}

			System.out.println("3");
			Glove glove;

			if(i < GLOVE_ADDR.length) {
				glove = new Glove(GLOVE_ADDR[i], hardware);
			} else {
				glove = new Glove(-1, hardware);
			}

			System.out.println("4");
			Color color;

			if(i < PLAYER_COLORS.length) {
				color = PLAYER_COLORS[i];
			} else {
				color = NEUTRAL_COLOR;
			}
			System.out.println("5");

			players[i] = new Player(parent, names[i], color, patches, glove);
		}
		System.out.println("6");

		parent.saveStrings(parent.dataPath(plistFile), names);
		System.out.println("7");

		parent.changeGameState(GameState.LevelSelect);
	}

	public void show() {
		isVisible = true;
		controlP5.show();
	}

	public void hide() {
		isVisible = false;
		controlP5.hide();
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void controlEvent(ControlEvent theEvent) {
		if(isVisible) {
			String name = theEvent.controller().name();

			if(name.equals("NEXT")) {
				process();
			} else if(name.equals("ADD")) {
				addPlayer("Player "+(playerFields.size()+1));
			} else if(name.equals("REMOVE")) {
				removePlayer();
			}
		}
	}

	/**
	 * Receive a keyPressed event.
	 * 
	 * @param key the char of the keyPressed event.
	 * @param keycode the keycode of the keyPressed event.
	 */
	public void keyPressed(char key, int keycode) {
		if(isVisible && (key == PConstants.ENTER || key == ' ')) process();
	}
}
