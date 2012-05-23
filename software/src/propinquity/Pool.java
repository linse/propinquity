package propinquity;

import java.util.Vector;

import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PGraphicsOpenGL;

public class Pool {

	/** The starting height of the liquid's pool. */
	public static final float INITIAL_HEIGHT = 50f;

	private static final int POINTS = 7;

	private Propinquity parent;
	private GL gl;
	private Liquid liquid;
	private Color color;

	private float height;
	private float rising;

	private float waveHit;
	private float[] randomHeights;
	private float[] offsets;
	private float time;

	public Pool(Propinquity parent, Liquid liquid) {
		this.parent = parent;
		this.gl = ((PGraphicsOpenGL) parent.g).gl;
		this.liquid = liquid;

		color = liquid.color;
		height = INITIAL_HEIGHT;
		rising = 0f;

		// Randomize the liquid's wave front.
		waveHit = 0f;
		time = 0f;
		offsets = new float[POINTS];
		randomHeights = new float[POINTS];
		for (int i = 0; i < POINTS; i++)
			offsets[i] = (float) (Math.random() * 8 - 4f);
	}

	public void reset() {
		height = INITIAL_HEIGHT;
		rising = 0f;

		time = 0f;
		for (int i = 0; i < POINTS; i++)
			offsets[i] = (float) (Math.random() * 8 - 4f);
	}

	public void update() {

		// Update random waves.
		time += 0.2f;
		for (int i = 0; i < POINTS; i++)
			randomHeights[i] += (float) (PApplet.sin(time + offsets[i]) / 4f);

		if (waveHit > 0f)
			waveHit /= 1.02f;

		if (rising > 0f) {
			float diff = rising / 5f;
			rising -= diff;
			height += diff;
		}

		// Detect if particles have entered the liquid.
		Vector<Particle> particles = liquid.particlesHeld;
		Vector<Particle> particlesToRemove = new Vector<Particle>();

		for (Particle p : particles) {
			float dist = PApplet.sq(p.position.x - parent.width / 2) + PApplet.sq(p.position.y - parent.height / 2);
			if (dist > PApplet.sq(Fences.OUTER_RADIUS - height)) {
				rising += 2f;
				waveHit += 1f;
				p.kill();
				particlesToRemove.add(p);
			}
		}

		for (Particle p : particlesToRemove)
			particles.remove(p);
	}

	public void draw() {

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBlendEquation(GL.GL_FUNC_ADD);

		parent.noFill();
		parent.smooth();
		parent.stroke(color.toInt(parent));
		parent.strokeWeight(10f);
		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		parent.translate(PApplet.cos(parent.hud.angle - liquid.angleOffset) * (Fences.OUTER_RADIUS),
				PApplet.sin(parent.hud.angle - liquid.angleOffset) * (Fences.OUTER_RADIUS));
		parent.rotate(parent.hud.angle + liquid.angleOffset - PConstants.HALF_PI);
		
		for (int i = 0; i < 4; i++) {
			float lineHeight = height + 14f - (8f * i);
			
			parent.beginShape();
			parent.curveVertex(-Fences.OUTER_RADIUS * 1.5f, lineHeight);
			
			parent.curveVertex(-Fences.OUTER_RADIUS, lineHeight + waveHit * randomHeights[0] - rising);
			parent.curveVertex(-Fences.OUTER_RADIUS * 0.666f, lineHeight + waveHit * randomHeights[1]);
			parent.curveVertex(-Fences.OUTER_RADIUS * 0.333f, lineHeight + waveHit * randomHeights[2] - rising);
			parent.curveVertex(0, lineHeight + waveHit * randomHeights[3] - rising * 3);
			parent.curveVertex(Fences.OUTER_RADIUS * 0.333f, lineHeight + waveHit * randomHeights[4] - rising);
			parent.curveVertex(Fences.OUTER_RADIUS * 0.666f, lineHeight + waveHit * randomHeights[5]);
			parent.curveVertex(Fences.OUTER_RADIUS, lineHeight + waveHit * randomHeights[6] - rising);
			
			parent.curveVertex(Fences.OUTER_RADIUS * 1.5f, lineHeight);
			parent.endShape();
		}
		parent.popMatrix();

		parent.fill(color.toInt(parent));
		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		parent.translate(PApplet.cos(parent.hud.angle - liquid.angleOffset) * (Fences.OUTER_RADIUS),
				PApplet.sin(parent.hud.angle - liquid.angleOffset) * (Fences.OUTER_RADIUS));
		parent.rotate(parent.hud.angle + liquid.angleOffset - PConstants.HALF_PI);
		parent.rect(-Fences.OUTER_RADIUS, 0, Fences.OUTER_RADIUS * 2, height);
		parent.popMatrix();

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_ALPHA);
		gl.glBlendEquation(GL.GL_FUNC_REVERSE_SUBTRACT);

		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		parent.scale(parent.width / 2, parent.height / 2);
		parent.beginShape(PConstants.QUADS);
		parent.texture(parent.hud.hudMask);
		parent.vertex(-1, -1, 0, 0, 0);
		parent.vertex(1, -1, 0, 1, 0);
		parent.vertex(1, 1, 0, 1, 1);
		parent.vertex(-1, 1, 0, 0, 1);
		parent.endShape(PConstants.CLOSE);
		parent.popMatrix();

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBlendEquation(GL.GL_FUNC_ADD);
	}
}
