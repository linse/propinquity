package propinquity;

import java.util.Vector;

import org.jbox2d.common.Vec2;

import processing.core.*;

public class LevelSelect implements PConstants, UIElement {

	Propinquity parent;

	Hud hud;

	int selected;
	Level[] levels;

	Particle[] particles;

	boolean isVisible;

	public LevelSelect(Propinquity parent, Hud hud, Level[] levels) {
		this.parent = parent;
		this.hud = hud;
		this.levels = levels;

		isVisible = false;
	}

	public Level getSelectedLevel() {
		return levels[selected];
	}

	public void reset() {
		selected = 0;
		createParticles(levels.length, PlayerConstants.NEUTRAL_COLOR);
	}

	void createParticles(int num, Color color) {
		int radius = parent.height / 2 - Hud.WIDTH * 2;

		particles = new Particle[num];

		for (int i = 0; i < num; i++) {
			Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius,
					PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), color, true);
			p.scale = 1f;
			particles[i] = p;
		}
	}

	void killParticles() {
		if (particles == null) return;
		for (Particle particle : particles) particle.kill();
	}

	void drawParticles() {
		if(particles == null) return;

		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		
		for (int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
		
		parent.popMatrix();
	}

	public void draw() {
		if (!isVisible) return;

		hud.drawInnerBoundary();
		hud.drawOuterBoundary();
		
		drawParticles();

		hud.drawCenterText("Select Song", hud.getAngle());
		hud.drawBannerCenter(levels[selected].getName(), PlayerConstants.NEUTRAL_COLOR, PApplet.TWO_PI/levels.length*selected);
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
		switch (keycode) {
		case BACKSPACE: {
			killParticles();
			parent.changeGameState(GameState.PlayerSelect);
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

	public void left() {
		selected = (selected + levels.length - 1) % levels.length;
	}

	public void right() {
		selected = (selected + 1) % levels.length;
	}

	public void select() {
		killParticles();
		parent.changeGameState(GameState.Play);
	}
}
