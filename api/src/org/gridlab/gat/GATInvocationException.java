/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

/**
 * This exception is thrown when an invocation on a gat object fails.
 * 
 * @author rob
 */

@SuppressWarnings("serial")
public class GATInvocationException extends NestedException {

    public GATInvocationException(String s) {
        super(s);
    }

    public GATInvocationException() {
        super();
    }

    public GATInvocationException(String adaptor, Throwable t) {
        super(adaptor, t);
    }
}
