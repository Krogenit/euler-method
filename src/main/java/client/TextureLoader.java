 package client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL30;

import client.Texture;
import util.PathHelper;
import static org.lwjgl.opengl.GL11.*;

public class TextureLoader
{
	static String gamePath = PathHelper.getGamePath() + "/";
	static String texturesPath = "content/texture/";
	
	public static Texture button_square = loadTexture("gui/button_square");
	
	public static Texture loadTexture(String name)
	{
        try{
        	ClassLoader loader = TextureLoader.class.getClassLoader();
			InputStream in = new FileInputStream(new File(".", texturesPath + name + ".png"));
			BufferedImage image = ImageIO.read(in);
			
	        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(4*image.getWidth()*image.getHeight());
	        byte buffer[] = (byte[])image.getRaster().getDataElements(0,0,image.getWidth(),image.getHeight(),null);
	        imageBuffer.clear();
	        imageBuffer.put(buffer);
	        imageBuffer.rewind();
			
			return generateTexture(image.getWidth(), image.getHeight(), imageBuffer);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	
        	return null;
        }
	}
	
	public static Texture generateTexture(int width, int height, ByteBuffer image)
	{
		Texture texture = new Texture(width, height);
		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		
		GL30.glGenerateMipmap(GL_TEXTURE_2D);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		return texture;
	}
}
