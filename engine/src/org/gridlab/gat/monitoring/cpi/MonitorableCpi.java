package org.gridlab.gat.monitoring.cpi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author aagapi
 * 
 * Monitorable interface impl
 */
public abstract class MonitorableCpi implements Monitorable {

    protected static Logger logger = LoggerFactory.getLogger(MonitorableCpi.class);

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("addMetricListener", true);
        capabilities.put("removeMetricListener", true);
        capabilities.put("getMetricDefinitions", true);
        capabilities.put("getMeasurement", true);
        capabilities.put("getMetricDefinitionByName", true);

        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = new Preferences();
        return preferences;
    }

    /** elements are of type MetricListenerNode */
    private Vector<MetricListenerNode> metricListeners = new Vector<MetricListenerNode>();

    /** elements are of type MetricNode */
    private Vector<MetricNode> metricTable = new Vector<MetricNode>();

    public synchronized void addMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {

        // check whether the adaptor actually registered this metric
        boolean found = false;

        for (int i = 0; i < metricTable.size(); i++) {
            MetricNode n = metricTable.get(i);

            if (n.definition.equals(metric.getDefinition())) {
                found = true;

                break;
            }
        }

        if (!found) {
            throw new GATInvocationException();
        }

        metricListeners.add(new MetricListenerNode(metricListener, metric));
    }

    public void registerMetric(String methodName, MetricDefinition definition) {
        metricTable.add(new MetricNode(methodName, definition));
    }

    public synchronized void removeMetricListener(
            MetricListener metricListener, Metric metric)
            throws GATInvocationException {

        if (!metricListeners.remove(new MetricListenerNode(metricListener,
                metric))) {
            throw new NoSuchElementException();
        }
    }

    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        Vector<MetricDefinition> res = new Vector<MetricDefinition>();

        for (int i = 0; i < metricTable.size(); i++) {
            res.add(metricTable.get(i).definition);
        }
        return res;
    }

    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        if (metric.getDefinition().getMeasurementType() != MetricDefinition.DISCRETE) {
            throw new GATInvocationException(
                    "internal adaptor error: GATEngine.getMeasurement can only handle discrete metrics");
        }

        for (int i = 0; i < metricTable.size(); i++) {
            MetricNode n = metricTable.get(i);

            if (n.definition.equals(metric.getDefinition())) {
                if (n.lastValue == null) {
                    throw new GATInvocationException(
                            "No data available for this metric");
                }

                return n.lastValue;
            }
        }
        throw new GATInvocationException("No data available for this metric");
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        for (int i = 0; i < metricTable.size(); i++) {
            MetricNode n = metricTable.get(i);
            if (name.equals(n.definition.getMetricName())) {
                return n.definition;
            }
        }

        throw new GATInvocationException("the metric name is incorrect: "
                + name);
    }

    public void fireMetric(MetricEvent v) {
        // look for all callbacks that were installed for this metric, call
        // them.
        MetricListenerNode[] listenerNodes = metricListeners
                .toArray(new MetricListenerNode[metricListeners.size()]);

        for (int i = 0; i < listenerNodes.length; i++) {
            if (listenerNodes[i].metric.equals(v.getMetric())) {
                // hiha, right metric
                // call the handler
                try {
                    listenerNodes[i].metricListener.processMetricEvent(v);
                } catch (Throwable t) {
                    logger.warn("WARNING, user callback threw exception", t);
                }
            }
        }

        // now, also store the last value, a user might poll for it with the
        // getMeasurement call.
        for (int i = 0; i < metricTable.size(); i++) {
            MetricNode n = metricTable.get(i);

            if (n.definition.equals(v.getMetric().getDefinition())) {
                n.setLastValue(v);

                return;
            }
        }

        throw new Error("Internal error: event fired for non-registered metric");
    }
}
