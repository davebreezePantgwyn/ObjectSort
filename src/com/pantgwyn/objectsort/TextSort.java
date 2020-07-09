package com.pantgwyn.objectsort;

import com.pantgwyn.objectsort.FileWrapper.Type;

public class TextSort extends Sort<String>
{

	public TextSort()
	{
		super();

	}

	public TextSort(double memoryFactor, boolean verbose)
	{
		super(memoryFactor, verbose);

	}

	public TextSort(double memoryFactor)
	{
		super(memoryFactor);

	}


	@Override
	protected Type getSortType()
	{
		
		return FileWrapper.Type.TEXT ;
	}

}
