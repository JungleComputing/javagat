package org.gridlab.gat.util;

/**
 * An instance of this class represents a time period. A time period is an
 * period of time.
 */
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
	 * This method constructs a TimePeriod instance corresponding to the passed
	 * StartTime and StopTime.
	 * 
	 * @param startTime
	 *            The number of milliseconds after January 1, 1970, 00:00:00 GMT
	 *            when the time period starts, a long
	 * @param stopTime
	 *            The number of milliseconds after January 1, 1970, 00:00:00 GMT
	 *            when the time period stops, a long
	 */
	public TimePeriod(long startTime, long stopTime) {
		this.stopTime = stopTime;
		this.startTime = startTime;
	}

	/**
	 * Tests this TimePeriod for equality with the passed Object.
	 * <p>
	 * If the given object is not a TimePeriod, then this method immediately
	 * returns false.
	 * <p>
	 * If the passed object is a TimePeriod, then it is deemed equal if it has a
	 * numerically equivalent start time and a numerically equivalent stop time
	 * to the passed TimePeriod instance.
	 * 
	 * @param object
	 *            The Object to test for equality
	 * @return A boolean indicating equality
	 */
	public boolean equals(Object object) {
		TimePeriod timePeriod = null;

		if (false == (object instanceof TimePeriod))
			return false;

		timePeriod = (TimePeriod) object;

		if (startTime != timePeriod.startTime)
			return false;
		if (stopTime != timePeriod.stopTime)
			return false;

		return true;
	}

	/**
	 * This method returns the number of milliseconds after January 1, 1970,
	 * 00:00:00 GMT when the time period starts, a long
	 * 
	 * @return The number of milliseconds after January 1, 1970, 00:00:00 GMT
	 *         when the time period starts, a long
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * This method returns the number of milliseconds after January 1, 1970,
	 * 00:00:00 GMT when the time period stops, a long
	 * 
	 * @return The number of milliseconds after January 1, 1970, 00:00:00 GMT
	 *         when the time period stops, a long
	 */
	public long getStopTime() {
		return stopTime;
	}
}