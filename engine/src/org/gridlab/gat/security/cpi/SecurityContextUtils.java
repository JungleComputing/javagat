/*
 * Created on Jul 28, 2005
 */
package org.gridlab.gat.security.cpi;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.security.SecurityContext;

/** This class privides some utility methods to help adaptors to deal
 * with GAT security contexts.
 *
 * @author rob
 */
public class SecurityContextUtils {
    /**
     * Returns a list of security contexts that can be used for the given adaptor.
     *
     * @param context the gatContext that contains the securityContexts
     * @param preferences the preferences
     * @param adaptorName The name of the adaptor that needs the contexts
     * @param securityContextType the type of security context, e.g.
     * org.gridlab.gat.security.PasswordSecurityContext
     * @param host the host which needs to support the context
     * @param port the port to connect to
     * @return the list of security contexts that is valid for this adaptor
     */
    public static List getValidSecurityContexts(GATContext context,
            Preferences preferences, String adaptorName, String host, int port) {
        return getValidSecurityContextsByType(context, preferences,
            null /* no type */, adaptorName, host, port);
    }

    /**
     * Returns a list of security contexts that can be used for the given adaptor.
     *
     * @param context the gatContext that contains the securityContexts
     * @param preferences the preferences
     * @param type the fully qualified type name of the security context
     * @param adaptorName The name of the adaptor that needs the contexts
     * @param securityContextType the type of security context, e.g.
     * org.gridlab.gat.security.PasswordSecurityContext
     * @param host the host which needs to support the context
     * @param port the port to connect to
     * @return the list of security contexts that is valid for this adaptor
     */
    public static List getValidSecurityContextsByType(GATContext context,
            Preferences preferences, String type, String adaptorName,
            String host, int port) {
        ArrayList result = new ArrayList();

        List l;

        if (type != null) {
            l = context.getSecurityContextsByType(type);
        } else {
            l = context.getSecurityContexts();
        }

        if (l == null) {
            return null;
        }

        for (int i = 0; i < l.size(); i++) {
            SecurityContext c = (SecurityContext) l.get(i);

            if (c.isValidFor(adaptorName, host, port)) {
                result.add(c);
            }
        }

        if (result.size() == 0) {
            return null;
        }

        return result;
    }

    /**
     * Returns the adaptor-specific data object that is associated with a security context.
     * Uses the user provided SecurityContextCreator object to create new adaptor-specific security data.
     *
     * @param context
     * @param preferences
     * @param adaptorName the name of the adaptor that calls this method
     * @param dataObjectKey the key used to store the adaptor-specific data in the security context
     * @param location destionation machine/port used for the security data
     * @param defaultPort the default port for the protocol used by the adaptor
     * @param creator the SecurityContextCreator that should be used to create new security data for the adaptor
     * @return the adaptor-specific data object that was associated with a security context that is valid for this adaptor
     * @throws GATInvocationException
     */
    public static Object getSecurityUserData(GATContext context,
            Preferences preferences, String adaptorName, String dataObjectKey,
            URI location, int defaultPort, SecurityContextCreator creator) throws CouldNotInitializeCredentialException, CredentialExpiredException {
        // get the list of securityContext that might be valid for this adaptor
        List l = SecurityContextUtils.getValidSecurityContexts(context,
            preferences, adaptorName, location.resolveHost(), location
                .getPort(defaultPort));

        if (l != null) {
            // ok, we found a valid certificate context in the list
            for (int i = 0; i < l.size(); i++) {
                SecurityContext c = (SecurityContext) l.get(i);
                Object userData = c.getDataObject(dataObjectKey);

                if (userData != null) {
                    if (GATEngine.DEBUG) {
                        System.err
                            .println("reusing security object for adaptor "
                                + adaptorName);
                    }

                    return userData;
                } else {
                    // we need to try to create the user data object given the securityContext
                    // if it fails, just try the next one on the list.
                    if (GATEngine.DEBUG) {
                        System.err
                            .println("creating a new security object based on a security context for adaptor "
                                + adaptorName);
                    }

                    userData = creator.createUserData(context, preferences,
                        location, c);

                    if (userData != null) {
                        c.putDataObject(dataObjectKey, userData);

                        return userData;
                    }
                }
            }
        }

        // automatically try and insert the default credential if it was not there.
        if (GATEngine.DEBUG) {
            System.err.println("Getting default security object for adaptor "
                + adaptorName);
        }

        SecurityContext c = creator.createDefaultSecurityContext(context,
            preferences, location);
        c.addNote("adaptors", adaptorName); // limit the context to this adaptor only
        context.addSecurityContext(c);

        Object userData = c.getDataObject(dataObjectKey);

        return userData;
    }
}
