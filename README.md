#FurnaceJS#

##About##
**FurnaceJS** is a framework for easily embedding and interfacing JavaScript into a Java application.

Most script embedding systems for Java either automatically wrap all public functions and variables in an object or require the creation of custom wrappers. In order to create a cleaner and more secure interface between the script and the application; FurnaceJS uses a system of annotations instead.

##Examples##
###Java###
```java
public abstract class Thing
{
	@JSConst("foo")
	public String foo = "Bar";

	@JSVar("asdf")
	public String asdf = "ghjk";

	public String name;
	
	@JSFunction("rename")
	public void rename(String name)
	{
		this.name = name;
	}
	
	@JSGet("name")
	public String getName()
	{
		return this.name;
	}
		
	@JSSet("name")
	public void setName(String name)
	{
		this.rename(name);
	}
}
```
###JS###
```js
thing.foo = "NotBar"; //throws error
thing.foo == "Bar"; //true

thing.asdf ="kjhg";
thing.asdf == "kjhg"; //true

thing.name = "Test1"; //same as thing.rename("Test1")
thing.rename("Test2");
thing.name == "Test2"; //true
```

----------
