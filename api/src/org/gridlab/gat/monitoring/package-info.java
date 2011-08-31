/**
 * This package contains classes and interfaces which are used
 * to monitor resources.
 * The most important classes and interfaces in this package are
 * {@link org.gridlab.gat.monitoring.Metric Metric},
 * {@link org.gridlab.gat.monitoring.MetricEvent MetricEvent},
 * {@link org.gridlab.gat.monitoring.MetricListener MetricListener}, and
 * {@link org.gridlab.gat.monitoring.Monitorable Monitorable}.
 * An instance of the class {@link org.gridlab.gat.monitoring.Metric Metric}
 * represents a measurable quantity within a monitoring system and is used to
 * specify a measurable quantity.
 * An instance of the class
 * {@link org.gridlab.gat.monitoring.MetricEvent MetricEvent}
 * represents the measured value of a quantity measured by a monitoring system
 * and is used to specify to interested parties that a measurement of a
 * quantity corresponding to a {@link org.gridlab.gat.monitoring.Metric Metric}
 * has taken place.
 * The interface
 * {@link org.gridlab.gat.monitoring.MetricListener MetricListener}
 * is implemented by classes that want to be informed of
 * {@link org.gridlab.gat.monitoring.MetricEvent MetricEvents} and is used to
 * inform instances of such classes of
 * {@link org.gridlab.gat.monitoring.MetricEvent MetricEvents}.
 * The interface {@link org.gridlab.gat.monitoring.Monitorable Monitorable}
 * is implemented by classes that want to be monitored for
 * {@link org.gridlab.gat.monitoring.MetricEvent MetricEvents} and is used to
 * inform interested parties of such events.
 */ 

package org.gridlab.gat.monitoring;
