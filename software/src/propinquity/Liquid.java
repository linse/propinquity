package propinquity;

import java.util.Vector;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PConstants;

public class Liquid {

	/** The strength of the acceleration acting on the particles. */
	public static final float GRAVITY_STRENGTH = 0.01f;

	public Vector<Particle> particlesCreated;
	public Vector<Particle> particlesHeld;

	Propinquity parent;
	Color color;

	public Liquid(Propinquity parent, Color color) {

		this.parent = parent;
		this.color = color;

		particlesCreated = new Vector<Particle>();
		particlesHeld = new Vector<Particle>();
	}

	public void reset() {
		for(Particle particle : particlesCreated) particle.kill();
		particlesCreated = new Vector<Particle>();
		for(Particle particle : particlesHeld) particle.kill();
		particlesHeld = new Vector<Particle>();
	}

	public void createParticle() {
		particlesCreated.add(new Particle(parent, new Vec2(parent.width / 2f, parent.height / 2f),
				color, true));
	}

	public void transferParticles() {
		for(Particle particle : particlesCreated) {
			Particle newParticle = new Particle(parent, particle.position, particle.color, false);
			particlesHeld.add(newParticle);
			particle.kill();
		}

		particlesCreated = new Vector<Particle>();
	}

	public void applyGravity() {
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle + PConstants.HALF_PI);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle + PConstants.HALF_PI);
		Vec2 gravity = new Vec2(gravX, gravY);

		for(Particle particle : particlesCreated)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());

		for(Particle particle : particlesHeld)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());
	}

	public void applyReverseGravity() {
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle - PConstants.HALF_PI);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle - PConstants.HALF_PI);
		Vec2 antiGravity = new Vec2(gravX, gravY);

		for(Particle particle : particlesCreated)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());

		for(Particle particle : particlesHeld)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());
	}

	public void draw() {
		for(Particle particle : particlesCreated)
			particle.draw();

		for(Particle particle : particlesHeld)
			particle.draw();
	}
}
