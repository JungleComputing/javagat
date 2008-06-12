package org.gridlab.gat.security.glite;

public class GlobusProxyException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6855681750428337915L;

	public GlobusProxyException(final String msg) {
		super(msg);
	}
	
	public GlobusProxyException(final String msg, final Throwable throwable) {
		super(msg, throwable);
	}
}
