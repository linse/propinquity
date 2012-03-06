package proclipsingpinquity;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;

import processing.core.PApplet;
import processing.core.PFont;
import processing.serial.Serial;

public class ProclipsingPinquity extends PApplet {
	
	private XBeeManager xbeeManager;

	public void setup() {
		// setup general stuff
		size(1024, 768, OPENGL);
		smooth(); // anti-aliasing for graphic display
		PFont font = loadFont("SansSerif-10.vlw");
		textFont(font); // use the font for text
		// required by the xbee api library, needs to be in your data folder.
		PropertyConfigurator.configure(dataPath("") + "log4j.properties");
		
		// setup game
		xbeeManager = new XBeeManager(this);
		System.exit(0);
	}

	public void draw() {
	}

	// key pressed - do something
	public void keyPressed(){
		int keyNum = Character.getNumericValue(key);
		println(key);

//		  if (gameState == STATE_XBEE_INIT) {
//		    switch(key) {
//		      case ENTER:
//		        xbeeManager.save();
//		        //xbeeManager.dispose();
//		        //initPlayerListCtrl();
//		        gameState++;
//		        println("gamestate = " + gameState);
//		        break;
//		    }
//		  }
//		  else if (gameState == STATE_PLAYER_LIST) {
//		    switch(key) {
//		      case ENTER:
//		        playerList.process();
//		        if (playerList.isDone()) {
//		          gameState++;
//		          println("gamestate = " + gameState);
//		        }
//		        break;
//		    }
//		  }	
	} 
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { proclipsingpinquity.ProclipsingPinquity.class
				.getName() });
		println("Exiting Propinquity");
	}
}
