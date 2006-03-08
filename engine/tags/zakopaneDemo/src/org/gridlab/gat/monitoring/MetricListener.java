package org.gridlab.gat.monitoring;

/**
 * This is language dependent, and represents either a listener object with the
 * method defined below or a callback function with a signature compatible with
 * the ProcessMetricEvent method defined below, depending on the languages
 * normal callback functionality.
 * <p>
 * This interface allows instances of classes which implement this interface to
 * receive MetricEvents from instances which are sources of MetricEvents.
 */
public interface MetricListener {
	/**
	 * An instance of a class implementing this interface receives MetricEvents
	 * through calls to this method when it is registered to receive such
	 * events.
	 * 
	 * @param val
	 *            The MetricValue which triggered this method call
	 */
	public void ProcessMetricEvent(MetricValue val);
}