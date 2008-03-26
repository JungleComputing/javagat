package org.gridlab.gat.security;

import org.gridlab.gat.URI;

/**
 * A container for security Information based upon certificates. Contexts based
 * upon these mechanisms can be used by adaptors to create further contexts
 * containing opaque data objects, e.g. GSSAPI credentials.
 */
public class CertificateSecurityContext extends SecurityContext {

    /**
     * This member variables holds the URI of the keyfile of the SecurityContext
     */
    private URI keyfile = null;

    /**
     * Some ssh implementations on windows (tunnelier) use a private key slot
     */
    private int privateKeySlot = -1;

    private URI certfile = null;

    /**
     * Constructs a {@link CertificateSecurityContext} out of a {@link URI}
     * pointing to the private key, a {@link URI} pointing to the certificate, a
     * username and a password.
     * 
     * @param keyfile
     *                the private key file (for example userkey.pem)
     * @param certfile
     *                the certificate file (for example usercert.pem)
     * @param username
     *                the username
     * @param password
     *                the password or passphrase belonging to the key and
     *                certificate.
     */
    public CertificateSecurityContext(URI keyfile, URI certfile,
            String username, String password) {
        super(username, password);
        this.keyfile = keyfile;
        this.certfile = certfile;
    }

    /**
     * Constructs a {@link CertificateSecurityContext} out of a {@link URI}
     * pointing to the private key, a {@link URI} pointing to the certificate
     * and a password.
     * 
     * @param keyfile
     *                the private key file (for example userkey.pem)
     * @param certfile
     *                the certificate file (for example usercert.pem)
     * @param password
     *                the password or passphrase belonging to the key and
     *                certificate.
     */
    public CertificateSecurityContext(URI keyfile, URI certfile, String password) {
        this(keyfile, certfile, null, password);
    }

    /**
     * Check two SecurityContexts for equality.
     * 
     * @param obj
     *                the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof CertificateSecurityContext)) {
            return false;
        }

        CertificateSecurityContext other = (CertificateSecurityContext) obj;

        return other.password.equals(password) && other.keyfile.equals(keyfile)
                && other.username.equals(username)
                && other.certfile.equals(certfile);
    }

    /**
     * Returns a clone of this context.
     * 
     * @return the clone of this security context (but not the associated
     *         adaptor data)
     */
    public Object clone() throws CloneNotSupportedException {
        return new CertificateSecurityContext(keyfile, certfile, username,
                password);
    }

    /**
     * Returns the location of the keyfile associated with the context.
     * 
     * @return The location of the keyfile associated with the context.
     */
    public URI getKeyfile() {
        return keyfile;
    }

    /**
     * Sets the location of the keyfile associated with the context.
     * 
     * @param keyfile
     *                the location of the keyfile associated with the context.
     */
    public void setKeyfile(URI keyfile) {
        this.keyfile = keyfile;
    }

    public int hashCode() {
        return keyfile.hashCode();
    }

    public String toString() {
        return "CertificateSecurityContext(keyfile = " + keyfile
                + " certfile = " + certfile
                + ((username == null) ? "" : ("username = " + username))
                + ((dataObjects == null) ? "" : ("userdata = " + dataObjects))
                + ")";
    }

    /**
     * Returns the private key slot. Some ssh implementations on windows
     * (tunnelier) use a private key slot.
     * 
     * @return the private key slot.
     */
    public int getPrivateKeySlot() {
        return privateKeySlot;
    }

    /**
     * Sets the private key slot. Some ssh implementations on windows
     * (tunnelier) use a private key slot.
     * 
     * @param privateKeySlot
     *                the new private key slot.
     */
    public void setPrivateKeySlot(int privateKeySlot) {
        this.privateKeySlot = privateKeySlot;
    }

    /**
     * Returns the {@link URI} of the certificate file.
     * 
     * @return the {@link URI} of the certificate file.
     */
    public URI getCertfile() {
        return certfile;
    }

    /**
     * Set the location of the certificate file.
     * 
     * @param certfile
     *                the location of the certificate file.
     */
    public void setCertfile(URI certfile) {
        this.certfile = certfile;
    }
}
