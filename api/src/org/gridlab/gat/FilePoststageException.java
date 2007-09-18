/*
 * Created on Mar 2, 2006
 */
package org.gridlab.gat;

@SuppressWarnings("serial")
public class FilePoststageException extends GATInvocationException {

    public FilePoststageException() {
        super();
    }

    public FilePoststageException(String adaptor, Throwable t) {
        super(adaptor, t);
    }

    public FilePoststageException(String s) {
        super(s);
    }
}
