package org.gridlab.gat.resources.cpi;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;

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
public class DefaultHardwareResource extends HardwareResource {

	/**
	 * Constructs a DefaultHardwareResource
	 */
	public DefaultHardwareResource(GATContext gatContext,
			Preferences preferences) {
		super(gatContext, preferences);
	}

	/**
	 * Gets the HardwareResourceDescription which describes this
	 * HardwareResource instance.
	 * 
	 * @return A HardwareResourceDescription which describes this
	 *         HardwareResource instance.
	 */
	public ResourceDescription getResourceDescription() {
		Hashtable hashtable = new Hashtable();

		return new HardwareResourceDescription(hashtable);
	}

	public Reservation getReservation() {
		return null;
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