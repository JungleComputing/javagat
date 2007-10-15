package hu.sztaki.lpds.monitor;

import java.io.IOException;

/**
 * A <code>MonitorException</code> is thrown when an error occurs in the
 * monitoring API.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
@SuppressWarnings("serial")
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
