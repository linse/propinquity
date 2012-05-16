package propinquity;

import java.util.Vector;
import java.lang.System;
import org.jbox2d.common.Vec2;
import processing.core.*;

public class PlayerSelect implements PConstants, UIElement {

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
			System.out.println("Warning too many player names passed to PlayerSelect, truncating names");
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
			createParticles(playerNames.length, players[state].getColor());
		} else if(state == playerNames.length) {
			killParticles();
			parent.changeGameState(GameState.LevelSelect);
		} else {
			System.out.println("Warning: Unknown Player Select State");
		}
	}

	void createParticles(int num, Color color) {
		int radius = parent.height/2 - Hud.WIDTH * 2;

		particles = new Particle[num];

		for(int i = 0; i < num; i++) {
			Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI/particles.length * i) * radius,
					PApplet.sin(PApplet.TWO_PI/particles.length * i) * radius), color, true);
			p.scale = 1f;
			particles[i] = p;
		}
	}

	void killParticles() {
		if(particles == null) return;
		for(Particle particle : particles) particle.kill();
	}

	void drawParticles() {
		if(particles == null) return;

		parent.pushMatrix();
		parent.translate(parent.width/2, parent.height/2);
		
		for(int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
		
		parent.popMatrix();
	}

	public void draw() {
		if(!isVisible) return;

		hud.drawInnerBoundary();
		hud.drawOuterBoundary();
		
		drawParticles();

		hud.drawCenterText("Select Player " + (state + 1), hud.getAngle());
		hud.drawBannerCenter(playerNames[selected], players[state].getColor(), PApplet.TWO_PI/playerNames.length * selected);
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
			if(state == 0) {
				killParticles();
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
		players[state].setName(playerNames[selected]);
		stateChange(state + 1);
	}
}
