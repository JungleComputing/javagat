/*
 * Created on May 14, 2004
 */
package org.gridlab.gat.engine;

import java.io.IOException;

import org.gridlab.gat.GATInvocationException;

/**
 * @author rob
 */
public class GATIOException extends IOException {
	GATInvocationException cause;

	public GATIOException(GATInvocationException e) {
		super();
		cause = e;
	}

	public String toString() {
		return cause.toString();
	}

	public void printStackTrace() {
		cause.printStackTrace();
	}
}