package propinquity;

import processing.core.*;
import controlP5.*;

import propinquity.hardware.*;

public class XBeeManager implements UIElement {

	Propinquity parent;

	boolean isVisible;

	ControlP5 controlP5;
	Button plNextButton;
	Button plScanButton;

	XBeeBaseStation xbeeBaseStation;

	/**
	 * Create a new XBeeBaseStation.
	 * 
	 * @param parent the parent Propinquity object
	 * @param xbeeBaseStation the XBeeBaseStation object which actually hold the
	 *            XBee
	 */
	public XBeeManager(Propinquity parent, XBeeBaseStation xbeeBaseStation) {
		this.parent = parent;
		this.xbeeBaseStation = xbeeBaseStation;

		parent.registerDispose(this);

		isVisible = true; // controlP5 defaults to true
		controlP5 = new ControlP5(parent);

		// Button to scan for XBees
		plScanButton = controlP5.addButton("XBeeBaseStation Scan", 0, parent.width / 2 + 60, parent.height / 2 + 50,
				50, 20);
		plScanButton.setCaptionLabel("SCAN");

		// Next button
		plNextButton = controlP5.addButton("XBeeBaseStation Next", 0, parent.width / 2 + 60 + 50 + 10,
				parent.height / 2 + 50, 50, 20);
		plNextButton.setCaptionLabel("NEXT");

		hide();
	}

	/* --- GUI Controls --- */

	/**
	 * Receive an event callback from controlP5
	 * 
	 * @param event the controlP5 event.
	 */
	public void controlEvent(ControlEvent event) {
		if (isVisible) {
			if (event.controller().name().equals("XBeeBaseStation Scan"))
				xbeeBaseStation.scan();
			else if (event.controller().name().equals("XBeeBaseStation Next"))
				processUIEvent();
		}
	}

	/**
	 * Receive a keyPressed event.
	 * 
	 * @param keycode the keycode of the keyPressed event.
	 */
	public void keyPressed(int keycode) {
		if (isVisible && keycode == PConstants.ENTER)
			processUIEvent();
	}

	/**
	 * Do the actions for a UI event.
	 * 
	 */
	void processUIEvent() {
		if (xbeeBaseStation.isScanning())
			return;
		else if (parent != null)
			parent.changeGameState(GameState.PlayerList); // TODO Fix this is
															// horrid.
	}

	/* --- Graphics --- */

	/**
	 * Shows the GUI.
	 * 
	 */
	public void show() {
		isVisible = true;
		controlP5.show();
	}

	/**
	 * Hides the GUI.
	 * 
	 */
	public void hide() {
		isVisible = false;
		controlP5.hide();
	}

	/**
	 * Returns true if the GUI is visible.
	 * 
	 * @return true is and only if the GUI is visible.
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Draw method, draws the GUI when called.
	 * 
	 */
	public void draw() {
		if (isVisible) {

			String msg = "";
			if (xbeeBaseStation.isScanning())
				msg = "Scanning...";
			else {
				for (String s : xbeeBaseStation.listXBees())
					msg += s;
				if (msg.isEmpty())
					msg = "No XBees found";
			}

			parent.pushMatrix();
			parent.translate(parent.width / 2, parent.height / 2);
			parent.textFont(Graphics.font, Hud.FONT_SIZE);
			parent.textAlign(PConstants.CENTER, PConstants.CENTER);
			parent.fill(255);
			parent.noStroke();
			parent.text("Detecting XBee modules... ", 0, 0);
			parent.translate(0, 30);
			parent.textFont(Graphics.font, Hud.FONT_SIZE * 0.65f);
			parent.text(msg, 0, 0);
			parent.popMatrix();
		}
	}

	/**
	 * Handle the dispose when processing window is closed.
	 * 
	 */
	public void dispose() {
		xbeeBaseStation.reset();
		if (controlP5 != null) {
			controlP5.dispose();
			controlP5 = null;
		}
	}

}