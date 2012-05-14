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
	
	public static final short CAT_NEW = 0x0001;
	public static final short CAT_OLD = 0x0002;
	public static final short CAT_INNER = 0x0004;
	public static final short CAT_OUTER = 0x0008;
	
	public static final short MASK_NEW = CAT_NEW | CAT_OLD | CAT_INNER | CAT_OUTER;
	public static final short MASK_OLD = CAT_NEW | CAT_OLD | CAT_OUTER;
	public static final short MASK_INNER = CAT_NEW;
	public static final short MASK_OUTER = CAT_NEW | CAT_OLD;

	Propinquity parent;
	
	Body innerFence, outerFence;

	public Fences(Propinquity parent) {

		this.parent = parent;
		
		CreateInnerFence();
		CreateOuterFence();
	}
	
	private void CreateInnerFence() {
		BodyDef bd = new BodyDef();
		bd.position.set(0.0f, 0.0f);
		
		PolygonDef sd = new PolygonDef();
		sd.filter.categoryBits = CAT_INNER;
		sd.filter.maskBits = MASK_INNER;

		float fenceDepth = 0.2f;
		float worldScale = parent.height / parent.worldSize;
		float radius = INNER_RADIUS / worldScale + fenceDepth;
		float perimeter = 2 * PConstants.PI * radius;

		innerFence = parent.box2d.createBody(bd);
		
		for(int i = 0; i < SECTIONS; i++) {
			float angle = 2 * PConstants.PI / SECTIONS * i;
			sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
					* radius), angle + PConstants.PI / 2);
			innerFence.createShape(sd);
		}
	}
	
	private void CreateOuterFence() {
		BodyDef bd = new BodyDef();
		bd.position.set(0.0f, 0.0f);

		PolygonDef sd = new PolygonDef();
		sd.filter.categoryBits = CAT_OUTER;
		sd.filter.maskBits = MASK_OUTER;

		float fenceDepth = 0.2f;
		float worldScale = parent.height / parent.worldSize;
		float radius = (parent.worldSize - (Hud.WIDTH / worldScale)) / 2f + fenceDepth / 2;
		float perimeter = 2 * PConstants.PI * radius;

		outerFence = parent.box2d.createBody(bd);
		
		for(int i = 0; i < SECTIONS; i++) {
			float angle = 2 * PConstants.PI / SECTIONS * i;
			sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
					* radius), angle + PConstants.PI / 2);
			outerFence.createShape(sd);
		}
	}

}
