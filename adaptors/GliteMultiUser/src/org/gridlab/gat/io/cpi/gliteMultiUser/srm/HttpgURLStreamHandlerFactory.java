package org.gridlab.gat.io.cpi.gliteMultiUser.srm;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Factory class for creating {@link URLStreamHandler} for the httpg protocol.
 * 
 * @author Stefan Bozic
 */
public class HttpgURLStreamHandlerFactory implements URLStreamHandlerFactory {
	
	/**
	 * Creates a new instance of a httpg handler.
	 * @param protocol the protocol
	 * @return a new instance of a httpg handler.
	 */
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (protocol.equalsIgnoreCase("httpg")) {
			return new org.globus.net.protocol.httpg.Handler();
		}
		return null;
	}
}
