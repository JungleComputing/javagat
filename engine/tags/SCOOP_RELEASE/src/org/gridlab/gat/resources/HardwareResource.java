package org.gridlab.gat.resources;

import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;

/**
 * An instance of this interface is an abstract representation of a physical
 * hardware resource which is monitorable.
 * <p>
 * An instance of this interface allows on to examine the various properties of
 * the physical hardware resource to which this instance corresponds. In
 * addition, it allows one to monitor the physical hardware resource to which
 * this instance corresponds.
 */
public abstract class HardwareResource implements Resource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetricDefinitions() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws GATInvocationException {
		throw new Error("Not implemented");
	}

	public MetricValue getMeasurement(Metric metric)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}
}