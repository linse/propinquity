import processing.core.*;

public class LevelSelectParticle {
	float scale;
	PVector position;
	PGraphics tex;
	float texHalfSize;
	float[][] texVertices;
	private Propinquity parent;

	public LevelSelectParticle(Propinquity p, PVector pos, float sca,
			PGraphics tex) {
		this.parent = p;
		this.position = pos;
		this.scale = sca;

		this.tex = tex;
		texHalfSize = tex.width / 2f;
		texVertices = new float[2][4];
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 4; j++)
				texVertices[i][j] = 1.0f + parent.random(-0.4f, 0.4f);
	}

	public void draw() {
		parent.pushMatrix();
		parent.translate(position.x, position.y, position.z);
		parent.scale(scale * texHalfSize);
		parent.beginShape(PApplet.QUADS);
		parent.texture(tex);
		parent.vertex(-texVertices[0][0], -texVertices[1][0], 0, 0, 0);
		parent.vertex(texVertices[0][1], -texVertices[1][1], 1, 0);
		parent.vertex(texVertices[0][2], texVertices[1][2], 0, 1, 1);
		parent.vertex(-texVertices[0][3], texVertices[1][3], 0, 0, 1);
		parent.endShape(PApplet.CLOSE);
		parent.popMatrix();
	}
}
