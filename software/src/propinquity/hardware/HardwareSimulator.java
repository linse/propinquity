package propinquity.hardware;

import processing.core.*;
import java.util.*;
import java.awt.event.KeyEvent;
import propinquity.*;

/**
 * Provides a simulated hardware system which can be controlled using they keyboard.
 *
 */
public class HardwareSimulator implements HardwareInterface, UIElement {

	PApplet p;

	boolean isVisible;

	int width, height, current_patch;

	PFont font;

	Vector<Glove> gloves;
	Vector<Patch> patches;
	Vector<ProxEventListener> proxListeners;
    Vector<AccelEventListener> accelListeners;

	public HardwareSimulator(PApplet parent) {
		this(parent, 500, 100);
	}

	public HardwareSimulator(PApplet parent, int width, int height) {
		this.p = parent;

		this.width = width;
		this.height = height;

		font = p.createFont("Arial", 20);

		gloves = new Vector<Glove>();
		patches = new Vector<Patch>();
        proxListeners = new Vector<ProxEventListener>();
        accelListeners = new Vector<AccelEventListener>();

		p.registerKeyEvent(this);
	}

	public void addPatch(Patch patch) {
		if(patches.indexOf(patch) == -1) patches.add(patch);
	}

	public boolean removePatch(Patch patch) {
		return patches.remove(patch);
	}

	public void addGlove(Glove glove) {
		if(gloves.indexOf(glove) == -1) gloves.add(glove);
	}

	public boolean removeGlove(Glove glove) {
		return gloves.remove(glove);
	}

	public void addProxEventListener(ProxEventListener listener) {
		if(proxListeners.indexOf(listener) == -1) proxListeners.add(listener);
	}

	public boolean removeProxEventListener(ProxEventListener listener) {
		return proxListeners.remove(listener);
	}

	public void addAccelEventListener(AccelEventListener listener) {
      if(accelListeners.indexOf(listener) == -1) accelListeners.add(listener);
	}

	public boolean removeAccelEventListener(AccelEventListener listener) {
      return accelListeners.remove(listener);
	}

	public void sendPacket(Packet packet) {
		//Do nothing
	}

	public void draw() {
		if(!isVisible) return;

		p.strokeWeight(3);
		p.stroke(100);
		p.fill(0);

		p.rect(-3, -3, width+6, height+6);

		int num_patch = patches.size();
		int num_gloves = gloves.size();
		int total = num_patch+num_gloves;
		int local_width = width/(total+1);

		p.strokeWeight(1);
		p.noStroke();

		for(int i = 0;i < num_patch+num_gloves;i++) {
			p.pushMatrix();
			p.translate(local_width*(i), 0);

			if(i < num_patch) {
				int[] color = patches.get(i).getColor();
				if(patches.get(i).getActive()) p.fill(color[0], color[1], color[2]);
				else p.fill(0);
				p.rect(0, 0, local_width*0.9f, height/2);

				int vibe = patches.get(i).getVibeLevel();
				if(patches.get(i).getActive()) p.fill(vibe);
				else p.fill(0);
				p.rect(0, height/2, local_width*0.9f, height/2);

				int prox = patches.get(i).getProx();
				p.fill(100);
				p.rect(local_width*0.9f, height, local_width*0.1f, PApplet.map(prox, 0, 1024, 0, -height));
			} else {
				int vibe = gloves.get(i-num_patch).getVibeLevel();
				if(gloves.get(i-num_patch).getActive()) p.fill(vibe);
				else p.fill(0);
				p.rect(0, height/2, local_width*0.9f, height/2);
			}

			if(current_patch == i) {
				p.fill(100, 0, 0);
				p.rect(2, 2, 15, 15);
			}

			p.stroke(100);
			p.noFill();

			p.rect(0, 0, local_width, height);

			p.noStroke();

			p.popMatrix();
		}
	}

	public void show() {
		isVisible = true;
	}

	public void hide() {
		isVisible = false;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void keyEvent(KeyEvent e) {
		if(!isVisible) return;
		int keycode = e.getKeyCode();
		
		if(keycode >= 48 && keycode <= 57) {
			current_patch = PApplet.constrain(keycode-48, 0, patches.size()-1);
		} else if(keycode == KeyEvent.VK_MINUS) {
			if(current_patch >= patches.size()) return;
			Patch patch = patches.get(current_patch);
			if(patch != null && patch.getActive() && ((patch.getActivationMode() & Mode.PROX) != 0)) {
				patch.setProx(patch.getProx()-50);
				for(ProxEventListener listener : proxListeners) listener.proxEvent(patch);
			}
		} else if(keycode == KeyEvent.VK_EQUALS) {
			if(current_patch >= patches.size()) return;
			Patch patch = patches.get(current_patch);
            if(patch != null && patch.getActive() && ((patch.getActivationMode() & Mode.PROX) != 0)) {
				patch.setProx(patch.getProx()+50);
				for(ProxEventListener listener : proxListeners) listener.proxEvent(patch);
			}
        } else if (keycode == KeyEvent.VK_Q && e.getID() == KeyEvent.KEY_PRESSED) {
          if(current_patch >= patches.size()) return;
          Patch patch = patches.get(current_patch);
          if(patch != null && patch.getActive() && ((patch.getActivationMode() & Mode.ACCEL_INT0) != 0)) {
              patch.setInterrupt0(0);
              for(AccelEventListener listener : accelListeners) listener.accelInterrupt0Event(patch);
          }
        } else if (keycode == KeyEvent.VK_W && e.getID() == KeyEvent.KEY_PRESSED) {
          if(current_patch >= patches.size()) return;
          Patch patch = patches.get(current_patch);
          if(patch != null && patch.getActive() && ((patch.getActivationMode() & Mode.ACCEL_INT1) != 0)) {
              patch.setInterrupt1(0);
              for(AccelEventListener listener : accelListeners) listener.accelInterrupt1Event(patch);
          }
        }
		
	}
}