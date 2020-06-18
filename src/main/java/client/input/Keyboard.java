package client.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard
{
	static Map<Integer, Key> keyMap = new HashMap<Integer, Key>();
	
	public static Key KEY_W = new Key();
	public static Key KEY_A = new Key();
	public static Key KEY_S = new Key();
	public static Key KEY_D = new Key();
	public static Key KEY_UP = new Key();
	public static Key KEY_DOWN = new Key();
	public static Key KEY_RIGHT = new Key();
	public static Key KEY_LEFT = new Key();
	
	public static Key KEY_ESC = new Key();
	
	static
	{
		keyMap.put(GLFW_KEY_W, KEY_W);
		keyMap.put(GLFW_KEY_A, KEY_A);
		keyMap.put(GLFW_KEY_S, KEY_S);
		keyMap.put(GLFW_KEY_D, KEY_D);
		keyMap.put(GLFW_KEY_LEFT, KEY_LEFT);
		keyMap.put(GLFW_KEY_RIGHT, KEY_RIGHT);
		keyMap.put(GLFW_KEY_UP, KEY_UP);
		keyMap.put(GLFW_KEY_DOWN, KEY_DOWN);
		keyMap.put(GLFW_KEY_ESCAPE, KEY_ESC);
	}
	
	public static void setKeyState(int keyId, int isPress)
	{
		Key key = keyMap.get(keyId);
		if(key != null)
		{
			key.isPress = isPress;
		}
	}
}
