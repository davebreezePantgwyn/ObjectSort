package com.pantgwyn.objectsort;

/**
 * Interface for using a callback to process sorted data.
 * <p>
 * This is provided as an optional alternative to writing the sorted data to a file.
 * It is called by the ObjectSort sort function when it has the next sorted
 * object to output. This will replace writes to an output file.
 * @author Dave Breeze
 *
 * @param <T> class of the objects being sorted.
 */
public interface SortOutCallback<T>
{
	/**
	 * Processes the next object to be output from the sort.
	 * <p>
	 * if the ObjectSort sort function is called with a SortOutCallback then instead
	 * of writing data from the sort to a file sort will make a call to consumeSortOut.
	 * 
	 * @param sortObj - a class T object or null for end of data.
	 */
	public void consumeSortOut(T sortObj) ;
}
