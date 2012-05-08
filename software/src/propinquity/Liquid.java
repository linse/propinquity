package propinquity;

import java.util.ArrayList;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class Liquid {

	/** The strength of the acceleration acting on the particles. */
	public static final float GRAVITY_STRENGTH = 10;

	public ArrayList<Particle> particlesCreated;
	public ArrayList<Particle> particlesHeld;

	private Propinquity parent;
	private Colour colour;

	private PImage particleImage;
	private PGraphics pgParticle;

	public Liquid(Propinquity parent, Colour colour) {

		this.parent = parent;
		this.colour = colour;

		particlesCreated = new ArrayList<Particle>();
		particlesHeld = new ArrayList<Particle>();

		particleImage = parent.graphics.loadParticle();

		pgParticle = new PGraphics();
		pgParticle = parent.createGraphics(particleImage.width, particleImage.height, PApplet.P2D);
		pgParticle.background(particleImage);
		pgParticle.mask(particleImage);
	}

	public void reset() {
		particlesCreated = new ArrayList<Particle>();
		particlesHeld = new ArrayList<Particle>();
	}

	public void createParticle() {
		// TODO
		particlesCreated.add(new Particle(parent, new Vec2(parent.width / 2f, parent.height / 2f), pgParticle, colour));
	}

	public void transferParticles() {
		for (Particle particle : particlesCreated)
			particlesHeld.add(particle);

		particlesCreated = new ArrayList<Particle>();
	}

	public void update() {
		for (Particle particle : particlesCreated)
			particle.update();

		for (Particle particle : particlesHeld)
			particle.update();
	}

	public void draw() {
		for (Particle particle : particlesCreated)
			particle.draw();

		for (Particle particle : particlesHeld)
			particle.draw();
	}

}
