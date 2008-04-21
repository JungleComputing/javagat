/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

import java.io.IOException;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class GATIOException extends IOException {
    Exception cause;

    public GATIOException(Exception e) {
        super();
        cause = e;
    }

    /** {@inheritDoc} */
    public String toString() {
        return cause.toString();
    }

    /** {@inheritDoc} */
    public void printStackTrace() {
        cause.printStackTrace();
    }
}
