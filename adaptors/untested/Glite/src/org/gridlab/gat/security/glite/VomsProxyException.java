package org.gridlab.gat.security.glite;


public class VomsProxyException extends GlobusProxyException {

	/**
	 * eclipse-computed serial version UID
	 */
	private static final long serialVersionUID = 7594676848100110031L;
	
	/**
	 * Create a VomsProxyException with a defined message and a cause
	 * @param msg The message associated with this exception
	 * @param cause The cause of the exception
	 */
	public VomsProxyException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * Create a VomsProxyException with a defined message
	 * @param msg The message associated with this exception
	 */
	public VomsProxyException(final String msg) {
		super(msg);
	}
}
