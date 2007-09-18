/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * 
 * @author rob
 *
 * Thrown when the adaptor was excluded by the user's preferences. 
 */
@SuppressWarnings("serial")
public class AdaptorNotSelectedException extends GATObjectCreationException {
    public AdaptorNotSelectedException(String s) {
        super(s);
    }

    public AdaptorNotSelectedException() {
        super();
    }

    public AdaptorNotSelectedException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    /**
     * Overrides the <code>fillInStackTrace</code> from <code>Throwable</code>.
     * This version does not actually create a stack trace, which not very useful
     * if the adaptor was not selected by the user. 
     * @return this inlet.
     */
    public Throwable fillInStackTrace() {
        return this;
    }
}
