package org.gridlab.gat.io.cpi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

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
public class DefaultFileOutputStreamAdaptor extends FileOutputStreamCpi {

	FileOutputStream out;

	DefaultFileOutputStreamAdaptor(GATContext gatContext,
			Preferences preferences, File file, boolean append) throws IOException {
		super(gatContext, preferences, file, append);
		java.io.File f = new java.io.File(file.toURI());
				out = new FileOutputStream(f, append);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		out.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int offset, int len) throws IOException {
		out.write(b, offset, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException {
		out.write(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int arg0) throws IOException {
		out.write(arg0);
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