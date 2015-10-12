package util;

import java.lang.reflect.Method;

import org.apache.uima.jcas.cas.TOP;

public class RTMethod {
	/**
	 * Bundler class holding a method and its parameters
	 * Can be invoked in the same way as a usual Method
	 * 
	 * @author maki
	 */
	private Method method;
	private Object[] params;
		
	public RTMethod(Method method, Object... params)
	{
		this.method = method;
		this.params = params;
	}
	
	public Object invoke(TOP obj)
	{
		try {
			return this.method.invoke(obj, this.params);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
}
