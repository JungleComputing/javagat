/*
 * Created on Mar 3, 2006
 */
package org.gridlab.gat;

@SuppressWarnings("serial")
public class CommandNotFoundException extends GATInvocationException {

    public CommandNotFoundException() {
        super();
    }

    public CommandNotFoundException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    public CommandNotFoundException(String s) {
        super(s);
    }
}
