package org.gridlab.gat.resources;

import java.rmi.RemoteException;
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;

/**
 * An instance of this interface is an abstract representation of a physical
 * hardware resource which is monitorable.
 * <p>
 * An instance of this interface presents an abstract, system-independent view
 * of a physical hardware resource which is monitorable. Various systems use
 * system-dependent means of representing a physical hardware resource. GAT,
 * however, uses an instance of this interface as an operating system
 * independent description of a physical hardware resource which is monitorable.
 * <p>
 * An instance of this interface allows on to examine the various properties of
 * the physical hardware resource to which this instance corresponds. In
 * addition is allows one to monitor the physical hardware resource to which
 * this instance corresponds.
 */
public abstract class HardwareResource implements Resource {
	GATContext gatContext;

	Preferences preferences;

	protected HardwareResource(GATContext gatContext, Preferences preferences) {
		this.gatContext = gatContext;
		this.preferences = preferences;
	}

	public static HardwareResource create(GATContext gatContext) {
		return create(gatContext, null);
	}

	public static HardwareResource create(GATContext gatContext,
			Preferences preferences) {
		GATEngine gatEngine = GATEngine.getGATEngine();
		HardwareResourceCpi h = (HardwareResourceCpi) gatEngine.getAdaptor(
				HardwareResourceCpi.class, gatContext, preferences, null);
		return h;
	}
	
	/* (non-Javadoc)
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener, org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
		throw new Error("Not implemented");
	}

	/* (non-Javadoc)
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetrics() throws RemoteException {
		throw new Error("Not implemented");
	}

	/* (non-Javadoc)
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener, org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
		throw new Error("Not implemented");
	}
}