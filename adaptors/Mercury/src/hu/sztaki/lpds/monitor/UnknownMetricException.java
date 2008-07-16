package hu.sztaki.lpds.monitor;

/**
 * A <code>UnknownMetricException</code> is thrown when an unknown metric or control
 * is requested.
 *
 * @author G??bor Gomb??s
 * @version $Id: UnknownMetricException.java 857 2006-04-19 09:43:22Z rob $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class UnknownMetricException extends MonitorException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new <code>UnknownMetricException</code> with the given message.
     *
     * @param msg                the message of the exception.
     */
    public UnknownMetricException(String msg) {
        super(msg);
    }
}
