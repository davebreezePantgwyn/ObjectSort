package com.pantgwyn.objectsort;

import com.pantgwyn.objectsort.FileWrapper.Type;

/**
 * Main class for performing an object sort.
 * <p>
 * This provides a number of sort methods that may be used to sort java objects
 * that implement java.io.Serializable. The caller must provide the appropriate
 * Comparator for the objects being sorted.
 * 
 * @author Dave Breeze
 *
 * @param <T> class of the objects being sorted.
 */
public class ObjectSort<T> extends Sort<T>
{

	public ObjectSort()
	{
		super();

	}

	public ObjectSort(double memoryFactor, boolean verbose)
	{
		super(memoryFactor, verbose);

	}

	public ObjectSort(double memoryFactor)
	{
		super(memoryFactor);

	}


	
	@Override
	protected Type getSortType()
	{		
		return FileWrapper.Type.OBJECT ;
	}

}
