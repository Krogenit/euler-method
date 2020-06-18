package input;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;
import java.util.Map;

import javax.jws.Oneway;

import org.joml.Vector2d;
import org.joml.Vector2f;

public class Mouse
{
	static Vector2d pos, prevPos, scroll;
	static Vector2f deltaPos;
	
	static int isLeftDown, isRightDown, isMiddleDown;
	static int isLeftRelease, isRightRelease, isMiddleRelease;

	static boolean isActive;
	
	public static void init(long window)
	{
		glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
			pos.x = xpos;
			pos.y = ypos;
        });
		
		glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {
			isActive = entered;
        });
		glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
			if(button == GLFW_MOUSE_BUTTON_LEFT)
			{
				isLeftDown = action;
				if(!isLeftDown())
					isLeftRelease = 1;
			}
			if(button == GLFW_MOUSE_BUTTON_RIGHT)
			{
				isRightDown = action;
				if(!isRightDown())
					isRightRelease = 1;
			}
			if(button == GLFW_MOUSE_BUTTON_MIDDLE)
			{
				isMiddleDown = action;
				if(!isMiddleDown())
					isMiddleRelease = 1;
			}
		});
		glfwSetScrollCallback(window, (windowHandle, xoffset, yoffset) -> {
			scroll.x += xoffset;
			scroll.y += yoffset;
		});
		
		scroll = new Vector2d();
		pos = new Vector2d();
		prevPos = new Vector2d();
		deltaPos = new Vector2f();
	}
	
	public void onMouseScroll(double x, double y)
	{
		scroll.x += x;
		scroll.y += y;
	}
	
	public static void postUpdateState()
	{
		isLeftRelease = 0;
		isRightRelease = 0;
		isMiddleRelease = 0;
		scroll.x = 0;
		scroll.y = 0;
	}
	
	public static void updateState()
	{
		deltaPos.x = 0;
		deltaPos.y = 0;

        if (prevPos.x > 0 && prevPos.y > 0 && isActive) {
            double deltax = pos.x - prevPos.x;
            double deltay = pos.y - prevPos.y;
            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;
            if (rotateX) {
            	deltaPos.y = (float) deltax;
            }
            if (rotateY) {
            	deltaPos.x = (float) deltay;
            }
        }
        
        prevPos.x = pos.x;
        prevPos.y = pos.y;
	}
	
	public static boolean isLeftRelease()
	{
		return isLeftRelease == 1;
	}
	
	public static boolean isRightRelease()
	{
		return isRightRelease == 1;
	}
	
	public static boolean isLeftDown()
	{
		return isLeftDown == 1;
	}
	
	public static boolean isRightDown()
	{
		return isRightDown == 1;
	}
	
	public static boolean isMiddleDown()
	{
		return isMiddleDown == 1;
	}
	
	public static Vector2f getDelta()
	{
		return deltaPos;
	}
	
	public static Vector2d getPosition()
	{
		return pos;
	}
	
	public static Vector2d getScroll()
	{
		return scroll;
	}
}
