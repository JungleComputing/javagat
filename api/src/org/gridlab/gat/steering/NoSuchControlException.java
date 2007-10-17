package org.gridlab.gat.steering;

import org.gridlab.gat.GATInvocationException;

@SuppressWarnings("serial")
public class NoSuchControlException extends GATInvocationException {

    public NoSuchControlException() {
        super();
    }

    public NoSuchControlException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    public NoSuchControlException(String s) {
        super(s);
    }
}
