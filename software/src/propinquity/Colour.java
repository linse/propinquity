package propinquity;

import processing.core.PApplet;

/**
 * Handles the creation, manipulation and conversion of colours.
 * 
 * @author Stephane Beniak
 */
public class Colour {

	private int r, g, b, a;

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0.0f and 1.0f.
	 * @param blue The "green" channel value, between 0.0f and 1.0f.
	 * @param green The "blue" channel value, between 0.0f and 1.0f.
	 */
	public Colour(float red, float green, float blue) {
		this(red, green, blue, 1.0f);
	}

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0.0f and 1.0f.
	 * @param blue The "green" channel value, between 0.0f and 1.0f.
	 * @param green The "blue" channel value, between 0.0f and 1.0f.
	 * @param alpha The "alpha" channel value, between 0.0f and 1.0f.
	 */
	public Colour(float red, float green, float blue, float alpha) {
		this((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
	}

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0 and 255.
	 * @param blue The "green" channel value, between 0 and 255.
	 * @param green The "blue" channel value, between 0 and 255.
	 */
	public Colour(int red, int green, int blue) {
		this(red, green, blue, 255);
	}

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0 and 255.
	 * @param blue The "green" channel value, between 0 and 255.
	 * @param green The "blue" channel value, between 0 and 255.
	 * @param alpha The "alpha" channel value, between 0 and 255.
	 */
	public Colour(int red, int green, int blue, int alpha) {
		r = PApplet.constrain(red, 0, 255);
		g = PApplet.constrain(green, 0, 255);
		b = PApplet.constrain(blue, 0, 255);
		a = PApplet.constrain(alpha, 0, 255);
	}

	/**
	 * Get the "red" channel value for this colour.
	 * 
	 * @return The "red" channel value of this colour, between 0 and 255.
	 */
	public int getR() {
		return r;
	}

	/**
	 * Get the "green" channel value for this colour.
	 * 
	 * @return The "green" channel value of this colour, between 0 and 255.
	 */
	public int getG() {
		return g;
	}

	/**
	 * Get the "blue" channel value for this colour.
	 * 
	 * @return The "blue" channel value of this colour, between 0 and 255.
	 */
	public int getB() {
		return b;
	}

	/**
	 * Get the "alpha" channel value for this colour.
	 * 
	 * @return The "alpha" channel value of this colour, between 0 and 255.
	 */
	public int getA() {
		return a;
	}

	/**
	 * Gets a processing-friendly instance of the colour as a single integer.
	 * 
	 * @param parent The Propinquity instance.
	 * @return The current colour in single integer form.
	 */
	public int toInt(Propinquity parent) {
		return parent.color(r, g, b, a);
	}

	/* ------------------------------------------------------------ */

	/**
	 * Gets a predefined colour with the value R:0 G:0 B:0 A:255.
	 * 
	 * @return The colour "black" as a Colour object.
	 */
	public static Colour black() {
		return new Colour(0, 0, 0);
	}
	
	/**
	 * Gets a predefined colour with the value R:55 G:137 B:254 A:255.
	 * 
	 * @return The colour "blue" as a Colour object.
	 */
	public static Colour blue() {
		return new Colour(55, 137, 254);
	}
	
	/**
	 * Gets a predefined colour with the value R:255 G:25 B:0 A:255.
	 * 
	 * @return The colour "red" as a Colour object.
	 */
	public static Colour red() {
		return new Colour(255, 25, 0);
	}
	
	/**
	 * Gets a predefined colour with the value R:142 G:20 B:252 A:255.
	 * 
	 * @return The colour "violet" as a Colour object.
	 */
	public static Colour violet() {
		return new Colour(142, 20, 252);
	}

}
