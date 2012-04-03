package propinquity;

import java.io.PrintWriter;

import processing.video.MovieMaker;

/**
 * Handles both text and video logging of the application as it runs.
 * 
 * @author Stephane Beniak
 */
public class Logger {
	
	/**
	 * Output log file location.
	 */
	public static final String outputFile = "bin/messages.txt";
	
	private static final boolean isRecording = false;
	
	private static PrintWriter output;
	private static MovieMaker movieMaker;
	
	private static Propinquity app;
	
	/**
	 * Suppress default constructor to disable instantiability.
	 */
	private Logger () {
		throw new AssertionError();
	}
	
	/**
	 * Setup the text logger and prepare for writing.
	 * 
	 * @param application The parent application to be logged.
	 */
	public static void setup(Propinquity application) {
		
		app = application;
		
		output = app.createWriter(outputFile);
		output.println("Starting Logging of Propinquity Test.");
	}
	
	/**
	 * Print a line of output to the text log file.
	 * 
	 * @param line The string to be logged.
	 */
	public static void printOutput(String line) {
		output.println(line);
	}
	
	/**
	 * Record a frame of video.
	 */
	public static void recordFrame() {
		if (isRecording)
			movieMaker.addFrame();
	}
	
	/**
	 * Close the text logger.
	 */
	public static void close() {
		output.flush();
		output.close();
	}
	
}
