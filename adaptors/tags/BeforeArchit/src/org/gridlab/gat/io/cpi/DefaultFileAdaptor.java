package org.gridlab.gat.io.cpi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.net.RemoteException;

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
public class DefaultFileAdaptor extends FileCpi {

	File f;

	/**
	 * @param gatContext
	 * @param preferences
	 * @param location
	 */
	public DefaultFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws AdaptorCreationException {
		super(gatContext, preferences, location);

		location = correctURI(location);

		System.err.println("LOCATION = " + location);

		if (!location.getScheme().equals("file")) {
			throw new AdaptorCreationException(
					"The DefaultFileAdaptor can only handle local files.");
		}

		f = new File(location);
	}

	// Make life a bit easier for the programmer:
	// If the URI does not have a scheme part, just consider it a local
	// file.
	// The ctor of java.io.file does not accept this.
	protected URI correctURI(URI in) {
		URI tmpLocation = in;

		if (in.getScheme() == null) {
			java.io.File tmp = new java.io.File(tmpLocation.toString());
			tmpLocation = tmp.toURI();
		}

		return tmpLocation;
	}

	/**
	 * This method copies the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param loc
	 *            The new location
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void copy(URI loc) throws GATInvocationException, IOException {
		// Step 1: Create destination file
		File destinationFile = new File(correctURI(loc));
		destinationFile.createNewFile();

		// Step 3: Copy source to destination
		FileInputStream in = new FileInputStream(f);
		BufferedInputStream inBuf = new BufferedInputStream(in);

		FileOutputStream out = new FileOutputStream(destinationFile);
		BufferedOutputStream outBuf = new BufferedOutputStream(out);

		long length = f.length();

		try {
			for (int i = 0; i < length; i++) {
				int b = inBuf.read();
				outBuf.write(b);
			}
		} finally {
			try {
				outBuf.close();
			} catch (IOException ioException) {
				// Ignore ioException
			}
			try {
				inBuf.close();
			} catch (IOException ioException) {
				// Ignore ioException
			}
		}
	}

	/**
	 * This method adds the passed instance of a MetricListener to the
	 * java.util.List of MetricListeners which are notified of MetricEvents by
	 * an instance of this class. The passed MetricListener is only notified of
	 * MetricEvents which correspond to Metric instance passed to this method.
	 * 
	 * @param metricListener
	 *            The MetricListener to notify of MetricEvents
	 * @param metric
	 *            The Metric corresponding to the MetricEvents for which the
	 *            passed MetricListener will be notified
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
	}

	/**
	 * Removes the passed MetricListener from the java.util.List of
	 * MetricListeners which are notified of MetricEvents corresponding to the
	 * passed Metric instance.
	 * 
	 * @param metricListener
	 *            The MetricListener to notify of MetricEvents
	 * @param metric
	 *            The Metric corresponding to the MetricEvents for which the
	 *            passed MetricListener will be notified
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
	}

	/**
	 * This method returns a java.util.List of Metric instances. Each Metric
	 * instance in this java.util.List is a Metric which can be monitored on
	 * this instance.
	 * 
	 * @return An java.util.List of Metric instances. Each Metric instance in
	 *         this java.util.List is a Metric which can be monitored on this
	 *         instance.
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public List getMetrics() throws RemoteException {
		return new Vector();
	}
}