package hu.sztaki.lpds.monitor;

/**
 * A <code>MetricListener</code> receives metric values arriving from a producer.
 *
 * @author G??bor Gomb??s
 * @version $Id: MetricListener.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public interface MetricListener {
    /**
     * Invoked when a new metric value becomes available.
     *
     * If this method throws an exception, the connection will be
     * terminated, and the exception will be reported by invoking the
     * {@link #processError} method.
     *
     * @param value                the received metric value.
     * @return                <code>true</code> if the metric value was processed by this
     *                        listener. If <code>false</code> is returned, the metric value
     *                        will be passed to the next listener.
     */
    public boolean processMetric(MetricValue value);

    /**
     * Invoked when the connection is terminated due to an error.
     *
     * @param conn                the connection that was terminated.
     * @param error                the exception that caused the connection thread
     *                        to terminate.
     */
    public void processError(MonitorConsumer conn, Exception error);
}
