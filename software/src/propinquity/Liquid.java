package propinquity;

import java.util.Vector;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * The Liquid class provides a mechanism to create and manage a group of particles affect by a gravity which are intented to simulate a "liquid". Each instance of the liquid class supports it's own "independant" gravity. In addition it manages transfering particles between the inner "temporary" zone where they are held during rounds and the outer zone which hold the overall liquid.
 *
 */
public class Liquid {

	/** The strength of the acceleration acting on the particles. */
	public static final float GRAVITY_STRENGTH = 0.01f;
	public static final float BUMP_STRENGTH = 0.01f;
	
	/** The maximum allowable number of particles per player's */
	public static final int MAX_PARTICLES = 40;
	private static final int MERGE_COUNT = 4;	
	private static final int MERGE_VOLUME = 3;

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
		particlesCreated.add(new Particle(parent, parent.getOffscreen(), new Vec2(parent.width/2f, parent.height/2f), pColor, Particle.SMALL_SIZE, true));
	}

	public void transferParticles() {
		for(Particle particle : particlesCreated) {
			Particle newParticle = new Particle(parent, parent.getOffscreen(), particle.getPosition(), particle.getColor(), Particle.SMALL_SIZE, false);
			particlesHeld.add(newParticle);
			particle.kill();
		}

		particlesCreated = new Vector<Particle>();
	}
	
	private void mergeParticles() {
		for(int i = 0;i < MERGE_COUNT;i++) {
			Particle[] toMerge = new Particle[MERGE_VOLUME];
			int k = 0;

			Particle firstParticle = particlesHeld.get(0);

			for(Particle particle : particlesHeld) {
				if(particle.color.equals(firstParticle.color)) {
					toMerge[k] = particle;
					k++;
					if(k == toMerge.length) break;
				}
			}

			if(k < toMerge.length) {
				toMerge = new Particle[MERGE_VOLUME];
				k = 0;

				boolean coopDone = false;
				if(firstParticle.color.equals(PlayerConstants.NEUTRAL_COLOR)) coopDone = true;

				for(Particle particle : particlesHeld) {
					if((coopDone && particle.color.equals(this.color)) || (!coopDone && particle.color.equals(PlayerConstants.NEUTRAL_COLOR))) {
						toMerge[k] = particle;
						k++;
						if(k == toMerge.length) break;
					}
				}
			}

			if(k < toMerge.length) return; //Insufficient particles to merge

			float avgX = 0, avgY = 0;
			
			for(int j = 0; j < MERGE_VOLUME; j++) {
				avgX += toMerge[j].getPosition().x;
				avgY += toMerge[j].getPosition().y;
				toMerge[j].kill();
				particlesHeld.remove(toMerge[j]);
			}
			
			particlesLarge.add(new Particle(parent, parent.getOffscreen(), new Vec2(avgX / MERGE_VOLUME, avgY / MERGE_VOLUME), toMerge[0].color, Particle.LARGE_SIZE, false));
		}
	}

	public void bump() {
		for(Particle particle : particlesCreated) {
			float angle = parent.random(0, parent.TWO_PI);
			float bumpX = BUMP_STRENGTH * PApplet.cos(angle);
			float bumpY = BUMP_STRENGTH * PApplet.sin(angle);
			Vec2 bump = new Vec2(bumpX, bumpY);
			particle.getBody().applyForce(bump, particle.getBody().getWorldCenter());
		}

		for(Particle particle : particlesHeld) {
			float angle = parent.random(0, parent.TWO_PI);
			float bumpX = BUMP_STRENGTH * PApplet.cos(angle);
			float bumpY = BUMP_STRENGTH * PApplet.sin(angle);
			Vec2 bump = new Vec2(bumpX, bumpY);
			particle.getBody().applyForce(bump, particle.getBody().getWorldCenter());
		}
		
		for(Particle particle : particlesLarge) {
			float angle = parent.random(0, parent.TWO_PI);
			float bumpX = BUMP_STRENGTH * PApplet.cos(angle);
			float bumpY = BUMP_STRENGTH * PApplet.sin(angle);
			Vec2 bump = new Vec2(bumpX, bumpY);
			particle.getBody().applyForce(bump, particle.getBody().getWorldCenter());
		}
	}

	private void applyGravity() {
		int playerCount = parent.level.players.length;
		int playerIndex = 0; //FIXME: There should be a standard way of determining what player we are dealing with.
		
		for(int i = 0; i < playerCount; i++) {
			if(color.equals(PlayerConstants.PLAYER_COLORS[i]))
				playerIndex = i;
		}
		
		float offset = (PConstants.PI + PConstants.TWO_PI * playerIndex) / playerCount;
		
		float gravX = GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle + offset);
		float gravY = GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle + offset);
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
		
		if(particlesHeld.size() > MAX_PARTICLES) {
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
