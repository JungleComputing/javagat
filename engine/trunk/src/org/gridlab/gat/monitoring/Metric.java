package org.gridlab.gat.monitoring;

import java.io.Serializable;
import java.util.Hashtable;
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
 * A metric definition contains the following information:
 * <ul>
 * <li>Metric parameters</li>
 * <li>Metric measurement frequency</li>
 * </ul>
 * <p>
 * <em>Metric parameters</em> The Metric parameters field in the metric
 * definition contains the formal definition of the metric parameters. Many
 * metrics can be measured at different places simultaneously. For example, CPU
 * utilisation can be measured on several hosts or grid resources. The metric
 * parameters can be used to distinguish between these different metric
 * instances.
 */
public class Metric implements Serializable {
    private MetricDefinition definition;

    /**
     * This member variable holds the Metric parameters
     */
    private Hashtable metricParameters;

    private long frequency;

    /** in milliseconds */
    /**
     * Constructs a Metric instance from the passed Metric name and concrete
     * values for the Metric parameters. This constructor is used for DISCRETE
     * metrics. Hence, there is no frequency specified.
     * <p>
     * The passed Metric name must be equal, as determined by the Equals method
     * of the java.lang.String class, to the Metric name is the desired target
     * Metric definition. In addition, the passed concrete values for the Metric
     * parameters must be of the same name and type as the Metric parameters in
     * the desired target Metric definition. Also, all the required Metric
     * parameters as specified in the Metric definition must be present.
     *
     * @param definition
     *            The Metric definition to create an instance of
     * @param metricParameters
     *            The Metric parameters, a java.util.Map, for the desired Metric
     *            definition
     */
    public Metric(MetricDefinition definition, Map metricParameters) {
        this.definition = definition;

        if (metricParameters == null) {
            this.metricParameters = new Hashtable();
        } else {
            this.metricParameters = new Hashtable(metricParameters);
        }
    }

    /**
     * Constructs a Metric instance from the passed Metric name and concrete
     * values for the Metric parameters. This constructor is used for CONTINUOUS
     * metrics. Hence, the frequency must be specified.
     * <p>
     * The passed Metric name must be equal, as determined by the Equals method
     * of the java.lang.String class, to the Metric name is the desired target
     * Metric definition. In addition, the passed concrete values for the Metric
     * parameters must be of the same name and type as the Metric parameters in
     * the desired target Metric definition. Also, all the required Metric
     * parameters as specified in the Metric definition must be present.
     *
     * @param definition
     *            The Metric definition to create an instance of
     * @param metricParameters
     *            The Metric parameters, a java.util.Map, for the desired Metric
     *            definition
     * @param frequency
     *            The measuring frequency.
     */
    public Metric(MetricDefinition definition, Map metricParameters,
            long frequency) {
        this.definition = definition;
        this.frequency = frequency;

        if (metricParameters == null) {
            this.metricParameters = new Hashtable();
        } else {
            this.metricParameters = new Hashtable(metricParameters);
        }
    }

    /**
     * Tests this Metric for equality with the passed Object.
     * <p>
     * If the given object is not a Metric, then this method immediately returns
     * false.
     * <p>
     * For two Metric instances to be considered as equal they must have equal
     * descriptors, parameters and frequency.
     *
     * @param object
     *            The Object to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object) {
        Metric metric = null;

        if (!(object instanceof Metric)) {
            return false;
        }

        metric = (Metric) object;

        return definition.equals(metric.definition)
            && metricParameters.equals(metric.metricParameters);
    }

    /**
     * Gets the Metric parameters associated with this Metric.
     *
     * @return The Metric parameters, a java.util.Map
     */
    public Map getMetricParameters() {
        return metricParameters;
    }

    public long getFrequency() {
        return frequency;
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

    public MetricDefinition getDefinition() {
        return definition;
    }

    public String toString() {
        return "Metric(def = " + definition + ", params = " + metricParameters
            + ", freq = " + frequency + ")";
    }
}
