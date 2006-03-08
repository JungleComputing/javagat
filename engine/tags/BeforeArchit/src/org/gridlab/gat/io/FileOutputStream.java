package org.gridlab.gat.io;

import java.io.IOException;
import java.io.OutputStream;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * A FileStream represents a connection to open file, the file may be either
 * remote or local.
 * <p>
 * A FileStream represents a seekable connection to a file and has semantics
 * similar to a standard Unix filedescriptor. It provides methods to query the
 * current position in the file and to seek to new positions.
 * <p>
 * To Write data to a FileStream it is necessary to construct a Buffer and pack
 * it with data. Similarly, to read data a buffer must be created to store the
 * read data. Writes and reads may either be blocking, or asynchronous.
 * Asynchronous writes or reads must be completed by appropriate call.
 */
public abstract class FileOutputStream extends OutputStream implements
		Monitorable {
	protected GATContext gatContext;

	protected Preferences preferences;

	protected File file;

	/**
	 * This creates a FileStream attached to the physical file at the specified
	 * Location. The file may be opened in several modes:
	 * <ul>
	 * <li>FileStream.READ --- Open file for reading. The stream is positioned
	 * at the beginning of the file.</li>
	 * <li>FileStream.WRITE --- Truncate file to zero length or create file for
	 * writing. The stream is positioned at the beginning of the file.</li>
	 * <li>FileStream.READWRITE --- Open for reading and writing. The stream is
	 * positioned at the beginning of the file.</li>
	 * <li>FileStream.APPEND --- Open for appending (writing at end of file).
	 * The file is created if it does not exist. The stream is positioned at the
	 * end of the file.</li>
	 * </ul>
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @param location
	 *            The Location of the file to open.
	 * @param mode
	 *            The mode to open it --- READ, WRITE, READWRITE, or APPEND,
	 *            member variables of this class
	 * @throws java.lang.Exception
	 *             Thrown upon creation error of some sort
	 */
	protected FileOutputStream(GATContext gatContext, Preferences preferences,
			File file) throws IOException {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.file = file;
	}

	/**
	 * This creates a FileStream attached to the physical file at the specified
	 * Location. The file may be opened in several modes:
	 * <ul>
	 * <li>FileStream.READ --- Open file for reading. The stream is positioned
	 * at the beginning of the file.</li>
	 * <li>FileStream.WRITE --- Truncate file to zero length or create file for
	 * writing. The stream is positioned at the beginning of the file.</li>
	 * <li>FileStream.READWRITE --- Open for reading and writing. The stream is
	 * positioned at the beginning of the file.</li>
	 * <li>FileStream.APPEND --- Open for appending (writing at end of file).
	 * The file is created if it does not exist. The stream is positioned at the
	 * end of the file.</li>
	 * </ul>
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param location
	 *            The Location of the file to open.
	 * @param mode
	 *            The mode to open it --- READ, WRITE, READWRITE, or APPEND,
	 *            member variables of this class
	 * @throws java.lang.Exception
	 *             Thrown upon creation error of some sort
	 */
	public FileOutputStream create(GATContext gatContext, File file)
			throws IOException {
		return create(gatContext, null, file);
	}

	/**
	 * This creates a FileStream attached to the physical file at the specified
	 * Location. The file may be opened in several modes:
	 * <ul>
	 * <li>FileStream.READ --- Open file for reading. The stream is positioned
	 * at the beginning of the file.</li>
	 * <li>FileStream.WRITE --- Truncate file to zero length or create file for
	 * writing. The stream is positioned at the beginning of the file.</li>
	 * <li>FileStream.READWRITE --- Open for reading and writing. The stream is
	 * positioned at the beginning of the file.</li>
	 * <li>FileStream.APPEND --- Open for appending (writing at end of file).
	 * The file is created if it does not exist. The stream is positioned at the
	 * end of the file.</li>
	 * </ul>
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @param location
	 *            The Location of the file to open.
	 * @param mode
	 *            The mode to open it --- READ, WRITE, READWRITE, or APPEND,
	 *            member variables of this class
	 * @throws java.lang.Exception
	 *             Thrown upon creation error of some sort
	 */
	public FileOutputStream create(GATContext gatContext,
			Preferences preferences, File file) throws IOException {
		GATEngine gatEngine = GATEngine.getGATEngine();

		Object[] array = new Object[1];
		array[0] = file;

		FileOutputStreamCpi f = (FileOutputStreamCpi) gatEngine.getAdaptor(
				FileOutputStreamCpi.class, gatContext, preferences, array);

		return f;
	}
}