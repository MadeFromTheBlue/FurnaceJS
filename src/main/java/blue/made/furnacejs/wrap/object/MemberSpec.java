package blue.made.furnacejs.wrap.object;

import blue.made.furnacejs.FObject;

public abstract class MemberSpec
{
	public final String name;
	
	public MemberSpec(String name)
	{
		this.name = name;
	}
	
	public abstract boolean isGet();
	
	public abstract boolean isSet();
	
	public Object get(FObject on)
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean set(FObject on, Object to)
	{
		throw new UnsupportedOperationException();
	}
}
