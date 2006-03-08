/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;


/**
 * 
 * @author rob
 */

/**
 * This exception is throw when the GAT engine cannot instantiate a gat object.
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