package propinquity;

import org.jbox2d.common.Vec2;

import processing.core.*;

public class Particle {
	
	PVector position;
	Vec2 push;
	
	private float scale;
	private PGraphics texture;
	private Colour colour;
	
	private Propinquity parent;

	public Particle(Propinquity parent, PVector position, float scale, PGraphics texture, Colour colour) {
		
		this.parent = parent;
		this.position = position;
		this.scale = scale;
		this.texture = texture;
		this.colour = colour;
		
		push = new Vec2(0, 0);
	}

	public void update() {
		
	}
	
	public void draw() {
		
		parent.pushMatrix();
		parent.translate(position.x, position.y);
		parent.scale(scale * texture.width / 2f);
		parent.beginShape();
		parent.texture(texture);
		parent.tint(colour.toInt(parent));
		parent.vertex(-1, -1, 0, 0, 0);
		parent.vertex(1, -1, 0, 1, 0);
		parent.vertex(1, 1, 0, 1, 1);
		parent.vertex(-1, 1, 0, 0, 1);
		parent.noTint();
		parent.endShape();
		parent.popMatrix();
	}
}
