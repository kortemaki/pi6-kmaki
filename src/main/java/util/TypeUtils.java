package util;

import java.lang.reflect.Method;
import java.util.Iterator;
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

	public static <T> Method makeMethod(String className, String methodName, Class<T>... params)
	{
		try 
		{
			Class<?> cls = Class.forName(className);
			return cls.getMethod(methodName, params);
		} 
		catch(Throwable e)
		{
			throw new IllegalArgumentException("Error: " + e.getMessage());
		}
	}
	
	public static <T> TOP get(FSList list, Method checkMethod, T... args)
	{
		
		while(list instanceof NonEmptyFSList)
		{
			TOP el = (TOP) ((NonEmptyFSList) list).getHead();
			try
			{
				if((Boolean) checkMethod.invoke(el, args))
				{
					return el;
				}
			}
			catch(Throwable e)
			{
				//This element obviously did not match
				continue;
			}
			list = ((NonEmptyFSList) list).getTail();
		}
		return null;
	}
	
}


/**
 * Clever wrapper class to iterate through elements of list in reverse order
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