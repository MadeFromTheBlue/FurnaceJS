package blue.made.furnacejs.wrap.object;

public abstract class MemberSpec
{
	public final String name;
	
	public MemberSpec(String name)
	{
		this.name = name;
	}
	
	public abstract boolean isGet();
	
	public abstract boolean isSet();
	
	public Object get(Object on)
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean set(Object on, Object to)
	{
		throw new UnsupportedOperationException();
	}
}
