package propinquity;

import processing.core.PApplet;

/**
 * Represents a color. Handles the creation, manipulation and conversion of colors.
 * 
 * @author Stephane Beniak
 */
public class Color {

	public final int r, g, b, a;

	/**
	 * Create a new color with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0.0f and 1.0f.
	 * @param blue The "green" channel value, between 0.0f and 1.0f.
	 * @param green The "blue" channel value, between 0.0f and 1.0f.
	 */
	public Color(float red, float green, float blue) {
		this(red, green, blue, 1.0f);
	}

	/**
	 * Create a new color with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0.0f and 1.0f.
	 * @param blue The "green" channel value, between 0.0f and 1.0f.
	 * @param green The "blue" channel value, between 0.0f and 1.0f.
	 * @param alpha The "alpha" channel value, between 0.0f and 1.0f.
	 */
	public Color(float red, float green, float blue, float alpha) {
		this((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
	}

	/**
	 * Create a new color with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0 and 255.
	 * @param blue The "green" channel value, between 0 and 255.
	 * @param green The "blue" channel value, between 0 and 255.
	 */
	public Color(int red, int green, int blue) {
		this(red, green, blue, 255);
	}

	/**
	 * Create a new color with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0 and 255.
	 * @param blue The "green" channel value, between 0 and 255.
	 * @param green The "blue" channel value, between 0 and 255.
	 * @param alpha The "alpha" channel value, between 0 and 255.
	 */
	public Color(int red, int green, int blue, int alpha) {
		r = PApplet.constrain(red, 0, 255);
		g = PApplet.constrain(green, 0, 255);
		b = PApplet.constrain(blue, 0, 255);
		a = PApplet.constrain(alpha, 0, 255);
	}

	/**
	 * Get the "red" channel value for this color.
	 * 
	 * @return The "red" channel value of this color, between 0 and 255.
	 */
	public int getR() {
		return r;
	}

	/**
	 * Get the "green" channel value for this color.
	 * 
	 * @return The "green" channel value of this color, between 0 and 255.
	 */
	public int getG() {
		return g;
	}

	/**
	 * Get the "blue" channel value for this color.
	 * 
	 * @return The "blue" channel value of this color, between 0 and 255.
	 */
	public int getB() {
		return b;
	}

	/**
	 * Get the "alpha" channel value for this color.
	 * 
	 * @return The "alpha" channel value of this color, between 0 and 255.
	 */
	public int getA() {
		return a;
	}

	/**
	 * Gets a processing-friendly instance of the color as a single integer.
	 * 
	 * @param parent The Propinquity instance.
	 * @return The current color in single integer form.
	 */
	public int toInt(Propinquity parent) {
		return parent.color(r, g, b, a);
	}

	/* ------------------------------------------------------------ */

	/**
	 * Gets a predefined color with the value R:0 G:0 B:0 A:255.
	 * 
	 * @return The color "black" as a Color object.
	 */
	public static Color black() {
		return new Color(0, 0, 0);
	}
	
	/**
	 * Gets a predefined color with the value R:255 G:255 B:255 A:255.
	 * 
	 * @return The color "white" as a Color object.
	 */
	public static Color white() {
		return new Color(255, 255, 255);
	}
	
	/**
	 * Gets a predefined color with the value R:55 G:137 B:254 A:255.
	 * 
	 * @return The color "blue" as a Color object.
	 */
	public static Color blue() {
		return new Color(55, 137, 254);
	}
	
	/**
	 * Gets a predefined color with the value R:255 G:25 B:0 A:255.
	 * 
	 * @return The color "red" as a Color object.
	 */
	public static Color red() {
		return new Color(255, 25, 0);
	}

	/**
	 * Gets a predefined color with the value R:0 G:255 B:0 A:255.
	 * 
	 * @return The color "green" as a Color object.
	 */
	public static Color green() {
		return new Color(0, 255, 0);
	}
	
	
	/**
	 * Gets a predefined color with the value R:142 G:20 B:252 A:255.
	 * 
	 * @return The color "violet" as a Color object.
	 */
	public static Color violet() {
		return new Color(142, 20, 252);
	}

	/**
	 * Gets a predefined color with the value R:142 G:20 B:252 A:255.
	 * 
	 * @return The color "violet" as a Color object.
	 */
	public static Color yellow() {
		return new Color(200, 200, 0);
	}

	/**
	 * Gets a predefined color with the value R:0 G:255 B:100 A:255.
	 * 
	 * @return The color "teal" as a Color object.
	 */
	public static Color teal() {
		return new Color(0, 200, 200);
	}

}
