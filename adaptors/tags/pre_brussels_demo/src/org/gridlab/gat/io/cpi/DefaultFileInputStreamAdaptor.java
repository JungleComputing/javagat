package org.gridlab.gat.io.cpi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * A DefaultFileStreamAdaptor represents a connection to open file, the file may
 * be either remote or local.
 * <p>
 * A DefaultFileStreamAdaptor represents a seekable connection to a file and has
 * semantics similar to a standard Unix filedescriptor. It provides methods to
 * query the current position in the file and to seek to new positions.
 * <p>
 * To Write data to a DefaultFileStreamAdaptor it is necessary to construct a
 * Buffer and pack it with data. Similarly, to read data a buffer must be
 * created to store the read data. Writes and reads may either be blocking, or
 * asynchronous. Asynchronous writes or reads must be completed by appropriate
 * call.
 */
public class DefaultFileInputStreamAdaptor extends FileInputStreamCpi {

	FileInputStream in;

	DefaultFileInputStreamAdaptor(GATContext gatContext,
			Preferences preferences, File file) throws IOException, AdaptorCreationException {
		super(gatContext, preferences, file);
		java.io.File f = new java.io.File(file.toURI());
		in = new FileInputStream(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return in.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		in.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int arg0) {
		in.mark(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return in.markSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return in.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int offset, int len) throws IOException {
		return in.read(b, offset, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] arg0) throws IOException {
		return in.read(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		in.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long arg0) throws IOException {
		return in.skip(arg0);
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
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric) {
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
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) {
	}

	/**
	 * This method returns a java.util.List of Metric instances. Each Metric
	 * instance in this java.util.List is a Metric which can be monitored on
	 * this instance.
	 * 
	 * @return An java.util.List of Metric instances. Each Metric instance in
	 *         this java.util.List is a Metric which can be monitored on this
	 *         instance.
	 */
	public List getMetrics() {
		return new Vector();
	}
}