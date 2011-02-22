package org.gridlab.gat.security;

/**
 * A container for security informations based on assertions stored in an object. An assertion is a document which
 * allows a trusted instance to access a service on behalf of the issuer.
 * 
 * @author Andreas Bender
 */
public class AssertionSecurityContext extends SecurityContext {

	/**
	 * the assertion document as string
	 */
	protected String assertion;

	/**
	 * Constructor.
	 * 
	 * @param username
	 *            a unique name of the assertion issuer
	 * @param assertion
	 *            the assertion document
	 */
	public AssertionSecurityContext(String username, String assertion) {
		super(username, null);

		this.assertion = assertion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assertion == null) ? 0 : assertion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssertionSecurityContext other = (AssertionSecurityContext) obj;
		if (assertion == null) {
			if (other.assertion != null)
				return false;
		} else if (!assertion.equals(other.assertion))
			return false;
		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AssertionSecurityContext(username, assertion);
	}

	/**
	 * Returns the assertion.
	 * 
	 * @return the assertion
	 */
	public String getAssertion() {
		return assertion;
	}

}
