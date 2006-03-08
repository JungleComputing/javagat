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
 */
public abstract class FileOutputStream extends OutputStream implements
		Monitorable {
	protected GATContext gatContext;

	protected Preferences preferences;

	protected File file;

	boolean append = false;
	
	/**
	 * This creates a FileStream attached to the physical file at the specified
	 * Location. 
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @param file the file to get the stream from.
	 * @param append append to the file or overwrite it
	 * @throws IOException
	 *             Thrown upon creation error of some sort
	 */
	protected FileOutputStream(GATContext gatContext, Preferences preferences,
			File file, boolean append) throws IOException {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.file = file;
		this.append = append;
	}

	/**
	 * This creates a FileStream attached to the physical file at the specified
	 * Location.	 
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param file the file to get the stream from.
	 * @return the file output stream that has been created
	 * @throws IOException
	 *             Thrown upon creation error of some sort
	 */
	public FileOutputStream create(GATContext gatContext, File file)
			throws IOException {
		return create(gatContext, null, file, false);
	}

	/**
	 * This creates a FileStream attached to the physical file at the specified
	 * Location.
	 *
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @param file the file to get the stream from.
	 * @param append append to the file or overwrite it
	 * @return the file output stream that has been created
	 * @throws IOException
	 *             Thrown upon creation error of some sort
	 */
	public FileOutputStream create(GATContext gatContext,
			Preferences preferences, File file, boolean append) throws IOException {
		GATEngine gatEngine = GATEngine.getGATEngine();

		Object[] array = {file, Boolean.valueOf(append)};

		FileOutputStreamCpi f = (FileOutputStreamCpi) gatEngine.getAdaptor(
				FileOutputStreamCpi.class, gatContext, preferences, array);

		return f;
	}
}