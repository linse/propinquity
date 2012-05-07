package propinquity;

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
	 * @param blue The "blue" channel value, between 0.0f and 1.0f.
	 * @param green The "green" channel value, between 0.0f and 1.0f.
	 */
	public Colour(float red, float blue, float green) {
		this(red, blue, green, 1.0f);
	}

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0.0f and 1.0f.
	 * @param blue The "blue" channel value, between 0.0f and 1.0f.
	 * @param green The "green" channel value, between 0.0f and 1.0f.
	 * @param alpha The "alpha" channel value, between 0.0f and 1.0f.
	 */
	public Colour(float red, float blue, float green, float alpha) {
		this((int) (red * 255), (int) (blue * 255), (int) (green * 255), (int) (alpha * 255));
	}

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0 and 255.
	 * @param blue The "blue" channel value, between 0 and 255.
	 * @param green The "green" channel value, between 0 and 255.
	 */
	public Colour(int red, int blue, int green) {
		this(red, blue, green, 255);
	}

	/**
	 * Create a new colour with the specified channel parameters.
	 * 
	 * @param red The "red" channel value, between 0 and 255.
	 * @param blue The "blue" channel value, between 0 and 255.
	 * @param green The "green" channel value, between 0 and 255.
	 * @param alpha The "alpha" channel value, between 0 and 255.
	 */
	public Colour(int red, int blue, int green, int alpha) {
		r = red;
		b = blue;
		g = green;
		a = alpha;
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
	 * Gets a predefined colour with the value R:142 G:20 B:252 A:255.
	 * 
	 * @return The colour "violet" as a Colour object.
	 */
	public static Colour violet() {
		return new Colour(142, 20, 252);
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
	 * Gets a predefined colour with the value R:55 G:137 B:254 A:255.
	 * 
	 * @return The colour "blue" as a Colour object.
	 */
	public static Colour blue() {
		return new Colour(55, 137, 254);
	}

}
