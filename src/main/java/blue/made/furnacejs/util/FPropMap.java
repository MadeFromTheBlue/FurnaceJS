package blue.made.furnacejs.util;

import blue.made.furnacejs.annotation.JSGet;
import blue.made.furnacejs.annotation.JSSet;

public interface FPropMap<T>
{
	@JSGet
	public default T get(String id)
	{
		return null;
	}
	
	@JSSet
	public default boolean set(String id, T value)
	{
		return false;
	}
}
