package propinquity;

import java.io.PrintWriter;

import processing.core.PApplet;
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
	String outputFile;
	
	boolean isRecording = false;
	
	PrintWriter output;
	MovieMaker movieMaker;
		
	/**
	 * Make a new logger object.
	 * @param p the Propinquity instance.
	 * @param outputFile the log file to use.
	 */
	public Logger(PApplet parent, String outputFile) {
		this.outputFile = outputFile;

		output = parent.createWriter(outputFile);
		output.println("Starting Logging of Propinquity Test.");
	}
	
	/**
	 * Print a line of output to the text log file.
	 * 
	 * @param line The string to be logged.
	 */
	public void printOutput(String line) {
		output.println(line);
	}
	
	/**
	 * Record a frame of video.
	 */
	public void recordFrame() {
		if (isRecording)
			movieMaker.addFrame();
	}
	
	/**
	 * Close the text logger.
	 */
	public void close() {
		output.flush();
		output.close();
	}
	
}
