package org.gridlab.gat.security;

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
		throw new CloneNotSupportedException();
	}
	
	
	public int hashCode() {
		return username.hashCode();
	}
}