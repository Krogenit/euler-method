package input;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class Keyboard
{
	static long window;
	
	public static void init(long win)
	{
		window = win;
	}
	
	public static void setKeyState(int keyId, int isPress)
	{

	}
	
	public static boolean isKeyPressed(int keyCode) 
	{
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }
}
