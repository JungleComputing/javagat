package org.gridlab.gat.monitoring.cpi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author aagapi
 * 
 * Monitorable interface impl
 */
public class MonitorableCpi implements Monitorable {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("addMetricListener", false);
        capabilities.put("removeMetricListener", false);
        capabilities.put("getMetricDefinitions", false);
        capabilities.put("getMeasurement", false);
        capabilities.put("getMetricDefinitionByName", false);

        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = new Preferences();
        return preferences;
    }

    GATContext gatContext;

    Preferences preferences;

    /**
     * Create an instance of the AdvertService using the provided preference.
     * 
     * @param gatContext
     *                The context to use.
     */
    public MonitorableCpi(GATContext gatContext) {
        this.gatContext = gatContext;
    }

    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
