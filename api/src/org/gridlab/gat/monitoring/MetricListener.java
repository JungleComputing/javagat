package org.gridlab.gat.monitoring;

/**
 * This interface allows instances of classes which implement this interface to
 * receive MetricEvents from instances which are sources of MetricEvents.
 */
public interface MetricListener {
    /**
     * An instance of a class implementing this interface receives MetricEvents
     * through calls to this method when it is registered to receive such
     * events.
     * 
     * @param event
     *                The MetricValue which triggered this method call
     */
    public void processMetricEvent(MetricEvent event);
}
