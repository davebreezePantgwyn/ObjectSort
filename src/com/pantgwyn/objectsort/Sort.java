package com.pantgwyn.objectsort;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
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
public abstract class Sort<T>
{

	protected abstract FileWrapper.Type getSortType();

	protected static final Logger		LOG										= Logger.getLogger(Sort.class.getName());
	protected static final double		DEFAULT_MEMORY_FACTOR	= 0.5;
	protected static final int			MAX_WORK_FILES				= 32;

	protected double								memoryFactor;
	protected int										queueLimit;
	protected int										workFileId;
	protected Level									logLevel;
	protected List<FileWrapper<T>>	workFileList;
	protected long									recordsRead;

	protected FileWrapper<T>				sortIn;
	protected FileWrapper<T>				sortOut;

	/**
	 * Default constructor for class ObjectSort
	 * <p>
	 * This will by default use 50% of the available memory and non verbose
	 * messages.
	 */

	public Sort()
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
	public Sort(double memoryFactor)
	{
		this(memoryFactor, false);
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
	public Sort(double memoryFactor, boolean verbose)
	{
		workFileList			= new ArrayList<FileWrapper<T>>();
		this.memoryFactor	= memoryFactor;

		logLevel					= Level.FINE;
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

		sortIn	= new FileWrapper<T>(getSortType(), sortInName);
		sortOut	= new FileWrapper<T>(getSortType(), sortOutName);

		return sort(comparator, tempDir);
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
		sortIn	= new FileWrapper<T>(getSortType(), sortInName);
		sortOut	= new FileWrapper<T>(getSortType(), outCallback);

		return sort(comparator, tempDir);
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
		sortIn	= new FileWrapper<T>(getSortType(), inCallback);
		sortOut	= new FileWrapper<T>(getSortType(), outCallback);

		return sort(comparator, tempDir);
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
		sortIn	= new FileWrapper<T>(getSortType(), inCallback);
		sortOut	= new FileWrapper<T>(getSortType(), sortOutName);
		return sort(comparator, tempDir);
	}

	protected boolean createDir(
															String tempDir)
	{
		boolean	created	= true;

		File		dir			= new File(tempDir);

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

	protected String getFileName(
																String tempDir,
																String fileName)
	{
		if (tempDir.endsWith("/"))
			return tempDir + fileName;
		return tempDir + "/" + fileName;
	}

	protected boolean thereIsInputData(
																			List<FileWrapper<T>> workFileList)
	{
		for (FileWrapper<T> file : workFileList)
		{
			if (file.getCurrentInputItem() != null)
				return true;
		}

		return false;
	}

	protected long getAvailableMemory()
	{
		long	allocated	= (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		long	free			= Runtime.getRuntime().maxMemory() - allocated;

		return free;

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

	protected boolean isListFull(
																List<T> objQueue,
																double freeSpaceTarget)
	{

		if (queueLimit == 0) // not set yet
		{
			long freeNow = getAvailableMemory();

			if (freeNow < freeSpaceTarget)
			{
				LOG.log(logLevel,
								"stopped internal sort with :" + freeNow / 1024 / 1024 +
										"mb - available memory");

				queueLimit = objQueue.size();
				if (queueLimit == 0)
					queueLimit = 1000;
				return true;
			}

			return false;
		}

		if (queueLimit <= objQueue.size())
			return true;

		return false;
	}

	protected boolean sort(
													Comparator<T> comparator,
													String tempDir)
																					throws ClassNotFoundException,
																					IOException
	{

		List<T> objQueue;
		workFileList.clear();

		System.gc();

		boolean	sortOk									= true;
		long		availableMemoryAtStart	= getAvailableMemory();
		double	target									= availableMemoryAtStart * (1 - memoryFactor);

		LOG.log(logLevel,
						"available memory at start:" + availableMemoryAtStart / 1024 / 1024 +
								"mb - setting target available memory to:" + target / 1024 / 1024 + "mb");

		queueLimit	= 0;
		workFileId	= 0;

		recordsRead	= 0;

		if (!createDir(tempDir))
		{
			LOG.log(Level.WARNING, "cannot accss temp directory " + tempDir);
			return false;
		}

		sortIn.startInStream();

		T readObj = null;
		readObj = sortIn.getCurrentInputItem();

		while (readObj != null)
		{

			objQueue = new LinkedList<T>();

			while (!isListFull(objQueue, target))
			{

				if (readObj == null)
					break; // end of file

				objQueue.add(readObj);

				recordsRead++;
				sortIn.getNextInput();
				readObj = sortIn.getCurrentInputItem();
			}

			if (readObj != null)
			{
				createWorkFile(objQueue, workFileList, tempDir, comparator);
				if (workFileList.size() >= MAX_WORK_FILES)
				{
					FileWrapper<T> mergeTarget = getNextWorkFile(tempDir);
					merge(mergeTarget, workFileList, comparator);
					workFileList.add(mergeTarget);
				}
			} else
			{
				/*
				 * reached end of data - if no work files write direct to the output file
				 */

				if (workFileList.isEmpty())
					createOutput(objQueue, comparator);
				else
					createWorkFile(objQueue, workFileList, tempDir, comparator);

			}

		}

		sortIn.stopInStream();

		if (!workFileList.isEmpty())
		{
			LOG.log(logLevel, "added to files:");

			for (FileWrapper<T> workFile : workFileList)
			{
				LOG.log(logLevel, workFile.getFileName());
			}
		}

		if (!workFileList.isEmpty())
			merge(sortOut, workFileList, comparator);

		return sortOk;
	}

	protected void merge(	FileWrapper<T> mergeOut,
												List<FileWrapper<T>> mergeFileList,
												Comparator<T> itemComparator)
																											throws IOException,
																											ClassNotFoundException
	{

		Comparator<FileWrapper<T>>	workFileCompare	= new Comparator<FileWrapper<T>>()
																								{
																									@Override
																									public int compare(	FileWrapper<T> arg0,
																																			FileWrapper<T> arg1)
																									{
																										
																										T	item0	= arg0.getCurrentInputItem();
																										T	item1	= arg1.getCurrentInputItem();

																										return itemComparator.compare(item0, item1);
																									}
																								};

		Queue<FileWrapper<T>>				fileQueue				= new PriorityQueue<FileWrapper<T>>(
																																										mergeFileList.size(),
																																											workFileCompare);

		mergeOut.startOutStream();

		for (FileWrapper<T> inFile : mergeFileList)
		{
			inFile.startInStream();
			if (inFile.getCurrentInputItem() != null)
				fileQueue.add(inFile);
		}

		while (!fileQueue.isEmpty())
		{

			FileWrapper<T> lowestFile = fileQueue.remove();

			mergeOut.write(lowestFile.getCurrentInputItem());

			lowestFile.getNextInput();
			if (lowestFile.getCurrentInputItem() != null)
				fileQueue.add(lowestFile);

		}

		mergeOut.stopOutStream();

		mergeFileList.parallelStream().forEachOrdered(t -> {

			try
			{
				t.stopInStream();
			} catch (IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException("IO error stopping streams on merge");
			}
			t.deleteFile();

		});

		mergeFileList.clear();

	}

	protected void createOutput(
															List<T> objQueue,
															Comparator<T> comparator)
																												throws IOException
	{
		sortOut.startOutStream();

		sortOut.pushData(objQueue, comparator);

		sortOut.stopOutStream();

	}

	public int getWorkFileCount()
	{
		return workFileId;
	}

	public long getRecordsRead()
	{
		return recordsRead;
	}

	protected void createWorkFile(List<T> objQueue,
																List<FileWrapper<T>> workFileList,
																String tempDir,
																Comparator<T> comparator) throws IOException
	{

		FileWrapper<T> workFile = getNextWorkFile(tempDir);
		workFileList.add(workFile);
		workFile.startOutStream();
		workFile.pushData(objQueue, comparator);
		workFile.stopOutStream();

	}

	protected FileWrapper<T> getNextWorkFile(String tempDir)
	{
		String name = "sortWork" + workFileId;
		workFileId++;

		return new FileWrapper<T>(
															getSortType(),
																getFileName(tempDir, name));
	}

}
