package org.gridlab.gat.security;

import org.gridlab.gat.URI;

/**
 * A container for security Information based upon credentials stored in a file. Contexts
 * based upon these mechanisms can be used by adaptors to create further
 * contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
public class CertificateSecurityContext extends SecurityContext {

    /**
     * This member variables holds the passphrase of the SecurityContext
     */
    private String passphrase = null;

    /**
     * This member variables holds the URI of the keyfile of the SecurityContext
     */
    private URI keyfile = null;

    /**
     * Some protocols need a username and a private key (e.g. sftp)
     */
    private String username = null;

    public CertificateSecurityContext() {
    }

    /**
     * @param passphrase
     * @param keyfile
     */
    public CertificateSecurityContext(URI keyfile, String username,
            String passphrase) {
        this.passphrase = passphrase;
        this.keyfile = keyfile;
        this.username = username;
    }

    /**
     * Makes this a "Certificate" type security context and stores the
     * information about the location of keyfile in the
     * context.
     * 
     * @param newKeyfile
     *            The URI of keyfile
     */
    public CertificateSecurityContext(URI newKeyfile) {
        this.keyfile = newKeyfile;
    }

    /**
     * Check two SecurityContexts for equality.
     * 
     * @param obj
     *            the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof CertificateSecurityContext))
            return false;

        CertificateSecurityContext other = (CertificateSecurityContext) obj;

        return other.passphrase.equals(passphrase)
                && other.keyfile.equals(keyfile)
                && other.username.equals(username);
    }

    /**
     * Returns a clone of this context.
     * 
     * @return the clone of this security context (but not the associated adaptor data)
     */
    public Object clone() throws CloneNotSupportedException {
        return new CertificateSecurityContext(keyfile, username, passphrase);
    }

    /**
     * Gets the type associated with the security context.
     * 
     * @return The location of the keyfile associated with the context.
     */
    public URI getKeyfile() {
        return keyfile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setKeyfile(URI keyfile) {
        this.keyfile = keyfile;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public int hashCode() {
        return keyfile.hashCode();
    }
    
    public String toString() {
        return "CertificateSecurityConext(keyfile = " + keyfile +
        (username == null ? "" : "username = "  + username) + 
        (dataObjects == null ? "" : "userdata = "  + dataObjects) +
        ")";
    }
}