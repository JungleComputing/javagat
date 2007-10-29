package org.gridlab.gat.security;

import org.gridlab.gat.URI;

/**
 * A container for security Information based upon credentials stored in a file. Contexts
 * based upon these mechanisms can be used by adaptors to create further
 * contexts containing opaque data objects, e.g. GSSAPI credentials.
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

    public CertificateSecurityContext(URI keyfile, URI certfile, String username, String password) {
    	super(username, password);
    	this.keyfile = keyfile;
    	this.certfile = certfile;
    }
	
    public CertificateSecurityContext(URI keyfile, URI certfile, String password) {
    	this(keyfile, certfile, null, password);
    }	
	/**
     * @param password
     * @param keyfile
     */
    public CertificateSecurityContext(URI keyfile, String username,
        String password) {
        this(keyfile, null, username, password);
    }

    /**
     * Makes this a "Certificate" type security context and stores the
     * information about the location of keyfile in the
     * context.
     *
     * @param keyfile
     *            The URI of keyfile
     */
    public CertificateSecurityContext(URI keyfile) {
        this(keyfile, null, null, null);
    }

    public CertificateSecurityContext() {
        super(null, null);
    }

    public CertificateSecurityContext(URI keyfile, String password) {
    	this(keyfile, null, null, password);
    }

	/**
     * Check two SecurityContexts for equality.
     *
     * @param obj
     *            the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof CertificateSecurityContext)) {
            return false;
        }

        CertificateSecurityContext other = (CertificateSecurityContext) obj;

        return other.password.equals(password)
            && other.keyfile.equals(keyfile) && other.username.equals(username);
    }

    /**
     * Returns a clone of this context.
     *
     * @return the clone of this security context (but not the associated adaptor data)
     */
    public Object clone() throws CloneNotSupportedException {
        return new CertificateSecurityContext(keyfile, username, password);
    }

    /**
     * Gets the type associated with the security context.
     *
     * @return The location of the keyfile associated with the context.
     */
    public URI getKeyfile() {
        return keyfile;
    }

    public void setKeyfile(URI keyfile) {
        this.keyfile = keyfile;
    }

    public int hashCode() {
        return keyfile.hashCode();
    }

    public String toString() {
        return "CertificateSecurityConext(keyfile = " + keyfile
            + ((username == null) ? "" : ("username = " + username))
            + ((dataObjects == null) ? "" : ("userdata = " + dataObjects))
            + ")";
    }

    public int getPrivateKeySlot() {
        return privateKeySlot;
    }

    public void setPrivateKeySlot(int privateKeySlot) {
        this.privateKeySlot = privateKeySlot;
    }

	public URI getCertfile() {
		return certfile ;
	}
	
	public void setCertfile(URI certfile) {
		this.certfile = certfile;
	}
}
