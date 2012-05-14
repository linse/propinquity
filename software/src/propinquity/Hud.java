package propinquity;

import javax.media.opengl.GL;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;

/**
 * 
 * 
 * @author Stephane Beniak
 */
public class Hud {

	// HUD constants
	public static final int FONT_SIZE = 30;
	public static final int WIDTH = 50;
	public static final int OFFSET = 4;
	public static final int SCORE_RADIUS_OFFSET = 40;
	public static final float SCORE_ANGLE_OFFSET = 0.35f;
	public static final float SCORE_ROT_SPEED = 0.0001f;
	public static final float PROMPT_ROT_SPEED = 0.002f;
	public static final int BOUNDARY_WIDTH = 5;

	Propinquity parent;
	Sounds sounds;

	float angle = 0;
	float velocity = -PConstants.TWO_PI / 500f;

	boolean isSnapped = false;

	public PFont font;

	public PImage hudInnerBoundary, hudOuterBoundary;
	public PImage hudPlay, hudLevelComplete, hudPlayAgain;
	public PImage hudBannerSide, hudBannerCenter;
	public PImage hudPlayers[], hudCoop;

	/**
	 * 
	 * 
	 * @param parent
	 */
	public Hud(Propinquity parent, Sounds sounds) {
		this.parent = parent;
		this.sounds = sounds;

		font = parent.loadFont("hud/Calibri-Bold-32.vlw");

		hudInnerBoundary = parent.loadImage("hud/innerBoundary.png");
		hudOuterBoundary = parent.loadImage("hud/outerBoundary.png");

		hudBannerCenter = parent.loadImage("hud/bannercenter.png");
		hudBannerSide = parent.loadImage("hud/bannerside.png");

		hudPlay = parent.loadImage("hud/sbtoplay.png");
		hudLevelComplete = parent.loadImage("hud/levelcomplete.png");
		hudPlayAgain = parent.loadImage("hud/sbtoplayagain.png");

		hudPlayers = new PImage[2];
		for(int i = 0; i < 2; i++) //TODO this is going out, should be gone
			hudPlayers[i] = parent.loadImage("data/hud/player" + (i + 1) + "score.png");

		// Load co-op HUD
		hudCoop = parent.loadImage("data/hud/level.png");

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
			parent.scale(hudCoop.width / 2, hudCoop.height / 2);
			parent.beginShape(PConstants.QUADS);
			parent.texture(hudCoop);
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
			parent.textFont(font, Hud.FONT_SIZE);
			String score = String.valueOf(parent.level.getTotalPoints() / 2);
			String name = "Coop";
			while(parent.textWidth(score + name) < 240)
				name += ' ';

			drawArc(name + score, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, ang - Hud.SCORE_ANGLE_OFFSET);

			parent.popMatrix();

		} else {

			if(!parent.level.getLastCoopDone()) {
				sounds.complete.play();
				sounds.complete.rewind();
				parent.level.setLastCoopDone(true);
			}
			for(int i = 0; i < parent.players.length; i++) {
				Player player = parent.level.getPlayer(i);
				player.approachHudTo(-PConstants.HALF_PI + PConstants.TWO_PI / parent.players.length * i);
				float ang = angle - PConstants.HALF_PI + player.hudAngle;
				parent.pushMatrix();
				parent.translate(parent.width / 2 + PApplet.cos(ang) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET),
						parent.height / 2 + PApplet.sin(ang) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
				parent.rotate(ang + PConstants.HALF_PI);
				parent.scale(hudPlayers[i].width / 2, hudPlayers[i].height / 2);
				parent.beginShape(PConstants.QUADS);
				parent.texture(hudPlayers[i]);
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
				parent.textFont(font, Hud.FONT_SIZE);
				String score = String.valueOf(player.score.getScore());
				String name = player.getName().length() > 12 ? player.getName().substring(0, 12) : player.getName();
				while(parent.textWidth(score + name) < 240)
					name += ' ';

				drawArc(name + score, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, ang - Hud.SCORE_ANGLE_OFFSET);

				parent.popMatrix();
			}
		}
	}

	/**
	 * 
	 * @param message
	 * @param radius
	 * @param startAngle
	 * @param parent
	 */
	public void drawArc(String message, float radius, float startAngle) {
		// We must keep track of our position along the curve
		float arclength = 0;

		// For every box
		for(int i = 0; i < message.length(); i++) {
			// Instead of a constant width, we check the width of each
			// character.
			char currentChar = message.charAt(i);
			float w = parent.textWidth(currentChar);

			// Each box is centered so we move half the width
			arclength += w / 2;
			// Angle in radians is the arclength divided by the radius
			// Starting on the left side of the circle by adding PI
			float theta = startAngle + arclength / radius;

			parent.pushMatrix();
			// Polar to cartesian coordinate conversion
			parent.translate(radius * PApplet.cos(theta), radius * PApplet.sin(theta));
			// Rotate the box
			parent.rotate(theta + PConstants.PI / 2); // rotation is offset by
														// 90 degrees
			// Display the character
			// fill(0);
			parent.text(currentChar, 0, 0);
			parent.popMatrix();
			// Move halfway again
			arclength += w / 2;
		}
	}

	public void drawBannerSide(String text, Color color, float angle) {
		drawBanner(text, color, angle, hudBannerSide);
	}

	public void drawBannerCenter(String text, Color color, float angle) {
		drawBanner(text, color, angle, hudBannerCenter);
	}

	public void drawBanner(String text, Color color, float angle, PImage bannerImg) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.noStroke();
		parent.noFill();

		parent.pushMatrix();
		parent.translate(PApplet.cos(angle) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET), PApplet.sin(angle)
				* (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
		parent.rotate(angle + PApplet.PI / 2);
		parent.scale(bannerImg.width / 2, bannerImg.height / 2);
		parent.beginShape(PApplet.QUADS);
		parent.texture(bannerImg);
		parent.tint(color.toInt(parent));
		parent.vertex(-1, -1, 0, 0, 0);
		parent.vertex(1, -1, 0, 1, 0);
		parent.vertex(1, 1, 0, 1, 1);
		parent.vertex(-1, 1, 0, 0, 1);
		parent.noTint();
		parent.endShape(PApplet.CLOSE);
		parent.popMatrix();

		parent.pushMatrix();
		parent.fill(255);
		parent.noStroke();
		parent.textAlign(PApplet.CENTER, PApplet.BASELINE);
		parent.textFont(font, FONT_SIZE);
		String cropped_text = text.length() > 24 ? text.substring(0, 24) : text;
		float offset = (parent.textWidth(cropped_text) / 2) / (2 * PApplet.PI * (parent.height / 2 - Hud.SCORE_RADIUS_OFFSET)) * PApplet.TWO_PI;
		drawArc(cropped_text, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET, angle - offset);
		parent.popMatrix();
	}


	void drawCenterText(String line1) {
		drawCenterText(line1, null);
	}

	void drawCenterText(String line1, String line2) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.fill(255);
		parent.pushMatrix();
		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);

		parent.textFont(font, FONT_SIZE);
		parent.text(line1, 0, 0);
		if(line2 != null) parent.text(line2, 0, -20);

		parent.popMatrix();
	}

	private void drawCenterImage(PImage image) {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.fill(255);
		parent.pushMatrix();
		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);

		parent.image(image, 0, 0);

		parent.popMatrix();
	}

	public void drawInnerBoundary() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.pushMatrix();
		parent.translate(parent.width / 2 - 1, parent.height / 2);
		parent.image(hudInnerBoundary, 0, 0);
		parent.popMatrix();
	}

	public void drawOuterBoundary() {
		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
		parent.gl.glEnable(GL.GL_BLEND);
		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		parent.pushMatrix();
		parent.translate(parent.width / 2 - 1, parent.height / 2);
		parent.image(hudOuterBoundary, 0, 0);
		parent.popMatrix();
	}

}
