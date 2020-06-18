package client.gui;

import static org.lwjgl.opengl.GL11.*;
import math.collision.AABB;

import org.joml.Vector2f;

import core.Main;
import client.Texture;
import client.input.Mouse;
import entity.TextureObject;

public class Button extends TextureObject
{
	AABB aabb;
	String text;
	int id;
	Vector2f textOrigin;
	
	public Button(int id, Texture texture, Vector2f pos, String text, Vector2f textOrigin)
	{
		super(texture, pos);
		this.createAABB();
		this.id = id;
		this.text = text;
		this.textOrigin = textOrigin;
	}
	
	public Button(int id, Texture texture, Vector2f pos, String text)
	{
		super(texture, pos);
		this.createAABB();
		this.id = id;
		this.text = text;
		this.textOrigin = new Vector2f(0,0);
	}
	
	private void createAABB()
	{
		this.aabb = new AABB(new Vector2f(position.x + origin.x, position.y + origin.y),
				new Vector2f(position.x - origin.x, position.y - origin.y));
	}
	
	public void update()
	{
		
	}
	
	public void render()
	{
		glPushMatrix();
		glTranslatef(position.x, position.y, 0);
		glRotatef(rotate, 0, 0, 1);
		glTranslatef(origin.x, origin.y, 0);
		glPushMatrix();
		glTranslatef(textOrigin.x, textOrigin.y, 0);
		Main.font.render(text, 0, 0, Main.black);
		glPopMatrix();
		glScalef(scale.x, scale.y, 1);
		glDisable(GL_TEXTURE_2D);
		glColor3f(0.5f, 0.5f, 0.5f);
		glCallList(quadList);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glCallList(quadList);
		glPopMatrix();
	}
	
	public boolean isIntersects()
	{
		return aabb.isIntersects(Mouse.getPosition());
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setText(String s)
	{
		this.text = s;
	}
	
	public void setTextOrigin(float x, float y)
	{
		this.textOrigin.x = x;
		this.textOrigin.y = y;
	}
	
	@Override
	public void setOrigin(Vector2f origin)
	{
		super.setOrigin(origin);
		this.createAABB();
	}
}
