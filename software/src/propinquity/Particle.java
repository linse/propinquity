package propinquity;

import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

import processing.core.*;

public class Particle {

	/**
	 * The minimum amount of time (in ms) a player must have their glove in the
	 * sweet spot to get a single point.
	 */
	public static final int SPAWN_DELAY = 500;

	public Vec2 position;
	public float scale;
	public Color color;

	Body body;
	CircleDef shape;

	PGraphics texture;

	private Propinquity parent;

	public Particle(Propinquity parent, Vec2 position, Color color, boolean isNew) {
		this.parent = parent;
		this.position = position;
		this.color = color;

		PImage imgParticle = parent.loadImage("data/particles/particle.png");
		texture = new PGraphics();
		texture = parent.createGraphics(imgParticle.width, imgParticle.height, PApplet.P2D);
		texture.background(imgParticle);
		texture.mask(imgParticle);

		scale = 0.5f;

		shape = new CircleDef();
		shape.radius = parent.box2d.scalarPixelsToWorld((texture.width - 22) * scale / 2f);
		shape.density = 1.0f;
		shape.friction = 0.01f;
		shape.restitution = 0.3f;
		if (isNew) {
			shape.filter.categoryBits = Fences.CAT_NEW;
			shape.filter.maskBits = Fences.MASK_NEW;
		} else {
			shape.filter.categoryBits = Fences.CAT_OLD;
			shape.filter.maskBits = Fences.MASK_OLD;
		}

		BodyDef bd = new BodyDef();
		bd.position.set(parent.box2d.coordPixelsToWorld(position));

		body = parent.box2d.createBody(bd);
		body.createShape(shape);
		body.setMassFromShapes();
	}

	public void kill() {
		parent.box2d.destroyBody(body);
	}

	public Body getBody() {
		return body;
	}

	public CircleDef getCircleDef() {
		return shape;
	}

	public void update() {

	}

	public void draw() {
		position = parent.box2d.getBodyPixelCoord(body);

		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		parent.translate(position.x, position.y);
		parent.scale(scale * texture.width / 2f);
		parent.beginShape();
		parent.texture(texture);
		parent.tint(color.toInt(parent));
		parent.vertex(-1, -1, 0, 0, 0);
		parent.vertex(1, -1, 0, 1, 0);
		parent.vertex(1, 1, 0, 1, 1);
		parent.vertex(-1, 1, 0, 0, 1);
		parent.noTint();
		parent.endShape();
		parent.popMatrix();
	}
}
