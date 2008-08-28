package org.gridlab.gat.security.glite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Set;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleX509Extension;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;
import org.gridforum.jgss.ExtendedGSSContext;
import org.gridlab.gat.GATInvocationException;
import org.ietf.jgss.GSSException;

/**
 * Inspired by the geclipse-project (http://www.geclipse.eu)
 * The VomsProxyManager can be used to create a voms proxy and store it on the 
 * filesystem. First, a temporary globus proxy is created using jglobus libraries,
 * then a secure socket to the voms server is established over which VOMS AC
 * requests and responses are passed. 
 * The information from the VOMS response is saved as an AC in the globus certificate
 * which can then be stored on the hard-disk.
 * 
 * @author thomas
 */
public class VomsProxyManager extends GlobusProxyManager {

	private String serverURI = "";
	private int serverPort = -1;
	private String hostDN = "";
	private GlobusGSSManagerImpl gssMan = null;
	private GssSocket gssSocket = null;
	
	/**
	 * X509 extension numbers for the attribute certificates and fqans needed
	 * for voms proxys
	 */
	private static final String AC_OID = "1.3.6.1.4.1.8005.100.100.5";
	private static final String KU_OID = "2.5.29.15";

	/**
	 * Create a VOMS proxy This is virtually a globus proxy with an X509
	 * extension set allowing for the storage of attribute certificates (ACs)
	 * based on FQANs during the credential negotiations with the voms server
	 * 
	 * @param keyPassword
	 * @param lifetime
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws GSSException
	 */
	public VomsProxyManager(String userCertFile,
							String userKeyFile,
							String keyPassword, 
							int lifetime)
			throws IOException, GeneralSecurityException, GSSException {
		super(userCertFile, userKeyFile, keyPassword, lifetime);
		gssMan = new GlobusGSSManagerImpl();
	}

	public VomsProxyManager(String userCertFile,
							String userKeyFile,
							String keyPassword, 
							int lifetime, 
							String hostDN, 
							String serverURI, 
							int serverPort)
			throws IOException, GeneralSecurityException, GSSException {
		this(userCertFile, userKeyFile, keyPassword, lifetime);
		this.hostDN = hostDN;
		this.serverURI = serverURI;
		this.serverPort = serverPort;
	}

	/**
	 * Basically this follows the factory design pattern. Hence the concrete
	 * credential depends on whether the MyProxyManagement is a
	 * MyProxyManagement class or a MyVomsProxyManagement class
	 * 
	 * @param ac The attribute certificate containing the "visa" given by the voms server
	 * @return The stored credential or a new one if there doesn't exist one
	 * @throws GeneralSecurityException 
	 * @throws GSSException 
	 */
	protected void createProxyCredential(AttributeCertificate ac) throws GATInvocationException {
		ASN1EncodableVector acVec = new ASN1EncodableVector();
		acVec.add(ac);
		
		X509ExtensionSet extSet = new X509ExtensionSet();
		
		DERSequence ds = new DERSequence(acVec);
		DERSequence encodable = new DERSequence(ds);
		
		KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyEncipherment | KeyUsage.dataEncipherment);
		
		BouncyCastleX509Extension bc = new BouncyCastleX509Extension(AC_OID,
				encodable);
		BouncyCastleX509Extension bc1 = new BouncyCastleX509Extension(KU_OID, keyUsage.getDERObject());

		extSet.add(bc);
		extSet.add(bc1);

		BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory
				.getDefault();

		try {
			proxyCred = factory.createCredential(proxyCerts, proxyKey
					.getPrivateKey(), KEY_LENGTH, lifetime,
					GSIConstants.GSI_2_PROXY, extSet);
		} catch (GeneralSecurityException e) {
			throw new GATInvocationException("Problem creating a globus credential!", e);
		}

	}

	/**
	 * Get a VOMS (Attribute Certificate) proxy credential
	 * @param requestCode
	 * @throws GeneralSecurityException
	 * @throws GSSException
	 */
	public void makeProxyCredential(String requestCode) throws GATInvocationException {
		
		if (proxyCred == null) {
			createProxyCredential();
			connectToVomsServer();
			AttributeCertificate ac = query(requestCode);
			disconnectFromVomsServer();
			createProxyCredential(ac);
		}
	}

	// TODO the socket appears as closed even though wireshark reveals that
	// communication IS going on - don't rely on this method yet
	private boolean alreadyConnected() {
		if (gssSocket == null) {
			return false;
		} else {
			return gssSocket.isConnected();
		}
	}

	private void connectToVomsServer() throws GATInvocationException {
		if (alreadyConnected()) {
			return;
		}

		try {
			if (this.proxyCred == null) {
				// create a credential first
				createProxyCredential();
			}

			logger.info("creating gss name with host-dn "
					+ proxyCred.getIdentity());

			GlobusGSSCredentialImpl gsci = 
				new GlobusGSSCredentialImpl(proxyCred, 
											GlobusGSSCredentialImpl.INITIATE_AND_ACCEPT);
			
			// connect with globus mechanism oid
			ExtendedGSSContext egssContext = (ExtendedGSSContext) this.gssMan
					.createContext(null, GSSConstants.MECH_OID, gsci, this.lifetime);

			IdentityAuthorization auth = new IdentityAuthorization(hostDN);

			// the configuration is from VomsServer (geclipse)
			egssContext.requestMutualAuth(true);
			egssContext.requestCredDeleg(false);
			egssContext.requestConf(true);
			egssContext.requestAnonymity(false);

			egssContext.setOption(GSSConstants.GSS_MODE, GSIConstants.MODE_GSI);
			egssContext.setOption(GSSConstants.REJECT_LIMITED_PROXY,
					Boolean.FALSE);
			egssContext.setOption(GSSConstants.CHECK_CONTEXT_EXPIRATION,
					Boolean.TRUE);

			GssSocketFactory sockFactory = GssSocketFactory.getDefault();
			gssSocket = (GssSocket) sockFactory.createSocket(serverURI,
					serverPort, egssContext);
			gssSocket.setWrapMode(2);
			gssSocket.setAuthorization(auth);

			logger.info("received voce voms gss socket at: "
					+ gssSocket.getInetAddress());

		} catch (GSSException e) {
			throw new GATInvocationException("Problem with the credentials detected!", e);
		} catch (IOException e) {
			throw new GATInvocationException("Could not open socket at the VOMS server!", e);
		} 

	}

	private void disconnectFromVomsServer() throws GATInvocationException {
		try {
			if (gssSocket != null) {
				gssSocket.close();
			}
		} catch (IOException e) {
			logger.error("Could not disconnect from the VOMS server!");
		}
	}

	/**
	 * Send a query to the Voms Server This must be called, after
	 * connectToVomsServer
	 * 
	 * @param requestCode
	 *            the requested FQANs
	 * @return the AttributeCertificate which is returned from the server as a response
	 * @throws IOException
	 *             If there are problems with the socket
	 */
	private AttributeCertificate query(String requestCode) throws GATInvocationException {
		AttributeCertificate ac = null;
		
		try {
		
			OutputStream oStream = gssSocket.getOutputStream();
			InputStream iStream = gssSocket.getInputStream();

			VomsServerCommunicator comm = new VomsServerCommunicator(requestCode, this.lifetime);
			comm.writeServerRequest(oStream);
			VomsServerResponse response = comm.readServerResponse(iStream);
		
			if (response.hasErrors()) {
				for (String error : response.getErrors()) {
					logger.info("Error msg: " + error);
				}
				
				throw new GATInvocationException("The server response has errors!");
			}
			
			ac = response.getAc();
			
		} catch (IOException e) {
			throw new GATInvocationException("Could not get streams from VOMS-server socket!", e);
		} 

		return ac;
	}
	
	
	/**
	 * Gets the lifetime of the proxy stored at path.
	 * If no proxy exists, -1 is returned as the lifetime
	 * @param path The path which points to the proxy for which to determine the remaining lifetime
	 * @return Long value of the lifetime or -1 if no valid proxy at path
	 */
	public static long getExistingProxyLifetime(String path) {
		long lifetime = 0L;
		
		try {
			GlobusCredential globCred = new GlobusCredential(path);
			
			// check whether the proxy is also a VOMS proxy and not only a globus proxy
			if (globCred.getCertificateChain()[0].getExtensionValue(AC_OID) != null) {
				lifetime = globCred.getTimeLeft();
			} else {
				lifetime = -1L;
			}
		} catch (Exception e) {
			lifetime = -1L;
		}
		
		return lifetime;
	}
	
	/**
	 * Save the managed proxy to a proxy file and set the permissions accordingly
	 * @param path The path at which the proxy should be stored
	 * @throws IOException If something with the file is wrong
	 * @throws GSSException 
	 */
	public void saveProxyToFile(String path) throws IOException, GSSException {
		FileOutputStream outstream = new FileOutputStream(new File(path));
		proxyCred.save(outstream);
		Runtime.getRuntime().exec("chmod 600 " + path);
		outstream.flush();
		outstream.close();
	}

}
