package propinquity;

import processing.core.PConstants;

public interface UIElement extends PConstants {

	public void draw();

	public void show();

	public void hide();

	public boolean isVisible();

}