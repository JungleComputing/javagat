package org.gridlab.gat.steering;

import org.gridlab.gat.GATInvocationException;

@SuppressWarnings("serial")
public class SteeredIDUnknownException extends GATInvocationException {

    public SteeredIDUnknownException() {
        super();
    }

    public SteeredIDUnknownException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    public SteeredIDUnknownException(String s) {
        super(s);
    }
}
