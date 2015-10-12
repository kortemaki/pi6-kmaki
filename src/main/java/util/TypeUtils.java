package util;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.TOP;

public class TypeUtils {
	
	/**
	 * Converts a List type to an FSList holding the same data
	 * 
	 * @param list the list to convert
	 * @param jcas the jcas with which to associate the resulting FSList
	 * @return the resulting FSList
	 */
	public static <T> FSList ListToFSListInCas(List<T> list, JCas jcas)
	{
		FSList next = new EmptyFSList(jcas);
		for(T el : new ListReverser<T>(list))
		{
			NonEmptyFSList node = new NonEmptyFSList(jcas);
			node.setTail(next);
			node.setHead((TOP) el);
			next = node;
		}
		return next;
	}

	/**
	 * Auxiliary method to simplify the process of instantiating a reflection method instance.
	 * @param <T>
	 *  
	 * @param className the name of the class whose method should be instantiated.
	 * @param methodName the name of the method to be instantiated.
	 * @param params the classes of the respective parameters to the method to be instantiated.
	 * @return the instantiated method.
	 */
	public static <T> Method instantiateMethod(Class<T> cls, String methodName, Class<?>... params)
	{
		try 
		{
			return cls.getMethod(methodName, params);
		} 
		catch(Throwable e)
		{
			throw new IllegalArgumentException("Error: " + e.getMessage() +
						"\nCould not instantiate method " + cls.getName() + "." + methodName);
		}
	}
	
	/**
	 * Adds TOP el to an FSList list and returns the updated FSList, associated with JCas jcas.
	 * 
	 * @param list the FSList to which to add el.
	 * @param el the TOP to add to list.
	 * @param jcas the JCas with which to associate created FSList objects.
	 * @return the updated FSList.
	 */
	public static FSList addToFSList(FSList list, TOP el, JCas jcas)
	{
		NonEmptyFSList next = new NonEmptyFSList(jcas);
		next.setHead(el);
		next.setTail(list);
		return next;
	}
	
	/**
	 * Finds the first TOP in the FSList list that checks out under Method checkMethod.
	 * If no such element is matched, returns null.
	 * 
	 * @param list the FSList to search for a matching TOP
	 * @param checkMethod the RTMethod with which to identify matching instances
	 * @return the matched element (if any), else null
	 */
	public static <T> TOP getFromFSList(FSList list, RTMethod checkMethod)
	{
		List<TOP> matched = getAllFromFSList(list,checkMethod);
		if(matched.size() > 0)
		{
			return matched.get(0);
		}
		return null;
	}
	
	/**
	 * Finds all TOP in the FSList list that check out under RTMethod checkMethod.
	 * If no such elements are matched, returns an empty List.
	 * 
	 * @param list the FSList to search for matching TOP
	 * @param checkMethod the RTMethod with which to identify matching instances
	 * @return a List<TOP> of the matched elements (if any)
	 */
	public static <T> List<TOP> getAllFromFSList(FSList list, RTMethod checkMethod)
	{
		List<TOP> results = new LinkedList<TOP>();
		while(list instanceof NonEmptyFSList)
		{
			TOP el = (TOP) ((NonEmptyFSList) list).getHead();
			try
			{
				if((Boolean) checkMethod.invoke(el))
				{
					results.add(el);
				}
			}
			catch(Throwable e)
			{
				//This element obviously did not match
				continue;
			}
			list = ((NonEmptyFSList) list).getTail();
		}
		return results;
	}
}

/**
 * Wrapper class to iterate through elements of list in reverse order
 * 
 * @author John Feminella, Allain Lalonde
 * @param <T>
 */
class ListReverser<T> implements Iterable<T> {
    private ListIterator<T> listIterator;        

    public ListReverser(List<T> wrappedList) {
        this.listIterator = wrappedList.listIterator(wrappedList.size());            
    }               

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            public boolean hasNext() {
                return listIterator.hasPrevious();
            }

            public T next() {
                return listIterator.previous();
            }

            public void remove() {
                listIterator.remove();
            }

        };
    }

}