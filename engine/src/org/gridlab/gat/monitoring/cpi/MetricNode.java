/*
 * Created on Oct 26, 2004
 */
package org.gridlab.gat.monitoring.cpi;

import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;

/**
 * @author rob
 */
class MetricNode {

    String methodName;

    MetricDefinition definition;

    MetricEvent lastValue;

    MetricNode(String methodName, MetricDefinition definition) {
        this.methodName = methodName;
        this.definition = definition;
    }

    void setLastValue(MetricEvent v) {
        lastValue = v;
    }

    /*
     * public boolean equals(Object o) { if (!(o instanceof MetricNode)) return
     * false;
     * 
     * MetricNode other = (MetricNode) o;
     * 
     * return other.adaptor == adaptor; }
     */
}
