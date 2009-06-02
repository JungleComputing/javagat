package org.gridlab.gat;

/**
 * An instance of this class represents a time period.
 */
@SuppressWarnings("serial")
public class TimePeriod implements java.io.Serializable {
    /**
     * This member variable represents the start time of this TimePeriod
     */
    private long startTime;

    /**
     * This member variable represents the stop time of this TimePeriod
     */
    private long stopTime;

    /**
     * This method constructs a {@link TimePeriod} instance corresponding to the
     * passed <code>startTime</code> and <code>stopTime</code>.
     * 
     * @param startTime
     *                The number of milliseconds after January 1, 1970, 00:00:00
     *                GMT when the time period starts, a long
     * @param stopTime
     *                The number of milliseconds after January 1, 1970, 00:00:00
     *                GMT when the time period stops, a long
     */
    public TimePeriod(long startTime, long stopTime) {
        this.stopTime = stopTime;
        this.startTime = startTime;
    }

    /**
     * Tests this {@link TimePeriod} for equality with the passed Object.
     * <p>
     * If the given object is not a {@link TimePeriod}, then this method
     * immediately returns <code>false</code>.
     * <p>
     * If the passed object is a {@link TimePeriod}, then it is deemed equal if
     * it has a numerically equivalent start time and a numerically equivalent
     * stop time to the passed {@link TimePeriod} instance.
     * 
     * @param object
     *                The {@link Object} to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object) {
        TimePeriod timePeriod = null;

        if (false == (object instanceof TimePeriod)) {
            return false;
        }

        timePeriod = (TimePeriod) object;

        if (startTime != timePeriod.startTime) {
            return false;
        }

        if (stopTime != timePeriod.stopTime) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (int) (startTime + stopTime);
    }

    /**
     * Returns the number of milliseconds after January 1, 1970, 00:00:00 GMT
     * when the time period starts, a long
     * 
     * @return The number of milliseconds after January 1, 1970, 00:00:00 GMT
     *         when the time period starts, a long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the number of milliseconds after January 1, 1970, 00:00:00 GMT
     * when the time period stops, a long
     * 
     * @return The number of milliseconds after January 1, 1970, 00:00:00 GMT
     *         when the time period stops, a long
     */
    public long getStopTime() {
        return stopTime;
    }
}
