/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when a credential could not be initialized
 * 
 * @author rob
 */
@SuppressWarnings("serial")
public class CouldNotInitializeCredentialException extends
        GATInvocationException {

    public CouldNotInitializeCredentialException() {
        super();
    }

    public CouldNotInitializeCredentialException(String s) {
        super(s);
    }

    public CouldNotInitializeCredentialException(String s, Throwable t) {
        super(s, t);
    }
}
