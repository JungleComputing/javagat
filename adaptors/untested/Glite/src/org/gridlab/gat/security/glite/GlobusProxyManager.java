package org.gridlab.gat.security.glite;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.CredentialInfo;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.resources.cpi.glite.GliteResourceBrokerAdaptor;
import org.ietf.jgss.GSSException;

/**
 * Create and manage a standard globus proxy.
 * @author thomas
 *
 */
public class GlobusProxyManager {
	protected static final Logger logger = Logger.getLogger(GlobusProxyManager.class);
	
	protected GlobusGSSCredentialImpl gsci = null;	/** The GSS-implemented globus credential */
	protected int lifetime = -1;					/** The lifetime of the the proxy */
	protected GlobusCredential proxyCred = null;		/** The globus credentials that will be obtained */
	protected OpenSSLKey proxyKey = null;				/** Internal representation of the private key of the user */
	protected X509Certificate[] proxyCerts = null;		/** Internal representation of the certificate(s) of the CA of the user */
	private CredentialInfo credInfo = null;			/** This is where the credential information will be encapsulated */
	
	protected static final int KEY_LENGTH = 512;
	/**
	 * Constructs a new instance of the proxy with specified lifetime.
	 * @throws GSSException 
	 */
	public GlobusProxyManager(String userCertFile,
							  String userKeyFile,
							  String keyPassword, 
							  int lifetime) 
	throws IOException, GeneralSecurityException, GSSException  {
		this.lifetime = lifetime;

		CoGProperties properties = CoGProperties.getDefault();
		String proxyLocation = "";
		String usercert = "";
		String userkey = "";
		
		// set the environment to the value stored in the proxy if it is currently unset
		if ((proxyLocation = properties.getProxyFile()) != null) {
			System.out.println("Setting proxy location to " + proxyLocation);
			System.setProperty(GliteResourceBrokerAdaptor.PROXY_VAR, proxyLocation);
		}
		
		if (userCertFile == null) {
			userCertFile = properties.getUserCertFile();
		}
		
		if (userKeyFile == null) {
			userKeyFile = properties.getUserKeyFile();
		}
		
		// force the CertUtil to initialize
		CertUtil.init();
		// get the X509 certificate from the globus directory
		proxyCerts = CertUtil.loadCertificates(userCertFile);
		// get the private key from the globus directory
		proxyKey = new BouncyCastleOpenSSLKey(userKeyFile);
		
		// decrypt the proxy key if it is encrypted
		if (proxyKey.isEncrypted()) {
			proxyKey.decrypt(keyPassword);
		}
		
	} // end constructor
	
	
	protected void createProxyCredential() throws GATInvocationException {
		BouncyCastleCertProcessingFactory factory = 
			BouncyCastleCertProcessingFactory.getDefault();
		
		try {
			proxyCred = factory.createCredential(proxyCerts, 
					 proxyKey.getPrivateKey(), 
					 KEY_LENGTH, 
					 lifetime, 
					 GSIConstants.GSI_2_PROXY);
			
			gsci = new GlobusGSSCredentialImpl(proxyCred, 
					   GlobusGSSCredentialImpl.INITIATE_AND_ACCEPT);
		} catch (GSSException e) {
			throw new GATInvocationException("Problem creating GlobusGSSCredentialImpl!", e);
		} catch (GeneralSecurityException e) {
			throw new GATInvocationException("Problem creating Globus proxy!", e);
		}
			

	}
	
	public void makeProxyCredential() throws GATInvocationException {
		if (this.gsci == null) {
			createProxyCredential();
		}

	}
	
	/**
	 * Returns credential information about the proxy.
	 * @return CredentialInfo instance encapsulating information about the proxy.
	 */
	public CredentialInfo getProxyInfo() {
		return credInfo;
	}
	
	public GlobusGSSCredentialImpl getGsci() {
		return gsci;
	}
	
	public String toString() {
		String infoStr = "";
		infoStr += "proxy management - currently managing the following proxy\n";
		infoStr += "*********************************************************\n";
		try {
			infoStr += "Root certificate: " + gsci.getCertificateChain()[0].toString();
			infoStr += "Remaining lifetime: " + gsci.getRemainingLifetime() + "\n";
			infoStr += "GSS name: " + gsci.getName() + "\n";
		} catch (GSSException e) {
			infoStr += "error: unable to retrieve proxy information!";
		}
		
		return infoStr;
	}
}
