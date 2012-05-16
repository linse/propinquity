package propinquity;

import java.util.Vector;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PConstants;

public class Liquid {

	/** The strength of the acceleration acting on the particles. */
	public static final float GRAVITY_STRENGTH = 0.01f;
	
	/** The maximum allowable number of particles per player's liquid. */
	public static final int MAX_PARTICLES = 250;

	private Vector<Particle> particlesCreated;
	private Vector<Particle> particlesHeld;
	private Vector<Particle> particlesLarge;

	Propinquity parent;
	Color color;

	public Liquid(Propinquity parent, Color color) {

		this.parent = parent;
		this.color = color;

		particlesCreated = new Vector<Particle>();
		particlesHeld = new Vector<Particle>();
		particlesLarge = new Vector<Particle>();
	}

	public void reset() {
		for(Particle particle : particlesCreated) particle.kill();
		particlesCreated = new Vector<Particle>();
		for(Particle particle : particlesHeld) particle.kill();
		particlesHeld = new Vector<Particle>();
		for(Particle particle : particlesLarge) particle.kill();
		particlesLarge = new Vector<Particle>();
	}

	public void createParticle() {
		particlesCreated.add(new Particle(parent, new Vec2(parent.width/2f, parent.height/2f),
				color, 0.5f, true));
	}

	public void transferParticles() {
		for(Particle particle : particlesCreated) {
			Particle newParticle = new Particle(parent, particle.position, particle.color, 0.5f, false);
			particlesHeld.add(newParticle);
			particle.kill();
		}

		particlesCreated = new Vector<Particle>();
	}
	
	private void MergeParticles() {
		
	}

	private void applyGravity() {
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle + PConstants.HALF_PI);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle + PConstants.HALF_PI);
		Vec2 gravity = new Vec2(gravX, gravY);

		for(Particle particle : particlesCreated)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());

		for(Particle particle : particlesHeld)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());
		
		for(Particle particle : particlesLarge)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());
	}

	private void applyReverseGravity() {
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle - PConstants.HALF_PI);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle - PConstants.HALF_PI);
		Vec2 antiGravity = new Vec2(gravX, gravY);

		for(Particle particle : particlesCreated)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());

		for(Particle particle : particlesHeld)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());
		
		for(Particle particle : particlesLarge)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());
	}

	public void Update() {
		
		if(color.equals(PlayerConstants.PLAYER_COLORS[1]))
			applyReverseGravity();
		else
			applyGravity();
		
		if (particlesCreated.size() + particlesHeld.size() > MAX_PARTICLES) {
			MergeParticles();
		}
	}
	
	public void draw() {
		for(Particle particle : particlesCreated)
			particle.draw();

		for(Particle particle : particlesHeld)
			particle.draw();
	}
}
