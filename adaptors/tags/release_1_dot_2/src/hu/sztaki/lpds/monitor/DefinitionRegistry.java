package hu.sztaki.lpds.monitor;

/**
 * Provides definitions for metrics and controls.
 *
 * @author G??bor Gomb??s
 * @version $Id: DefinitionRegistry.java,v 1.2 2005/10/07 11:05:55 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public interface DefinitionRegistry {

    /**
     * Returns the definition of a metric.
     *
     * @param metricName	the name of a metric.
     * @return		the metric definition.
     *
     * @throws UnknownMetricException if the metric name is not known.
     */
    public MetricDefinition getMetricDefinition(String metricName)
        throws UnknownMetricException;

    /**
     * Returns the definition of a control.
     *
     * @param controlName	the name of a control.
     * @return		the control definition.
     *
     * @throws UnknownMetricException if the control name is not known.
     */
    public ControlDefinition getControlDefinition(String controlName)
        throws UnknownMetricException;
}
