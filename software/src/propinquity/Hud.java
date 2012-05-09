package propinquity;

import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import processing.opengl.PGraphicsOpenGL;

/**
 * 
 * 
 * @author Stephane Beniak
 */
public class Hud {

	// HUD constants
	public static final int FONT_SIZE = 32;
	public static final int WIDTH = 50;
	public static final int OFFSET = 4;
	public static final int SCORE_RADIUS_OFFSET = 40;
	public static final float SCORE_ANGLE_OFFSET = 0.35f;
	public static final float SCORE_ROT_SPEED = 0.0001f;
	public static final float PROMPT_ROT_SPEED = 0.002f;
	public static final int BOUNDARY_WIDTH = 5;

	Propinquity parent;
	Sounds sounds;
	Graphics graphics;
	PGraphics hudMask;

	float angle = 0;
	float velocity = -PConstants.TWO_PI / 500f;

	boolean isSnapped = false;

	/**
	 * 
	 * 
	 * @param parent
	 */
	public Hud(Propinquity parent, Sounds sounds, Graphics graphics) {
		this.parent = parent;
		this.sounds = sounds;
		this.graphics = graphics;

		hudMask = parent.createGraphics(parent.width, parent.height, PConstants.P2D);
		hudMask.background(0);
		hudMask.beginDraw();
		hudMask.noStroke();
		hudMask.fill(255);
		hudMask.ellipse(parent.width / 2, parent.height / 2, parent.height - Hud.WIDTH * 2 + BOUNDARY_WIDTH, parent.height - Hud.WIDTH * 2 + BOUNDARY_WIDTH);
		hudMask.endDraw();
	}

	/**
	 * Return the HUD's current angle.
	 * 
	 * @return The HUD's current angle in radians.
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Reset the HUD to its default angle and velocity.
	 */
	public void reset() {
		isSnapped = false;
		velocity = -PConstants.TWO_PI / 500f;
	}

	/**
	 * 
	 */
	public void snap() {

		if(!isSnapped) {
			angle -= ((int) (angle / PConstants.TWO_PI)) * PConstants.TWO_PI;
			isSnapped = true;
		}

		update(PConstants.TWO_PI, PConstants.TWO_PI / 500f, PConstants.TWO_PI / 10f);
	}

	/**
	 * 
	 * 
	 * @param targetAngle
	 * @param targetAcceleration
	 * @param maxVelocity
	 */
	public void update(float targetAngle, float targetAcceleration, float maxVelocity) {

		float diff = targetAngle - angle;
		int dir = diff < 0 ? -1 : 1;

		if(diff * dir < PConstants.TWO_PI / 5000f) {
			angle = targetAngle;
			return;
		}

		velocity += dir * targetAcceleration;
		if(velocity / dir > maxVelocity)
			velocity = dir * maxVelocity;

		velocity *= 0.85;

		angle += velocity;
	}

	/**
	 * 
	 */
	public void draw() {
		// TODO: Fix this
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.noStroke();
		parent.noFill();

		if(parent.level.isCoop() && !parent.level.isCoopDone()) {

			float ang = angle - PConstants.HALF_PI;
			parent.pushMatrix();
			parent.translate(parent.width / 2 + PApplet.cos(ang) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET),
					parent.height / 2 + PApplet.sin(ang) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
			parent.rotate(ang + PConstants.HALF_PI);
			parent.scale(graphics.hudCoop.width / 2, graphics.hudCoop.height / 2);
			parent.beginShape(PConstants.QUADS);
			parent.texture(graphics.hudCoop);
			parent.vertex(-1, -1, 0, 0, 0);
			parent.vertex(1, -1, 0, 1, 0);
			parent.vertex(1, 1, 0, 1, 1);
			parent.vertex(-1, 1, 0, 0, 1);
			parent.endShape(PConstants.CLOSE);
			parent.popMatrix();
			parent.pushMatrix();
			parent.translate(parent.width / 2, parent.height / 2);
			parent.fill(255);
			parent.noStroke();
			parent.textAlign(PConstants.CENTER, PConstants.BASELINE);
			parent.textFont(Graphics.font, Hud.FONT_SIZE);
			String score = String.valueOf(parent.level.getTotalPoints() / 2);
			String name = "Coop";
			while(parent.textWidth(score + name) < 240)
				name += ' ';

			Text.drawArc(name + score, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, ang - Hud.SCORE_ANGLE_OFFSET,
					parent);

			parent.popMatrix();

		} else {

			if(!parent.level.getLastCoopDone()) {
				sounds.complete.play();
				sounds.complete.rewind();
				parent.level.setLastCoopDone(true);
			}
			for(int i = 0; i < parent.level.getNumberOfPlayers(); i++) {
				Player player = parent.level.getPlayer(i);
				player.approachHudTo(-PConstants.HALF_PI + PConstants.TWO_PI / parent.level.getNumberOfPlayers() * i);
				float ang = angle - PConstants.HALF_PI + player.hudAngle;
				parent.pushMatrix();
				parent.translate(parent.width / 2 + PApplet.cos(ang) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET),
						parent.height / 2 + PApplet.sin(ang) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
				parent.rotate(ang + PConstants.HALF_PI);
				parent.scale(graphics.hudPlayers[i].width / 2, graphics.hudPlayers[i].height / 2);
				parent.beginShape(PConstants.QUADS);
				parent.texture(graphics.hudPlayers[i]);
				parent.vertex(-1, -1, 0, 0, 0);
				parent.vertex(1, -1, 0, 1, 0);
				parent.vertex(1, 1, 0, 1, 1);
				parent.vertex(-1, 1, 0, 0, 1);
				parent.endShape(PConstants.CLOSE);
				parent.popMatrix();
				parent.pushMatrix();
				parent.translate(parent.width / 2, parent.height / 2);
				parent.fill(255);
				parent.noStroke();
				parent.textAlign(PConstants.CENTER, PConstants.BASELINE);
				parent.textFont(Graphics.font, Hud.FONT_SIZE);
				String score = String.valueOf(player.getTotalPts());
				String name = player.getName().length() > 12 ? player.getName().substring(0, 12) : player.getName();
				while(parent.textWidth(score + name) < 240)
					name += ' ';

				Text.drawArc(name + score, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, ang - Hud.SCORE_ANGLE_OFFSET,
						parent);

				parent.popMatrix();
			}
		}
	}

}
