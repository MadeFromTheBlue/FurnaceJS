package blue.made.furnacejs;

public interface FObject
{
	public default String name()
	{
		String name = this.getClass().getCanonicalName();
		if (name == null)
		{
			return "Object";
		}
		if (name.startsWith("F"))
		{
			name = name.substring(1);
		}
		return name;
	}
}
