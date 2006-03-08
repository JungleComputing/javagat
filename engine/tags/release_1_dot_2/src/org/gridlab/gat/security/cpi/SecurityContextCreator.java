/*
 * Created on Aug 5, 2005
 */
package org.gridlab.gat.security.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.SecurityContext;

public interface SecurityContextCreator {
    /**
     * This method is called by the methods in the SecurityContextUtils class to instantiate adaptor-specific security data.
     * The security context can be used to get information needed to create the user data object.
     * @param gatContext
     * @param preferences
     * @param location the host/port to create the object for
     * @param c the security context that can be used to construct the adaptor-specific data
     * @return the adaptor specific security token
     * @throws GATInvocationException
     */
    public Object createUserData(GATContext gatContext,
        Preferences preferences, URI location, SecurityContext c)
        throws GATInvocationException;

    /**
     * This method is called by the methods in the SecurityContextUtils class to instantiate adaptor-specific security data.
     * It is called when no valid security context could be found for the adaptor.
     * Sometimes a default security context can be created as a "last resort".
     * @param gatContext
     * @param preferences
     * @param location the host/port to create the object for
     * @return the new default security context
     * @throws GATInvocationException
     */
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
        Preferences preferences, URI location) throws GATInvocationException;
}
