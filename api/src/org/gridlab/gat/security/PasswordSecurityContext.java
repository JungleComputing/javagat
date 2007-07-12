package org.gridlab.gat.security;

/**
 * A security context based upon password information.
 */
public class PasswordSecurityContext extends SecurityContext {

    public PasswordSecurityContext(String username, String password) {
        super(username, password);
    }

    /**
     * Check two SecurityContexts for equality.
     *
     * @param obj
     *            the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PasswordSecurityContext)) {
            return false;
        }

        PasswordSecurityContext other = (PasswordSecurityContext) obj;

        return other.username.equals(username)
            && other.password.equals(password);
    }

    /**
     * Returns a clone of this context.
     *
     * @return the clone of this security context
     */
    public Object clone() throws CloneNotSupportedException {
        return new PasswordSecurityContext(username, password);
    }

    public int hashCode() {
        return username.hashCode();
    }
}
