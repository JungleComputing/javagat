/*
 * Created on Nov 12, 2007
 */
package org.gridlab.gat;

@SuppressWarnings("serial")
public class InvalidUsernameOrPasswordException extends GATInvocationException {
    public InvalidUsernameOrPasswordException(String s) {
        super(s);
    }
    
    public InvalidUsernameOrPasswordException(Exception e) {
        super(e.getMessage());
    }
}
