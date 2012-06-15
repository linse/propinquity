package propinquity;

import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

import processing.core.*;
import codeanticode.glgraphics.*;

/**
 * Represents a particle with a scale, color and position. As well particles must be specified to draw to arbitrary offscreen canavases (used to apply opengl effects) and can be specified to addhere to different sets of fences.
 *
 */
public class Particle {

	public static final float SMALL_SIZE = 0.5f;
	public static final float LARGE_SIZE = 1f;

	public static final float METABALL_OVERSIZE_FACTOR = 1.5f;
	
	Vec2 position;
	Color color;

	float scale;
	Body body;
	CircleDef shape;
	PGraphics texture;

	Propinquity parent;
	GLGraphicsOffScreen canvas;

	public Particle(Propinquity parent, GLGraphicsOffScreen canvas, Vec2 position, Color color, float scale, boolean isNew) {
		this.parent = parent;
		this.position = position;
		this.color = color;
		this.scale = scale;

		this.canvas = canvas;

		PImage imgParticle = parent.loadImage("data/particles/softparticle.png");
		texture = new PGraphics();
		texture = parent.createGraphics(imgParticle.width, imgParticle.height, PApplet.P2D);
		texture.background(imgParticle);
		// texture.mask(imgParticle);

		shape = new CircleDef();
		shape.radius = parent.box2d.scalarPixelsToWorld((texture.width - 22) * scale/2f);
		shape.density = 1.0f;
		shape.friction = 0.01f;
		shape.restitution = 0.3f;
		if(isNew) {
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

	public float getScale() {
		return scale;
	}

	public Color getColor() {
		return color;
	}

	public Vec2 getPosition() {
		return position;
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

	public void draw() {
		position = parent.box2d.getBodyPixelCoord(body);

		canvas.beginDraw();

		canvas.pushMatrix();
		canvas.translate(position.x, position.y);
		canvas.scale(METABALL_OVERSIZE_FACTOR * scale * texture.width/2f);
		canvas.tint(color.toInt(parent));
		canvas.image(texture, -1, -1, 2, 2);
		canvas.noTint();
		canvas.popMatrix();

		canvas.endDraw();
	}
}
