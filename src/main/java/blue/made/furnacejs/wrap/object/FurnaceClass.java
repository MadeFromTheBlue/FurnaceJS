package blue.made.furnacejs.wrap.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Scriptable;

import blue.made.furnacejs.FurnaceException;
import blue.made.furnacejs.annotation.JSConst;
import blue.made.furnacejs.annotation.JSFunc;
import blue.made.furnacejs.annotation.JSGet;
import blue.made.furnacejs.annotation.JSListMembers;
import blue.made.furnacejs.annotation.JSLiteralNull;
import blue.made.furnacejs.annotation.JSNew;
import blue.made.furnacejs.annotation.JSSet;
import blue.made.furnacejs.annotation.JSVar;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;

public class FurnaceClass
{
	public final Class<?> clazz;
	/**
	 * Specific members (only apply to members with the correct name)
	 */
	public ArrayListMultimap<String, MemberSpec> elems = ArrayListMultimap.create();
	/**
	 * All general {@link JSGet} methods (apply to all members)
	 */
	public ArrayList<Method> allGet = new ArrayList<Method>();
	/**
	 * All general {@link JSSet} methods (apply to all members)
	 */
	public ArrayList<Method> allSet = new ArrayList<Method>();
	/**
	 * All methods that add keys to the member list (marked with
	 * {@link JSListMembers})
	 */
	public ArrayList<Method> listExtra = new ArrayList<Method>();
	public Method asFunctionMethod = null;
	
	public FurnaceClass(Class<?> clazz)
	{
		this.clazz = clazz;
		this.init();
	}
	
	protected void init()
	{
		for (Method m : this.clazz.getMethods())
		{
			this.initMethod(m);
		}
		for (Field f : this.clazz.getFields())
		{
			this.initField(f);
		}
	}
	
	protected void initMethod(Method m)
	{
		if (m.isAnnotationPresent(JSListMembers.class))
		{
			this.listExtra.add(m);
		}
		else if (m.isAnnotationPresent(JSFunc.class))
		{
			
		}
		else if (m.isAnnotationPresent(JSGet.class))
		{
			String id = m.getAnnotation(JSGet.class).value();
			if (id.isEmpty())
			{
				this.allGet.add(m);
			}
			else
			{
				
			}
		}
		else if (m.isAnnotationPresent(JSSet.class))
		{
			String id = m.getAnnotation(JSSet.class).value();
			if (id.isEmpty())
			{
				this.allSet.add(m);
			}
			else
			{
				
			}
		}
		else if (m.isAnnotationPresent(JSNew.class))
		{
			
		}
	}
	
	protected void initField(Field f)
	{
		if (f.isAnnotationPresent(JSVar.class))
		{
			String id = f.getAnnotation(JSVar.class).value();
			this.elems.put(id, new MemberSpec(id)
			{
				@Override
				public boolean isGet()
				{
					return true;
				}
				
				@Override
				public boolean isSet()
				{
					return true;
				}
				
				@Override
				public Object get(Object on)
				{
					try
					{
						return f.get(on);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						throw new FurnaceException("Could not get the value of %s", on.getClass().getCanonicalName(), this.name);
					}
				}
				
				@Override
				public boolean set(Object on, Object to)
				{
					try
					{
						f.set(on, to);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						throw new FurnaceException("Could not set the value of %s", on.getClass().getCanonicalName(), this.name);
					}
					return true;
				}
			});
		}
		else if (f.isAnnotationPresent(JSConst.class))
		{
			String id = f.getAnnotation(JSVar.class).value();
			this.elems.put(id, new MemberSpec(id)
			{
				@Override
				public boolean isGet()
				{
					return true;
				}
				
				@Override
				public boolean isSet()
				{
					return false;
				}
				
				@Override
				public Object get(Object on)
				{
					try
					{
						return f.get(on);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						throw new FurnaceException("Could not get the value of %s", on.getClass().getCanonicalName(), this.name);
					}
				}
			});
		}
	}
	
	/**
	 * Attempts to get a member in the object
	 * 
	 * @param on The object containing the member
	 * @param id The name of the member to get
	 * @return The value of the member
	 */
	public Object get(Object on, String id)
	{
		//check specific members
		for (MemberSpec spec : this.elems.get(id))
		{
			//does the member theoretically allow getting
			if (spec.isGet())
			{
				Object out = spec.get(on);
				//if the value is not undefined, return it
				if (out != Scriptable.NOT_FOUND)
				{
					return out;
				}
			}
		}
		//by default, undefined should be returned
		boolean undef = true;
		for (Method m : this.allGet)
		{
			try
			{
				Object out = null;
				out = m.invoke(on, id);
				//if we did not get null
				if (out != null)
				{
					if (out != Scriptable.NOT_FOUND)
					{
						//we found it
						return out;
					}
					//even if NOT_FOUND was returned, we should not necessarily return undefined because a literal null may have been found
				}
				//if we got null, and {@link JSLiteralNull} is present
				else if (m.isAnnotationPresent(JSLiteralNull.class))
				{
					//literal null (null was found), null should be returned, not undefined
					undef = false;
					//continue searching for a non-null value, which overrides a null one
				}
			}
			catch (Exception e)
			{
				throw new FurnaceException("An error occured while getting %s in a %s object", e, id, on.getClass().getCanonicalName());
			}
		}
		//we could not find the member
		if (undef)
		{
			return Scriptable.NOT_FOUND;
		}
		//we found null
		return null;
	}
	
	/**
	 * Attempts to set a member in the object
	 * 
	 * @param on The object containing the member
	 * @param id The name of the member to set
	 * @param to The value to set the member to
	 */
	public void set(Object on, String id, Object to)
	{
		List<MemberSpec> mems = this.elems.get(id);
		//check specific members
		for (MemberSpec spec : mems)
		{
			//does the member theoretically allow setting
			if (spec.isSet())
			{
				//did the set succeed
				if (spec.set(on, to))
				{
					//no need to try the general setters, we are done
					return;
				}
			}
		}
		//a flag to mark if successful
		boolean flag = false;
		for (Method m : this.allSet)
		{
			try
			{
				Object out = m.invoke(on, id, to);
				if (out == null)
				{
					//it worked, but keep trying
					flag = true;
				}
				//if the setter function returns a boolean, check if it is true
				else if (out instanceof Boolean)
				{
					if ((Boolean) out)
					{
						//don't keep trying
						return;
					}
					//if it was false, keep trying, and don't assume success
				}
			}
			catch (Exception e)
			{
				throw new FurnaceException("An error occured while getting %s in a %s object", e, id, on.getClass().getCanonicalName());
			}
		}
		if (flag)
		{
			//we are good
			return;
		}
		if (mems.isEmpty())
		{
			//we couldn't find any specific members and none of the general setters worked
			throw new FurnaceException.MemberNotFound(on, id);
		}
		else
		{
			//we found specific members, but they were all read-only (and none of the general setters worked)
			throw new FurnaceException.MemberReadOnly(on, id);
		}
	}
	
	/**
	 * Checks if the object has the member
	 * 
	 * @param in The object
	 * @param id The member to search for
	 * @return true if the member can be found or is listed, false otherwise
	 */
	public boolean has(Object in, String id)
	{
		//Check with standard members
		boolean has = this.elems.containsKey(id);
		if (has == true)
		{
			//found one
			return true;
		}
		//check extra members, exiting early if found
		return this.onListedExtra(in, o -> id.equals(o));
	}
	
	/**
	 * List the objects members
	 * 
	 * @param in The object
	 * @return A list of member names
	 */
	public Set<String> list(Object in)
	{
		HashSet<String> list = new HashSet<String>(this.elems.keySet());
		this.onListedExtra(in, o -> {
			if (o instanceof String)
			{
				list.add((String) o);
			}
			//we want to get all of the members, never exit early
			return false;
		});
		return list;
	}
	
	/**
	 * Calls methods annotated with {@link JSListMembers} and iterates on the
	 * returned lists, arrays, or objects. Each object is passed to a predicate.
	 * This method terminates early if the predicate returns true.
	 * 
	 * @param in The object with the methods
	 * @param consume The predicate to pass the results to
	 * @return true if the predicate ever returned true, false otherwise
	 */
	private boolean onListedExtra(Object in, Predicate<Object> consume)
	{
		for (Method m : this.listExtra)
		{
			try
			{
				//get extra members
				Object out = m.invoke(in);
				//we received some sort of list
				if (out instanceof Iterable)
				{
					Iterable<?> iter = (Iterable<?>) out;
					for (Object o : iter)
					{
						if (consume.apply(o))
						{
							return true;
						}
					}
				}
				//we received some sort of array
				else if (out != null && out.getClass().isArray())
				{
					int l = Array.getLength(out);
					for (int i = 0; i < l; i++)
					{
						if (consume.apply(Array.get(out, i)))
						{
							return true;
						}
					}
				}
				//we received null or some other type
				else
				{
					if (consume.apply(out))
					{
						return true;
					}
				}
			}
			//make safe
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
