package org.gridlab.gat.io.cpi;

import java.util.List;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.EndPoint;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.io.PipeListener;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.net.RemoteException;

/**
 * Capability provider interface to the Endpoint class.
 * <p>
 * Capability provider wishing to provide the functionality of the Endoint class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this EndPoint class and will be used to implement the corresponding method in
 * the EndPoint class at runtime.
 */
public abstract class EndpointCpi implements EndPoint {

	GATContext gatContext;

	Preferences preferences;

	/**
	 * Constructs a EndPointCpi instance which corresponds to the physical
	 * EndPoint identified by the passed Location and whose access rights are
	 * determined by the passed GATContext.
	 * 
	 * @param location
	 *            A Location which represents the URI corresponding to the
	 *            physical EndPoint.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this EndPointCpi.
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	protected EndpointCpi(GATContext gatContext, Preferences preferences)
			throws AdaptorCreationException {
		this.gatContext = gatContext;
		this.preferences = preferences;
	}

	public boolean equals(Object object) {
		throw new Error("Not implemented");
	}

	public Pipe connect() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	public Pipe listen() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	public void listen(PipeListener pipeListener) throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
		throw new Error("Not implemented");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetrics() throws RemoteException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
		throw new Error("Not implemented");
	}
}