package org.gridlab.gat.monitoring;

import java.util.Map;

/**
 * An instance of this class represents a measurable quantity within a
 * monitoring system.There are two classes of metrics a monitoring system must
 * deal with:
 * <ul>
 * <li><em>Local metrics</em> --- Local metrics are metrics that are directly
 * measured on a resource. These can be highly dependent on the physical
 * parameters of the resource. Thus, local metrics originating from two
 * different resources are not necessarily comparable ( E.g. 1 hour CPU time on
 * a 1 GHz Intel processor is different than 1 hour CPU time on an 800 MHz PPC
 * processor although the numeric values are equal.) However resource
 * administrators who know the configuration of the resource need local metrics
 * for detailed monitoring of the status and operation of the resource.</li>
 * <li><em>Grid metrics</em> --- Grid metrics are metrics that have
 * predefined semantics thus, they are resource independent. Grid metrics are
 * derived from one or more local metrics by applying a specific, well defined
 * algorithm (such as unit conversion, aggregation or averaging). Because of
 * this transformation grid metrics may have less precision or they could be
 * less specific but are guaranteed to be comparable between different
 * resources. Unlike local metrics which a local resource is free to change grid
 * metrics must be agreed upon, standardised and introduced by community
 * consensus.</li>
 * </ul>
 * Instances of this class deal with both classes of metrics.
 * <p>
 * A Metric can only be measured if there exists a sensor which can measure the
 * quantity corresponding to the Metric. Such sensors are created by sensor
 * developers and must embedded in a resource to allow the corresponding Metric
 * or Metrics to be measured. (The creation of sensors is beyond the scope of
 * this document and as such will not be covered.) Such sensors, once created,
 * define the Metric or Metrics which they allow to be measured using a metric
 * definitions.
 * <p>
 * A metric definition contains the following information:
 * <ul>
 * <li>Metric name</li>
 * <li>Metric parameters</li>
 * <li>Metric measurement type</li>
 * <li>Metric data type</li>
 * <li>Metric Unit</li>
 * </ul>
 * <p>
 * <em>Metric name</em> The Metric name is used to identify the metric
 * definition (e.g. CPU usage). It consists of dot separated words, e.g.
 * host.cpu.user. The last component of a metric name is the actual name of the
 * metric, the preceding components are called scope. The scope can be used to
 * group metrics as well as to differentiate between similar metrics defined at
 * different levels (for example, CPU utilisation can be measured on a per-job
 * or per-host level).
 * <p>
 * <em>Metric parameters</em> The Metric parameters field in the metric
 * definition contains the formal definition of the metric parameters. Many
 * metrics can be measured at different places simultaneously. For example, CPU
 * utilisation can be measured on several hosts or grid resources. The metric
 * parameters can be used to distinguish between these different metric
 * instances.
 * <p>
 * <em>Metric measurement type</em> The Metric Measurement type can be
 * <it>continuous </it> meaning data is always available or <it>event-like </it>
 * meaning data only becomes available when some external event happens (e.g. a
 * sensor embedded in an application can send events any time). Continuous
 * metrics are only available using pull model delivery unless the user
 * specifies a measurement frequency. (The reason for this is without a
 * specified measurement frequency there is no event triggering the measurement
 * of a continuous metric.) If the user asks for periodic measurements by
 * specifying a measurement frequency the system will generate periodic events
 * automatically. This avoids polling and allows the system to use the
 * measurement frequency information to optimise measurements.
 * <p>
 * <em>Metric data type</em> The Metric data type contains the definition of
 * the storage used for representing measurement data.
 * <p>
 * <em>Metric Unit</em> The Metric unit specifies the physical unit in which
 * the metric is measured as a java.lang.String. It is only valid for simple
 * numeric types and java.util.List's of these types. In the latter case it
 * means the unit of all elements of the java.util.List.
 * <p>
 * In using the Metric class one must make an instance of the Metric class. One
 * does so using the Metric constructor; it takes the Metric name and concrete
 * values for the Metric parameters and yields a Metric instance. The notion of
 * Metric instances is necessary because the same metric could be measured at
 * different places (e.g. on different hosts) at the same time. Metric instances
 * are used to differentiate between these measurements, e.g. instances of a
 * Metric describing the available memory on a host are distinguished by a
 * parameter containing the hostname.
 */
public class Metric {
	/**
	 * This member variable holds the Metric name
	 */
	private String metricName = null;

	/**
	 * This member variable holds the Metric parameters
	 */
	private Map metricParameters = null;

	/**
	 * Constructs a Metric instance from the passed Metric name and concrete
	 * values for the Metric parameters.
	 * <p>
	 * The passed Metric name must be equal, as determined by the Equals method
	 * of the java.lang.String class, to the Metric name is the desired target
	 * Metric definition. In addition, the passed concrete values for the Metric
	 * parameters must be of the same name and type as the Metric parameters in
	 * the desired target Metric definition. Also, all the required Metric
	 * parameters as specified in the Metric definition must be present.
	 * 
	 * @param metricName
	 *            The Metric name, a java.lang.String, of the desired target
	 *            Metric definition
	 * @param metricParameters
	 *            The Metric parameters, a java.util.Map, for the desired Metric
	 *            definition
	 */
	public Metric(String metricName, Map metricParameters) {
		this.metricName = metricName;
		this.metricParameters = metricParameters;
	}

	/**
	 * Tests this Metric for equality with the passed Object.
	 * <p>
	 * If the given object is not a Metric, then this method immediately returns
	 * false.
	 * <p>
	 * For two Metric instances to be considered as equal they must have equal
	 * Metric names, a java.lang.String, as determined my the Equals method on
	 * java.lang.String. In addition, they must have equal Metric parameters as
	 * determined by the Equals method on java.util.Map.
	 * 
	 * @param object
	 *            The Object to test for equality
	 * @return A boolean indicating equality
	 */
	public boolean equals(Object object) {
		Metric metric = null;

		if (false == (object instanceof Metric)) {
			return false;
		}
		metric = (Metric) object;

		if (false == metricName.equals(metric.metricName)) {
			return false;
		}
		if (false == metricParameters.equals(metric.metricParameters)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the Metric name associated with this Metric.
	 * 
	 * @return The Metric name, a java.lang.String
	 */
	public String getMetricName() {
		return metricName;
	}

	/**
	 * Gets the Metric parameters associated with this Metric.
	 * 
	 * @return The Metric parameters, a java.util.Map
	 */
	public Map getMetricParameters() {
		return metricParameters;
	}

	/**
	 * Gets the Metric parameter value associated with the passed Metric
	 * parameter name. The value null is returned if there is no Metric
	 * parameter value with the passed name.
	 * 
	 * @param name
	 *            The Metric parameter name, a java.lang.String, for which to
	 *            obtain the associated Metric
	 * @return The Metric parameter value, an Object, associated with the passed
	 *         Metric parameter name.
	 */
	public Object getMetricParameterByName(String name) {
		return metricParameters.get(name);
	}
}