package hu.sztaki.lpds.monitor;

import java.io.IOException;

/**
 * A <code>MonitorException</code> is thrown when an error occurs in the
 * monitoring API.
 *
 * @author G??bor Gomb??s
 * @version $Id: MonitorException.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MonitorException extends IOException {
    /**
     * Creates a new <code>MonitorException</code> with the given message.
     *
     * @param msg                the message of the exception.
     */
    public MonitorException(String msg) {
        super(msg);
    }
}
