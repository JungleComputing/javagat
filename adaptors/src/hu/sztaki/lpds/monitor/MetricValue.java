package hu.sztaki.lpds.monitor;

import java.util.Date;

/**
 * Represents the result of a measurement.
 *
 * @author G??bor Gomb??s
 * @version $Id: MetricValue.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MetricValue {
    /** Metric ID that is sent when a metric is destroyed inside the producer. */
    public static final int MID_KILLED = 128;

    /** Metric ID that is sent when the producer had to drop some messages. */
    public static final int MID_DROPPED = 129;

    /** The <code>MonitorConsumer</code> that received the metric value. */
    private MonitorConsumer conn;

    /** The metric ID that generated this metric value. */
    private int metricId;

    /** The contents of the metric value. */
    private Object value;

    /** The timestamo of the metric value. */
    private Date timestamp;

    /** The definition of the metric that generated this value. */
    private MetricDefinition def;

    /**
     * Constructs a new <code>MetricValue</code>.
     *
     * @param conn                the connection this metric value has arrived
     *                        on.
     * @param metricId        the metric ID that generated this metric value.
     * @param value                the measured data.
     * @param timestamp        time of the measurement.
     * @param def                the metric definition.
     */
    public MetricValue(MonitorConsumer conn, int metricId, Object value,
            Date timestamp, MetricDefinition def) {
        this.conn = conn;
        this.metricId = metricId;
        this.value = value;
        this.timestamp = timestamp;
        this.def = def;
    }

    /**
     * Returns the measured data.
     *
     * @return                the measured data.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the time of the measurement.
     *
     * @return                the time of the measurement.
     */
    public Date getTimeStamp() {
        return timestamp;
    }

    /**
     * Returns the definition of the metric this measurement belongs to.
     *
     * @return                the metric definition.
     */
    public MetricDefinition getDefinition() {
        return def;
    }

    /**
     * Returns the monitoring connection the metric value was received on.
     *
     * @return                the connection that received the metric value.
     */
    public MonitorConsumer getConnection() {
        return conn;
    }

    /**
     * Returns the metric ID that generated this metric value.
     *
     * @return                the metric ID.
     */
    public int getMetricId() {
        return metricId;
    }

    /**
     * Checks if this is a MID_KILLED message.
     *
     * @return                true if the message is a MID_KILLED.
     */
    public boolean isKilledMessage() {
        return metricId == MID_KILLED;
    }

    /**
     * Returns the ID of the killed metric.
     *
     * The metric must be a MID_KILLED message.
     *
     * @return                the ID of the killed metric.
     * @throws RuntimeMonitorException if the metric is not a MID_KILLED message.
     */
    public int getKilledId() {
        if (metricId != MID_KILLED) {
            throw new RuntimeMonitorException(
                "This is not a MID_KILLED message.");
        }

        UnsignedInteger val = (UnsignedInteger) value;

        return val.intValue();
    }

    /**
     * Checks if this is a MID_DROPPED message.
     *
     * @return                true if the message is a MID_DROPPED.
     */
    public boolean isDroppedMessage() {
        return metricId == MID_DROPPED;
    }

    /**
     * Returns the ID of the dropped metric.
     *
     * The metric must be a MID_DROPPED message.
     *
     * @return                the ID of the dropped metric.
     * @throws RuntimeMonitorException if the metric is not a MID_DROPPED message.
     */
    public int getDroppedId() {
        if (metricId != MID_DROPPED) {
            throw new RuntimeMonitorException(
                "This is not a MID_DROPPED message.");
        }

        UnsignedInteger[] val = (UnsignedInteger[]) value;

        return val[0].intValue();
    }

    /**
     * Returns the number of messages dropped.
     *
     * The metric must be a MID_DROPPED message.
     *
     * @return                the ID of the dropped metric.
     * @throws RuntimeMonitorException if the metric is not a MID_DROPPED message.
     */
    public int getDroppedCount() {
        if (metricId != MID_DROPPED) {
            throw new RuntimeMonitorException(
                "This is not a MID_DROPPED message.");
        }

        UnsignedInteger[] val = (UnsignedInteger[]) value;

        return val[1].intValue();
    }

    /**
     * Returns a string representation of the metric value.
     *
     * @return                the string representation.
     */
    public String toString() {
        return def.getType().toString(value);
    }
}
