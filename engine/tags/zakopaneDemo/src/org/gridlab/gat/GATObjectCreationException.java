/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

/**
 * @author rob
 */
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