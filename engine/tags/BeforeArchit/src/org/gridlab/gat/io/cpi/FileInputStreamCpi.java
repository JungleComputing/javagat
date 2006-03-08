package org.gridlab.gat.io.cpi;

import java.io.IOException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
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
public abstract class FileInputStreamCpi extends FileInputStream implements
		Monitorable {

	/**
	 * This creates a FileStreamCpi attached to the physical file at the
	 * specified Location. The file may be opened in several modes:
	 * <ul>
	 * <li>FileStream.read --- Open file for reading. The stream is positioned
	 * at the beginning of the file.</li>
	 * <li>FileStream.write --- Truncate file to zero length or create file for
	 * writing. The stream is positioned at the beginning of the file.</li>
	 * <li>FileStream.readwrite --- Open for reading and writing. The stream is
	 * positioned at the beginning of the file.</li>
	 * <li>FileStream.append --- Open for appending (writing at end of file).
	 * The file is created if it does not exist. The stream is positioned at the
	 * end of the file.</li>
	 * </ul>
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param location
	 *            The Location of the file to open.
	 * @param mode
	 *            The mode to open it --- read, write, readwrite, or append,
	 *            member variables of the FileStream class
	 */
	protected FileInputStreamCpi(GATContext gatContext,
			Preferences preferences, File file) throws IOException {
		super(gatContext, preferences, file);
	}
}