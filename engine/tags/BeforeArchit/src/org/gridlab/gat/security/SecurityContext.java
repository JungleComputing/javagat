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
public class SecurityContext implements Cloneable {
	/**
	 * This member variables holds the name of the SecurityContext
	 */
	private String name = null;

	/**
	 * This member variables holds the type of the SecurityContext
	 */
	private String type = null;

	/**
	 * This member variables holds the object of the SecurityContext
	 */
	private Object object = null;

	/**
	 * This member variables holds the username of the SecurityContext
	 */
	private String username = null;

	/**
	 * This member variables holds the password of the SecurityContext
	 */
	private String password = null;

	/**
	 * This member variables holds the URI of the keyfile of the SecurityContext
	 */
	private URI keyfile = null;

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
	 * Creates a new security context.
	 * 
	 * @param name
	 *            A java.lang.String description
	 */
	public SecurityContext(String name) {
		this.name = name;
	}

	/**
	 * Creates a new security context with a specific name, type and data object
	 * associated with it.
	 * 
	 * @param name
	 *            A java.lang.String description
	 * @param type
	 *            A java.lang.String description
	 * @param object
	 *            An Object associated with this data
	 */
	public SecurityContext(String name, String type, Object object) {
		this.name = name;
		this.type = type;
		this.object = object;
	}

	/**
	 * Check two SecurityContexts for equality.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof SecurityContext))
			return false;

		SecurityContext other = (SecurityContext) obj;

		return other.name.equals(name) && other.type.equals(type);
	}

	/**
	 * Returns a clone of this context.
	 */
	public Object clone() {
		return null;
	}

	/**
	 * Makes this a "Password" type security context and stores the username and
	 * password in the context.
	 * 
	 * @param username
	 *            A username, a java.lang.String, associated with password.
	 * @param password
	 *            A password, a java.lang.String.
	 */
	public void passwordAuthenticate(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Makes this a "Certificate" type security context and stores the
	 * information about the location of keyfile and certificate file in the
	 * context.
	 * 
	 * @param keyfile
	 *            The URI of keyfile
	 * @param certificate
	 *            The URI of certificate file
	 */
	public void certificateAuthenticate(URI keyfile, URI certificate) {
		this.keyfile = keyfile;
		this.certificate = certificate;
	}

	/**
	 * Makes this a "Certificate" type security context and stores the
	 * information about the location of keyfile and certificate file in the
	 * context.
	 * 
	 * @param keyfile
	 *            The URI of keyfile
	 * @param certificate
	 *            The URI of certificate file
	 * @param passphrase
	 *            The passphrase a java.lang.String
	 */
	public void certificateAuthenticate(URI keyfile, URI certificate,
			String passphrase) {
		this.keyfile = keyfile;
		this.certificate = certificate;
		this.passphrase = passphrase;
	}

	/**
	 * Gets the name associated with the security context.
	 * 
	 * @return The name, a java.lang.String, associated with the context.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type associated with the security context.
	 * 
	 * @return The type associated with the context, a java.lang.String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the username associated with the security context.
	 * 
	 * @return The type associated with the context, a java.lang.String
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the type associated with the security context.
	 * 
	 * @return The password associated with the context, a java.lang.String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Gets the type associated with the security context.
	 * 
	 * @return The location of the keyfile associated with the context.
	 */
	public URI getKeyfile() {
		return keyfile;
	}
}