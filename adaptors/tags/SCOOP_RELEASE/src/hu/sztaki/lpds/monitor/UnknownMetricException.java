package hu.sztaki.lpds.monitor;

/**
 * A <code>UnknownMetricException</code> is thrown when an unknown metric or control
 * is requested.
 *
 * @author Gábor Gombás
 * @version $Id: UnknownMetricException.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public class UnknownMetricException extends MonitorException {

	/**
	 * Creates a new <code>UnknownMetricException</code> with the given message.
	 *
	 * @param msg		the message of the exception.
	 */
	public UnknownMetricException(String msg) {
		super(msg);
	}
}
