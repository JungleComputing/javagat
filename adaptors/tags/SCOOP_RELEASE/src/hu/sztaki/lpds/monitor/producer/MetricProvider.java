package hu.sztaki.lpds.monitor.producer;

import hu.sztaki.lpds.monitor.MetricDefinition;
import hu.sztaki.lpds.monitor.MonitorArg;

/**
 * A <code>MetricProvider</code> defines application-private metrics.
 *
 * @author Gábor Gombás
 * @version $Id: MetricProvider.java,v 1.1 2005/06/13 01:49:15 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public interface MetricProvider {
	/**
	 * Checks if the given argument list is acceptable for the metric.
	 *
	 * @param def		the metric definition.
	 * @param args		the metric arguments.
	 * @return		<code>true</code> if the arguments are good,
	 *			<code>false</code> otherwise.
	 */
	public boolean checkMetricArguments(MetricDefinition def, MonitorArg[] args);

	/**
	 * Performs a measurement of a metric.
	 *
	 * @param def		the metric definition.
	 * @param args		the metric arguments.
	 * @param notifier	a {@link MeasurementNotifier} object that is to be
	 *			used to send back the result
	 */
	public void performMeasurement(MetricDefinition def, MonitorArg[] args, MeasurementNotifier notifier);
}
