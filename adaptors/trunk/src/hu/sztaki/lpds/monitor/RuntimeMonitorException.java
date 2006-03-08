package hu.sztaki.lpds.monitor;

/**
 * An <code>RuntimeMonitorException</code> is thrown when the JNI glue code detects
 * an error within itself.
 *
 * @author G??bor Gomb??s
 * @version $Id: RuntimeMonitorException.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class RuntimeMonitorException extends RuntimeException {
    /**
     * Creates a new <code>RuntimeMonitorException</code> with the
     * given message.
     *
     * @param msg                the message of the exception.
     */
    public RuntimeMonitorException(String msg) {
        super(msg);
    }

    /**
     * Creates a new <code>RuntimeMonitorException</code> with the
     * given message and cause.
     *
     * @param msg                the message of the exception.
     * @param cause                the cause of the exception.
     */
    public RuntimeMonitorException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates a new <code>RuntimeMonitorException</code> with the
     * given cause.
     *
     * @param cause                the cause of the exception.
     */
    public RuntimeMonitorException(Throwable cause) {
        super(cause);
    }
}
