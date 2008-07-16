/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when an adaptor is created with parameters that do not make sense.
 * 
 * For instance, the local file adaptor would throw this exception created with
 * a remote URI as a parameter
 * 
 * @author rob
 * 
 */
@SuppressWarnings("serial")
public class AdaptorNotApplicableException extends GATObjectCreationException {

    public AdaptorNotApplicableException(String s) {
        super(s);
    }

    public AdaptorNotApplicableException() {
        super();
    }

    public AdaptorNotApplicableException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    /**
     * Overrides the <code>fillInStackTrace</code> from <code>Throwable</code>.
     * This version does not actually create a stack trace, which not very
     * useful if the adaptor was not applicable.
     * 
     * @return this inlet.
     */
    public Throwable fillInStackTrace() {
        return this;
    }
}
