package org.gridlab.gat.security.glite;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glite.security.trustmanager.ContextWrapper;
import org.globus.common.CoGProperties;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;

/**
 * Contains helper function to deal with gLite security.
 * 
 * @author Max Berger
 * @author Thomas Zangerl
 */
public final class GliteSecurityUtils {

    private final static int STANDARD_NEW_PROXY_LIFETIME = 12 * 3600;
    /**
     * If a proxy is going to be reused it should at least have a remaining
     * lifetime of 5 minutes
     */
    private final static int MINIMUM_PROXY_REMAINING_LIFETIME = 5 * 60;

    protected static Logger logger = LoggerFactory.getLogger(GliteSecurityUtils.class);

    private GliteSecurityUtils() {
        // Empty on purpose.
    }

    /**
     * @return the file path for the gLite Proxy.
     */
    public static String getProxyPath(GATContext context) {
    	String proxyFile = (String) context.getPreferences().get(GliteConstants.PREFERENCE_PROXY_PATH);
    	
    	if (proxyFile == null) {
	        proxyFile = System.getenv("X509_USER_PROXY");
    	}

        if (proxyFile == null) {
        	CoGProperties properties = CoGProperties.getDefault();
            proxyFile = properties.getProxyFile();
        }

        // JARs
        System.setProperty(ContextWrapper.CREDENTIALS_PROXY_FILE, proxyFile);
        return proxyFile;
    }

    /**
     * Create a new proxy or reuse the old one if needed.
     * 
     * @param context
     *            GATContext with security parameters.
     * @return path to the proxy.
     * @throws GATInvocationException
     */
    public static synchronized String touchVomsProxy(GATContext context)
            throws GATInvocationException {
        String proxyFile = GliteSecurityUtils.getProxyPath(context);

        Preferences prefs = context.getPreferences();

        int minLifetime = parseIntPref(prefs,
                GliteConstants.PREFERENCE_VOMS_MIN_LIFETIME,
                MINIMUM_PROXY_REMAINING_LIFETIME);
        int newLifetime = Math.max(minLifetime, parseIntPref(prefs,
                GliteConstants.PREFERENCE_VOMS_NEW_LIFETIME,
                STANDARD_NEW_PROXY_LIFETIME));
        
        //Check the "glite.proxycreation" value
        String proxyCreation = (String) prefs.get(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY); 
        if(proxyCreation == null){
        	proxyCreation = "ondemand";
        }
        
        if(proxyCreation.equalsIgnoreCase("never")){
        	//JavaGAT must not generate voms proxy
        	logger.info("Always reuse old voms proxy");
        	return proxyFile;
        }else if(proxyCreation.equalsIgnoreCase("ondemand")){ 
        	//JavaGAT will generate a new proxy if it these cases:
        	//	* The current proxy doesn't correspond anymore to javaGat preferences (VO, VO group, VO role, etc... )
        	//	* The current proxy lifetime is inferior to the requested lifetime
        	logger.info("Checking whether the VOMS proxy extensions correspond to the JavaGAT preferences.");
        	List<String> currentVomsProxyExtensionsList = VomsProxyManager.getExistingVOMSExtensions(""+ proxyFile);
        	boolean currentExtensionsOk = true;
        	StringBuilder reason = new StringBuilder();
        	if(currentVomsProxyExtensionsList != null){//There is an existing voms proxy
        		//Extensions order is important for gLite. Currently, javaGAT is only able to specify 1 extension (1 vo, 1 group, 1 role maximum)
        		//This JavaGAT extension must be the first one in the voms proxy.
        		if(currentVomsProxyExtensionsList.size() != 0){
        			String currentVomsProxyExtension = currentVomsProxyExtensionsList.get(0);
        			String currentJavaGATVo = GliteConstants.getVO(context);
        			String currentJavaGATVoGroup = (String) prefs.get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP);
        			String currentJavaGATVoRole = (String) prefs.get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE);
        			String currentJavaGATVoCapability = (String) prefs.get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY);
        			
        			if(currentJavaGATVoGroup == null){
        				currentJavaGATVoGroup = currentJavaGATVo;
        			}
        			if(currentJavaGATVoRole == null){
        				currentJavaGATVoRole = "NULL";
        			}
        			if(currentJavaGATVoCapability == null){
        				currentJavaGATVoCapability = "NULL";
        			}
        			String currentProxyVo = currentVomsProxyExtension.substring(1, currentVomsProxyExtension.indexOf("/Role=")).split("/")[0];
        			String currentProxyVoGroup = currentVomsProxyExtension.substring(1,currentVomsProxyExtension.indexOf("/Role=")).replaceFirst(currentProxyVo+"/", "");
        			String currentProxyVoRole =	currentVomsProxyExtension.substring(currentVomsProxyExtension.indexOf("/Role=")+6, currentVomsProxyExtension.indexOf("/Capability="));
        			String currentProxyVoCapability = currentVomsProxyExtension.substring(currentVomsProxyExtension.indexOf("/Capability=")+12, currentVomsProxyExtension.length());
        			
        			if(!currentProxyVo.equals(currentJavaGATVo)){
        				currentExtensionsOk = false;
        				reason.append("VO proxy ").append(currentProxyVo).append(" vs ").append(currentJavaGATVo).append(' ');
        			}
        			if(!currentProxyVoGroup.equals(currentJavaGATVoGroup)){
        				currentExtensionsOk = false;
                        reason.append("VoGroup proxy ").append(currentProxyVoGroup).append(" vs ").append(currentJavaGATVoGroup).append(' ');
        			}
        			if(!currentProxyVoRole.equals(currentJavaGATVoRole)){
        				currentExtensionsOk = false;
                        reason.append("VoRole proxy ").append(currentProxyVoRole).append(" vs ").append(currentJavaGATVoRole).append(' ');
        			}
        			if(!currentProxyVoCapability.equals(currentJavaGATVoCapability)){
        				currentExtensionsOk = false;
                        reason.append("VoCapability proxy ").append(currentProxyVoCapability).append(" vs ").append(currentJavaGATVoCapability).append(' ');
        			}
        		}else{
        			currentExtensionsOk = false;
        			reason.append("No VOMS extensions");
        		}
        	}else{
        		currentExtensionsOk = false;
        	}
        	
        	if(currentExtensionsOk == false){
        		logger.info("Current VOMS proxy extensions doesn't correspond to the JavaGAT preference. Creation of a new proxy");
        		logger.info("Reason: "+reason.toString());
        		createVomsProxy(newLifetime, context, proxyFile);
        	}else{
	        	logger.info("Checking the current proxy lifetime and generate a new one if needed");
	        	long existingLifetime = VomsProxyManager.getExistingProxyLifetime(proxyFile);
	        	if (existingLifetime < minLifetime) {
	        		logger.info("Current VOMS proxy lifetime not sufficient ("
                            + existingLifetime + " < " + minLifetime
                            + "). Creation of a new proxy");
	                createVomsProxy(newLifetime, context, proxyFile);
	            } else {
	                logger.info("Reusing old voms proxy with lifetime (seconds): "+ existingLifetime);
	            }
        	}
        }else if(proxyCreation.equalsIgnoreCase("always")){
        	//JavaGAT will always generate a new proxy
        	createVomsProxy(newLifetime, context, proxyFile);
        }else{
        	throw new GATInvocationException("Unknown "+ GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY + " value ("+proxyCreation+")" +
        			" Accepted values are: \"never\", \"ondemand\" and \"always\" (case-insensitive)");
        }

        return proxyFile;
    }

    private static int parseIntPref(Preferences prefs, String preferenceName,
            int defaultValue) {
        int retVal;
        String prefStr = (String) prefs.get(preferenceName);
        if (prefStr == null) {
            retVal = defaultValue;
        } else {
            try {
                retVal = Integer.parseInt(prefStr);
            } catch (NumberFormatException nfe) {
                retVal = defaultValue;
            }
        }
        return retVal;
    }

    /**
     * Create a VOMS proxy (with ACs) and store on the position on the
     * filesystem indicated by the global X509_USER_PROXY variable. All the
     * necessary parameters for the voms proxy creation such as path to the user
     * certificate, user key, password, desired lifetime and server specific
     * data such as host-dn, URL of the server and server port are expected to
     * be given as global preferences to the gat context. User key and
     * certificate location, as well as the key's password are expected to be
     * given within a CertificateSecurityContext that is part of the GATContext.
     * 
     * <p>
     * The preferences keys passed in the gatContext are expected to look as
     * follows (with String as their datatype, also for port and lifetime):
     * </p>
     * 
     * <table>
     * <tr>
     * <td>vomsLifetime</td>
     * <td>the desired proxy lifetime in seconds (optional)</td>
     * </tr>
     * <tr>
     * <td>vomsHostDN</td>
     * <td>the distinguished name of the VOMS host, (e.g.
     * /DC=cz/DC=cesnet-ca/O=CESNET/CN=skurut19.cesnet.cz)</td>
     * </tr>
     * <tr>
     * <td>vomsServerURL</td>
     * <td>the URL of the voms server, without protocol (e.g.
     * skurut19.cesnet.cz)</td>
     * </tr>
     * <tr>
     * <td>vomsServerPort</td>
     * <td>the port on which to connect to the voms server</td>
     * </tr>
     * <tr>
     * <td>VirtualOrganisationGroup</td>
     * <td>The group inside the virtual organisation for which the voms proxy is created</td>
     * </tr>
     * <tr>
     * <td>VirtualOrganisationRole</td>
     * <td>The role inside the virtual organisation for which the voms proxy is created (e.g. VOAdmin)</td>
     * </tr>
     * <td>VirtualOrganisationCapability</td>
     * <td>The capability inside the virtual organisation for which the voms proxy is created</td>
     * </tr>
     * </table>
     * 
     * @author thomas
     */
    private static void createVomsProxy(int lifetime, GATContext context,
            String proxyFile) throws GATInvocationException {
        logger.info("Creating new VOMS proxy with lifetime (seconds): "
                + lifetime);

        CertificateSecurityContext secContext = null;

        if (context.getSecurityContexts() == null) {
            throw new GATInvocationException(
                    "Error: found no security contexts in GAT Context!");
        }

        for (SecurityContext c : context.getSecurityContexts()) {
            if (c instanceof CertificateSecurityContext) {
                secContext = (CertificateSecurityContext) c;
            }
        }

        Preferences prefs = context.getPreferences();
        final URI userkeyuri = secContext.getKeyfile();
        final String userkey;
        if (userkeyuri == null) {
            userkey = personalGlobusDir() + "userkey.pem";
        } else {
            userkey = userkeyuri.getPath();
        }
        final URI usercerturi = secContext.getCertfile();
        final String usercert;
        if (usercerturi == null) {
            usercert = personalGlobusDir() + "usercert.pem";
        } else {
            usercert = usercerturi.getPath();
        }

        String hostDN = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN);
        String serverURI = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL);
        String serverPortStr = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT);
        int serverPort = Integer.parseInt(serverPortStr);

        String voName = GliteConstants.getVO(context);
        String voGroup = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP);
        String voRole = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE);
        String voCapability = (String) prefs
        		.get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY);

        String requestCode = voName + (voGroup == null ? "" : "/" + voGroup)
                + (voRole == null ? "" : "/Role=" + voRole)
        		+ (voCapability == null ? "" : "/Capability=" + voCapability);
        try {
            VomsProxyManager manager = new VomsProxyManager(usercert, userkey,
                    secContext.getPassword(), lifetime, hostDN, serverURI,
                    serverPort);
            manager.makeProxyCredential(requestCode);
            manager.saveProxyToFile(proxyFile);

        } catch (Exception e) {
            throw new GATInvocationException("Could not create VOMS proxy!", e);
        }
    }

    /**
     * @return the default personal globus directory.
     */
    private static String personalGlobusDir() {
        return System.getProperty("user.home") + File.separatorChar + ".globus"
                + File.separatorChar;
    }

    public static void addGliteSecurityPreferences(Preferences preferences) {
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION,
                "<no default>");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP,
                "");
        preferences
                .put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE, "");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY, "");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN,
                "<no default>");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL,
                "<no default>");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT,
                "<no default>");
        preferences.put(GliteConstants.PREFERENCE_VOMS_MIN_LIFETIME, Integer
                .toString(MINIMUM_PROXY_REMAINING_LIFETIME));
        preferences.put(GliteConstants.PREFERENCE_VOMS_NEW_LIFETIME, Integer
                .toString(STANDARD_NEW_PROXY_LIFETIME));
        preferences.put(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY,
                "ondemand");
    }
}
