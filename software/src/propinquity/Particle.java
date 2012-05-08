package propinquity;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import processing.core.*;

public class Particle {
	
	Body body;
	Shape shape;
	Vec2 push;
	
	private float scale;
	private PGraphics texture;
	
	private Propinquity parent;

	public Particle(Propinquity parent, Body body, Shape shape, float scale, PGraphics texture) {
		
		this.parent = parent;
		this.body = body;
		this.shape = shape;
		this.scale = scale;
		this.texture = texture;
		
		push = new Vec2(0, 0);
	}

	public void update() {
		
	}
	
	public void draw() {
		
		Vec2 pos = parent.box2d.getBodyPixelCoord(body);
		
		parent.pushMatrix();
		parent.translate(pos.x, pos.y);
		parent.scale(scale * texture.width / 2f);
		parent.beginShape(PApplet.SQUARE);
		parent.texture(texture);
		parent.vertex(-1, -1);
		parent.vertex(1, -1);
		parent.vertex(1, 1);
		parent.vertex(-1, 1);
		parent.endShape(PApplet.CLOSE);
		parent.popMatrix();
	}
}
