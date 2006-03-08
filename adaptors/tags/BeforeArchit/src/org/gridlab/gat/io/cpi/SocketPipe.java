/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io.cpi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.net.RemoteException;

/**
 * @author rob
 */
public class SocketPipe implements Pipe {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.Pipe#getInputStream()
	 */
	public InputStream getInputStream() throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.Pipe#getOutputStream()
	 */
	public OutputStream getOutputStream() throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetrics() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
		// TODO Auto-generated method stub

	}
}