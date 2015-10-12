package util;

import java.lang.reflect.Method;

import org.apache.uima.jcas.cas.TOP;

/**
 * Wrapper class implementing useful methods for interfacing with uima TOP objects
 * 
 * @author maki
 */

public class TOPWrapper extends TOP{
	
	/**
	 * Assert that this TOP is an instance of the given Class<?> cls
	 * 
	 * @param cls
	 * @return 
	 */
	public boolean assertClass(Class<?> cls)
	{
		return cls.isInstance(this);
	}
	/**
	 * Compares a field of this TOP with the Comparable value passed in
	 * 
	 * @param fieldGetter
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean compareField(Method fieldGetter, Comparable<?> value)
	{
		try {
			return ((Comparable<Comparable<?>>) fieldGetter.invoke(this)).compareTo(value) == 0;
		} catch (Throwable e) {
			throw new IllegalArgumentException("Error: " + e.getMessage());
		}
	}
		
	/**
	 * Allows the logical combination of several boolean query methods evaluated on this TOP. 
	 * @param obj
	 * @param methods
	 * @return
	 */
	public boolean logicalAnd(RTMethod... methods)
	{
		boolean result = true;
		for(RTMethod method : methods)
		{
			result &= (Boolean) method.invoke(this);
			if(!result)
				break;
		}
		return result;
	}
	
	
}
