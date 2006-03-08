package org.gridlab.gat.security;

import java.net.URI;

/**
 * A container for security Information. Each context has a name and a type, and
 * a data object associated with it. The data object is opaque to the GAT API
 * and is used and manipulated by adaptors based upon their interpretation of
 * the type.
 * <p>
 * Currently we provide additional auxiliary methods to create a context based
 * upon password information or upon credentials stored in a file. Contexts
 * based upon these mechanisms can be used by adaptors to create further
 * contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
public class CertificateSecurityContext extends SecurityContext {

	/**
	 * This member variables holds the URI of the certificate of the
	 * SecurityContext
	 */
	private URI certificate = null;

	/**
	 * This member variables holds the passphrase of the SecurityContext
	 */
	private String passphrase = null;

	/**
	 * This member variables holds the URI of the keyfile of the SecurityContext
	 */
	private URI keyfile = null;

	/**
	 * @param certificate
	 * @param passphrase
	 * @param keyfile
	 */
	public CertificateSecurityContext(URI certificate,
			String passphrase, URI keyfile) {
		this.certificate = certificate;
		this.passphrase = passphrase;
		this.keyfile = keyfile;
	}

	/**
	 * Makes this a "Certificate" type security context and stores the
	 * information about the location of keyfile and certificate file in the
	 * context.
	 * 
	 * @param newKeyfile
	 *            The URI of keyfile
	 * @param newCertificate
	 *            The URI of certificate file
	 */
	public CertificateSecurityContext(URI newKeyfile, URI newCertificate) {
		this.keyfile = newKeyfile;
		this.certificate = newCertificate;
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

		return other.certificate.equals(certificate)
				&& other.passphrase.equals(passphrase)
				&& other.keyfile.equals(keyfile);
	}

	/**
	 * Returns a clone of this context.
	 * 
	 * @return the clone of this security context
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Gets the type associated with the security context.
	 * 
	 * @return The location of the keyfile associated with the context.
	 */
	public URI getKeyfile() {
		return keyfile;
	}
	
	
	public int hashCode() {
		return certificate.hashCode();
	}
}