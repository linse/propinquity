package propinquity;

import java.util.ArrayList;

import javax.media.opengl.GL;

import org.jbox2d.testbed.TestSettings;

import pbox2d.PBox2D;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

public class Liquid {

	private Propinquity parent;

	Fences fences;

	PImage particleImage;

	ArrayList<Particle> particlesCreated;
	ArrayList<Particle> particlesHeld;

	public Liquid(Propinquity parent) {

		this.parent = parent;

		initBox2D();

		fences = new Fences(parent);

		particlesCreated = new ArrayList<Particle>();
		particlesHeld = new ArrayList<Particle>();

		particleImage = parent.graphics.loadParticle();
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
		//particlesCreated.add(new Particle(parent));
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
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_ZERO, GL.GL_ZERO);

		parent.noStroke();
		parent.noFill();

		for (Particle particle : particlesCreated)
			particle.draw();
		
		for (Particle particle : particlesHeld)
			particle.draw();
	}

}
