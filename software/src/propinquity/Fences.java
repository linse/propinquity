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
			float worldScale = parent.height / Propinquity.WORLD_SIZE;
			float radius = INNER_RADIUS / worldScale + fenceDepth;
			float perimeter = 2 * PConstants.PI * radius;

			for(int i = 0; i < SECTIONS; i++) {
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
			float worldScale = parent.height / Propinquity.WORLD_SIZE;
			float radius = (Propinquity.WORLD_SIZE - (Hud.WIDTH / worldScale)) / 2f + fenceDepth / 2;
			float perimeter = 2 * PConstants.PI * radius;

			for(int i = 0; i < SECTIONS; i++) {
				float angle = 2 * PConstants.PI / SECTIONS * i;
				sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
						* radius), angle + PConstants.PI / 2);
				outerFence.createShape(sd);
			}
		}
	}

}
