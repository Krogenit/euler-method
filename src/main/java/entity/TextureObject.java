package entity;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector2f;
import org.joml.Vector4f;

import client.Texture;

public class TextureObject
{
	protected Texture texture;
	protected Vector2f origin, position, scale;
	protected Vector4f color;
	protected float rotate;
	
	public TextureObject(Texture texture, Vector2f pos)
	{
		this.texture = texture;
		this.position = pos;
		this.rotate = 0;
		this.color = new Vector4f(1,1,1,1);
		this.origin = new Vector2f(-texture.getWidth()/2.0f, -texture.getHeight()/2.0f);
		this.scale = new Vector2f(texture.getWidth(), texture.getHeight());
	}
	
	public void update() {};
	
	public void render()
	{
		glPushMatrix();
		glTranslatef(position.x, position.y, 0);
		glRotatef(rotate, 0, 0, 1);
		glTranslatef(origin.x, origin.y, 0);
		glScalef(scale.x, scale.y, 1);
		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glCallList(quadList);
		glPopMatrix();
	}
	
	public void setSize(Vector2f scale)
	{
		this.scale = scale;
	}
	
	public void setOrigin(Vector2f origin)
	{
		this.origin = origin;
	}
	
	public static int quadList = glGenLists(1);
	
	static
	{
		glNewList(quadList, GL_COMPILE);
		glBegin(GL_QUADS);
		glTexCoord2d(0, 1);
		glVertex3d(0, 0, 0);
		glTexCoord2d(0, 0);
		glVertex3d(0, 1, 0);
		glTexCoord2d(1, 0);
		glVertex3d(1, 1, 0);
		glTexCoord2d(1, 1);
		glVertex3d(1, 0, 0);
		glEnd();
		glEndList();
	}
}
