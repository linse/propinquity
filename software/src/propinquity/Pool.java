package propinquity;

import java.util.Vector;

import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PGraphicsOpenGL;

public class Pool {
	
	/** The starting height of the liquid's pool. */
	public static final float INITIAL_HEIGHT = 50f;
	
	private Propinquity parent;
	private GL gl;
	private Liquid liquid;
	private Color color;
	
	private float height;
	private float rising;
	
	public Pool(Propinquity parent, Liquid liquid) {
		this.parent = parent;
		this.gl = ((PGraphicsOpenGL) parent.g).gl;
		this.liquid = liquid;
		
		color = liquid.color;
		height = INITIAL_HEIGHT;
		rising = 0f;
	}
	
	public void reset() {
		height = INITIAL_HEIGHT;
		rising = 0f;
	}
	
	public void update() {
		
		if (rising > 0f) {
			float diff = rising / 5f;
			rising -= diff;
			height += diff;
		}
		
		Vector<Particle> particles = liquid.particlesHeld;
		Vector<Particle> particlesToRemove = new Vector<Particle>();
		
		for (Particle p : particles) {
			float dist = PApplet.sq(p.position.x - parent.width / 2) + PApplet.sq(p.position.y - parent.height / 2);
			if (dist > PApplet.sq(Fences.OUTER_RADIUS - height)) {
				rising += 2f;
				p.kill();
				particlesToRemove.add(p);
			}
		}
		
		for (Particle p : particlesToRemove)
			particles.remove(p);
	}
	
	public void draw() {
		
		float lineHeight = height + 9f;
		
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
		parent.beginShape();
		parent.curveVertex(-Fences.OUTER_RADIUS * 1.5f, lineHeight);
		parent.curveVertex(-Fences.OUTER_RADIUS, lineHeight - rising);
		parent.curveVertex(-Fences.OUTER_RADIUS / 2, lineHeight);
		parent.curveVertex(0, lineHeight - rising * 5);
		parent.curveVertex(Fences.OUTER_RADIUS / 2, lineHeight);
		parent.curveVertex(Fences.OUTER_RADIUS, lineHeight - rising);
		parent.curveVertex(Fences.OUTER_RADIUS * 1.5f, lineHeight);
		parent.endShape();
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
