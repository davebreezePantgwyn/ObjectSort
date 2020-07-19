package com.pantgwyn.objectsort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * A encapsulation of the work files used by ObjectSort.
 * <p>
 * An instance of FileWrapper is created for every sort work file required (if
 * any) to perform the sort. The work files are only used if the sort cannot be
 * completed in memory.
 * 
 * @author Dave Breeze
 *
 * @param <T> The class of objects being sorted
 */
public class FileWrapper<T>
{

	private static final int				BUFF_SIZE					= 8 * 1024;

	private String									fileName;
	private InputStream							inStream					= null;
	private OutputStream						outStream					= null;
	private T												currentInputItem	= null;
	private BufferedReader					bufferedReader		= null;
	private BufferedWriter					bufferedWriter		= null;
	private Type										type;
	private Destination							outDestination;
	private Destination							inSource;
	private SortInCallback<T>				inCallback				= null;
	private SortOutCallback<T>			outCallback				= null;
	private CSVPrinter							csvPrinter				= null;
	private Spliterator<CSVRecord>	csvIterator				= null;

	/**
	 * Constructor for FileWrapper
	 * <p>
	 * Create a FileWrapper for an input only file served by a callback
	 * 
	 * @param type       - the type of data being managed by the file
	 * @param inCallback - callback to obtain the data
	 */
	public FileWrapper(Type type, SortInCallback<T> inCallback)
	{
		this(type, null, inCallback, null);
	}

	/**
	 * Constructor for FileWrapper
	 * <p>
	 * Create a FileWrapper for an output only file served by a callback
	 * 
	 * @param type        - the type of data being managed by the file
	 * @param outCallback - callback to send the data to
	 */
	public FileWrapper(Type type, SortOutCallback<T> outCallback)
	{
		this(type, null, null, outCallback);
	}

	/**
	 * Constructor for FileWrapper
	 * <p>
	 * Create a FileWrapper for a work file - used as input and output.
	 * 
	 * @param fileName - the fully qualified filename of the FileWrapper
	 * @param type     - the type of data being managed by the file
	 */
	public FileWrapper(Type type, String fileName)
	{
		this(type, fileName, null, null);

	}

	private FileWrapper(Type type, String fileName, SortInCallback<T> inCallback, SortOutCallback<T> outCallback)
	{

		this.fileName			= fileName;
		this.type					= type;
		this.inCallback		= inCallback;
		this.outCallback	= outCallback;

		outDestination		= Destination.NONE;
		if (fileName != null)
			outDestination = Destination.FILE;
		if (outCallback != null)
			outDestination = Destination.CALLBACK;

		inSource = Destination.NONE;
		if (fileName != null)
			inSource = Destination.FILE;
		if (inCallback != null)
			inSource = Destination.CALLBACK;

	}

	/**
	 * @return String - fully qualified filename for the FileWrapper
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * @param fileName String - fully qualified filename for the FileWrapper
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * Get the last object that was read by this WorfFile
	 * 
	 * @return Object of class T that is the currently read object for the
	 *         FileWrapper or null if end of file reached
	 */
	public T getCurrentInputItem()
	{
		return currentInputItem;
	}

	/**
	 * Start the InputStream for this FileWrapper
	 * <p>
	 * Called at the start of a file merge to ready the FileWrapper for reading.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void startInStream() throws IOException,
															ClassNotFoundException
	{

		if (this.inSource == Destination.FILE)
		{
			FileInputStream inFileStream = new FileInputStream(new File(fileName));

			switch (type)
			{
				case OBJECT:
					inStream = new ObjectInputStream(inFileStream);
					break;
				case TEXT:
					bufferedReader = new BufferedReader(new InputStreamReader(inFileStream));
					break;
				case CSV:
					bufferedReader = new BufferedReader(new InputStreamReader(inFileStream));
					CSVParser parser = new CSVParser(bufferedReader, CSVFormat.DEFAULT);
					csvIterator = parser.spliterator();
					break;
				default:
					break;
			}
		}

		getNextInput();

	}

	/**
	 * Start the OutputStream for this FileWrapper
	 * <p>
	 * Called when a sort cannot be completed in memory. Makes the FileWrapper ready
	 * to receive objects.
	 * 
	 * @throws IOException
	 */
	public void startOutStream() throws IOException
	{

		if (outDestination != Destination.FILE)
			return;

		FileOutputStream outFileStream = new FileOutputStream(new File(fileName));

		switch (type)
		{
			case OBJECT:
				outStream = new ObjectOutputStream(outFileStream);
				break;
			case TEXT:
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(outFileStream, Charset.defaultCharset()), BUFF_SIZE);
				break;
			case CSV:
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(outFileStream, Charset.defaultCharset()), BUFF_SIZE);
				csvPrinter = new CSVPrinter(new BufferedWriter(bufferedWriter), CSVFormat.DEFAULT);
			default:
				break;
		}

	}

	/**
	 * Stop the OutputStream for this FileWrapper.
	 * 
	 * @throws IOException
	 */
	public void stopOutStream() throws IOException
	{

		switch (outDestination)
		{
			case FILE:

				switch (type)
				{
					case CSV:
						if (csvPrinter != null)
						{
							csvPrinter.flush();
							Sort.close(csvPrinter);
						}
						break;
					default:
						if (outStream != null)
						{
							outStream.flush();
							Sort.close(outStream);
						} else
						{
							bufferedWriter.flush();
							Sort.close(bufferedWriter);
						}
						break;
				}

				break;
			case CALLBACK:
				outCallback.consumeSortOut(null);
				break;
			default:
				break;
		}

	}

	/**
	 * Stop the InputStream for this FileWrapper.
	 * 
	 * @throws IOException
	 */
	public void stopInStream() throws IOException
	{
		if (inSource == Destination.FILE)
			Sort.close(inStream, bufferedReader);
	}

	/**
	 * Read the next object from the FileWrapper.
	 * <p>
	 * Called in merge processing. The next object is read and currentInputItem is
	 * updated.
	 * 
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void getNextInput() throws ClassNotFoundException
	{

		switch (inSource)
		{
			case FILE:

				try
				{
					switch (type)
					{
						case OBJECT:
							currentInputItem = (T) ((ObjectInputStream) inStream).readUnshared();
							break;
						case TEXT:
							currentInputItem = (T) bufferedReader.readLine();
							break;
						case CSV:
							Consumer<? super CSVRecord> action = a -> currentInputItem = (T) a;
							if (!csvIterator.tryAdvance(action))
								currentInputItem = null;
							break;
						default:
							break;
					}

				} catch (IOException e)
				{
					currentInputItem = null;
				}

				break;

			case CALLBACK:
				currentInputItem = (T) inCallback.produceSortIn();
				break;
			default:
				currentInputItem = null;
				break;

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
		file.delete();
	}

	public enum Type
	{
		OBJECT,
		CSV,
		TEXT;
	}

	private enum Destination
	{
		FILE,
		CALLBACK,
		NONE;
	}

	public void write(T o) throws IOException
	{
		switch (outDestination)
		{
			case FILE:

				switch (type)
				{
					case OBJECT:
						((ObjectOutputStream) outStream).writeUnshared(o);
						((ObjectOutputStream) outStream).reset();
						break;
					case TEXT:
						String s = (String) o;
						bufferedWriter.write(s);
						bufferedWriter.newLine();
						break;
					case CSV:
						CSVRecord record = (CSVRecord) o;
						csvPrinter.printRecord(record);
						break;
					default:
						break;
				}

				break;

			case CALLBACK:

				outCallback.consumeSortOut(o);

				break;
			default:
				break;

		}

	}

	public void pushData(	List<T> objQueue,
												Comparator<T> comparator)
	{
		
		objQueue.sort(comparator);

		for (T t:objQueue)
		{
			try
			{
				write(t) ;
			} catch (IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException("IO error on streams");
			}
		}
		
		objQueue.clear();

	}

}
