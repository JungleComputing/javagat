/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when the adaptor was excluded by the user's preferences.
 * 
 * @deprecated This Exception will no longer be thrown by GAT adaptors, since an
 *             adaptor won't be invoked when it's excluded by the user's
 *             preferences.
 * @author rob
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
     * This version does not actually create a stack trace, which not very
     * useful if the adaptor was not selected by the user.
     * 
     * @return this inlet.
     */
    public Throwable fillInStackTrace() {
        return this;
    }
}
