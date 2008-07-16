package org.gridlab.gat.security;

/**
 * A container for security Information based upon credentials stored in an
 * object. Such an object can be the credential itself (e.g. a GSSCredential for
 * Globus) or an object representation that can be converted to the credential
 * (for instance a String representation of Globus certificate, which can be
 * converted to a GSSCredential, for optimization reasons one can store the
 * converted credential again in the CredentialSecurityContext).
 */
public class CredentialSecurityContext extends SecurityContext {

    /**
     * This member variable holds the credential object
     */
    private Object credential;

    /**
     * Constructor of the CredentialSecurityContext.
     * 
     * Use {@link #setCredential(Object)} to set the credential object.
     */
    public CredentialSecurityContext() {
        super(null, null);
    }

    /**
     * Constructor of the CredentialSecurityContext.
     * 
     * @param credential
     *                creates a {@link CredentialSecurityContext} with the
     *                supplied credential
     */
    public CredentialSecurityContext(Object credential) {
        super(null, null);
        this.credential = credential;
    }

    /**
     * Returns a clone of this context.
     * 
     * @return the clone of this security context
     */
    public Object clone() throws CloneNotSupportedException {
        CredentialSecurityContext result = new CredentialSecurityContext();
        result.setCredential(credential);
        return result;
    }

    /**
     * Check two SecurityContexts for equality.
     * 
     * @param obj
     *                the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof CredentialSecurityContext) {
            return credential
                    .equals(((CredentialSecurityContext) obj).credential);
        }
        return false;
    }

    /**
     * Gets the hashcode of this security context
     * 
     * @return The hashcode of this object
     */
    public int hashCode() {
        return credential.hashCode();
    }

    /**
     * Gets the credential associated with this security context.
     * 
     * @return The credential associated with this security context.
     */
    public Object getCredential() {
        return credential;
    }

    /**
     * Sets the credential associated with this security context.
     * 
     * @param credential
     *                The credential to be associated with this security
     *                context.
     */
    public void setCredential(Object credential) {
        this.credential = credential;
    }

}
