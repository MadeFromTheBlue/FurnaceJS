package blue.made.furnacejs;

import org.mozilla.javascript.ScriptableObject;

import blue.made.furnacejs.wrap.FurnaceWrapFactory;

public class Furnace
{
	/**
	 * An object containing the standard objects
	 */
	protected static ScriptableObject shared;
	
	public static FurnaceWrapFactory wrapFactory = new FurnaceWrapFactory();
}
