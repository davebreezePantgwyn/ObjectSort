package com.pantgwyn.objectsort;

import org.apache.commons.csv.CSVRecord;

import com.pantgwyn.objectsort.FileWrapper.Type;

/**
 * Class for performing a CSV sort.
 * <p>
 * This provides a number of sort methods that may be used to sort CSV.
 * The caller must provide the appropriate comparator for the CSV structures being
 * sorted. The sort input may be from an input file or from a callback function,
 * Likewise the output may be either to an output file or to a callback
 * function.
 * 
 * @author Dave Breeze
 *
 */

public class CSVSort extends Sort<CSVRecord>
{
	/**
	 * Default constructor for class ObjectSort
	 * <p>
	 * This will by default use 50% of the available memory and non verbose
	 * messages.
	 */
	public CSVSort()
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
	public CSVSort(double memoryFactor, boolean verbose)
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
	public CSVSort(double memoryFactor)
	{
		super(memoryFactor);

	}

	@Override
	protected Type getSortType()
	{
		return FileWrapper.Type.CSV;
	}

}
