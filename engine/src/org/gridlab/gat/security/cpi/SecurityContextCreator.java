/*
 * Created on Aug 5, 2005
 */
package org.gridlab.gat.security.cpi;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.SecurityContext;

/**
 * This interface defines factory methods for adaptor depended security objects.
 * 
 * @author Stefan Bozic
 */
public interface SecurityContextCreator {
	/**
	 * This method is called by the SecurityContextUtils classes to instantiate adaptor-specific security data, e.g
	 * credentials. The security context can be used to get information needed to create the user data object.
	 * 
	 * @param gatContext the gat context
	 * @param location the host/port to create the object for
	 * @param c the security context that can be used to construct the adaptor-specific data
	 * 
	 * @return the adaptor specific security token
	 * @throws CouldNotInitializeCredentialException an exception that might occurs
	 * @throws CredentialExpiredException an exception that might occurs
	 * @throws InvalidUsernameOrPasswordException an exception that might occurs
	 */
	public Object createUserData(GATContext gatContext, URI location, SecurityContext c)
			throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException;

	/**
	 * This method is called by the methods in the SecurityContextUtils class to instantiate adaptor-specific security
	 * data. It is called when no valid security context could be found for the adaptor. Sometimes a default security
	 * context can be created as a "last resort".
	 * 
	 * @param gatContext the gat context
	 * @param location the host/port to create the object for
	 * @return the new default security context
	 * @throws CouldNotInitializeCredentialException an exception that might occurs
	 * @throws CredentialExpiredException an exception that might occurs
	 * @throws InvalidUsernameOrPasswordException an exception that might occurs
	 */
	public SecurityContext createDefaultSecurityContext(GATContext gatContext, URI location)
			throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException;
}
