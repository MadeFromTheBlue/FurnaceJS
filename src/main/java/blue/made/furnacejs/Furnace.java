package blue.made.furnacejs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import blue.made.furnacejs.wrap.FurnaceWrapFactory;

public class Furnace
{
	private static Furnace instance;
	
	private Context context;
	private ScriptableObject shared;
	
	public Scriptable global;
	
	public Furnace()
	{
		instance = this;
	}
	
	public void enter()
	{
		if (this.context == null)
		{
			this.context = Context.enter();
			this.context.setWrapFactory(new FurnaceWrapFactory(this));
			this.shared = this.context.initSafeStandardObjects();
			this.shared.sealObject();
		}
	}
	
	public void exit()
	{
		if (this.context != null)
		{
			Context.exit();
			this.context = null;
		}
	}
	
	public FurnaceContext makeContext()
	{
		return new FurnaceContext(this.context, this.newObject());
	}
	
	public Scriptable newObject()
	{
		Scriptable o = this.context.newObject(this.shared);
		o.setParentScope(this.global);
		return o;
	}
	
	public static Furnace instance()
	{
		return instance;
	}
	
	public static Scriptable scope()
	{
		return instance.global;
	}
}
