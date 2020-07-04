package com.pantgwyn.objectsort;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ObjectSort<T>
{

	private static final Logger	LOG																			= Logger.getLogger(ObjectSort.class.getName());
	private static final double	DEFAULT_MEMORY_FACTOR	= 0.5;

	private double														memoryFactor;
	private int																	listLimit;
	private int																	workFileId;
	private Level															logLevel;

	/**
	 * Default constructor for class ObjectSort
	 * <p>
	 * This will by default use 50% of the available memory and non verbose
	 * messages.
	 */

	public ObjectSort()
	{
		this(DEFAULT_MEMORY_FACTOR, false);
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
		this(memoryFactor, false);
	}

	/**
	 * Constructor for class ObjectSort
	 * 
	 * @param memoryFactor a duoble that defines the available memory usage. This is
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
		this.memoryFactor	= memoryFactor;

		logLevel										= Level.FINE;
		if (verbose)
			logLevel = Level.INFO;

		if (memoryFactor >= 1)
		{
			LOG.log(Level.WARNING,
											"invalid memory factor specified " + memoryFactor + " defaulting to " + DEFAULT_MEMORY_FACTOR);
		}
	}

	/**
	 * Perform an object sort of Objects of class T.
	 * <p>
	 * This executes a sort from a sortin to a sortout file
	 *
	 * @param sortInName   - String of the fully qualified sortin file name
	 * @param sortOutName- String of the fully qualified sortout file name
	 * @param comparator   - The Comparator for Class T that will be called to
	 *                     perform the sort.
	 * @param tempDir      - - String of the fully qualified directory name for work
	 *                     files (if required)
	 * @return true - sort completed - false - sort failed.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public boolean sort(
																					String sortInName,
																					String sortOutName,
																					Comparator<T> comparator,
																					String tempDir)
																																					throws ClassNotFoundException,
																																					IOException
	{
		return sort(sortInName, sortOutName, comparator, tempDir, null, null);
	}

	/**
	 * Perform an object sort of Objects of class T.
	 * <p>
	 * This executes a sort from a sortin file and passes the results to a
	 * SortOutCallback.
	 *
	 * @param sortInName  - String of the fully qualified sortin file name
	 * @param outCallback - an implementor of the SortOutCallback interface
	 * @param comparator  - The Comparator for Class T that will be called to
	 *                    perform the sort.
	 * @param tempDir     - - String of the fully qualified directory name for work
	 *                    files (if required)
	 * @return true - sort completed - false - sort failed.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public boolean sort(
																					String sortInName,
																					SortOutCallback<T> outCallback,
																					Comparator<T> comparator,
																					String tempDir)
																																					throws ClassNotFoundException,
																																					IOException
	{
		return sort(sortInName, null, comparator, tempDir, null, outCallback);
	}

	/**
	 * Perform an object sort of Objects of class T.
	 * <p>
	 * This executes a sort obtaining input objects from a SortInCallback and passes
	 * the results to a SortOutCallback.
	 *
	 * @param inCallback  - an implementor of the SortInCallback interface
	 * @param outCallback - an implementor of the SortOutCallback interface
	 * @param comparator  - The Comparator for Class T that will be called to
	 *                    perform the sort.
	 * @param tempDir     - - String of the fully qualified directory name for work
	 *                    files (if required)
	 * @return true - sort completed - false - sort failed.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public boolean sort(
																					SortInCallback<T> inCallback,
																					SortOutCallback<T> outCallback,
																					Comparator<T> comparator,
																					String tempDir)
																																					throws ClassNotFoundException,
																																					IOException
	{
		return sort(null, null, comparator, tempDir, inCallback, outCallback);
	}

	/**
	 * Perform an object sort of Objects of class T.
	 * <p>
	 * This executes a sort obtaining input objects from a SortInCallback and writes
	 * the result to the sortOut file name.
	 *
	 * @param inCallback   - an implementor of the SortInCallback interface
	 * @param sortOutName- String of the fully qualified sortout file name
	 * @param comparator   - The Comparator for Class T that will be called to
	 *                     perform the sort.
	 * @param tempDir      - - String of the fully qualified directory name for work
	 *                     files (if required)
	 * @return true - sort completed - false - sort failed.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public boolean sort(
																					SortInCallback<T> inCallback,
																					String sortOutName,
																					Comparator<T> comparator,
																					String tempDir)
																																					throws ClassNotFoundException,
																																					IOException
	{
		return sort(null, sortOutName, comparator, tempDir, inCallback, null);
	}

	private boolean sort(
																						String sortInName,
																						String sortOutName,
																						Comparator<T> comparator,
																						String tempDir,
																						SortInCallback<T> inCallback,
																						SortOutCallback<T> outCallback)
																																																						throws ClassNotFoundException,
																																																						IOException
	{

		System.gc();

		boolean											sortOk																	= true;
		ObjectInputStream	sortInStream											= null;
		long														availableMemoryAtStart	= getAvailableMemory();
		double												target																	= availableMemoryAtStart * (1 - memoryFactor);

		LOG.log(logLevel,
										"available memory at start:" + availableMemoryAtStart / 1024 / 1024 +
												"mb - setting target available memory to:" + target / 1024 / 1024 + "mb");

		listLimit		= 0;
		workFileId	= 0;

		long														total								= 0;
		List<WorkFile<T>>	workFileList	= new ArrayList<WorkFile<T>>();

		if (!createDir(tempDir))
		{
			LOG.log(Level.WARNING, "cannot accss temp directory " + tempDir);
			return false;
		}

		List<T> objList = new ArrayList<T>();

		if (inCallback == null)
		{
			sortInStream = openInputStream(sortInName);

			if (sortInStream == null)
				return false;
		}

		T readObj = null;
		readObj = readData(sortInStream, inCallback);

		while (readObj != null)
		{

			if (listLimit == 0)
				objList = new ArrayList<T>();
			else
				objList = new ArrayList<T>(listLimit + 1);

			while (!isListFull(objList, target))
			{

				if (readObj == null)
					break; // end of file

				objList.add(readObj);

				readObj = readData(sortInStream, inCallback);
			}

			Collections.sort(objList, comparator);

			if (readObj != null)
				createWorkFile(objList, workFileList, tempDir);
			else
			{
				/*
				 * reached end of data - if no work files write direct to the output file
				 */

				if (workFileList.isEmpty())
					createOutput(objList, sortOutName, outCallback);
				else
					createWorkFile(objList, workFileList, tempDir);
			}

			total = total + objList.size();

		}

		LOG.log(logLevel, "received total of " + total + " objects");

		if (inCallback == null)
		{
			close(sortInStream);
		}

		if (!workFileList.isEmpty())
		{
			LOG.log(logLevel, "added to files:");

			for (WorkFile<T> workFile : workFileList)
			{
				LOG.log(logLevel, workFile.getFileName());
			}
		}

		if (!workFileList.isEmpty())
			merge(workFileList, sortOutName, comparator, outCallback);

		return sortOk;
	}

	private void createOutput(
																											List<T> objList,
																											String sortOutName,
																											SortOutCallback<T> outCallback)
																																																											throws IOException
	{

		if (outCallback == null)
		{
			ObjectOutputStream os = openOutputStream(sortOutName);

			for (T obj : objList)
			{
				os.writeUnshared(obj);

				os.reset();

			}

			os.flush();
			close(os);
		}

		else
		{
			for (T obj : objList)
			{
				outCallback.consumeSortOut(obj);
			}
		}

	}

	private void merge(
																				List<WorkFile<T>> workFileList,
																				String sortOutName,
																				Comparator<T> comparator,
																				SortOutCallback<T> sortOutCallback)
																																																								throws IOException,
																																																								ClassNotFoundException
	{

		WorkFile<T>								sortOut	= null;
		ObjectOutputStream	os						= null;

		if (sortOutCallback == null)
		{
			sortOut	= new WorkFile<T>(sortOutName);
			os						= sortOut.startOutStream();
		}

		for (WorkFile<T> inFile : workFileList)
		{
			inFile.startInStream();
		}

		while (thereIsInputData(workFileList))
		{
			T			lowest						= null;
			int	lowestIndex	= -1;

			for (int i = 0; i < workFileList.size(); i++)
			{
				WorkFile<T> file = workFileList.get(i);

				if (lowest == null)
				{
					lowest						= file.getCurrentInputItem();
					lowestIndex	= i;
				}
				if (lowest != null)
				{
					if (file.getCurrentInputItem() != null)
					{
						int result = comparator.compare(file.getCurrentInputItem(), lowest);

						if (result < 0)
						{
							lowest						= file.getCurrentInputItem();
							lowestIndex	= i;
						}
					}
				}

			}

			if (lowestIndex != -1)
			{
				if (sortOutCallback == null)
				{
					os.writeUnshared(lowest);
					os.reset();
				}

				else
					sortOutCallback.consumeSortOut(lowest);

				workFileList.get(lowestIndex).getNextInput();
			}

		}

		if (sortOutCallback == null)
			sortOut.stopOutStream();

		for (WorkFile<T> inFile : workFileList)
		{
			inFile.stopInStream();
			inFile.deleteFile();
		}
	}

	private boolean thereIsInputData(
																																		List<WorkFile<T>> workFileList)
	{
		for (WorkFile<T> file : workFileList)
		{
			if (file.getCurrentInputItem() != null)
				return true;
		}

		return false;
	}

	private void createWorkFile(
																													List<T> objList,
																													List<WorkFile<T>> workFileList,
																													String tempDir)
																																													throws IOException
	{
		if (objList == null)
			return;
		if (objList.isEmpty())
			return;

		String name = "sortWork" + workFileId;
		workFileId++;

		WorkFile<T> workFile = new WorkFile<T>(getFileName(tempDir, name));
		workFileList.add(workFile);

		ObjectOutputStream outStream = workFile.startOutStream();

		for (Object o : objList)
		{
			outStream.writeUnshared(o);
			outStream.reset();
		}

		workFile.stopOutStream();

	}

	private String getFileName(
																												String tempDir,
																												String fileName)
	{
		if (tempDir.endsWith("/"))
			return tempDir + fileName;
		return tempDir + "/" + fileName;
	}

	private boolean createDir(
																											String tempDir)
	{
		boolean	created	= true;

		File				dir					= new File(tempDir);

		if (!dir.exists())

			try
			{
				dir.mkdir();

			} catch (SecurityException se)
			{
				created = false;
			}
		return created;
	}

	@SuppressWarnings("unchecked")
	private T readData(
																				ObjectInputStream sortInStream,
																				SortInCallback<T> inCallback)
																																																		throws ClassNotFoundException
	{
		Object	o								= null;
		T						readItem	= null;

		if (inCallback == null)
		{
			try
			{
				o = sortInStream.readUnshared();
			} catch (IOException e)
			{
				// assume end of file
			}

			if (o == null)
				return null;

			readItem = (T) o;
		}

		else
		{
			readItem = inCallback.produceSortIn();
		}

		return readItem;
	}

	private boolean isListFull(
																												List<T> objList,
																												double freeSpaceTarget)
	{

		if (listLimit == 0) // not set yet
		{
			long freeNow = getAvailableMemory();

			if (freeNow < freeSpaceTarget)
			{
				LOG.log(logLevel,
												"stopped internal sort with :" + freeNow / 1024 / 1024 +
														"mb - available memory");

				listLimit = objList.size();
				if (listLimit == 0)
					listLimit = 1000;
				return true;
			}

			return false;
		}

		if (listLimit <= objList.size())
			return true;

		return false;
	}

	private long getAvailableMemory()
	{
		long	allocated	= (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		long	free						= Runtime.getRuntime().maxMemory() - allocated;

		return free;

	}

	private ObjectInputStream openInputStream(
																																											String fileName)
																																																												throws IOException
	{
		FileInputStream			fileInputStream;
		ObjectInputStream	objectInputStream	= null;

		fileInputStream			= new FileInputStream(fileName);
		objectInputStream	= new ObjectInputStream(fileInputStream);

		return objectInputStream;

	}

	private ObjectOutputStream openOutputStream(
																																													String fileName)
																																																														throws IOException
	{
		FileOutputStream			fileOutputStream;
		ObjectOutputStream	objectOutputStream	= null;

		fileOutputStream			= new FileOutputStream(fileName);
		objectOutputStream	= new ObjectOutputStream(fileOutputStream);

		return objectOutputStream;

	}

	/**
	 * Convience method for closing Closable items and ignoring exceptions
	 * 
	 * @param closeables
	 */
	public static void close(
																										Closeable... closeables)
	{
		for (Closeable closeable : closeables)
		{
			try
			{
				if (closeable != null)
					closeable.close();
			} catch (IOException e)
			{
				// ignore
			}
		}
	}

}
