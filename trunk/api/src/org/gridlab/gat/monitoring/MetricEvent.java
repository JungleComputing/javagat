package org.gridlab.gat.monitoring;

import java.util.EventObject;

/**
 * An instance of this class represents an metric event.
 * <p>
 * A metric event occurs whenever the monitored resource, be it a job or a
 * hardware resource, sends out an event to the monitoring system. This can
 * encompass almost any type of event from disk space running out to memory
 * becoming available. The various events are defined by the various sensors.
 * This topic is covered in more detail in the Metric documentation.
 */
@SuppressWarnings("serial")
public class MetricEvent extends EventObject {
    /**
     * This member variable holds the event time of this MetricEvent
     */
    private long eventTime = 0;

    /**
     * This member variable holds the value of this MetricEvent
     */
    private Object value = null;

    /**
     * This member variable holds the metric of this MetricEvent
     */
    private Metric metric = null;

    /**
     * Constructs a MetricEvent with the specified properties
     * 
     * @param source
     *                The source of the MetricEvent
     * @param value
     *                The value of the MetricEvent
     * @param metric
     *                The Metric of the MetricEvent
     * @param eventTime
     *                The number of milliseconds after January 1, 1970, 00:00:00
     *                GMT when the MetricEvent happened.
     */
    public MetricEvent(Object source, Object value, Metric metric,
            long eventTime) {
        super(source);

        this.value = value;
        this.metric = metric;
        this.eventTime = eventTime;
    }

    /**
     * This method returns an instance of the Metric to which this MetricEvent
     * corresponds.
     * 
     * @return A Metric corresponding to this MetricEvent
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * This method returns the value corresponding to this MetricEvent.
     * 
     * @return An Object which is the value of this MetricEvent.
     */
    public Object getValue() {
        return value;
    }

    /**
     * This method returns the number of milliseconds after January 1, 1970,
     * 00:00:00 GMT when the event happened.
     * 
     * @return A long, the number of milliseconds after January 1, 1970,
     *         00:00:00 GMT when the event happened.
     */
    public long getEventTime() {
        return eventTime;
    }

    public String toString() {
        return "MetricValue(time = " + eventTime + ", val =" + value
                + ", metric =" + metric;
    }
}
