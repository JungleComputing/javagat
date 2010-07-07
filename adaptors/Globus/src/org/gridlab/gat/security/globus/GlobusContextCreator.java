package org.gridlab.gat.security.globus;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.security.MyProxyServerCredentialSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements {@link SecurityContextCreator} for globus toolkit.
 * 
 * @author Stefan Bozic
 */
public class GlobusContextCreator implements SecurityContextCreator {

	/** Logger instance */
	protected static Logger logger = LoggerFactory.getLogger(GlobusContextCreator.class);

	/**
	 * @see org.gridlab.gat.security.cpi.SecurityContextCreator#createDefaultSecurityContext(org.gridlab.gat.GATContext, org.gridlab.gat.URI)
	 */
	public SecurityContext createDefaultSecurityContext(GATContext gatContext, URI location)
			throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException {
		// automatically try and insert the default credential if it was not there.
		GSSCredential cred = GlobusSecurityUtils.getDefaultCredential();
		CredentialSecurityContext c = new CredentialSecurityContext();
		c.putDataObject("globus", cred);
		return c;
	}
	
	/**
	 * @see org.gridlab.gat.security.cpi.SecurityContextCreator#createUserData(org.gridlab.gat.GATContext, org.gridlab.gat.URI, org.gridlab.gat.security.SecurityContext)
	 */
	public Object createUserData(GATContext gatContext, URI location, SecurityContext inContext)
			throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException {
		// we need to try to create the credential given the securityContext
		// if it fails, just try the next one on the list.
		if (inContext instanceof CredentialSecurityContext) {
			CredentialSecurityContext c = (CredentialSecurityContext) inContext;
			Object credentialObject = c.getCredential();
			if (credentialObject != null) {
				// Added check if it already is a credential object.
				// If so, just return it. --Ceriel
				if (credentialObject instanceof GSSCredential) {
					if (logger.isDebugEnabled()) {
						logger.debug("CredentialSecurityContext credential is instance of GSSCredential");
					}
					return credentialObject;
				}
				if (credentialObject instanceof byte[] || credentialObject instanceof String) {
					// if it is of the type String or byte[] we can try to
					// derive a GSSCredential from it
					if (credentialObject instanceof String) {
						if (logger.isDebugEnabled()) {
							logger.debug("CredentialSecurityContext credential is instance of String");
						}
						try {
							credentialObject = ((String) credentialObject).getBytes("UTF-8");
						} catch (UnsupportedEncodingException e) {
							if (logger.isDebugEnabled()) {
								logger.debug("Got exception", e);
							}
							return null;
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("CredentialSecurityContext credential is instance of byte[]");
					}
					try {
						GlobusCredential globusCred = new GlobusCredential(new ByteArrayInputStream(
								(byte[]) credentialObject));
						GSSCredential result = new GlobusGSSCredentialImpl(globusCred,
								GSSCredential.INITIATE_AND_ACCEPT);
						return result;
					} catch (GlobusCredentialException e) {
						if (logger.isDebugEnabled()) {
							logger.debug("Got exception", e);
						}
						return null;
					} catch (GSSException e) {
						if (logger.isDebugEnabled()) {
							logger.debug("Got exception", e);
						}
						return null;
					}
				}
			}
		}
		
		if (inContext instanceof CertificateSecurityContext) {
			CertificateSecurityContext c = (CertificateSecurityContext) inContext;
			String passphrase = c.getPassword();
			String keyFile = c.getKeyfile().toString();
			String certFile = c.getCertfile().toString();
			try {
				GATGridProxyModel model = new GATGridProxyModel();
				GlobusCredential globusCred = model.createProxy(passphrase, certFile, keyFile);
				GSSCredential result = new GlobusGSSCredentialImpl(globusCred, GSSCredential.INITIATE_AND_ACCEPT);

				if (logger.isDebugEnabled()) {
					logger.debug("Passphrase: SUCCESS");
				}
				return result;
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Passphrase: FAILED " + e);
				}
				return null;
			}
		} else if (inContext instanceof MyProxyServerCredentialSecurityContext) {
			MyProxyServerCredentialSecurityContext c = (MyProxyServerCredentialSecurityContext) inContext;

			GSSCredential cred = GlobusSecurityUtils.getCredentialFromMyProxyServer(gatContext, c.getHost(), c
					.getPort(), c.getUsername(), c.getPassword());

			return cred;
		}
		return null; // unknown security context type
	}
}
