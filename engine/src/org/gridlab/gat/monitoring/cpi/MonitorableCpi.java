package org.gridlab.gat.monitoring.cpi;

import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author aagapi
 *
 *  Monitorable interface impl
 */
public class MonitorableCpi implements Monitorable {
    GATContext gatContext;

    Preferences preferences;

    /**
     * Create an instance of the AdvertService using the provided preference.
     *
     * @param gatContext
     *            The context to use.
     * @param preferences
     *            The user preferences.
     */
    public MonitorableCpi(GATContext gatContext, Preferences preferences) {
        this.gatContext = gatContext;
        this.preferences = preferences;
    }

    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<MetricDefinition> getMetricDefinitions() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricValue getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
