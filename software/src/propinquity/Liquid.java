package propinquity;

import java.util.Vector;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PConstants;

public class Liquid {

	/** The strength of the acceleration acting on the particles. */
	public static final float GRAVITY_STRENGTH = 0.01f;
	
	/** The maximum allowable number of particles per player's liquid. */
	public static final int MAX_PARTICLES = 50;
	private static final int MERGE_COUNT = 3;	
	private static final int MERGE_VOLUME = 5;

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

	public void createParticle(Color color) {
		Color pColor = this.color;
		if(color != null) pColor = color;
		particlesCreated.add(new Particle(parent, new Vec2(parent.width/2f, parent.height/2f), pColor, Particle.SMALL_SIZE, true, true));
	}

	public void transferParticles() {
		for(Particle particle : particlesCreated) {
			Particle newParticle = new Particle(parent, particle.getPosition(), particle.getColor(), Particle.SMALL_SIZE, false, true);
			particlesHeld.add(newParticle);
			particle.kill();
		}

		particlesCreated = new Vector<Particle>();
	}
	
	private void mergeParticles() {
		for(int i = 0;i < MERGE_COUNT;i++) {
			Particle[] toMerge = new Particle[MERGE_VOLUME];
			int k = 0;
			for(Particle particle : particlesHeld) {
				if(particle.color.equals(this.color)) {
					toMerge[k] = particle;
					k++;
					if(k == toMerge.length) break;
				}
			}

			if(k < toMerge.length) break; //Insufficient particles to merge

			float avgX = 0, avgY = 0;
			
			for(int j = 0; j < MERGE_VOLUME; j++) {
				avgX += toMerge[j].getPosition().x;
				avgY += toMerge[j].getPosition().y;
				toMerge[j].kill();
				particlesHeld.remove(toMerge[j]);
			}
			
			particlesLarge.add(new Particle(parent, new Vec2(avgX / MERGE_VOLUME, avgY / MERGE_VOLUME), color, Particle.LARGE_SIZE, false, true));
		}
	}

	private void applyGravity() {
		
		int playerCount = parent.level.players.length;
		int playerIndex = 0;
		
		for(int i = 0; i < playerCount; i++) {
			if(color.equals(PlayerConstants.PLAYER_COLORS[i]))
				playerIndex = i;
		}
		
		float offset = (PConstants.PI + PConstants.TWO_PI * playerIndex) / playerCount;
		
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle + offset);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle + offset);
		Vec2 gravity = new Vec2(gravX, gravY);

		for(Particle particle : particlesCreated)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());

		for(Particle particle : particlesHeld)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());
		
		for(Particle particle : particlesLarge)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());
	}

	public void Update() {
		
		applyGravity();
		
		if(particlesCreated.size() + particlesHeld.size() > MAX_PARTICLES) {
			mergeParticles();
		}
	}
	
	public void draw() {
		for(Particle particle : particlesCreated)
			particle.draw();

		for(Particle particle : particlesHeld)
			particle.draw();
		
		for(Particle particle : particlesLarge)
			particle.draw();
	}
}
