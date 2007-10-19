package hu.sztaki.lpds.monitor.producer;

import hu.sztaki.lpds.monitor.MonitorConnectionException;
import hu.sztaki.lpds.monitor.RuntimeMonitorException;
import hu.sztaki.lpds.monitor.Version;

/**
 * The <code>Logger</code> class provides the interface to the GridLab
 * logging service.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public final class Logger {
    static {
        /* Load the native code. Ensure that the consumer side is
         * loaded first so the GLib threading system is properly
         * initialized */
        System.loadLibrary("mercury_consumer_java-" + Version.getVersion());
        System.loadLibrary("mercury_producer_java-" + Version.getVersion());

        if (!Version.getVersion().equals(Version.getNativeVersion())) {
            throw new RuntimeException("Mercury version "
                + "mismatch: Java side is " + Version.getVersion()
                + ", JNI side is " + Version.getNativeVersion());
        }
    }

    /** Used by the native code to map the object to a prod_gridlog_handle */
    @SuppressWarnings("unused")
	private int nativeHandle;

    /**
     * Creates a new <code>Logger</code> object using the specified serivice
     * identifier and connects to the Local Monitor.
     *
     * @param service        the service identifier to use in subsequent
     *                        messages.
     *
     * @throws MonitorConnectionException if connecting to the Local Monitor has
     *        failed.
     */
    public Logger(String service) {
        nativeHandle = connect(service);
    }

    /**
     * Connects to the Local Monitor.
     *
     * @param service        the service identifier to use in subsequent
     *                        messages.
     *
     * @throws MonitorConnectionException if connecting to the Local
     *        Monitor has failed.
     */
    private native int connect(String service);

    /**
     * Sends a log message.
     *
     * @param level                the severity of the message.
     * @param component        the component of the service that generated the
     *                        message.
     * @param message        the message to send.
     *
     * @throws MonitorConnectionException if the connection to the Local
     *        Monitor was broken.
     */
    public synchronized native void log(LogLevel level, String component,
            String message);

    /**
     * Sends a log message without a component part.
     *
     * @param level                the severity of the message.
     * @param message        the message to send.
     *
     * @throws RuntimeMonitorException if the connection to the Local
     *        Monitor was broken.
     */
    public void log(LogLevel level, String message) {
        log(level, null, message);
    }

    /**
     * Disconnects from the Local Monitor and releases the resources
     * allocated by the native code.
     */
    protected native void finalize();
}
