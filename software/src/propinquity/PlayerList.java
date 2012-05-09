package propinquity;

import processing.core.*;
import java.util.*;
import controlP5.*;

public class PlayerList implements UIElement {

	final int MAX_PLAYERS = 2;
	final int MIN_PLAYERS = 2;

	final int WIDTH = 200;
	final int PLAYER_HEIGHT = 20;
	final int VERT_SPACER = 20;

	Propinquity parent;

	ControlP5 controlP5;

	String plistFile;

	String[] playerNames;
	Vector<Textfield> playerFields;

	Button nextButton;
	Button addButton;
	Button removeButton;

	boolean isVisible;

	public PlayerList(Propinquity parent) {
		this(parent, null);
	}

	public PlayerList(Propinquity parent, String plistFile) {
		this.parent = parent;
		this.plistFile = plistFile;

		isVisible = true;
		controlP5 = new ControlP5(parent);

		// create text fields for each
		playerNames = null;
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

	public String[] getNames() {
		return playerNames;
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

	public void process() {
		Vector<String> tmp_names = new Vector<String>();
		// clear empty textfields
		for(Textfield tf : playerFields) {
			if(PApplet.trim(tf.getText()).length() != 0) {
				tmp_names.add(tf.getText());
			}
		}

		playerNames = tmp_names.toArray(new String[0]);

		parent.saveStrings(parent.dataPath(plistFile), playerNames);

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

	public void keyPressed(int keycode) {
		if(isVisible && keycode == PConstants.ENTER) process();
	}
}
