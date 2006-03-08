/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when an adaptor is selected, but it cannot execute the requested operation.
 * For instance, the local file adaptor would throw this exception when asked to copy
 * to a remote destination.
 * @author rob
 *
 */
public class AdaptorNotApplicableException extends GATInvocationException {
    public AdaptorNotApplicableException(String s) {
        super(s);
    }

    public AdaptorNotApplicableException() {
        super();
    }

    public AdaptorNotApplicableException(String adaptor, Throwable t) {
        super(adaptor, t);
    }
}
