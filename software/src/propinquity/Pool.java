package propinquity;

import java.util.Vector;

import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PGraphicsOpenGL;

public class Pool {
	
	private Propinquity parent;
	private GL gl;
	private Liquid liquid;
	private Color color;
	
	private float height;
	
	public Pool(Propinquity parent, Liquid liquid) {
		this.parent = parent;
		this.gl = ((PGraphicsOpenGL) parent.g).gl;
		this.liquid = liquid;
		
		color = liquid.color;
		height = 50;
	}
	
	public void reset() {
		height = 50;
	}
	
	public void update() {
		
		Vector<Particle> particles = liquid.particlesHeld;
		Vector<Particle> particlesToRemove = new Vector<Particle>();
		
		for (Particle p : particles) {
			float dist = PApplet.sq(p.position.x - parent.width / 2) + PApplet.sq(p.position.y - parent.height / 2);
			if (dist > PApplet.sq(Fences.OUTER_RADIUS - height)) {
				height += 2f;
				p.kill();
				particlesToRemove.add(p);
			}
		}
		
		for (Particle p : particlesToRemove)
			particles.remove(p);
	}
	
	public void draw() {
		
		parent.fill(color.toInt(parent));
		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		parent.translate(PApplet.cos(parent.hud.angle - liquid.angleOffset) * (Fences.OUTER_RADIUS),
				PApplet.sin(parent.hud.angle - liquid.angleOffset) * (Fences.OUTER_RADIUS));
		parent.rotate(parent.hud.angle + liquid.angleOffset - PConstants.HALF_PI);
		parent.rect(-350, 0, 700, height);
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
