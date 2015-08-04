package blue.made.furnacejs.wrap.object;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

public class FurnaceObject implements Wrapper, Scriptable
{
	public FurnaceClass spec;
	public Object of;
	public Scriptable parent;
	
	public FurnaceObject(Object of, FurnaceClass clazz, Scriptable scope)
	{
		this.of = of;
		this.spec = clazz;
		this.parent = scope;
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
	public Object get(String name, Scriptable start)
	{
		return Context.javaToJS(this.spec.get(this.of, name), this);
	}
	
	@Override
	public Object get(int index, Scriptable start)
	{
		return this.get(String.valueOf(index), start);
	}
	
	@Override
	public boolean has(String name, Scriptable start)
	{
		return this.spec.has(this.of, name);
	}
	
	@Override
	public boolean has(int index, Scriptable start)
	{
		return this.has(String.valueOf(index), start);
	}
	
	@Override
	public void put(String name, Scriptable start, Object value)
	{
		this.spec.set(this.of, name, value);
	}
	
	@Override
	public void put(int index, Scriptable start, Object value)
	{
		this.put(String.valueOf(index), start, value);
	}
	
	@Override
	public void delete(String name)
	{
	}
	
	@Override
	public void delete(int index)
	{
		this.delete(String.valueOf(index));
	}
	
	@Override
	public Scriptable getPrototype()
	{
		return null;
	}
	
	@Override
	public void setPrototype(Scriptable prototype)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Scriptable getParentScope()
	{
		return this.parent;
	}
	
	@Override
	public void setParentScope(Scriptable parent)
	{
		this.parent = parent;
	}
	
	@Override
	public Object[] getIds()
	{
		return this.spec.list(this.of).toArray();
	}
	
	@Override
	public Object getDefaultValue(Class<?> hint)
	{
		return null;
	}
	
	@Override
	public boolean hasInstance(Scriptable instance)
	{
		return false;
	}
	
	@Override
	public Object unwrap()
	{
		return this.of;
	}
}
