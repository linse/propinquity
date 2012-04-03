package propinquity;

import ddf.minim.*;

/**
 * Handles sound content initialization and usage.
 * 
 * @author Stephane Beniak
 */
public class Sounds {
	
	public static final String SONG_FOLDER = "songs/";
	
	public static final int BUFFER_SIZE = 2048;
	
	public static AudioPlayer complete;
	public static AudioPlayer negativeCoop;
	public static AudioPlayer negativeP1;
	public static AudioPlayer negativeP2;
	public static AudioPlayer song;
	
	private static Minim minim;
	
	private static Propinquity app;
	
	/**
	 * Suppress default constructor to disable instantiability.
	 */
	private Sounds () {
		throw new AssertionError();
	}
	
	/**
	 * Setup the Minim audio manager.
	 * 
	 * @param application
	 */
	public static void setup(Propinquity application) {
		
		app = application;
		
		minim = new Minim(app);
	}
	
	/**
	 * Load common sound content.
	 */
	public static void loadCommonContent() {
		
		complete = minim.loadFile("sounds/comp.mp3", BUFFER_SIZE);
		complete.setGain(5);
	}
	
	/**
	 * Load level-specific sound content.
	 */
	public static void loadLevelContent() {
		
		negativeCoop = minim.loadFile("sounds/neg.mp3", BUFFER_SIZE);
		negativeP1 = minim.loadFile("sounds/neg1.mp3", BUFFER_SIZE);
		negativeP2 = minim.loadFile("sounds/neg2.mp3", BUFFER_SIZE);
	}
	
	/**
	 * Load the song for the current level.
	 */
	public static void loadSong(String file) {
		
		song = minim.loadFile(SONG_FOLDER + file, BUFFER_SIZE);
	}
	
}
