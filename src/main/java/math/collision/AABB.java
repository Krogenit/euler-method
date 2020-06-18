package math.collision;

import org.joml.Vector2f;

public class AABB
{
	Vector2f min, max;
	
	public AABB(Vector2f min, Vector2f max)
	{
		this.max = max;
		this.min = min;
	}
	
	public boolean isIntersects(Vector2f vect)
	{
		return vect.x >= min.x && vect.x < max.x && vect.y >= min.y && vect.y < max.y;
	}
}
