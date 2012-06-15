package propinquity;

import java.lang.System;
import org.jbox2d.common.Vec2;
import processing.core.*;
import propinquity.hardware.*;

/**
 * The player select draws a circular GUI which lets each player select his/her color. All the colors are displayed as dots around a ring. By using the arrow keys, each player can select the color of the hardware he/she is wearing. This color is then illiminated before the next player is made to select his/her color.
 *
 */
public class PlayerSelect implements UIElement {

	Propinquity parent;

	Hud hud;

	int state, selected;

	String[] playerNames;
	Player[] players;

	Particle[] particles;

	boolean isVisible;

	public PlayerSelect(Propinquity parent, Hud hud, Player[] players) {
		this.parent = parent;
		this.hud = hud;
		this.players = players;

		isVisible = false;
	}

	public void setPlayerNames(String[] playerNames) {
		this.playerNames = playerNames;
		if(playerNames.length > players.length) {
			System.err.println("Warning: More player names passed to PlayerSelect than there are players, truncating names. Maybe you should define more players in PlayerConstants.java");
			this.playerNames = new String[players.length];
			System.arraycopy(playerNames, 0, this.playerNames, 0, players.length);
		}
	}

	public void reset() {
		for(Player player : players) player.setName(null);
		stateChange(0);
	}

	void stateChange(int state) {
		this.state = state;
		selected = 0;

		if(state < playerNames.length) {
			while(nameTaken(playerNames[selected])) selected = (selected + 1) % playerNames.length;
			for(Patch patch : players[state].getPatches()) {
				patch.setActive(true);
				patch.setColor(players[state].getColor());
			}
			createParticles(playerNames.length, players[state].getColor());
		} else if(state == playerNames.length) {
			parent.changeGameState(GameState.LevelSelect);
		} else {
			System.err.println("Warning: Unknown Player Select State");
		}
	}

	void createParticles(int num, Color color) {
		int radius = parent.height/2 - Hud.WIDTH * 2;

		particles = new Particle[num];

		for(int i = 0; i < num; i++) {
			Particle p = new Particle(parent, parent.getOffscreen(), new Vec2(parent.width/2+PApplet.cos(PApplet.TWO_PI/particles.length * i) * radius,
					parent.height/2+PApplet.sin(PApplet.TWO_PI/particles.length * i) * radius), color, Particle.LARGE_SIZE, true);
			particles[i] = p;
		}
	}

	void cleanup() {
		for(Player player : players) {
			for(Patch patch : player.getPatches()) {
				// patch.clear();
				patch.setActive(false);
			}
		}

		if(particles != null) {
			for(Particle particle : particles) particle.kill();
		}
	}

	void drawParticles() {
		if(particles == null) return;
		
		for(int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
	}

	public void draw() {
		if(!isVisible) return;

		hud.drawInnerBoundary();
		hud.drawOuterBoundary();
		
		drawParticles();

		hud.drawCenterText("Select Player " + (state + 1), hud.getAngle());
		hud.drawBannerCenter(playerNames[selected], players[PApplet.constrain(state, 0, players.length)].getColor(), PApplet.TWO_PI/playerNames.length * selected);
	}

	public void show() {
		isVisible = true;
	}

	public void hide() {
		isVisible = false;
	}

	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Receive a keyPressed event.
	 * 
	 * @param key the char of the keyPressed event.
	 * @param keycode the keycode of the keyPressed event.
	 */
	public void keyPressed(char key, int keycode) {
		switch(keycode) {
		case BACKSPACE: {
			cleanup();

			if(state == 0) {
				parent.changeGameState(GameState.PlayerList);
			} else {
				reset();
			}
			break;
		}
		case LEFT: {
			left();
			break;
		}
		case RIGHT: {
			right();
			break;
		}
		case ENTER:
		case ' ': {
			select();
			break;
		}
		}
	}

	boolean nameTaken(String name) {
		for(Player player : players) {
			if(player.getName() == playerNames[selected])
				return true; // Use == not .equals
		}
		return false;
	}

	public void left() {
		do {
			selected = (selected + playerNames.length - 1) % playerNames.length;
		} while(nameTaken(playerNames[selected]));
	}

	public void right() {
		do {
			selected = (selected + 1) % playerNames.length;
		} while(nameTaken(playerNames[selected]));
	}

	public void select() {
		players[PApplet.constrain(state, 0, players.length)].setName(playerNames[selected]);
		cleanup();
		stateChange(state + 1);
	}
}
