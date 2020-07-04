package com.pantgwyn.objectsort;

/**
 * Interface for using a callback to provide sortin data.
 * <p>
 * This is provided as an optional alternative to sorting data from a file.
 * It is called by the ObjectSort sort function when it requires input.
 * This will replace reads to an input file.
 * @author Dave Breeze
 *
 * @param <T> class of the objects being sorted.
 */
public interface SortInCallback<T>
{
	/**
	 * Generate the next object to be inserted into the sort.
	 * <p>
	 * if the ObjectSort sort function is called with a SortInCallback then instead
	 * of reading data from a file sort will make a call to produceSortIn.
	 * 
	 * @return a class T object - or null for end of data.
	 */
	public T produceSortIn() ;
	
}
