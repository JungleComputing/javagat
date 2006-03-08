package org.gridlab.gat.io.cpi;

import java.io.FileNotFoundException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.RandomAccessFile;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality of the File class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this File class and will be used to implement the corresponding method in the
 * File class at runtime.
 */
public abstract class RandomAccessFileCpi extends RandomAccessFile {

	/**
	 * Constructs a FileCpi instance which corresponds to the physical file
	 * identified by the passed Location and whose access rights are determined
	 * by the passed GATContext.
	 * 
	 * @param location
	 *            A Location which represents the URI corresponding to the
	 *            physical file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this FileCpi.
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	protected RandomAccessFileCpi(GATContext gatContext,
			Preferences preferences, File file, String mode)
			throws FileNotFoundException {
		super(gatContext, preferences, file, mode);
	}
}