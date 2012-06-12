package propinquity;

import processing.core.*;
import java.util.*;
import controlP5.*;

public class PlayerList implements PlayerConstants, UIElement {

	final int WIDTH = 200;
	final int PLAYER_HEIGHT = 20;
	final int VERT_SPACER = 20;

	Propinquity parent;

	String plistFile;
	String[] playerNames;

	Vector<Textfield> playerFields;

	ControlP5 controlP5;

	Button nextButton;
	Button addButton;
	Button removeButton;

	boolean isVisible;


	public PlayerList(Propinquity parent, String plistFile) {
		this.parent = parent;
		this.plistFile = plistFile;

		isVisible = true;

		controlP5 = new ControlP5(parent);

		// create button to add new players
		addButton = controlP5.addButton("ADD", 0, parent.width/2 - WIDTH/2, parent.height/2, 50, 20);
		removeButton = controlP5.addButton("REMOVE", 0, parent.width/2 - WIDTH/2 + WIDTH/3, parent.height/2, 50, 20);

		// create next button
		nextButton = controlP5.addButton("NEXT", 0, parent.width/2 - WIDTH/2 + 2 * WIDTH/3, parent.height/2, 50, 20);

		playerFields = new Vector<Textfield>();

		reset();

		hide();
	}

	public String[] getPlayerNames() {
		return playerNames;
	}

	public void draw() {

	}

	void addPlayer(String name) {
		// add a new text field to the list
		Textfield tf = controlP5.addTextfield("Player " + playerFields.size(), parent.width/2 - WIDTH/2, parent.height/2, WIDTH, PLAYER_HEIGHT);
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
		float y = parent.height/2 - (PLAYER_HEIGHT + VERT_SPACER) * (playerFields.size() + 1)/2;

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
		//I tried to lock the text field to no avail. I give up on actually using controlP5
		while(playerFields.size() > 0) removePlayer();

		// load the player list
		String[] plistLines = parent.loadStrings(plistFile);
		if(plistLines != null) {
			for(int i = 0; i < plistLines.length; i++) {
				if(PApplet.trim(plistLines[i]).length() > 0) {
					addPlayer(plistLines[i]);
				}
			}
		}

		if(playerFields.size() < 1) addPlayer("Player 1");
		if(playerFields.size() < 2) addPlayer("Player 2");

		playerNames = new String[playerFields.size()];
		for(int i = 0;i < playerFields.size();i++) playerNames[i] = playerFields.get(i).getText();
	}

	public void process() {
		playerNames = new String[playerFields.size()];

		for(int i = 0;i < playerFields.size();i++) {
			playerNames[i] = PApplet.trim(playerFields.get(i).getText());
		}

		parent.saveStrings(parent.dataPath(plistFile), playerNames);

		parent.changeGameState(GameState.PlayerSelect);
	}

	public void show() {
		isVisible = true;
		for(Textfield tf : playerFields) tf.unlock();
		controlP5.show();
	}

	public void hide() {
		isVisible = false;
		for(Textfield tf : playerFields) tf.lock();
		controlP5.hide();
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void controlEvent(ControlEvent theEvent) {
		if(!isVisible) return;
		String name = theEvent.controller().name();

		if(name.equals("NEXT")) {
			process();
		} else if(name.equals("ADD")) {
			addPlayer("Player "+(playerFields.size()+1));
		} else if(name.equals("REMOVE")) {
			removePlayer();
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
