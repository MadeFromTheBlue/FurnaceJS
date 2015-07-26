package blue.made.furnacejs.wrap.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Scriptable;

import blue.made.furnacejs.FObject;
import blue.made.furnacejs.FurnaceException;
import blue.made.furnacejs.annotation.JSConst;
import blue.made.furnacejs.annotation.JSExtraMembers;
import blue.made.furnacejs.annotation.JSFunc;
import blue.made.furnacejs.annotation.JSGet;
import blue.made.furnacejs.annotation.JSNew;
import blue.made.furnacejs.annotation.JSNullIsUndef;
import blue.made.furnacejs.annotation.JSSet;
import blue.made.furnacejs.annotation.JSVar;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;

public class FurnaceClass
{
	public final Class<? extends FObject> clazz;
	public ArrayListMultimap<String, MemberSpec> elems = ArrayListMultimap.create();
	public ArrayList<Method> allGet = new ArrayList<Method>();
	public ArrayList<Method> allSet = new ArrayList<Method>();
	public ArrayList<Method> listExtra = new ArrayList<Method>();
	
	public FurnaceClass(Class<? extends FObject> clazz)
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
		if (m.isAnnotationPresent(JSExtraMembers.class))
		{
			this.listExtra.add(m);
		}
		else if (m.isAnnotationPresent(JSFunc.class))
		{
			
		}
		else if (m.isAnnotationPresent(JSGet.class))
		{
			String id = m.getAnnotation(JSGet.class).value();
			if (id.equals("*"))
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
			if (id.equals("*"))
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
			
		}
		else if (f.isAnnotationPresent(JSConst.class))
		{
			
		}
	}
	
	public Object get(FObject on, String id)
	{
		for (MemberSpec spec : this.elems.get(id))
		{
			if (spec.isGet())
			{
				Object out = spec.get(on);
				if (out != Scriptable.NOT_FOUND)
				{
					return out;
				}
			}
		}
		boolean flag = false;
		for (Method m : this.allGet)
		{
			try
			{
				Object out = null;
				out = m.invoke(on, id);
				if (out != null)
				{
					if (out != Scriptable.NOT_FOUND)
					{
						return out;
					}
				}
				else if (!m.isAnnotationPresent(JSNullIsUndef.class))
				{
					flag = true;
				}
			}
			catch (Exception e)
			{
				throw new FurnaceException("An error occured while getting %s in a %s object", e, id, on.name());
			}
		}
		if (flag)
		{
			return null;
		}
		return Scriptable.NOT_FOUND;
	}
	
	public void set(FObject on, String id, Object to)
	{
		List<MemberSpec> mems = this.elems.get(id);
		for (MemberSpec spec : mems)
		{
			if (spec.isSet())
			{
				if (spec.set(on, to))
				{
					return;
				}
			}
		}
		boolean flag = false;
		for (Method m : this.allSet)
		{
			try
			{
				Object out = m.invoke(on, id, to);
				if (out == null)
				{
					flag = true;
				}
				else if (out instanceof Boolean)
				{
					if ((Boolean) out)
					{
						return;
					}
				}
			}
			catch (Exception e)
			{
				throw new FurnaceException("An error occured while getting %s in a %s object", e, id, on.name());
			}
		}
		if (flag)
		{
			return;
		}
		if (mems.isEmpty())
		{
			throw new FurnaceException.MemberNotFound(on, id);
		}
		else
		{
			throw new FurnaceException.MemberReadOnly(on, id);
		}
	}
	
	public boolean has(FObject in, String id)
	{
		boolean has = this.elems.containsKey(id);
		if (has == true)
		{
			return true;
		}
		return this.onListedExtra(in, o -> id.equals(o));
	}
	
	public Set<String> list(FObject in)
	{
		HashSet<String> list = new HashSet<String>(this.elems.keySet());
		this.onListedExtra(in, o -> {
			if (o instanceof String)
			{
				list.add((String) o);
			}
			return false;
		});
		return list;
	}
	
	/**
	 * Calls methods annotated with JSExtraMembers and iterates on the returned
	 * lists, arrays, or objects. Each object is passed to a predicate.
	 * This method terminates early if the predicate returns true.
	 * 
	 * @param in The object with the methods
	 * @param consume The predicate to pass the results to
	 * @return true if the predicate ever returned true, false otherwise
	 */
	private boolean onListedExtra(FObject in, Predicate<Object> consume)
	{
		for (Method m : this.listExtra)
		{
			try
			{
				Object out = m.invoke(in);
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
				else
				{
					if (consume.apply(out))
					{
						return true;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
