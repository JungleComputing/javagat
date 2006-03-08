package hu.sztaki.lpds.monitor;

/**
 * A <code>MonitorConnectionException</code> is thrown when an error occurs
 * during communication with the producer.
 *
 * @author G??bor Gomb??s
 * @version $Id: MonitorConnectionException.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MonitorConnectionException extends RuntimeMonitorException {
    /**
     * Creates a new <code>MonitorConnectionException</code> with the
     * given message.
     *
     * @param msg                the message of the exception.
     */
    public MonitorConnectionException(String msg) {
        super(msg);
    }

    /**
     * Creates a new <code>MonitorConnectionException</code> with the
     * given message and cause.
     *
     * @param msg                the message of the exception.
     * @param cause                the cause of this exception.
     */
    public MonitorConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates a new <code>MonitorConnectionException</code> with the
     * given cause.
     *
     * @param cause                the cause of this exception.
     */
    public MonitorConnectionException(Throwable cause) {
        super(cause);
    }
}
