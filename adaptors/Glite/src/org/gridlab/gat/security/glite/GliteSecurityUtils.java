package org.gridlab.gat.security.glite;

import java.io.File;

import org.apache.log4j.Logger;
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

    private final static int STANDARD_PROXY_LIFETIME = 12 * 3600;
    /**
     * If a proxy is going to be reused it should at least have a remaining
     * lifetime of 5 minutes
     */
    private final static int MINIMUM_PROXY_REMAINING_LIFETIME = 5 * 60;

    protected static Logger logger = Logger.getLogger(GliteSecurityUtils.class);

    private GliteSecurityUtils() {
        // Empty on purpose.
    }

    /**
     * @return the file path for the gLite Proxy.
     */
    public static String getProxyPath() {
        CoGProperties properties = CoGProperties.getDefault();
        String proxyFile = System.getenv("X509_USER_PROXY");

        if (proxyFile == null) {
            proxyFile = properties.getProxyFile();
        }

        System.setProperty("gridProxyFile", proxyFile); // for glite security
        // JARs
        System.setProperty(ContextWrapper.CREDENTIALS_PROXY_FILE, proxyFile);
        return proxyFile;
    }

    /**
     * Create a new proxy or reuse the old one if the lifetime is still longer
     * than the lifetime specified in the vomsLifetime preference OR, if the
     * vomsLifetime preference is not specified, the remaining lifetime is
     * longer than the MINIMUM_PROXY_REMAINING_LIFETIME specified in this class
     * 
     * @param context
     *            GATContext with security parameters.
     * @return path to the proxy.
     * @throws GATInvocationException
     */
    public static synchronized String touchVomsProxy(GATContext context)
            throws GATInvocationException {
        String proxyFile = GliteSecurityUtils.getProxyPath();

        Preferences prefs = context.getPreferences();
        String lifetimeStr = (String) prefs
                .get(GliteConstants.PREFERENCE_VOMS_LIFETIME);
        int lifetime = STANDARD_PROXY_LIFETIME;

        boolean createNew = Boolean.parseBoolean((String) prefs
                .get(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY));
        long existingLifetime = -1;

        // determine the lifetime of the existing proxy only if the user wants
        // to reuse the
        // old proxy
        if (!createNew) {
            existingLifetime = VomsProxyManager
                    .getExistingProxyLifetime(proxyFile);
        }

        if (lifetimeStr == null) { // if a valid proxy exists, create a new one
            // only if the old one is below the minimum
            // lifetime
            if (existingLifetime < MINIMUM_PROXY_REMAINING_LIFETIME) {
                createVomsProxy(lifetime, context, proxyFile);
            } else {
                logger.info("Reusing old voms proxy with lifetime (seconds): "
                        + existingLifetime);
            }
        } else { // if a valid proxy exists, create a new one only if the old
            // one is below the specified lifetime
            lifetime = Integer.parseInt(lifetimeStr);

            if (existingLifetime < lifetime) {
                createVomsProxy(lifetime, context, proxyFile);
            } else {
                logger.info("Reusing old voms proxy with lifetime (seconds): "
                        + existingLifetime);
            }
        }
        return proxyFile;
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
     * <td>The role inside the virtual organisation or the group of the virtual
     * organisation for which the voms proxy is created (e.g. VOAdmin)</td>
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

        String voName = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
        String voGroup = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP);
        String voRole = (String) prefs
                .get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE);

        String requestCode = voName + (voGroup == null ? "" : "/" + voGroup)
                + (voRole == null ? "" : "/Role=" + voRole);
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
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN,
                "<no default>");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL,
                "<no default>");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT,
                "<no default>");
        preferences.put(GliteConstants.PREFERENCE_VOMS_LIFETIME, Integer
                .toString(STANDARD_PROXY_LIFETIME));
        preferences.put(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY,
                "false");
    }
}
