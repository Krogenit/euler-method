package client;

import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;

public class Texture
{
	private int width, height, id;
	
	public Texture(int width, int height)
	{
		this.id = glGenTextures();
		this.width = width;
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public int getId()
	{
		return id;
	}
	
	public void delete()
	{
		glDeleteTextures(id);
	}
}
