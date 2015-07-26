package blue.made.furnacejs;

import blue.made.furnacejs.annotation.JSGet;
import blue.made.furnacejs.annotation.JSNullIsUndef;
import blue.made.furnacejs.annotation.JSSet;

public interface FPropMap<T> extends FObject
{
	@JSGet
	@JSNullIsUndef
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
