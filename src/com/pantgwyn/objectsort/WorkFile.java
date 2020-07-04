package com.pantgwyn.objectsort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A encapsulation of the work files used by ObjectSort.
 * <p>
 * An instance of WorkFile is created for every sort work file required (if any) to 
 * perform the sort. The work files are only used if the sort cannot be completed in
 * memory. 
 * @author Dave Breeze
 *
 * @param <T> The class of objects being sorted
 */
public class WorkFile<T>
{
	private String fileName;
	private ObjectInputStream inStream = null;
	private ObjectOutputStream outStream = null;
	private FileInputStream inFileStream = null;
	private FileOutputStream outFileStream = null;
	private T currentInputItem = null ;
	
	/**
	 * Constructor for WorkFile
	 * <p>
	 * Create a WorkFile with the specified work file name.
	 * @param fileName - the fully qualified filename of the WorkFile
	 */
	public WorkFile(String fileName)
	{
		setFileName(fileName);
	}

	/**
	 * @return String - fully qualified filename for the WorkFile
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * @param fileName String - fully qualified filename for the WorkFile
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;		
	}

	/**
	 * Get the last object that was read by this WorfFile
	 * @return Object of class T that is the currently read object for the WorkFile or null
	 * if end of file reached
	 */
	public T getCurrentInputItem()
	{
		return currentInputItem;
	}

	
	
	/**
	 * Start the InputStream for this WorkFile
	 * <p>Called at the start of a file merge to ready the WorkFile for reading.
	 * @return ObjectInputStream of the WorkFile.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public ObjectInputStream startInStream() throws IOException, ClassNotFoundException
	{
		
		inFileStream	= new FileInputStream(new File(fileName));
		inStream					= new ObjectInputStream(inFileStream);
		
		
		getNextInput() ;
		
		return inStream ; 
	}
	
	/**
	 * Start the OutputStream for this WorkFile
	 * <p>Called when a sort cannot be completed in memory. Makes the WorkFile ready to 
	 * receive objects.
	 * @return ObjectOutputStream of the WorkFile.
	 * @throws IOException
	 */
	public ObjectOutputStream startOutStream() throws IOException
	{
		outFileStream	= new FileOutputStream(new File(fileName));
		outStream					= new ObjectOutputStream(outFileStream);
		
		return outStream ; 
	}
	
	/**
	 * Stop the OutputStream for this WorkFile.
	 * @throws IOException
	 */
	public void stopOutStream() throws IOException
	{
		if (outStream != null)
			outStream.flush();
		ObjectSort.close(outStream,outFileStream) ;
	}
	
	/**
	 * Stop the InputStream for this WorkFile.
	 * @throws IOException
	 */
	public void stopInStream() throws IOException
	{

		ObjectSort.close(inStream,inFileStream) ;
	}

	/**
	 * Read the next object from the WorKFile.
	 * <p>
	 * Called in merge processing. The next object is read and currentInputItem
	 * is updated.
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void getNextInput() throws ClassNotFoundException
	{
		try
		{
			currentInputItem = (T) inStream.readUnshared() ;
		} 
		catch (IOException e)
		{
			currentInputItem = null ;
		}
		
	}
	/**
	 * Delete the file from the io system
	 * <p>
	 * Called at the end of a merge process to ensure that all created work files
	 * are removed from the i/o subsystem.
	 */
	public void deleteFile()
	{
		File file = new File(fileName);
		file.delete() ;
	}
	
	
}
