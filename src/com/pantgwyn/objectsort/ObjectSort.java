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

	/**
	 * Default constructor for class ObjectSort
	 * <p>
	 * This will by default use 50% of the available memory and non verbose
	 * messages.
	 */
	public ObjectSort()
	{
		super();

	}

	/**
	 * Constructor for class ObjectSort
	 * 
	 * @param memoryFactor a double that defines the available memory usage. This is
	 *                     a value that must be less than 1. It represents the
	 *                     fraction of available memory ObjectSort will aim to use.
	 *                     <p>
	 *                     For example a value of 0.1 will result in 10% of
	 *                     available memory being used and a value of 0.75 will
	 *                     result in 75% of available memory being used. This value
	 *                     impacts whether or not an external or internal sort is
	 *                     performed.
	 * 
	 * @param verbose      a boolean that controls verbose messaging
	 */
	public ObjectSort(double memoryFactor, boolean verbose)
	{
		super(memoryFactor, verbose);

	}
	/**
	 * Constructor for class ObjectSort
	 * 
	 * @param memoryFactor a double that defines the available memory usage. This is
	 *                     a value that must be less than 1. It represents the
	 *                     fraction of available memory ObjectSort will aim to use.
	 *                     <p>
	 *                     For example a value of 0.1 will result in 10% of
	 *                     available memory being used and a value of 0.75 will
	 *                     result in 75% of available memory being used. This value
	 *                     impacts whether or not an external or internal sort is
	 *                     performed.
	 */
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
