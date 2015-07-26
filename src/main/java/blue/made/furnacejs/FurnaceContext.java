package blue.made.furnacejs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

public class FurnaceContext
{
	private Context context;
	private Scriptable scope;
	
	public FurnaceContext(Context context, Scriptable scope)
	{
		this.context = context;
		this.scope = scope;
	}
	
	public Object eval(String name, String js)
	{
		Object value = this.context.evaluateString(this.scope, js, name + String.format("%04x", (int) (Math.random() * 0x10000)), 0, null);
		if (value == null)
		{
			return null;
		}
		if (value == Context.getUndefinedValue())
		{
			return null;
		}
		if (value instanceof Wrapper)
		{
			value = ((Wrapper) value).unwrap();
		}
		return value;
	}
}
