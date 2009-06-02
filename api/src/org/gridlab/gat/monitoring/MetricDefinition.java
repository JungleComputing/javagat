package org.gridlab.gat.monitoring;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;

/**
 * An instance of this class is a decription of a measurable quantity within a
 * monitoring system. The GAT user can use this class to create metrics by
 * supplying parameters. For continuous metrics, a frequency can also be
 * provided. For instance, a MetricDefinition could describe a diskFree metric.
 * By supplying the deviceName ("/dev/hda1") as a parameter to the createMetric
 * method and "every 5 minutes" as the frequency, the user can create a metric.
 * This metric is then used to interact with the monitoring system.
 * 
 * There are two classes of Metrics a monitoring system must deal with:
 * <ul>
 * <li><em>Local Metrics</em> --- Local Metrics describe Metrics that are
 * directly measured on a resource. These can be highly dependent on the
 * physical parameters of the resource. Thus, local Metrics originating from two
 * different resources are not necessarily comparable ( E.g. 1 hour CPU time on
 * a 1 GHz Intel processor is different than 1 hour CPU time on an 800 MHz PPC
 * processor although the numeric values are equal.) However resource
 * administrators who know the configuration of the resource need local Metrics
 * for detailed monitoring of the status and operation of the resource.</li>
 * <li><em>Grid Metrics</em> --- Grid Metrics are Metrics that have
 * predefined semantics thus, they are resource independent. Grid Metrics are
 * derived from one or more local Metrics by applying a specific, well defined
 * algorithm (such as unit conversion, aggregation or averaging). Because of
 * this transformation grid Metrics may have less precision or they could be
 * less specific but are guaranteed to be comparable between different
 * resources. Unlike local MetricDefinitions which a local resource is free to
 * change grid Metrics must be agreed upon, standardised and introduced by
 * community consensus.</li>
 * </ul>
 * Instances of this class deal with both classes of Metrics.
 * <p>
 * A MetricDefinition definition contains the following information:
 * <ul>
 * <li>name</li>
 * <li>measurement type (continuous or discrete)</li>
 * <li>data type</li>
 * <li>unit</li>
 * </ul>
 * <p>
 * <em>name</em> The MetricDefinition name is used to identify the Metric
 * definition (e.g. CPU usage). It consists of dot separated words, e.g.
 * host.cpu.user. The last component of a MetricDefinition name is the actual
 * name of the MetricDefinition, the preceding components are called scope. The
 * scope can be used to group MetricDefinitions as well as to differentiate
 * between similar MetricDefinitions defined at different levels (for example,
 * CPU utilisation can be measured on a per-job or per-host level).
 * <p>
 * <em>measurement type</em> The MetricDefinition Measurement type can be
 * <it>continuous </it> meaning data is always available or <it>discrete</it>
 * meaning data only becomes available when some external event happens (e.g. a
 * sensor embedded in an application can send events any time). Continuous
 * MetricDefinitions are only available using pull model delivery unless the
 * user specifies a measurement frequency. If the user asks for periodic
 * measurements by specifying a measurement frequency the system will generate
 * periodic events automatically. This avoids polling and allows the system to
 * use the measurement frequency information to optimize measurements.
 * <p>
 * <em>data type</em> The MetricDefinition data type contains the definition
 * of the storage used for representing measurement data.
 * <p>
 * <em>unit</em> The MetricDefinition unit specifies the physical unit in
 * which the MetricDefinition is measured as a java.lang.String. It is only
 * valid for simple numeric types and java.util.List's of these types. In the
 * latter case it means the unit of all elements of the java.util.List.
 * <p>
 */
@SuppressWarnings("serial")
public class MetricDefinition implements Serializable {
    public static final int CONTINUOUS = 1;

    public static final int DISCRETE = 2;

    /**
     * This member variable holds the Metric name
     */
    private String metricName;

    /** CONTINUOUS or DISCRETE */
    private int measurementType;

    private String dataType;

    private String unit;

    private Map<String, Object> parameterDefinitions;

    private Map<String, Object> returnDefinition;

    /**
     * @param metricName
     * @param measurementType
     * @param dataType
     * @param unit
     */
    public MetricDefinition(String metricName, int measurementType,
            String dataType, String unit,
            Map<String, Object> parameterDefinitions,
            Map<String, Object> returnDefinition) {
        this.metricName = metricName;
        this.measurementType = measurementType;
        this.dataType = dataType;
        this.unit = unit;

        if (this.unit == null) {
            this.unit = "none";
        }

        this.parameterDefinitions = parameterDefinitions;

        if (this.parameterDefinitions == null) {
            this.parameterDefinitions = new HashMap<String, Object>();
        }

        this.returnDefinition = returnDefinition;

        if (this.returnDefinition == null) {
            this.returnDefinition = new HashMap<String, Object>();
        }
    }

    /**
     * Create a metric according to this definition. This method is used for
     * DISCRETE metrics, or CONTINUOUS metrics that are to be polled. Hence,
     * there is no frequency specified. This method works only if there are no
     * parameters needed for the metric
     * 
     * @return the new Netric
     */
    public Metric createMetric() throws GATInvocationException {
        return new Metric(this, null);
    }

    /**
     * Create a metric according to this definition. This method is used for
     * DISCRETE metrics, or CONTINUOUS metrics that are to be polled. Hence,
     * there is no frequency specified.
     * 
     * @param parameters
     *                the parameters to this metricDefinition. For instance,
     *                when creating a diskFree metric, a parameter could be the
     *                physical device name. If no special parameters are needed,
     *                it is OK to pass null
     * @return the new Netric
     */
    public Metric createMetric(Map<String, Object> parameters) {
        return new Metric(this, parameters);
    }

    /**
     * Create a metric according to this definition. This method is used for
     * CONTINUOUS metrics. Hence, there the frequency must be specified.
     * 
     * @param parameters
     *                the parameters to this metricDefinition. For instance,
     *                when creating a diskFree metric, a parameter could be the
     *                physical device name. If no special parameters are needed,
     *                it is OK to pass null
     * @return the new Metric
     */
    public Metric createMetric(Map<String, Object> parameters, long frequency) {
        return new Metric(this, parameters, frequency);
    }

    public boolean equals(Object o) {
        if (!(o instanceof MetricDefinition)) {
            return false;
        }

        MetricDefinition other = (MetricDefinition) o;

        return metricName.equals(other.metricName)
                && (measurementType == other.measurementType)
                && dataType.equals(other.dataType) && unit.equals(other.unit);
    }

    /**
     * @return Returns the dataType.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @return Returns the measurementType.
     */
    public int getMeasurementType() {
        return measurementType;
    }

    /**
     * @return Returns the metricName.
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * @return Returns the unit.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * returns a map of parameter names and types that are needed to retrieve
     * this metic.
     * 
     * @return the map
     */
    public Map<String, Object> getParamterDefinitions() {
        return parameterDefinitions;
    }

    public String toString() {
        String res = "MetricDefinition(" + metricName + ", type = ";
        res += ((measurementType == CONTINUOUS) ? "CONTINUOUS" : "DISCRETE");
        res += (", datatype = " + dataType + ", unit = " + unit + ", params = "
                + parameterDefinitions + ")");

        return res;
    }

    /**
     * returns a map of return value names and types that are generated by this
     * metic.
     * 
     * @return the map
     */
    public Map<String, Object> getReturnDefinition() {
        return returnDefinition;
    }
}
