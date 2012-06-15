package propinquity;

import processing.core.PConstants;

/**
 * Interface for drawable, hidable elements. Provides a generic way to handle all the major drawable and hidable components.
 *
 */
public interface UIElement extends PConstants {

	public void draw();

	public void show();

	public void hide();

	public boolean isVisible();

}