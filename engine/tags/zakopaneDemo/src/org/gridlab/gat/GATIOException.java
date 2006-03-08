/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

import java.io.IOException;

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