package org.gridlab.gat.io.cpi;

import java.io.IOException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the FileStream class.
 * <p>
 * Capability provider wishing to provide the functionality of the FileStream
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this FileStream class and will be used to implement the
 * corresponding method in the FileStream class at runtime.
 */
public abstract class FileOutputStreamCpi extends FileOutputStream implements
		Monitorable {

	/**
	 * This creates a FileStreamCpi attached to the physical file at the
	 * specified Location. 
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences The preferences to be associated with this stream
	 * @param file the file to get the stream from
	 * @param append Append to the end of the file, or overwrite it
	 * @throws IOException some local IO error occurred
	 */
	protected FileOutputStreamCpi(GATContext gatContext,
			Preferences preferences, File file, boolean append) throws IOException {
		super(gatContext, preferences, file, append);
	}
}