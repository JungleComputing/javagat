package org.gridlab.gat.security.glite;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.globus.common.CoGProperties;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GliteSecurityUtils.class);

    private static DocumentBuilder documentBuilder;
    
    /** Cached result of CoGProperties.getDefault().getProxyFile(). */
    private static String cogPropertiesProxyFile;
    
    private static boolean cogPropertiesProxyFileDone = false;

    private GliteSecurityUtils() {
        // Empty on purpose.
    }

    /**
     * @return the file path for the gLite Proxy.
     */
    public static String getProxyPath(final GATContext context) {
        String proxyFile = (String) context.getPreferences().get(
                GliteConstants.PREFERENCE_PROXY_PATH);

        if (proxyFile == null) {
            proxyFile = System.getenv("X509_USER_PROXY");
        }

        if (proxyFile == null) {
            synchronized(GliteSecurityUtils.class) {
                // getProxyFile() turns out to be a performance killer, so
                // cache the result. --Ceriel
                if (! cogPropertiesProxyFileDone) {
                    final CoGProperties properties = CoGProperties.getDefault();
                    cogPropertiesProxyFile = properties.getProxyFile();
                    cogPropertiesProxyFileDone = true;
                }
            }
            proxyFile = cogPropertiesProxyFile;
        }

//Try to avoid global properties for mutli-threaded environment !!!
//      // JARs
//      System.setProperty(ContextWrapper.CREDENTIALS_PROXY_FILE, proxyFile);
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
    public static synchronized String touchVomsProxy(final GATContext context) throws GATInvocationException {
        final String proxyFile = GliteSecurityUtils.getProxyPath(context);

        final Preferences prefs = context.getPreferences();

        final int minLifetime = GliteSecurityUtils.parseIntPref(prefs,
                GliteConstants.PREFERENCE_VOMS_MIN_LIFETIME,
                GliteSecurityUtils.MINIMUM_PROXY_REMAINING_LIFETIME);
        final int newLifetime = Math.max(minLifetime, GliteSecurityUtils
                .parseIntPref(prefs,
                        GliteConstants.PREFERENCE_VOMS_NEW_LIFETIME,
                        GliteSecurityUtils.STANDARD_NEW_PROXY_LIFETIME));

        // Check the "glite.proxycreation" value
        String proxyCreation = (String) prefs
                .get(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY);
        if (proxyCreation == null) {
            proxyCreation = "ondemand";
        }

        if (proxyCreation.equalsIgnoreCase("never")) {
            // JavaGAT must not generate voms proxy
            GliteSecurityUtils.LOGGER.info("Always reuse old voms proxy");
            return proxyFile;
        } else if (proxyCreation.equalsIgnoreCase("ondemand")) {
            // JavaGAT will generate a new proxy if it these cases:
            // * The current proxy doesn't correspond anymore to javaGat
            // preferences (VO, VO group, VO role, etc... )
            // * The current proxy lifetime is inferior to the requested
            // lifetime
            GliteSecurityUtils.LOGGER
                    .info("Checking whether the VOMS proxy extensions correspond to the JavaGAT preferences.");
            final List<String> currentVomsProxyExtensionsList = VomsProxyManager
                    .getExistingVOMSExtensions("" + proxyFile);
            boolean currentExtensionsOk = true;
            final StringBuilder reason = new StringBuilder("Proxy "+proxyFile+" -> ");
            if (currentVomsProxyExtensionsList != null) {// There is an existing
                // voms proxy
                // Extensions order is important for gLite. Currently, javaGAT
                // is only able to specify 1 extension (1 vo, 1 group, 1 role
                // maximum)
                // This JavaGAT extension must be the first one in the voms
                // proxy.
                if (currentVomsProxyExtensionsList.size() != 0) {
                    final String currentVomsProxyExtension = currentVomsProxyExtensionsList
                            .get(0);
                    final String currentJavaGATVo = GliteConstants
                            .getVO(context);
                    String currentJavaGATVoGroup = (String) prefs
                            .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP);
                    String currentJavaGATVoRole = (String) prefs
                            .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE);
                    String currentJavaGATVoCapability = (String) prefs
                            .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY);

                    if (currentJavaGATVoGroup == null) {
                        currentJavaGATVoGroup = currentJavaGATVo;
                    }
                    if (currentJavaGATVoRole == null) {
                        currentJavaGATVoRole = "NULL";
                    }
                    if (currentJavaGATVoCapability == null) {
                        currentJavaGATVoCapability = "NULL";
                    }
                    final String currentProxyVo = currentVomsProxyExtension
                            .substring(1,
                                    currentVomsProxyExtension.indexOf("/Role="))
                            .split("/")[0];
                    final String currentProxyVoGroup = currentVomsProxyExtension
                            .substring(1,
                                    currentVomsProxyExtension.indexOf("/Role="))
                            .replaceFirst(currentProxyVo + "/", "");
                    final String currentProxyVoRole = currentVomsProxyExtension
                            .substring(currentVomsProxyExtension
                                    .indexOf("/Role=") + 6,
                                    currentVomsProxyExtension
                                            .indexOf("/Capability="));
                    final String currentProxyVoCapability = currentVomsProxyExtension
                            .substring(currentVomsProxyExtension
                                    .indexOf("/Capability=") + 12,
                                    currentVomsProxyExtension.length());

                    if (!currentProxyVo.equals(currentJavaGATVo)) {
                        currentExtensionsOk = false;
                        reason.append("VO proxy ").append(currentProxyVo)
                                .append(" vs ").append(currentJavaGATVo)
                                .append(' ');
                    }
                    if (!currentProxyVoGroup.equals(currentJavaGATVoGroup)) {
                        currentExtensionsOk = false;
                        reason.append("VoGroup proxy ").append(
                                currentProxyVoGroup).append(" vs ").append(
                                currentJavaGATVoGroup).append(' ');
                    }
                    if (!currentProxyVoRole.equals(currentJavaGATVoRole)) {
                        currentExtensionsOk = false;
                        reason.append("VoRole proxy ").append(
                                currentProxyVoRole).append(" vs ").append(
                                currentJavaGATVoRole).append(' ');
                    }
                    if (!currentProxyVoCapability
                            .equals(currentJavaGATVoCapability)) {
                        currentExtensionsOk = false;
                        reason.append("VoCapability proxy ").append(
                                currentProxyVoCapability).append(" vs ")
                                .append(currentJavaGATVoCapability).append(' ');
                    }
                } else {
                    currentExtensionsOk = false;
                    reason.append("No VOMS extensions");
                }
            } else {
                currentExtensionsOk = false;
                reason.append("No VOMS extensions");
            }

            if (currentExtensionsOk == false) {
                GliteSecurityUtils.LOGGER
                        .info("Current VOMS proxy extensions doesn't correspond to the JavaGAT preference. Creation of a new proxy");
                GliteSecurityUtils.LOGGER.info("Reason: " + reason.toString());
                GliteSecurityUtils.createVomsProxy(newLifetime, context, proxyFile);
            } else {
                GliteSecurityUtils.LOGGER
                        .info("Checking the current proxy lifetime and generate a new one if needed");
                final long existingLifetime = VomsProxyManager
                        .getExistingProxyLifetime(proxyFile);
                if (existingLifetime < minLifetime) {
                    GliteSecurityUtils.LOGGER
                            .info("Current VOMS proxy lifetime not sufficient ("
                                    + existingLifetime
                                    + " < "
                                    + minLifetime
                                    + "). Creation of a new proxy");
                    GliteSecurityUtils.createVomsProxy(newLifetime, context,
                            proxyFile);
                } else {
                    GliteSecurityUtils.LOGGER
                            .info("Reusing old voms proxy with lifetime (seconds): "
                                    + existingLifetime);
                }
            }
        } else if (proxyCreation.equalsIgnoreCase("always")) {
            // JavaGAT will always generate a new proxy
            GliteSecurityUtils.createVomsProxy(newLifetime, context, proxyFile);
        } else {
            throw new GATInvocationException(
                    "Unknown "
                            + GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY
                            + " value ("
                            + proxyCreation
                            + ")"
                            + " Accepted values are: \"never\", \"ondemand\" and \"always\" (case-insensitive)");
        }

        return proxyFile;
    }

    private static int parseIntPref(final Preferences prefs,
            final String preferenceName, final int defaultValue) {
        int retVal;
        final String prefStr = (String) prefs.get(preferenceName);
        if (prefStr == null) {
            retVal = defaultValue;
        } else {
            try {
                retVal = Integer.parseInt(prefStr);
            } catch (final NumberFormatException nfe) {
                retVal = defaultValue;
            }
        }
        return retVal;
    }

    private static class VOInfo {
        private String hostDN;
        private String hostName;
        private Integer port;

        public VOInfo() {
            // Empty on purpose;
        }

        /**
         * @return the hostDN
         */
        public String getHostDN() {
            return this.hostDN;
        }

        /**
         * @return the hostName
         */
        public String getHostName() {
            return this.hostName;
        }

        /**
         * @return the port
         */
        public Integer getPort() {
            return this.port;
        }

        public boolean isValid() {
            return this.hostDN != null && this.hostName != null
                    && this.port != null;
        }

        public void setHostDNIfUnset(final String newHostDN) {
            if (this.hostDN == null) {
                this.hostDN = newHostDN;
            }
        }

        public void setHostNameIfUnset(final String newHostName) {
            if (this.hostName == null) {
                this.hostName = newHostName;
            }
        }

        public void setPortIfUnset(final String newPort) {
            try {
                if ((this.port == null) && (newPort != null)) {
                    this.port = Integer.valueOf(newPort);
                }
            } catch (final NumberFormatException e) {
                GliteSecurityUtils.LOGGER.warn("Invalid Port Number: "
                        + newPort, e);
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("VOInfo [hostDN=");
            builder.append(this.hostDN);
            builder.append(", hostName=");
            builder.append(this.hostName);
            builder.append(", port=");
            builder.append(this.port);
            builder.append("]");
            return builder.toString();
        }

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
     * <td>The group inside the virtual organisation for which the voms proxy is
     * created</td>
     * </tr>
     * <tr>
     * <td>VirtualOrganisationRole</td>
     * <td>The role inside the virtual organisation for which the voms proxy is
     * created (e.g. VOAdmin)</td>
     * </tr>
     * <td>VirtualOrganisationCapability</td>
     * <td>The capability inside the virtual organisation for which the voms
     * proxy is created</td>
     * </tr>
     * </table>
     * 
     * @author thomas
     */
    private static void createVomsProxy(final int lifetime,
            final GATContext context, final String proxyFile)
            throws GATInvocationException {
        GliteSecurityUtils.LOGGER
                .info("Creating new VOMS proxy with lifetime (seconds): "
                        + lifetime);

        CertificateSecurityContext secContext = null;

        if (context.getSecurityContexts() == null) {
        	GliteSecurityUtils.LOGGER.info("Error: found no security contexts in GAT Context!");
            throw new GATInvocationException(
                    "Error: found no security contexts in GAT Context!");
        }

        for (final SecurityContext c : context.getSecurityContexts()) {
            if (c instanceof CertificateSecurityContext) {
                secContext = (CertificateSecurityContext) c;
            }
        }

        if (secContext == null) {
        	GliteSecurityUtils.LOGGER.info("Error: found no CertificateSecurityContext in GAT Context!");
            throw new GATInvocationException(
                    "Error: found no CertificateSecurityContext in GAT Context!");
        }

        final Preferences prefs = context.getPreferences();
        final URI userkeyuri = secContext.getKeyfile();
        final String userkey;
        // CoGProperties also searches in paths given by X509_USER_CERT and
        // X509_USER_KEY
        final CoGProperties properties = CoGProperties.getDefault();
        if (userkeyuri == null) {
            userkey = properties.getUserKeyFile();
        } else {
            userkey = userkeyuri.getPath();
        }
        final URI usercerturi = secContext.getCertfile();
        final String usercert;
        if (usercerturi == null) {
            usercert = properties.getUserCertFile();
        } else {
            usercert = usercerturi.getPath();
        }

        final String voName = GliteConstants.getVO(context);
        if (voName == null) {
        	GliteSecurityUtils.LOGGER.info(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION + " must be set for gLite adaptor");
            throw new GATInvocationException(
                    GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION
                            + " must be set for gLite adaptor");
        }
        final VOInfo voInfo = new VOInfo();

        voInfo.setHostDNIfUnset((String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN));
        voInfo
                .setHostNameIfUnset((String) prefs
                        .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL));
        voInfo
                .setPortIfUnset((String) prefs
                        .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT));

        if (!voInfo.isValid()) {
            GliteSecurityUtils.loadVoCard(voName, voInfo);
        }

        if (!voInfo.isValid()) {
        	GliteSecurityUtils.LOGGER.info("Could not determine settings for VO: " + voName + ". "
                    + voInfo + " Please set them in preferences.");
            throw new GATInvocationException(
                    "Could not determine settings for VO: " + voName + ". "
                            + voInfo + " Please set them in preferences.");
        }

        final String voGroup = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP);
        final String voRole = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE);
        final String voCapability = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY);

        final String requestCode = voName
                + (voGroup == null ? "" : "/" + voGroup)
                + (voRole == null ? "" : "/Role=" + voRole)
                + (voCapability == null ? "" : "/Capability=" + voCapability);
        try {
            final VomsProxyManager manager = new VomsProxyManager(usercert,
                    userkey, secContext.getPassword(), lifetime, voInfo
                            .getHostDN(), voInfo.getHostName(), voInfo
                            .getPort());
            manager.makeProxyCredential(requestCode);
            manager.saveProxyToFile(proxyFile);

        } catch (final Exception e) {
        	GliteSecurityUtils.LOGGER.info("Could not create VOMS proxy. VO: " + voName + " voInfo: "
                    + voInfo + ", requestCode: " + requestCode, e);
            throw new GATInvocationException(
                    "Could not create VOMS proxy. VO: " + voName + " voInfo: "
                            + voInfo + ", requestCode: " + requestCode, e);
        }
    }

    public static void addGliteSecurityPreferences(final Preferences preferences) {
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION,
                "<no default>");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP,
                "");
        preferences
                .put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE, "");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY, "");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN,
                "<no default>");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL,
                "<no default>");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT,
                "<no default>");
        preferences.put(GliteConstants.PREFERENCE_VOMS_MIN_LIFETIME, Integer
                .toString(GliteSecurityUtils.MINIMUM_PROXY_REMAINING_LIFETIME));
        preferences.put(GliteConstants.PREFERENCE_VOMS_NEW_LIFETIME, Integer
                .toString(GliteSecurityUtils.STANDARD_NEW_PROXY_LIFETIME));
        preferences.put(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY,
                "ondemand");
        preferences.put(GliteConstants.PREFERENCE_SYNCH_LFC_DPM_PERMS, "false");
    }

    /**
     * Ensure the gatContext contains only a gLite compatible security context.
     * It does so by removing the old context and adding a single security
     * context containing the voms proxy.
     * <p>
     * The security context is passed as byte array with the contents of the
     * proxy. The reasons behind this are:
     * <ul>
     * <li>the original credentials do not gave the gLite specific extensions
     * and will fail on some WMS servers (while working on others).
     * <li>If the globus credentials would be created here, the class would be
     * incompatible to the globus credentials class loaded in the context of the
     * globus adaptor's classloader.
     * <li>The globus adaper is fully capable of re-creating the credential
     * information from a byte array.
     * </ul>
     * 
     * @param gatContext
     *            GATContext in which to replace the security information.
     */
    public static void replaceSecurityContextWithGliteContext(
            final GATContext gatContext) {
        CredentialSecurityContext gsc = null;
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final FileInputStream fis = new FileInputStream(GliteSecurityUtils
                    .getProxyPath(gatContext));
            final byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                baos.write(buffer);
            }
            gsc = new CredentialSecurityContext(baos.toByteArray());
        } catch (final FileNotFoundException e2) {
            GliteSecurityUtils.LOGGER
                    .info("The file denoted by gridProxyFile does not exist");
        } catch (final IOException e) {
            GliteSecurityUtils.LOGGER.info("Error reading the proxy file");
        }
        if (gsc != null) {
            gatContext.removeSecurityContexts();
            gatContext.addSecurityContext(gsc);
        }
    }

    private static DocumentBuilder getDocumentBuilder() {
        synchronized (GliteSecurityUtils.class) {
            if (GliteSecurityUtils.documentBuilder == null) {
                try {
                    GliteSecurityUtils.documentBuilder = DocumentBuilderFactory
                            .newInstance().newDocumentBuilder();
                } catch (final ParserConfigurationException e) {
                    GliteSecurityUtils.LOGGER.warn(
                            "Failed to create DOM Parser", e);
                } catch (final FactoryConfigurationError e) {
                    GliteSecurityUtils.LOGGER.warn(
                            "Failed to create DOM Parser", e);
                }
            }
        }
        return GliteSecurityUtils.documentBuilder;
    }

    private static Element getChild(final Element e, final String name) {
        final NodeList list = e.getElementsByTagName(name);
        return (Element) list.item(0);
    }

    private static void loadVoCard(final String voname, final VOInfo voInfo) {

        final String BASE_URL = "http://cic.gridops.org/downloadRP.php?section=lavoisier&rpname=vocard&vo=";

        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod(BASE_URL + voname);

        try {
            final int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                throw new HttpException("Status Code: " + statusCode);
            }

            final InputStream response = method.getResponseBodyAsStream();
            final DocumentBuilder b = GliteSecurityUtils.getDocumentBuilder();
            final Document d = b.parse(response);
            final Element root = d.getDocumentElement();
            final Element vomsServer = GliteSecurityUtils.getChild(
                    GliteSecurityUtils.getChild(root, "VOMSServers"),
                    "VOMSServer");
            final String hostname = GliteSecurityUtils.getChild(vomsServer,
                    "HOSTNAME").getFirstChild().getNodeValue();
            final String port = GliteSecurityUtils.getChild(vomsServer,
                    "VOMS_PORT").getFirstChild().getNodeValue();
            final String dn = GliteSecurityUtils.getChild(vomsServer, "DN")
                    .getFirstChild().getNodeValue();
            LOGGER.info("Loaded VO-info from CIC: " + hostname + " " + dn + " "
                    + port);
            voInfo.setHostNameIfUnset(hostname);
            voInfo.setHostDNIfUnset(dn);
            voInfo.setPortIfUnset(port);
        } catch (final IOException io) {
            GliteSecurityUtils.LOGGER.warn("Failed to load VO Data", io);
        } catch (final SAXException e) {
            GliteSecurityUtils.LOGGER.warn(
                    "Failed to load VO Data, VOCard could not be parsed", e);
        } catch (final NullPointerException e) {
            GliteSecurityUtils.LOGGER.warn(
                    "Failed to load VO Data, VOCard is malformed", e);
        }
    }

}
