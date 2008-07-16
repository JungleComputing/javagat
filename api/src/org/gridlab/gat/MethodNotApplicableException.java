/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when an adaptor is selected, but it cannot execute the requested
 * operation. For instance, the local file adaptor would throw this exception
 * when asked to copy to a remote destination.
 * 
 * @author rob
 * 
 */
@SuppressWarnings("serial")
public class MethodNotApplicableException extends GATInvocationException {
    public MethodNotApplicableException(String s) {
        super(s);
    }

    public MethodNotApplicableException() {
        super();
    }

    public MethodNotApplicableException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    /**
     * Overrides the <code>fillInStackTrace</code> from <code>Throwable</code>.
     * This version does not actually create a stack trace, which not very
     * useful if the method was not applicable.
     * 
     * @return this inlet.
     */
    public Throwable fillInStackTrace() {
        return this;
    }
}
