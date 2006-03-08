package org.gridlab.gat.monitoring;

import java.util.List;

import org.gridlab.gat.GATInvocationException;

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
     * @throws GATInvocationException
     *             The metric listener cannot be added
     */
    public void addMetricListener(MetricListener metricListener, Metric metric)
        throws GATInvocationException;

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
     * @throws GATInvocationException
     *             The metric listener cannot be removed
     */
    public void removeMetricListener(MetricListener metricListener,
        Metric metric) throws GATInvocationException;

    /**
     * This method returns a java.util.List of MetricDefinition instances. Each
     * instance in this java.util.List is a MetricDefinition which can be
     * monitored on this instance.
     * 
     * @return An java.util.List of MetricDefinition instances. Each instance in
     *         this java.util.List is a Metric which can be monitored on this
     *         instance
     * @throws GATInvocationException
     *             An error occurred while getting the list of metrics
     */
    public List getMetricDefinitions() throws GATInvocationException;

    /**
     * Gets the MetricDefinition using its name.
     * 
     * @param name
     *            The MetricDefinition name
     * @return The MetricDefinition with the given name
     * 
     * @throws GATInvocationException
     *             An error occurred while getting the list of metrics or no
     *             metric with this name exists
     */
    public MetricDefinition getMetricDefinitionByName(String name)
        throws GATInvocationException;

    /**
     * @return an metric event that represents the measured metric.
     * @throws GATInvocationException
     *             the measurement failed
     */
    public MetricValue getMeasurement(Metric metric)
        throws GATInvocationException;
}
