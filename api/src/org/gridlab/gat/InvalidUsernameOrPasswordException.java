/*
 * Created on Nov 12, 2007
 */
package org.gridlab.gat;

/**
 * This exception is thrown when the supplied username and/or password is not
 * valid for an adaptor.
 * 
 * @author roelof
 */
@SuppressWarnings("serial")
public class InvalidUsernameOrPasswordException extends GATInvocationException {

    public InvalidUsernameOrPasswordException(String s) {
        super(s);
    }

    public InvalidUsernameOrPasswordException(Exception e) {
        super(e.getMessage());
    }
}
