/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

/**
 * Thrown when something goes wrong during the prestaging
 * 
 * @author rob
 */

@SuppressWarnings("serial")
public class FilePrestageException extends GATInvocationException {

    public FilePrestageException() {
        super();
    }

    public FilePrestageException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    public FilePrestageException(String s) {
        super(s);
    }
}
