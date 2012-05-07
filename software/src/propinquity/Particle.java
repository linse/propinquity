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
	private float[][] textureVertices;
	
	private Propinquity parent;

	public Particle(Propinquity parent, Body body, Shape shape, float scale, PGraphics texture) {
		
		this.parent = parent;
		this.body = body;
		this.shape = shape;
		this.scale = scale;
		this.texture = texture;
		
		push = new Vec2(0, 0);
		
		textureVertices = new float[2][4];
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 4; j++)
				textureVertices[i][j] = 1.0f + parent.random(-0.4f, 0.4f);
	}

	public void draw() {
		
		Vec2 pos = parent.box2d.getBodyPixelCoord(body);
		
		parent.pushMatrix();
		parent.translate(pos.x, pos.y);
		parent.scale(scale * texture.width / 2f);
		parent.beginShape(PApplet.QUADS);
		parent.texture(texture);
		parent.vertex(-textureVertices[0][0], -textureVertices[1][0], 0, 0, 0);
		parent.vertex(textureVertices[0][1], -textureVertices[1][1], 1, 0);
		parent.vertex(textureVertices[0][2], textureVertices[1][2], 0, 1, 1);
		parent.vertex(-textureVertices[0][3], textureVertices[1][3], 0, 0, 1);
		parent.endShape(PApplet.CLOSE);
		parent.popMatrix();
	}
}
