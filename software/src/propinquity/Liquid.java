package propinquity;

import java.util.ArrayList;

import org.jbox2d.testbed.TestSettings;

import pbox2d.PBox2D;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class Liquid {

	public ArrayList<Particle> particlesCreated;
	public ArrayList<Particle> particlesHeld;
	
	private Propinquity parent;
	private Colour colour;
	private Fences fences;

	private PImage particleImage;
	private PGraphics pgParticle;

	public Liquid(Propinquity parent, Colour colour) {

		this.parent = parent;
		this.colour = colour;

		initBox2D();

		fences = new Fences(parent);

		particlesCreated = new ArrayList<Particle>();
		particlesHeld = new ArrayList<Particle>();

		particleImage = parent.graphics.loadParticle();
		
		pgParticle = new PGraphics();
		pgParticle = parent.createGraphics(particleImage.width, particleImage.height, PApplet.P2D);
		pgParticle.background(particleImage);
		pgParticle.mask(particleImage);
	}

	private void initBox2D() {
		// initialize box2d physics and create the world
		float worldSize = Propinquity.WORLD_SIZE;
		parent.box2d = new PBox2D(parent, (float) parent.height / worldSize);
		parent.box2d.createWorld(-worldSize / 2f, -worldSize / 2f, worldSize, worldSize);
		parent.box2d.setGravity(0.0f, 0.0f);

		// load default jbox2d settings
		parent.settings = new TestSettings();
	}

	public void reset() {
		particlesCreated = new ArrayList<Particle>();
		particlesHeld = new ArrayList<Particle>();
	}
	
	public void createParticle() {
		// TODO
		particlesCreated.add(new Particle(parent, new PVector(0, 0), 1f, pgParticle, colour));
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
