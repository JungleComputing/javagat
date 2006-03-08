/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

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
}
