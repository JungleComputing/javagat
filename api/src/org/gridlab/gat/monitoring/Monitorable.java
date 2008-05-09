package org.gridlab.gat.monitoring;

import java.util.List;

import org.gridlab.gat.GATInvocationException;

/**
 * Interface which is to be implemented by any classes which are capable of
 * being monitored.
 */
public interface Monitorable {
    /**
     * This method adds the passed instance of a {@link MetricListener} to the
     * list of {@link MetricListener}s which are notified of MetricEvents by an
     * instance of this class. The passed {@link MetricListener} is only
     * notified of MetricEvents which correspond to {@link Metric} instance
     * passed to this method.
     * 
     * @param metricListener
     *                The {@link MetricListener} to notify of MetricEvents
     * @param metric
     *                The {@link Metric} corresponding to the MetricEvents for
     *                which the passed {@link MetricListener} will be notified
     * @throws GATInvocationException
     *                 The {@link MetricListener} cannot be added
     */
    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException;

    /**
     * Removes the passed {@link MetricListener} from the {@link java.util.List}
     * of {@link MetricListener}s which are notified of MetricEvents
     * corresponding to the passed {@link Metric} instance.
     * 
     * @param metricListener
     *                The {@link MetricListener} to notify of MetricEvents
     * @param metric
     *                The {@link Metric} corresponding to the MetricEvents for
     *                which the passed {@link MetricListener} has been notified
     * @throws GATInvocationException
     *                 The {@link MetricListener} cannot be removed
     */
    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException;

    /**
     * This method returns a {@link java.util.List} of {@link MetricDefinition}
     * instances. Each instance in this {@link java.util.List} is a
     * {@link MetricDefinition} which can be monitored on this instance.
     * 
     * @return An {@link java.util.List} of {@link MetricDefinition} instances.
     *         Each instance in this {@link java.util.List} is a {@link Metric}
     *         which can be monitored on this instance
     * @throws GATInvocationException
     *                 An error occurred while getting the list of metrics
     */
    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException;

    /**
     * Gets the {@link MetricDefinition} using its <code>name</code>.
     * 
     * @param name
     *                The MetricDefinition name
     * @return The {@link MetricDefinition} with the given <code>name</code>
     * 
     * @throws GATInvocationException
     *                 An error occurred while getting the list of
     *                 {@link Metric}s or no {@link Metric} with this name
     *                 exists
     */
    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException;

    /**
     * Returns a {@link MetricEvent} that represents the measured {@link Metric}.
     * 
     * @return a {@link MetricEvent} that represents the measured {@link Metric}.
     * @throws GATInvocationException
     *                 the measurement failed
     */
    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException;
}
