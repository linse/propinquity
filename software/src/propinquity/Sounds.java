package propinquity;

import processing.core.PApplet;
import ddf.minim.*;

/**
 * Handles sound content initialization and usage.
 * 
 * @author Stephane Beniak
 */
public class Sounds {

	public static final String SONG_FOLDER = "songs/";
	public static final int BUFFER_SIZE = 2048;

	Minim minim;

	AudioSample bubbleLow, bubbleHigh;
	AudioSample complete;
	AudioSample positive;
	AudioSample negativeCoop;
	AudioSample[] negativePlayer;

	/**
	 * Setup the Minim audio manager.
	 * 
	 * @param parent
	 */
	public Sounds(PApplet parent) {
		minim = new Minim(parent);

		bubbleLow = minim.loadSample("sounds/bubble350Hz.mp3", BUFFER_SIZE);
		bubbleLow.setGain(0.5f);
		bubbleHigh = minim.loadSample("sounds/bubble600Hz.mp3", BUFFER_SIZE);
		bubbleHigh.setGain(0.5f);

		complete = minim.loadSample("sounds/comp.mp3", BUFFER_SIZE);
		complete.setGain(5);

		positive = minim.loadSample("sounds/pos.mp3", BUFFER_SIZE);

		negativeCoop = minim.loadSample("sounds/neg.mp3", BUFFER_SIZE);

		negativePlayer = new AudioSample[2];
		negativePlayer[0] = minim.loadSample("sounds/neg1.mp3", BUFFER_SIZE);
		negativePlayer[1] = minim.loadSample("sounds/neg2.mp3", BUFFER_SIZE);
	}

	/**
	 * Load the song for the current level.
	 */
	public AudioPlayer loadSong(String file) {
		return minim.loadFile(SONG_FOLDER + file, BUFFER_SIZE);
	}

	public AudioSample getComplete() {
		return complete;
	}

	public AudioSample getPositive() {
		return positive;
	}

	public AudioSample getNegativeCoop() {
		return negativeCoop;
	}

	public AudioSample getNegativePlayer(int player) {
		return negativePlayer[PApplet.constrain(player, 0, negativePlayer.length-1)];
	}

}
