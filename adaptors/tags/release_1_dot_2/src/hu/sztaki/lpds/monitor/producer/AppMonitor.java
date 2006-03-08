package hu.sztaki.lpds.monitor.producer;

import hu.sztaki.lpds.monitor.Buffer;
import hu.sztaki.lpds.monitor.ControlDefinition;
import hu.sztaki.lpds.monitor.MetricDefinition;
import hu.sztaki.lpds.monitor.MonitorArg;
import hu.sztaki.lpds.monitor.MonitorException;
import hu.sztaki.lpds.monitor.RuntimeMonitorException;
import hu.sztaki.lpds.monitor.Version;

/**
 * Provides application monitoring support.
 *
 * The <code>AppMonitor</code> class handles the registration of
 * application-private metrics and controls. It creates a manager thread
 * that will service the requests coming from the Local Monitor.
 *
 * @author G??bor Gomb??s
 * @version $Id: AppMonitor.java,v 1.3 2005/10/07 11:06:09 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public final class AppMonitor extends Thread {

    /** Used for enforcing that there are at most one monitoring
     * thread exists */
    private static AppMonitor monitor;

    static {
        /* Load the native code. Ensure that the consumer side is
         * loaded first so the GLib threading system is properly
         * initialized */
        System.loadLibrary("mercury_consumer_java-" + Version.getVersion());
        System.loadLibrary("mercury_producer_java-" + Version.getVersion());

        if (!Version.getVersion().equals(Version.getNativeVersion()))
            throw new RuntimeException("Mercury version "
                + "mismatch: Java side is " + Version.getVersion()
                + ", JNI side is " + Version.getNativeVersion());
    }

    /**
     * Connects to the local monitor.
     * The <code>appName</code> and <code>tid</code> parameters may be used
     * to identify a subprocess of a parallel application. These IDs will
     * be added to every outgoing message but otherwise are not interpreted
     * by the monitoring system.
     *
     * @param appName	the name of the application.
     * @param tid		unique id in case of a parallel application.
     * @param jobId		the job ID of the application.
     *
     * @throws MonitorException if connection to the Local Monitor has
     *	failed.
     */
    public AppMonitor(String appName, int tid, String jobId)
        throws MonitorException {
        if (monitor != null)
            throw new MonitorException(
                "There may me one AppMonitor object only");
        startApp(appName, tid, jobId);
        setName("Application Monitor");
        setDaemon(true);
        start();
        monitor = this;
    }

    /**
     * Retrieves the application monitor handle.
     *
     * @return		the active <code>AppMonitor</code> object.
     */
    public static AppMonitor getMonitor() {
        return monitor;
    }

    /**
     * Connects to the local monitor.
     */
    private native void startApp(String appName, int tid, String jobId);

    /**
     * Disconnects from the local monitor and shuts down the monitoring thread.
     */
    public void endApp() {
        monitor = null;
        nativeEndApp();
    }

    /**
     * Disconnects from the local monitor and shuts down the monitoring thread.
     */
    public synchronized native void nativeEndApp();

    /**
     * Checks for incoming requests and performs the requested measurements and/or
     * controls.
     */
    private native boolean checkEvents();

    /**
     * Waits for and services external requests.
     */
    public void run() {
        while (checkEvents())
            /* Nothing */;
    }

    /**
     * Registers a metric provider that will be notified when an external
     * request for a metric is received.
     *
     * @param def		the definition of the provided metric.
     * @param provider	the object providing the metric.
     *
     * @throws MonitorException if the application tries to register the
     *	same metric multiple times.
     * @throws IllegalArgumentException if the definition is not a metric
     *	but a control.
     */
    public synchronized native void registerMetricProvider(
        MetricDefinition def, MetricProvider provider) throws MonitorException;

    /**
     * Registers a control provider that will be notified when an external
     * request for a control is received.
     *
     * @param def		the definition of the provided control.
     * @param provider	the object providing the control.
     *
     * @throws MonitorException if the application tries to register the
     *	same control multiple times.
     */
    public synchronized native void registerControlProvider(
        ControlDefinition def, ControlProvider provider)
        throws MonitorException;

    /**
     * Generates an event.
     *
     * The metric must be
     * {@link hu.sztaki.lpds.monitor.MeasurementType#EVENT event-like} and
     * it must have been previously registered using
     * {@link #registerMetricProvider}.
     *
     * @param def		the metric definition.
     * @param args		the metric arguments.
     * @param value		the event's contents.
     *
     * @throws RuntimeMonitorException if the metric was not registered previously
     *	by {@link #registerMetricProvider}.
     * @throws ClassCastException if the passed value does not match
     *	the data type of the metric definition.
     */
    public void sendEvent(MetricDefinition def, MonitorArg[] args, Object value) {
        Buffer buf = new Buffer();
        buf.encode(value, def.getType());
        nativeSendEvent(def.getName(), args, buf.getData());
    }

    /**
     * Generates an event that has no arguments..
     *
     * @param def		the metric definition.
     * @param value		the event's contents.
     *
     * @throws RuntimeMonitorException if the metric was not registered previously
     *	by {@link #registerMetricProvider}.
     * @throws ClassCastException if the passed value does not match
     *	the data type of the metric definition.
     *
     * @see #sendEvent(MetricDefinition, MonitorArg[], Object)
     */
    public void sendEvent(MetricDefinition def, Object value) {
        Buffer buf = new Buffer();
        buf.encode(value, def.getType());
        nativeSendEvent(def.getName(), null, buf.getData());
    }

    /**
     * Generates an event.
     *
     * @param name		name of a previously registered metric.
     * @param args		the metric arguments.
     * @param value		the encoded event.
     */
    private synchronized native void nativeSendEvent(String name,
        MonitorArg[] args, byte[] value);

    /**
     * Blocks the current thread until the <tt>app.go</tt> control is
     * executed.
     *
     * This method cannot be used to synchronize multiple threads
     * simultaneously; that should be done using the facilities provided
     * by the Java language.
     */
    public synchronized native void block();
}
