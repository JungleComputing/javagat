/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

/**
 * This exception is thrown when the GAT engine cannot instantiate a gat object.
 * 
 * @author rob
 */
@SuppressWarnings("serial")
public class GATObjectCreationException extends NestedException {

    public GATObjectCreationException(String s) {
        super(s);
    }

    public GATObjectCreationException() {
        super();
    }

    public GATObjectCreationException(String adaptor, Throwable t) {
        super(adaptor, t);
    }
}
