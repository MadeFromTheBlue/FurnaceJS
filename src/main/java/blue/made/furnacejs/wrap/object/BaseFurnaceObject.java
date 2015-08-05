package blue.made.furnacejs.wrap.object;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

public abstract class BaseFurnaceObject implements Wrapper, Scriptable
{
	public Object of;
	public Scriptable parent;
	
	public BaseFurnaceObject(Object of)
	{
		this.of = of;
	}
	
	@Override
	public String getClassName()
	{
		if (this.of == null)
		{
			return "null";
		}
		return "Object";
	}
	
	public abstract Object get(String name);
	
	@Override
	public Object get(String name, Scriptable start)
	{
		return this.get(name);
	}
	
	@Override
	public Object get(int index, Scriptable start)
	{
		return this.get(String.valueOf(index), start);
	}
	
	public abstract boolean has(String name);
	
	@Override
	public boolean has(String name, Scriptable start)
	{
		return this.has(name);
	}
	
	@Override
	public boolean has(int index, Scriptable start)
	{
		return this.has(String.valueOf(index), start);
	}
	
	public abstract void set(String name, Object value);
	
	@Override
	public void put(String name, Scriptable start, Object value)
	{
		this.set(name, value);
	}
	
	@Override
	public void put(int index, Scriptable start, Object value)
	{
		this.put(String.valueOf(index), start, value);
	}
	
	@Override
	public abstract void delete(String name);
	
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
	public abstract Object[] getIds();
	
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
