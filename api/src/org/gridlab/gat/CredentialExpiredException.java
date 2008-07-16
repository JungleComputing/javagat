/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when a credential is found, but is expired
 * 
 * @author rob
 */

@SuppressWarnings("serial")
public class CredentialExpiredException extends GATInvocationException {

    public CredentialExpiredException(String s) {
        super(s);
    }
}
