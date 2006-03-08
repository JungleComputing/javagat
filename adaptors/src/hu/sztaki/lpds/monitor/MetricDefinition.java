package hu.sztaki.lpds.monitor;

/**
 * Represents the definition of a metric.
 *
 * A metric definition is much like a class description. Metrics have a name,
 * a data type, a (possibly empty) list of formal parameters and a
 * measurement type.
 *
 * Metrics are identified by name: if two metrics have the same name,
 * the rest of their definitions must also match.
 *
 * @author G??bor Gomb??s
 * @version $Id: MetricDefinition.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MetricDefinition {
    /** The name of the metric. */
    private String name;

    /** The list of metric parameters. <code>null</code> means the metric
     * has no parameters. */
    private MonitorParameter[] params;

    /** The data type of the metric. */
    private MonitorType type;

    /** The measurement type of the metric. */
    private MeasurementType measurementType;

    /**
     * Creates a new metric definition object.
     *
     * @param name                the name of the metric.
     * @param params        the parameters of the metric. May be
     *                        <code>null</code> if the metric has no
     *                        parameters.
     * @param type                the data type of the metric.
     * @param measurementType        either {@link MeasurementType#CONTINUOUS} or
     *                                {@link MeasurementType#EVENT}.
     */
    public MetricDefinition(String name, MonitorParameter[] params,
            MonitorType type, MeasurementType measurementType) {
        this.name = name;
        this.params = params;
        this.type = type;
        this.measurementType = measurementType;
    }

    /**
     * Creates a new metric definition object that has no parameters.
     *
     * @param name                the name of the metric.
     * @param type                the data type of the metric.
     * @param measurementType        either {@link MeasurementType#CONTINUOUS} or
     *                                {@link MeasurementType#EVENT}.
     */
    public MetricDefinition(String name, MonitorType type,
            MeasurementType measurementType) {
        this.name = name;
        this.type = type;
        this.measurementType = measurementType;
    }

    /**
     * Returns the name of the metric.
     *
     * @return                the metric name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameters of the metric.
     *
     * @return                the metric parameters. May be <code>null</code>
     *                        if the metric has no parameters.
     */
    public MonitorParameter[] getParams() {
        return params;
    }

    /**
     * Returns the data type of the metric.
     *
     * @return                the metric's data type.
     */
    public MonitorType getType() {
        return type;
    }

    /**
     * Returns the measurement type.
     *
     * @return                the measurement type.
     */
    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    /**
     * Tests the equality of two <code>MetricDefiniton</code> objects.
     *
     * Metrics are identified by name. Therefore if the metric names
     * match, the definitions ought to match.
     *
     * @param obj                the object to compare to.
     * @return                <code>true</code> if the passed object equals
     *                        to this <code>MetricDefinition</code>.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof MetricDefinition)) {
            return false;
        }

        return name.equals(((MetricDefinition) obj).name);
    }

    /**
     * Returns a hash code for this <code>MetricDefinition</code>.
     *
     * @return                a hash code value for this object.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Converts the metric definition to a textual form.
     *
     * @return                the definition as a string.
     */
    public String toString() {
        int i;
        String val = "Name: " + name + "\nParams:";

        for (i = 0; i < params.length; i++)
            val += ("\n\t" + params[i]);

        val += ("\nType: " + type);
        val += ("\nMeasurement: " + measurementType);

        return val;
    }
}
