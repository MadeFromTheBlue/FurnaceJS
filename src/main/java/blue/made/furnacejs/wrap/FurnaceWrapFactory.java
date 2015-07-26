package blue.made.furnacejs.wrap;

import org.mozilla.javascript.WrapFactory;

import blue.made.furnacejs.Furnace;

public class FurnaceWrapFactory extends WrapFactory
{
	public Furnace furnace;
	
	public FurnaceWrapFactory(Furnace furnace)
	{
		this.furnace = furnace;
	}
}
