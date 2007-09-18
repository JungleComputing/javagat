/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

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
