package org.gridlab.gat.steering;

import org.gridlab.gat.GATInvocationException;

@SuppressWarnings("serial")
public class SteeredIDExistsException extends GATInvocationException {

    public SteeredIDExistsException() {
        super();
    }

    public SteeredIDExistsException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    public SteeredIDExistsException(String s) {
        super(s);
    }
}
