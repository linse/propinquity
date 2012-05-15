package propinquity;

import pbox2d.*;

import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

import processing.core.PConstants;
import processing.core.PApplet;

/**
 * Pertains to the inner and outer fences of the Propinquity game world.
 * 
 * @author Stephane Beniak
 */
public class Fences {

	/**
	 * This denotes the number of sections into which the fence circle will be broken up to form a polygon.
	 */
	public static final int SECTIONS = 24;
	public static final int INNER_RADIUS = 100;
	public static final int OUTER_RADIUS = 350;
	
	public static final short CAT_NEW = 0x0001;
	public static final short CAT_OLD = 0x0002;
	public static final short CAT_INNER = 0x0004;
	public static final short CAT_OUTER = 0x0008;
	
	public static final short MASK_NEW = CAT_NEW | CAT_OLD | CAT_INNER | CAT_OUTER;
	public static final short MASK_OLD = CAT_NEW | CAT_OLD | CAT_OUTER;
	public static final short MASK_INNER = CAT_NEW;
	public static final short MASK_OUTER = CAT_NEW | CAT_OLD;

	Propinquity parent;
	PBox2D box2d;
	Body innerFence, outerFence;

	public Fences(Propinquity parent, PBox2D box2d) {
		this.parent = parent;
		this.box2d = box2d;
		
		createInnerFence();
		createOuterFence();
	}
	
	void createInnerFence() {
		BodyDef bd = new BodyDef();
		bd.position.set(0.0f, 0.0f);
		
		PolygonDef sd = new PolygonDef();
		sd.filter.categoryBits = CAT_INNER;
		sd.filter.maskBits = MASK_INNER;

		float fenceDepth = 0.05f;
		float worldScale = parent.height / parent.worldSize;
		float radius = INNER_RADIUS / worldScale + fenceDepth / 2 + 0.0125f;
		float perimeter = 2 * PConstants.PI * radius;

		innerFence = box2d.createBody(bd);
		
		for(int i = 0; i < SECTIONS; i++) {
			float angle = 2 * PConstants.PI / SECTIONS * i;
			sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
					* radius), angle + PConstants.PI / 2);
			innerFence.createShape(sd);
		}
	}
	
	void createOuterFence() {
		BodyDef bd = new BodyDef();
		bd.position.set(0.0f, 0.0f);

		PolygonDef sd = new PolygonDef();
		sd.filter.categoryBits = CAT_OUTER;
		sd.filter.maskBits = MASK_OUTER;

		float fenceDepth = 0.05f;
		float worldScale = parent.height / parent.worldSize;
		float radius = OUTER_RADIUS / worldScale + fenceDepth / 2;
		float perimeter = 2 * PConstants.PI * radius;

		outerFence = box2d.createBody(bd);
		
		for(int i = 0; i < SECTIONS; i++) {
			float angle = 2 * PConstants.PI / SECTIONS * i;
			sd.setAsBox(perimeter / SECTIONS, fenceDepth, new Vec2(PApplet.cos(angle) * radius, PApplet.sin(angle)
					* radius), angle + PConstants.PI / 2);
			outerFence.createShape(sd);
		}
	}

}
