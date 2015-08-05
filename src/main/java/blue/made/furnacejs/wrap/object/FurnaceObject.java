package blue.made.furnacejs.wrap.object;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class FurnaceObject extends BaseFurnaceObject
{
	public FurnaceClass spec;
	
	public FurnaceObject(Object of, FurnaceClass clazz, Scriptable scope)
	{
		super(of);
		this.parent = scope;
		this.spec = clazz;
	}
	
	@Override
	public String getClassName()
	{
		if (this.of == null)
		{
			return "null";
		}
		return "JavaObject";
	}
	
	@Override
	public Object get(String name)
	{
		return Context.javaToJS(this.spec.get(this.of, name), this);
	}
	
	@Override
	public boolean has(String name)
	{
		return this.spec.has(this.of, name);
	}
	
	@Override
	public void set(String name, Object value)
	{
		this.spec.set(this.of, name, value);
	}
	
	@Override
	public void delete(String name)
	{
	}
	
	@Override
	public Object[] getIds()
	{
		return this.spec.list(this.of).toArray();
	}
}
