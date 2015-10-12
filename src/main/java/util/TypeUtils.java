package util;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;

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
		
		return new FSList(jcas);
	}

}
