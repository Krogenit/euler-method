package client.input;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse
{
	static Map<Integer, Key> keyMap = new HashMap<Integer, Key>();
	
	static Vector2f mousePos = new Vector2f();
	static Vector2f mouseScroll = new Vector2f();
	
	static Key left = new Key();
	static Key right = new Key();
	static Key middle = new Key();
	static Key prevLeft = new Key();
	static Key prevRight = new Key();
	static Key prevMiddle = new Key();
	
	static
	{
		keyMap.put(GLFW_MOUSE_BUTTON_LEFT, left);
		keyMap.put(GLFW_MOUSE_BUTTON_RIGHT, right);
		keyMap.put(GLFW_MOUSE_BUTTON_MIDDLE, middle);
		keyMap.put(GLFW_MOUSE_BUTTON_LEFT+3, prevLeft);
		keyMap.put(GLFW_MOUSE_BUTTON_RIGHT+3, prevRight);
		keyMap.put(GLFW_MOUSE_BUTTON_MIDDLE+3, prevMiddle);
	}
	
	public static void onMouseScroll(double x, double y)
	{
		mouseScroll.x += x;
		mouseScroll.y += y;
	}
	
	public static void onMouseButton(int button, int action, int mods)
	{
		Key key = keyMap.get(button);
		if(key != null)
		{
			Key prevKey = keyMap.get(button+3);
			prevKey.isPress = key.isPress;
		
			key.isPress = action;
		}
	}
	
	public static void updateState()
	{
		keyMap.get(GLFW_MOUSE_BUTTON_LEFT+3).isPress = 0;
		keyMap.get(GLFW_MOUSE_BUTTON_RIGHT+3).isPress = 0;
		keyMap.get(GLFW_MOUSE_BUTTON_MIDDLE+3).isPress = 0;
		mouseScroll.x = 0;
		mouseScroll.y = 0;
	}
	
	public static boolean isLeftRelease()
	{
		return keyMap.get(GLFW_MOUSE_BUTTON_LEFT+3).isPress == 1 
				&& keyMap.get(GLFW_MOUSE_BUTTON_LEFT).isPress == 0;
	}
	
	public static boolean isRightRelease()
	{
		return keyMap.get(GLFW_MOUSE_BUTTON_RIGHT+3).isPress == 1 
				&& keyMap.get(GLFW_MOUSE_BUTTON_RIGHT).isPress == 0;
	}
	
	public static boolean isLeftDown()
	{
		return keyMap.get(GLFW_MOUSE_BUTTON_LEFT).isPress == 1;
	}
	
	public static boolean isRightDown()
	{
		return keyMap.get(GLFW_MOUSE_BUTTON_RIGHT).isPress == 1;
	}
	
	public static void setPosition(double x, double y)
	{
		mousePos.x = (float) x;
		mousePos.y = (float) y;
	}
	
	public static Vector2f getPosition()
	{
		return mousePos;
	}
	
	public static float getX()
	{
		return mousePos.x;
	}
	
	public static float getY()
	{
		return mousePos.y;
	}
	
	public static Vector2f getScroll()
	{
		return mouseScroll;
	}
}
