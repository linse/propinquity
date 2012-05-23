package propinquity;

import java.io.PrintWriter;

import processing.core.PApplet;
import processing.video.MovieMaker;

/**
 * Handles both text and video logging of the application as it runs.
 * 
 */
public class Logger {

	PApplet parent;

	boolean recording;
	boolean logging;

	PrintWriter output;
	MovieMaker movieMaker;

	/**
	 * Make a new logger object.
	 * 
	 * @param parent The parent processing applet.
	 */
	public Logger(PApplet parent) {
		this.parent = parent;
		parent.registerDispose(this);
	}

	/**
	 * Start logging text file.
	 *
	 * @param outputLogFile The path to the output text log file.
	 */
	public void startLogging(String outputLogFile) {
		output = parent.createWriter(outputLogFile);
		logging = true;

		println("Propinquity Log");
	}

	/**
	 * Start recording the movie.
	 *
	 * @param outputMovieFile The path to the output movie file.
	 */
	public void startRecording(String outputMovieFile) {
		movieMaker = new MovieMaker(parent, parent.width, parent.height, outputMovieFile);
		recording = true;
	}

	/**
	 * Record a frame of video.
	 *
	 */
	public void recordFrame() {
		if(recording) movieMaker.addFrame();
	}

	/**
	 * Print output text to the log file.
	 * 
	 * @param text The string to be logged.
	 */
	public void print(String text) {
		if(logging) {
			output.print(text);
			output.flush();
		}
	}

	/**
	 * Print output text to the log file followed by a new line.
	 * 
	 * @param text The string to be logged.
	 */
	public void println(String text) {
		if(logging) {
			output.println(text);
			output.flush();
		}
	}

	/**
	 * Close the text logger.
	 *
	 */
	public void close() {
		if(recording) movieMaker.finish();
		if(logging) {
			output.flush();
			output.close();
		}
	}

	/**
	 * Processing triggered dispose method.
	 *
	 */
	public void dispose() {
		close();
	}

}
