package org.gridlab.gat.monitoring;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * An instance of this class represents a measurable quantity within a
 * monitoring system. There are two classes of metrics a monitoring system must
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
 * predefined semantics, thus they are resource independent. Grid metrics are
 * derived from one or more local metrics by applying a specific, well defined
 * algorithm (such as unit conversion, aggregation or averaging). Because of
 * this transformation grid metrics may have less precision or they could be
 * less specific but are guaranteed to be comparable between different
 * resources. Unlike local metrics which a local resource is free to change grid
 * metrics must be agreed upon, standardized and introduced by community
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
@SuppressWarnings("serial")
public class Metric implements Serializable {
    private MetricDefinition definition;

    /**
     * This member variable holds the Metric parameters
     */
    private Hashtable<String, Object> metricParameters;

    private long frequency;

    /** in milliseconds */
    /**
     * Constructs a {@link Metric} instance from the passed Metric name and
     * concrete values for the Metric parameters. This constructor is used for
     * DISCRETE metrics. Hence, there is no frequency specified.
     * <p>
     * The passed {@link Metric} name must be equal, as determined by
     * {@link String#equals(Object)}, to the {@link Metric} name in the desired
     * target {@link MetricDefinition}. In addition, the passed concrete values
     * for the {@link Metric} parameters must be of the same name and type as
     * the {@link Metric} parameters in the desired target
     * {@link MetricDefinition}. Also, all the required {@link Metric}
     * parameters as specified in the {@link MetricDefinition} must be present.
     * 
     * @param definition
     *                The {@link MetricDefinition} to create an instance of
     * @param metricParameters
     *                The {@link Metric} parameters, a {@link java.util.Map},
     *                for the desired {@link MetricDefinition}.
     */
    public Metric(MetricDefinition definition,
            Map<String, Object> metricParameters) {
        this.definition = definition;

        if (metricParameters == null) {
            this.metricParameters = new Hashtable<String, Object>();
        } else {
            this.metricParameters = new Hashtable<String, Object>(
                    metricParameters);
        }
    }

    /**
     * Constructs a {@link Metric} instance from the passed {@link Metric} name
     * and concrete values for the {@link Metric} parameters. This constructor
     * is used for CONTINUOUS metrics. Hence, the frequency must be specified.
     * <p>
     * The passed {@link Metric} name must be equal, as determined by
     * {@link String#equals(Object)}, to the {@link Metric} name in the desired
     * target {@link MetricDefinition}. In addition, the passed concrete values
     * for the {@link Metric} parameters must be of the same name and type as
     * the {@link Metric} parameters in the desired target
     * {@link MetricDefinition}. Also, all the required {@link Metric}
     * parameters as specified in the {@link MetricDefinition} must be present.
     * 
     * @param definition
     *                The {@link MetricDefinition} to create an instance of
     * @param metricParameters
     *                The {@link Metric} parameters, a {@link java.util.Map},
     *                for the desired {@link MetricDefinition}
     * @param frequency
     *                The measuring frequency.
     */
    public Metric(MetricDefinition definition,
            Map<String, Object> metricParameters, long frequency) {
        this.definition = definition;
        this.frequency = frequency;

        if (metricParameters == null) {
            this.metricParameters = new Hashtable<String, Object>();
        } else {
            this.metricParameters = new Hashtable<String, Object>(
                    metricParameters);
        }
    }

    /**
     * Tests this {@link Metric} for equality with the passed {@link Object}.
     * <p>
     * If the given object is not a {@link Metric}, then this method
     * immediately returns false.
     * <p>
     * For two {@link Metric} instances to be considered as equal they must have
     * equal descriptors, parameters and frequency.
     * 
     * @param object
     *                The {@link Object} to test for equality
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
     * Gets the {@link Metric} parameters associated with this {@link Metric}.
     * 
     * @return The {@link Metric} parameters, a {@link java.util.Map}
     */
    public Map<String, Object> getMetricParameters() {
        return metricParameters;
    }

    /**
     * Gets the measurement frequency in milliseconds.
     * 
     * @return the measurement frequency in milliseconds.
     */
    public long getFrequency() {
        return frequency;
    }

    /**
     * Gets the {@link Metric} parameter value associated with the passed
     * {@link Metric} parameter name. The value <code>null</code> is returned
     * if there is no {@link Metric} parameter value with the passed name.
     * 
     * @param name
     *                The {@link Metric} parameter name, a
     *                {@link java.lang.String}, for which to obtain the
     *                associated {@link Metric}
     * @return The {@link Metric} parameter value, an {@link Object},
     *         associated with the passed {@link Metric} parameter name.
     */
    public Object getMetricParameterByName(String name) {
        return metricParameters.get(name);
    }

    /**
     * Gets the {@link MetricDefinition}.
     * 
     * @return the {@link MetricDefinition}
     */
    public MetricDefinition getDefinition() {
        return definition;
    }

    public String toString() {
        return "Metric(def = " + definition + ", params = " + metricParameters
                + ", freq = " + frequency + ")";
    }
}
