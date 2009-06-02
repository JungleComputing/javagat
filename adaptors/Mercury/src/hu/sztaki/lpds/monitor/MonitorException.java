package hu.sztaki.lpds.monitor;

import java.io.IOException;

/**
 * A <code>MonitorException</code> is thrown when an error occurs in the
 * monitoring API.
 *
 * @author G??bor Gomb??s
 * @version $Id: MonitorException.java 857 2006-04-19 09:43:22Z rob $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MonitorException extends IOException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new <code>MonitorException</code> with the given message.
     *
     * @param msg                the message of the exception.
     */
    public MonitorException(String msg) {
        super(msg);
    }
}
