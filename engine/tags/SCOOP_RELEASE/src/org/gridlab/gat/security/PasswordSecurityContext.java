package org.gridlab.gat.security;

/**
 * A security context based upon password information.
 */
public class PasswordSecurityContext extends SecurityContext {

    /**
     * This member variables holds the username of the SecurityContext
     */
    private String username = null;

    /**
     * This member variables holds the password of the SecurityContext
     */
    private String password = null;

    public PasswordSecurityContext(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Check two SecurityContexts for equality.
     * 
     * @param obj
     *            the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PasswordSecurityContext))
            return false;

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

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int hashCode() {
        return username.hashCode();
    }
}