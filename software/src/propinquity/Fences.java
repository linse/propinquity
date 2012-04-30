package propinquity;

import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

import processing.core.PConstants;
import processing.core.PApplet;

public class Fences {

	public static final int SECTIONS = 24;
	public static final int INNER_RADIUS = 100;
	public static final int OUTER_MASK = 0x8;
	public static final int INNER_MASK = 0x4;
	public static final int PLAYERS_MASK = 0x1 | 0x2;

	Propinquity parent;

	public Fences(Propinquity parent) {

		this.parent = parent;

		Body innerFence = null;
		{
			BodyDef bd = new BodyDef();
			bd.position.set(0.0f, 0.0f);
			innerFence = parent.box2d.createBody(bd);

			PolygonDef sd = new PolygonDef();
			// sd.filter.groupIndex = 1;
			sd.filter.categoryBits = INNER_MASK;
			sd.filter.maskBits = PLAYERS_MASK;

			float fenceDepth = 0.2f;
			float worldScale = parent.height / parent.WORLD_SIZE;
			float radius = INNER_RADIUS / worldScale + fenceDepth;
			float perimeter = 2 * PConstants.PI * radius;

			for (int i = 0; i < SECTIONS; i++) {
				float angle = 2 * PConstants.PI / SECTIONS * i;
				sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
						* radius), angle + PConstants.PI / 2);
				innerFence.createShape(sd);
			}
		}

		Body outerFence = null;
		{
			BodyDef bd = new BodyDef();
			bd.position.set(0.0f, 0.0f);
			outerFence = parent.box2d.createBody(bd);

			PolygonDef sd = new PolygonDef();
			// sd.filter.groupIndex = 1;
			sd.filter.categoryBits = OUTER_MASK;
			sd.filter.maskBits = PLAYERS_MASK;

			float fenceDepth = 0.2f;
			float worldScale = parent.height / parent.WORLD_SIZE;
			float radius = (parent.WORLD_SIZE - (Hud.WIDTH / worldScale)) / 2f + fenceDepth / 2;
			float perimeter = 2 * PConstants.PI * radius;

			for (int i = 0; i < SECTIONS; i++) {
				float angle = 2 * PConstants.PI / SECTIONS * i;
				sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
						* radius), angle + PConstants.PI / 2);
				outerFence.createShape(sd);
			}
		}
	}

	public void drawDebugFence() {
		parent.noFill();
		parent.stroke(0, 255, 0);
		parent.strokeWeight(1);

		parent.rectMode(PApplet.CENTER);
		float radius = parent.height / 2 - Hud.WIDTH;
		float perimeter = 2 * PConstants.PI * radius;
		float w = perimeter / SECTIONS;
		float h = 5f;
		float angle = 0;
		for (int i = 0; i < SECTIONS; i++) {
			angle = 2f * PConstants.PI / SECTIONS * i;
			parent.pushMatrix();
			parent.translate(parent.width / 2 + PApplet.cos(angle) * radius, parent.height / 2 + PApplet.sin(angle)
					* radius);
			parent.rotate(angle + PConstants.PI / 2);
			parent.rect(0, 0, w, h);
			parent.popMatrix();
		}

		radius = INNER_RADIUS;
		perimeter = 2 * PConstants.PI * radius;
		w = perimeter / SECTIONS;
		h = 5f;
		angle = 0;
		for (int i = 0; i < SECTIONS; i++) {
			angle = 2f * PConstants.PI / SECTIONS * i;
			parent.pushMatrix();
			parent.translate(parent.width / 2 + PApplet.cos(angle) * radius, parent.height / 2 + PApplet.sin(angle)
					* radius);
			parent.rotate(angle + PConstants.PI / 2);
			parent.rect(0, 0, w, h);
			parent.popMatrix();
		}
	}

}
