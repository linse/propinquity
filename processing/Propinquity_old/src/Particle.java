import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import processing.core.PGraphics;

public class Particle {
  float scale;
  Body body;
  Shape shape;
  PGraphics tex;
  float texHalfSize;
  float[][] texVertices;
  Vec2 push;
private Propinquity parent;
  
  
  public Particle(Propinquity p, Body b, Shape sh, float s, PGraphics t)
  {
	  this.parent = p;
    body = b;
    shape = sh;
    scale = s;
    push = new Vec2(0, 0);
    tex = t;
    texHalfSize = t.width/2f;
    texVertices = new float[2][4];
    for(int i = 0; i < 2; i++)
      for(int j = 0; j < 4; j++)
        texVertices[i][j] = 1.0f + parent.random(-0.4f, 0.4f);
  }
  
  public void draw() {
    Vec2 pos = parent.box2d.getBodyPixelCoord(body);
    parent.pushMatrix();
    parent.translate(pos.x,pos.y);
    parent.scale(scale*texHalfSize);
    parent.beginShape(parent.QUADS);
    parent.texture(tex);
    parent.vertex(-texVertices[0][0],-texVertices[1][0],0,0,0);
    parent.vertex(texVertices[0][1],-texVertices[1][1],1,0);
    parent.vertex(texVertices[0][2],texVertices[1][2],0,1,1);
    parent.vertex(-texVertices[0][3],texVertices[1][3],0,0,1);
    parent.endShape(parent.CLOSE);
    parent.popMatrix();
  }
}
