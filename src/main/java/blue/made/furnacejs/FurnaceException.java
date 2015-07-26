package blue.made.furnacejs;


public class FurnaceException extends RuntimeException
{
	private static final long serialVersionUID = -2673830522043727293L;
	
	public static class MemberNotFound extends FurnaceException
	{
		private static final long serialVersionUID = 715165112144088336L;
		
		public MemberNotFound(FObject in, String name)
		{
			super("Could not find member %s in %s", name, in.name());
		}
	}
	
	public static class MemberReadOnly extends FurnaceException
	{
		private static final long serialVersionUID = 507920141781221257L;
		
		public MemberReadOnly(FObject in, String name)
		{
			super("Member %s in %s is read-only", name, in.name());
		}
	}
	
	public FurnaceException(String msg, Object... arg)
	{
		super(String.format(msg, arg));
	}
	
	public FurnaceException(String msg, Throwable cause, Object... arg)
	{
		super(String.format(msg, arg), cause);
	}
	
	public String details()
	{
		return this.getMessage();
	}
}
