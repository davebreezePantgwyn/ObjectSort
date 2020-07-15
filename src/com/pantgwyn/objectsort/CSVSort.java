package com.pantgwyn.objectsort;

import org.apache.commons.csv.CSVRecord;

import com.pantgwyn.objectsort.FileWrapper.Type;

public class CSVSort extends Sort<CSVRecord>
{
	
	

	public CSVSort()
	{
		super();
	
	}

	public CSVSort(double memoryFactor, boolean verbose)
	{
		super(memoryFactor, verbose);

	}

	public CSVSort(double memoryFactor)
	{
		super(memoryFactor);
	
	}

	@Override
	protected Type getSortType()
	{
		return FileWrapper.Type.CSV ;
	}

}
