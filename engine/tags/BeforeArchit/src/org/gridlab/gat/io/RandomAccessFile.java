package org.gridlab.gat.io;

import java.io.FileNotFoundException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An abstract representation of a physical file.
 * <p>
 * An instance of this class presents an abstract, system-independent view of a
 * physical file. User interfaces and operating systems use system-dependent
 * pathname strings to identify physical files. GAT, however, uses an operating
 * system independent pathname string to identify a physical file. A physical
 * file in GAT is identified by a URI.
 * <p>
 * An instance of this File class allows for various high-level operations to be
 * preformed on a physical file. For example, one can, with a single API call,
 * copy a physical file from one location to a second location, move a physical
 * file from one location to a second location, delete a physical file, and
 * preform various other operations on a physical file. The utility of this
 * high-level view of a physical file is multi-fold. The client of an instance
 * of this class does not have to concern themselves with the details of reading
 * every single byte of a physical file when all they wish to do is copy the
 * physical file to a new location. Similarly, a client does not have to deal
 * with all the various error states that can occur when moving a physical file (
 * Have all the various bytes been read correctly? Have all the various bytes
 * been saved correctly? Did the deletion of the original file proceed
 * correctly? ); the client simply has to call a single API call and the
 * physical file is moved.
 */
public abstract class RandomAccessFile extends java.io.RandomAccessFile
		implements Monitorable, java.io.Serializable {

	protected File file;

	protected String mode;

	protected GATContext gatContext;

	protected Preferences preferences;

	/**
	 * Constructs a File instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param preferences
	 *            A Preferences which is used to determine the user's
	 *            preferences for this File.
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	protected RandomAccessFile(GATContext gatContext, Preferences preferences,
			File file, String mode) throws FileNotFoundException {
		super((java.io.File) file, mode);
		this.file = file;
		this.mode = mode;
		this.gatContext = gatContext;
		this.preferences = preferences;
	}

	/**
	 * Constructs a File instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	public static RandomAccessFile create(GATContext gatContext, File file,
			String mode) throws FileNotFoundException {
		return create(gatContext, null, file, mode);
	}

	/**
	 * Constructs a File instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param preferences
	 *            A Preferences which is used to determine the user's
	 *            preferences for this File.
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	public static RandomAccessFile create(GATContext gatContext,
			Preferences preferences, File file, String mode)
			throws FileNotFoundException {
		GATEngine gatEngine = GATEngine.getGATEngine();
		Object[] array = new Object[2];
		array[0] = file;
		array[1] = mode;
		RandomAccessFileCpi f = (RandomAccessFileCpi) gatEngine.getAdaptor(
				RandomAccessFileCpi.class, gatContext, preferences, array);
		return f;
	}

	/**
	 * Tests this File for equality with the passed Object.
	 * <p>
	 * If the given object is not a File, then this method immediately returns
	 * false.
	 * <p>
	 * If the given object is a File, then it is deemed equal to this instance
	 * if a URI object constructed from this File's location and a URI object
	 * constructed from the passed File's URI are equal as determined by the
	 * Equals method of URI.
	 * 
	 * @param object
	 *            The Object to test for equality
	 * @return A boolean indicating equality
	 */
	public boolean equals(Object object) {
		if (!(object instanceof org.gridlab.gat.io.RandomAccessFile))
			return false;

		org.gridlab.gat.io.RandomAccessFile other = (org.gridlab.gat.io.RandomAccessFile) object;
		return file.equals(other.file);
	}
}