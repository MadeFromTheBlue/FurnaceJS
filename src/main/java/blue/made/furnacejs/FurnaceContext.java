package blue.made.furnacejs;

import java.util.HashSet;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import blue.made.furnacejs.wrap.FurnaceWrapFactory;

/**
 * A context provides the environment for the execution of a script. JS executed
 * through the same context will share the same global scope. If a parent
 * context is given, then global variables defined in the child context are
 * still stored in the child context, but global variables in the parent are
 * visible to the child (the parent scope is treated as the child scope's
 * prototype.) Contexts should not be shared across threads while active (though
 * different contexts can run in parallel even when related.)
 *
 */
public class FurnaceContext
{
	/**
	 * Rhino contexts are per-thread. As many Furnace contexts can exist on the
	 * same thread, a system is needed to keep them all friendly.
	 */
	private static class ThreadContextManager
	{
		/**
		 * The Rhino context belonging to this {@link #ThreadContextManager}'s
		 * thread
		 */
		public Context context;
		/**
		 * The {@link #FurnaceContext}s active on this thread
		 */
		public HashSet<FurnaceContext> users = new HashSet<>();
		
		/**
		 * Called when a {@link #FurnaceContext} enters this thread
		 */
		public void join(FurnaceContext furnacecontext)
		{
			if (this.context == null)
			{
				//Initialize the Rhino context
				this.context = Context.enter();
				this.context.setWrapFactory(FurnaceWrapFactory.instance);
			}
			this.users.add(furnacecontext);
			furnacecontext.context = this.context;
			furnacecontext.mymanager = this;
		}
		
		/**
		 * Called when a {@link #FurnaceContext} exits this thread
		 */
		public void leave(FurnaceContext furnacecontext)
		{
			if (this.users.remove(furnacecontext))
			{
				furnacecontext.context = null;
				furnacecontext.mymanager = null;
				if (this.users.isEmpty())
				{
					//Exit the Rhino context
					Context.exit();
					this.context = null;
				}
			}
		}
		
		public void clear()
		{
			for (FurnaceContext user : this.users)
			{
				user.context = null;
				user.mymanager = null;
			}
			this.users.clear();
			Context.exit();
			this.context = null;
		}
	}
	
	/**
	 * This stores a {@link #ThreadContextManager} for each thread where a
	 * {@link #FurnaceContext} is active.
	 */
	private static final ThreadLocal<ThreadContextManager> contextManager = new ThreadLocal<ThreadContextManager>()
	{
		@Override
		protected ThreadContextManager initialValue()
		{
			return new ThreadContextManager();
		}
	};
	
	/**
	 * The rhino context for this context's active thread
	 */
	private Context context;
	/**
	 * The ThreadContextManager for this context's active thread
	 */
	private ThreadContextManager mymanager;
	
	/**
	 * The global scope
	 */
	private Scriptable scope;
	
	/**
	 * This contex's parent context (if there is one)
	 */
	public final FurnaceContext parent;
	
	/**
	 * if true, then the parent/standard objects are not usable by this context
	 * (only used for the instantiation of the global scope)
	 */
	private boolean isolate = false;
	
	/**
	 * Creates a new context with access to the standard objects.
	 * {@link #enter()} must be called before the context can be used.
	 */
	public FurnaceContext()
	{
		this.parent = null;
	}
	
	/**
	 * Creates a new context. {@link #enter()} must be called before the context
	 * can be used.
	 * 
	 * @param standard should the standard objects be available
	 */
	public FurnaceContext(boolean standard)
	{
		this();
		this.isolate = !standard;
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
			contextManager.get().join(this);
			if (Furnace.shared == null)
			{
				Furnace.shared = this.context.initSafeStandardObjects();
				Furnace.shared.sealObject();
			}
			if (this.scope == null)
			{
				if (this.parent == null)
				{
					this.scope = this.createScope(Furnace.shared, !this.isolate);
				}
				else
				{
					this.parent.enter();
					this.scope = this.createScope(this.parent.getScope(), !this.isolate);
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
			this.mymanager.leave(this);
		}
	}
	
	/**
	 * This is equivalent to calling {@link #exit()} on every
	 * {@link #FurnaceContext} that shares this context's active thread (the
	 * active thread is the thread that called {@link #enter()} NOT the current
	 * thread.)
	 */
	public void exitAll()
	{
		if (this.isActive())
		{
			this.mymanager.clear();
		}
	}
	
	/**
	 * @param proto an object containing variables accessible to the new scope
	 *            as its prototype
	 * @param use should the prototype be accessible (if false it is only used
	 *            for instantiation)
	 * @return a new top-level scope
	 */
	private Scriptable createScope(Scriptable proto, boolean use)
	{
		Scriptable newScope = this.context.newObject(proto);
		newScope.setPrototype(use ? proto : null);
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
