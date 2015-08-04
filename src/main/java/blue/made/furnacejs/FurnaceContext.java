package blue.made.furnacejs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * A context provides the environment for the execution of a script. JS executed
 * through the same context will share the same global scope. Contexts should
 * not be shared across threads, though many contexts can run in parallel.
 *
 */
//TODO Allow many contexts on one thread
public class FurnaceContext
{
	private Context context;
	private Scriptable scope;
	
	public final FurnaceContext parent;
	
	/**
	 * Creates a new context with access to the standard objects.
	 * {@link #enter()} must be called before the context can be used.
	 */
	public FurnaceContext()
	{
		this.parent = null;
	}
	
	/**
	 * Creates a new context with access to a parent context. {@link #enter()}
	 * must be called before the context can be used.
	 * 
	 * @param parent the parent context
	 */
	public FurnaceContext(FurnaceContext parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Has {@link #enter()} been successfully called without a corresponding
	 * {@link #exit()}?
	 * 
	 * @return true if the context is active, false otherwise
	 */
	public boolean isActive()
	{
		return this.context != null;
	}
	
	/**
	 * Enter the context. This will also create the global scope if it was not
	 * created by a previous call to {@link #enter()}. If this context has a
	 * parent context, then it will also be entered. Note that this method will
	 * do nothing is the context is already active.
	 */
	public void enter()
	{
		if (!this.isActive())
		{
			this.context = Context.enter();
			this.context.setWrapFactory(Furnace.wrapFactory);
			if (Furnace.shared == null)
			{
				Furnace.shared = this.context.initSafeStandardObjects();
				Furnace.shared.sealObject();
			}
			if (this.scope == null)
			{
				if (this.parent == null)
				{
					this.scope = this.createScope(Furnace.shared);
				}
				else
				{
					this.parent.enter();
					this.scope = this.createScope(this.parent.getScope());
				}
			}
		}
	}
	
	/**
	 * Exits the context if active. The global scope will remain intact.
	 */
	public void exit()
	{
		if (this.isActive())
		{
			Context.exit();
			this.context = null;
		}
	}
	
	/**
	 * @param proto an object containing variables accessible to the new scope
	 *            as its prototype
	 * @return a new top-level scope
	 */
	private Scriptable createScope(Scriptable proto)
	{
		Scriptable newScope = this.context.newObject(proto);
		newScope.setPrototype(proto);
		newScope.setParentScope(null);
		return newScope;
	}
	
	/**
	 * @return the global scope for this context
	 */
	public Scriptable getScope()
	{
		return this.scope;
	}
}
