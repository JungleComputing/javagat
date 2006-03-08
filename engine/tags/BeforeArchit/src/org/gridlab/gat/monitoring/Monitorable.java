package org.gridlab.gat.monitoring;

import java.util.List;

import org.gridlab.gat.net.RemoteException;

/**
 * Interface which is to be implemented by any classes which are capable of
 * being monitored.
 */
public interface Monitorable {
	/**
	 * This method adds the passed instance of a MetricListener to the list of
	 * MetricListeners which are notified of MetricEvents by an instance of this
	 * class. The passed MetricListener is only notified of MetricEvents which
	 * correspond to Metric instance passed to this method.
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
			throws RemoteException;

	/**
	 * Removes the passed MetricListener from the java.util.List of
	 * MetricListeners which are notified of MetricEvents corresponding to the
	 * passed Metric instance.
	 * 
	 * @param metricListener
	 *            The MetricListener to notify of MetricEvents
	 * @param metric
	 *            The Metric corresponding to the MetricEvents for which the
	 *            passed MetricListener has been notified
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException;

	/**
	 * This method returns a java.util.List of Metric instances. Each Metric
	 * instance in this java.util.List is a Metric which can be monitored on
	 * this instance.
	 * 
	 * @return An java.util.List of Metric instances. Each Metric instance in
	 *         this java.util.List is a Metric which can be monitored on this
	 *         instance
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public List getMetrics() throws RemoteException;
}