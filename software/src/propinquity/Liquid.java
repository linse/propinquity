package propinquity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.media.opengl.GL;

import org.jbox2d.collision.FilterData;
import org.jbox2d.collision.MassData;
import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.testbed.TestSettings;

import pbox2d.PBox2D;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

public class Liquid {

	public static final float PARTICLE_SCALE = 0.8f;
	public static final float PARTICLE_SCALE_RANGE = 0.5f;
	public static final int TEXTURE_HALF = 32;
	public static final int AVG_PTS_PER_STEP = 250;
	public static final int AVG_PARTICLE_PER_STEP = 50;
	public static final int APPROX_MAX_PARTICLES = 1600;
	public static final int MAX_PARTICLES_PER_FRAME = 5;
	public static final Integer LIQUID_MAGIC = new Integer(12345);
	public static final float EMITTER_RADIUS = 0.14f;
	public static final int SHADOW_X = 8;
	public static final int SHADOW_Y = 8;
	public static final float MIN_RELEASE_FORCE = 0.4f;
	public static final float MAX_RELEASE_FORCE = 0.6f;
	public static final float EMITTER_ANGULAR_VELOCITY = 4 * PConstants.TWO_PI;
	public static final float PUSH_PERIOD_ROT_SPEED = 1f;
	public static final float PUSH_DAMPENING = 0.98f;

	Propinquity parent;

	Fences fences;

	// Liquid parameters
	ArrayList<Integer>[][] hash;
	int hashWidth, hashHeight;
	float totalMass = 100.0f;
	float particleRadius = 0.11f;
	float particleViscosity = 0.005f;
	float damping = 0.7f;
	float fluidMinX = -Propinquity.WORLD_SIZE / 2f;
	float fluidMaxX = Propinquity.WORLD_SIZE / 2f;
	float fluidMinY = -Propinquity.WORLD_SIZE / 2f;
	float fluidMaxY = Propinquity.WORLD_SIZE / 2f;

	// Particles
	int ptsPerParticle = 0;
	LinkedList<Particle>[] particles;
	int numStepsPerPeriod;
	Particle[] lastPeriodParticle;
	boolean groupedParticles = false;
	int lastPeriodStep = 0;

	// Particle graphics
	PImage[] imgParticle;
	PImage imgShadow;
	PGraphics[] pgParticle;

	@SuppressWarnings("unchecked")
	// TODO: Fix this madness
	public Liquid(Propinquity parent) {

		this.parent = parent;

		initBox2D();
		initTextures();

		// create the boundary fences
		fences = new Fences(parent);

		// init hash to space sort particles
		hashWidth = 40;
		hashHeight = 40;
		hash = new ArrayList[hashHeight][hashWidth];
		for (int i = 0; i < hashHeight; ++i) {
			for (int j = 0; j < hashWidth; ++j) {
				hash[i][j] = new ArrayList<Integer>();
			}
		}

		// init particles
		particles = new LinkedList[parent.level.getNumPlayers()];
		for (int i = 0; i < particles.length; i++)
			particles[i] = new LinkedList<Particle>();

		ptsPerParticle = (parent.level.getNumSteps() * AVG_PTS_PER_STEP * parent.level.getNumPlayers())
				/ APPROX_MAX_PARTICLES;
		// pCount = new int[level.getNumPlayers()];
		numStepsPerPeriod = PApplet.round(AVG_PARTICLE_PER_STEP * ptsPerParticle / AVG_PTS_PER_STEP);
		if (numStepsPerPeriod == 0)
			++numStepsPerPeriod;
		lastPeriodParticle = new Particle[parent.level.getNumPlayers()];

		System.out.println("Points per particle: " + ptsPerParticle);
	}

	void initBox2D() {
		// initialize box2d physics and create the world
		parent.box2d = new PBox2D(parent, (float) parent.height / Propinquity.WORLD_SIZE);
		parent.box2d.createWorld(-Propinquity.WORLD_SIZE / 2f, -Propinquity.WORLD_SIZE / 2f, Propinquity.WORLD_SIZE,
				Propinquity.WORLD_SIZE);
		parent.box2d.setGravity(0.0f, 0.0f);

		// load default jbox2d settings
		parent.settings = new TestSettings();
	}

	void initTextures() {

		imgParticle = parent.graphics.loadParticles();
		imgShadow = parent.graphics.loadParticleShadow();

		pgParticle = new PGraphics[parent.level.getNumPlayers()];

		for (int i = 0; i < parent.level.getNumPlayers(); i++) {
			pgParticle[i] = parent.createGraphics(imgParticle[i].width, imgParticle[i].height, PConstants.P2D);
			pgParticle[i].background(imgParticle[i]);
			pgParticle[i].mask(imgParticle[i]);
		}
	}

	void updateParticles() {
		for (int i = 0; i < parent.level.getNumPlayers(); i++)
			updateParticles(i);
	}

	void updateParticles(int p) {
		Player player = parent.level.getPlayer(p);
		int nParticles;

		// release particles if the player has accumulated period pts
		nParticles = PApplet.min(player.getPeriodPts() / ptsPerParticle, MAX_PARTICLES_PER_FRAME);
		if (nParticles > 0)
			releaseParticles(p, nParticles);

		// kill particles if the player touched
		nParticles = PApplet.min(player.getKillPts() / ptsPerParticle, MAX_PARTICLES_PER_FRAME);
		if (nParticles > 0) {
			killParticles(p, nParticles);
		}
	}

	void releaseParticles(int p, int nParticles) {
		Player player = parent.level.getPlayer(p);

		float releaseAngle = parent.level.getTime() * Hud.SCORE_ROT_SPEED / EMITTER_ANGULAR_VELOCITY;
		if (p % 2 == 1)
			releaseAngle += PConstants.PI;

		float massPerParticle = totalMass / APPROX_MAX_PARTICLES;

		CircleDef pd = new CircleDef();
		pd.filter.categoryBits = p + 1;
		pd.filter.maskBits = Fences.INNER_MASK | Fences.OUTER_MASK | Fences.PLAYERS_MASK;
		pd.filter.groupIndex = -(p + 1);
		pd.density = 1.0f;
		pd.radius = 0.040f;
		pd.restitution = 0.1f;
		pd.friction = 0.0f;

		for (int i = 0; i < nParticles; ++i) {
			BodyDef bd = new BodyDef();
			bd.position = new Vec2(PApplet.cos(releaseAngle) * (EMITTER_RADIUS * parent.random(0.8f, 1)),
					PApplet.sin(releaseAngle) * (EMITTER_RADIUS * parent.random(0.8f, 1)));
			// bd.position = new Vec2(cx, cy);
			bd.fixedRotation = true;
			Body b = parent.box2d.createBody(bd);
			Shape sh = b.createShape(pd);
			sh.setUserData(LIQUID_MAGIC);
			MassData md = new MassData();
			md.mass = massPerParticle;
			md.I = 1.0f;
			b.setMass(md);
			b.allowSleeping(false);

			particles[p].add(new Particle(parent, b, sh, PARTICLE_SCALE
					* parent.random(1.0f - PARTICLE_SCALE_RANGE, 1.0f), pgParticle[p]));
		}

		// keep track of the released particles
		player.subPeriodPts(nParticles * ptsPerParticle);
	}

	void killParticles(int p, int nParticles) {
		// get player
		Player player = parent.level.getPlayer(p);

		// clear kill pts
		player.subKillPts(nParticles * ptsPerParticle);

		Particle particle;
		boolean killedLastPeriodParticle = false;
		while (nParticles > 0 && particles[p].size() > 0) {
			particle = particles[p].removeFirst();
			if (particle == lastPeriodParticle[p])
				killedLastPeriodParticle = true;
			parent.box2d.destroyBody(particle.body);
			nParticles--;
		}

		// adjust the last period particle push in case
		// we killed some particles that were within the
		// inner fence. if we don't do that the new particles
		// will get trapped in.
		if (killedLastPeriodParticle) {
			if (particles[p].isEmpty())
				lastPeriodParticle[p] = null;
			else
				lastPeriodParticle[p] = particles[p].getLast();
		}
	}

	void liquify() {
		for (int i = 0; i < parent.level.getNumPlayers(); i++)
			liquify(i);
	}

	void liquify(int p) {
		float dt = 1.0f / parent.settings.hz;

		hashLocations(p);
		applyLiquidConstraint(p, dt);
		dampenLiquid(p);
	}

	void hashLocations(int p) {
		for (int a = 0; a < hashWidth; a++) {
			for (int b = 0; b < hashHeight; b++) {
				hash[a][b].clear();
			}
		}

		Particle particle;

		int i = 0;
		ListIterator<Particle> it = particles[p].listIterator();
		while (it.hasNext()) {
			particle = it.next();
			int hcell = hashX(particle.body.m_sweep.c.x);
			int vcell = hashY(particle.body.m_sweep.c.y);
			if (hcell > -1 && hcell < hashWidth && vcell > -1 && vcell < hashHeight)
				hash[hcell][vcell].add(new Integer(i));
			i++;
		}
	}

	int hashX(float x) {
		float f = PApplet.map(x, fluidMinX, fluidMaxX, 0, hashWidth - .001f);
		return (int) f;
	}

	int hashY(float y) {
		float f = PApplet.map(y, fluidMinY, fluidMaxY, 0, hashHeight - .001f);
		return (int) f;
	}

	void applyLiquidConstraint(int p, float deltaT) {
		//
		// Unfortunately, this simulation method is not actually scale
		// invariant, and it breaks down for rad < ~3 or so. So we need
		// to scale everything to an ideal rad and then scale it back after.
		//
		final float idealRad = 50.0f;
		float multiplier = idealRad / particleRadius;

		int count = particles[p].size();
		float[] xchange = new float[count];
		float[] ychange = new float[count];
		Arrays.fill(xchange, 0.0f);
		Arrays.fill(ychange, 0.0f);

		float[] xs = new float[count];
		float[] ys = new float[count];
		float[] vxs = new float[count];
		float[] vys = new float[count];

		Particle particle;
		ListIterator<Particle> it;

		it = particles[p].listIterator();
		int i = 0;
		while (it.hasNext()) {
			particle = it.next();
			xs[i] = multiplier * particle.body.m_sweep.c.x;
			ys[i] = multiplier * particle.body.m_sweep.c.y;
			vxs[i] = multiplier * particle.body.m_linearVelocity.x;
			vys[i] = multiplier * particle.body.m_linearVelocity.y;
			i++;
		}

		it = particles[p].listIterator();
		i = 0;
		while (it.hasNext()) {
			particle = it.next();
			// Populate the neighbor list from the 9 proximate cells
			ArrayList<Integer> neighbors = new ArrayList<Integer>();
			int hcell = hashX(particle.body.m_sweep.c.x);
			int vcell = hashY(particle.body.m_sweep.c.y);
			for (int nx = -1; nx < 2; nx++) {
				for (int ny = -1; ny < 2; ny++) {
					int xc = hcell + nx;
					int yc = vcell + ny;
					if (xc > -1 && xc < hashWidth && yc > -1 && yc < hashHeight && hash[xc][yc].size() > 0) {
						for (int a = 0; a < hash[xc][yc].size(); a++) {
							Integer ne = hash[xc][yc].get(a);
							if (ne != null && ne.intValue() != i)
								neighbors.add(ne);
						}
					}
				}
			}

			// Particle pressure calculated by particle proximity
			// Pressures = 0 iff all particles within range are idealRad
			// distance away
			float[] vlen = new float[neighbors.size()];
			float pres = 0.0f;
			float pnear = 0.0f;
			for (int a = 0; a < neighbors.size(); a++) {
				Integer n = neighbors.get(a);
				int j = n.intValue();
				float vx = xs[j] - xs[i];
				float vy = ys[j] - ys[i];

				// early exit check
				if (vx > -idealRad && vx < idealRad && vy > -idealRad && vy < idealRad) {
					float vlensqr = (vx * vx + vy * vy);
					// within idealRad check
					if (vlensqr < idealRad * idealRad) {
						vlen[a] = (float) Math.sqrt(vlensqr);
						if (vlen[a] < Settings.EPSILON)
							vlen[a] = idealRad - .01f;
						float oneminusq = 1.0f - (vlen[a] / idealRad);
						pres = (pres + oneminusq * oneminusq);
						pnear = (pnear + oneminusq * oneminusq * oneminusq);
					} else {
						vlen[a] = Float.MAX_VALUE;
					}
				}
			}

			// Now actually apply the forces
			// System.out.println(p);
			float pressure = (pres - 5F) / 2.0F; // normal pressure term
			float presnear = pnear / 2.0F; // near particles term
			float changex = 0.0F;
			float changey = 0.0F;
			for (int a = 0; a < neighbors.size(); a++) {
				Integer n = neighbors.get(a);
				int j = n.intValue();
				float vx = xs[j] - xs[i];
				float vy = ys[j] - ys[i];

				if (vx > -idealRad && vx < idealRad && vy > -idealRad && vy < idealRad) {
					if (vlen[a] < idealRad) {
						float q = vlen[a] / idealRad;
						float oneminusq = 1.0f - q;
						float factor = oneminusq * (pressure + presnear * oneminusq) / (2.0F * vlen[a]);
						float dx = vx * factor;
						float dy = vy * factor;
						float relvx = vxs[j] - vxs[i];
						float relvy = vys[j] - vys[i];
						factor = particleViscosity * oneminusq * deltaT;
						dx -= relvx * factor;
						dy -= relvy * factor;

						xchange[j] += dx;
						ychange[j] += dy;
						changex -= dx;
						changey -= dy;
					}
				}
			}

			xchange[i] += changex;
			ychange[i] += changey;
			i++;
		}
		// multiplier *= deltaT;
		it = particles[p].listIterator();
		i = 0;
		while (it.hasNext()) {
			particle = it.next();
			particle.body.m_xf.position.x += xchange[i] / multiplier;
			particle.body.m_xf.position.y += ychange[i] / multiplier;
			particle.body.m_linearVelocity.x += xchange[i] / (multiplier * deltaT);
			particle.body.m_linearVelocity.y += ychange[i] / (multiplier * deltaT);
			i++;
		}
	}

	void dampenLiquid(int p) {
		Particle particle;
		ListIterator<Particle> it = particles[p].listIterator();
		while (it.hasNext()) {
			particle = it.next();
			particle.body.setLinearVelocity(particle.body.getLinearVelocity().mul(damping));
		}
	}

	void resetLiquid() {
		for (int p = 0; p < parent.level.getNumPlayers(); p++) {
			Particle particle;
			ListIterator<Particle> it = particles[p].listIterator();
			while (it.hasNext()) {
				particle = it.next();
				parent.box2d.destroyBody(particle.body);
				it.remove();
			}
		}
	}

	void pushPeriod() {
		pushPeriod(false);
	}

	void pushPeriod(boolean override) {
		int cStep = parent.level.getCurrentStep();

		// go through particles
		// apply the force from the previous push
		for (int p = 0; p < parent.level.getNumPlayers(); p++) {

			Particle particle = null;
			ListIterator<Particle> it = particles[p].listIterator();
			while (it.hasNext() && particle != lastPeriodParticle[p]) {
				particle = it.next();

				particle.body.m_linearVelocity.x += particle.push.x;
				particle.body.m_linearVelocity.y += particle.push.y;
				particle.push.x *= PUSH_DAMPENING;
				particle.push.y *= PUSH_DAMPENING;
			}
			// println("last period step " + lastPeriodStep);
			// println("num steps per period: " + numStepsPerPeriod);

			if (!override && (lastPeriodStep == cStep || cStep % numStepsPerPeriod != 0))
				continue;

			// go through particles
			// remove collision with inner fence
			// and apply push outward
			FilterData filter = new FilterData();
			filter.groupIndex = -(p + 1);
			filter.categoryBits = p + 1;
			filter.maskBits = Fences.OUTER_MASK | Fences.PLAYERS_MASK;

			float angle = parent.level.getTime() * PUSH_PERIOD_ROT_SPEED + PConstants.TWO_PI
					/ parent.level.getNumPlayers() * p;
			float force = parent.random(Liquid.MIN_RELEASE_FORCE, Liquid.MAX_RELEASE_FORCE);

			while (it.hasNext()) {
				particle = it.next();

				particle.shape.setFilterData(filter);
				parent.box2d.world.refilter(particle.shape);

				particle.push.x -= PApplet.cos(angle) * force;
				particle.push.y -= PApplet.sin(angle) * force;
			}

			lastPeriodParticle[p] = particle;
		}

		lastPeriodStep = cStep;
	}

	void groupParticles() {
		Particle particle;

		if (!groupedParticles) {
			for (int p = 0; p < parent.level.getNumPlayers(); p++) {
				FilterData filter = new FilterData();
				filter.groupIndex = -1;
				filter.categoryBits = p + 1;
				filter.maskBits = Fences.OUTER_MASK;

				ListIterator<Particle> it = particles[p].listIterator();
				while (it.hasNext()) {
					particle = it.next();
					particle.shape.setFilterData(filter);
					parent.box2d.world.refilter(particle.shape);
				}
			}

			groupedParticles = true;
		}

		for (int p = 0; p < parent.level.getNumPlayers(); p++) {
			ListIterator<Particle> it = particles[p].listIterator();
			while (it.hasNext()) {
				particle = it.next();

				Body b = particle.body;

				// if (p == 0 && pos.x < width/2)
				if (p == 0)
					b.m_linearVelocity.x += 0.20f;
				else if (p == 1)
					b.m_linearVelocity.x -= 0.20f;
			}
		}
	}

	void draw() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);

		for (int i = 0; i < parent.level.getNumPlayers(); i++)
			drawParticles(i);
	}

	void drawParticles(int p) {
		// draw balls
		parent.noStroke();
		parent.noFill();

		ListIterator<Particle> it = particles[p].listIterator();
		while (it.hasNext())
			(it.next()).draw();
	}

}
