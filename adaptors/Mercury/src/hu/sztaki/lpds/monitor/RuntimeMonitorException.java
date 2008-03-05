package hu.sztaki.lpds.monitor;

/**
 * An <code>RuntimeMonitorException</code> is thrown when the JNI glue code detects
 * an error within itself.
 *
 * @author Gabor Gomb??s
 * @version $Id: RuntimeMonitorException.java 857 2006-04-19 09:43:22Z rob $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class RuntimeMonitorException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
