import processing.core.PApplet;

public class Player extends PApplet {

  static final int XPANS_PER_PLAYER=3;

  Xpan[] xpans;
  
  Player() {
    this.xpans = new Xpan[XPANS_PER_PLAYER];
  }
}

