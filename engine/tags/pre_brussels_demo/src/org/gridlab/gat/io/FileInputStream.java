package org.gridlab.gat.io;

import java.io.IOException;
import java.io.InputStream;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
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
public abstract class FileInputStream extends InputStream implements
		Monitorable {
	protected GATContext gatContext;

	protected Preferences preferences;

	protected File file;

	/**
	 * This creates a FileInputStream attached to the physical file at the specified
	 * Location. 
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @param file the file to get the stream from.
	 * @throws IOException
	 *             Thrown upon creation error of some sort
	 */
	protected FileInputStream(GATContext gatContext, Preferences preferences,
			File file) throws IOException {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.file = file;
	}

	/**
	 * This creates a FileInputStream attached to the physical file at the specified
	 * Location.
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param file the file to get the stream from.
	 * @return the file input stream that has been created
	 * @throws IOException
	 *             Thrown upon creation error of some sort
	 */
	public FileInputStream create(GATContext gatContext, File file)
			throws IOException {
		return create(gatContext, null, file);
	}

	/**
	 * This creates a FileStream attached to the physical file at the specified location.
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @param file the file to get the stream from.
	 * @throws IOException
	 *             Thrown upon creation error of some sort
	 * @return the file input stream that has been created
	 */
	public FileInputStream create(GATContext gatContext,
			Preferences preferences, File file) throws IOException {
		GATEngine gatEngine = GATEngine.getGATEngine();

		Object[] array = new Object[1];
		array[0] = file;

		FileInputStreamCpi f = (FileInputStreamCpi) gatEngine.getAdaptor(
				FileInputStreamCpi.class, gatContext, preferences, array);

		return f;
	}
}