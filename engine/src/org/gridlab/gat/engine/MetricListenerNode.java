/*
 * Created on Oct 26, 2004
 */
package org.gridlab.gat.engine;

import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * @author rob
 */
class MetricListenerNode {
    MetricListener metricListener;

    Metric metric;

    Object adaptor;

    MetricListenerNode(Object adaptor, MetricListener metricListener,
            Metric metric) {
        this.metricListener = metricListener;
        this.metric = metric;
        this.adaptor = adaptor;
    }

    public boolean equals(Object o) {
        if (!(o instanceof MetricListenerNode)) {
            return false;
        }

        MetricListenerNode other = (MetricListenerNode) o;
        return (other.adaptor == adaptor) && other.metric.equals(metric)
                && other.metricListener.equals(metricListener);
    }
}
