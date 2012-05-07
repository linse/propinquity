package propinquity;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * 
 * 
 * @author Stephane Beniak
 */
public class Text {

	/**
	 * Suppress default constructor to disable instantiability.
	 */
	private Text() {
		throw new AssertionError();
	}

	/**
	 * 
	 * @param message
	 * @param radius
	 * @param startAngle
	 * @param parent
	 */
	public static void drawArc(String message, float radius, float startAngle, Propinquity parent) {
		// TODO: Fix this parental issue.

		// We must keep track of our position along the curve
		float arclength = 0;

		// For every box
		for (int i = 0; i < message.length(); i++) {
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

}
