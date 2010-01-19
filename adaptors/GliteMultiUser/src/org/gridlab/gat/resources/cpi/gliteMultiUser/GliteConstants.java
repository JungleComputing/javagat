package org.gridlab.gat.resources.cpi.gliteMultiUser;

import org.gridlab.gat.GATContext;

/**
 * Constants for values used by the gLite adapter.
 * 
 * @author Max Berger
 */
public final class GliteConstants {

    /**
     * In gLite: the id of the work que (CE).
     */
    public static final String RESOURCE_MACHINE_NODE = "machine.node";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION = "VirtualOrganisation";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION_GROUP = "VirtualOrganisationGroup";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION_ROLE = "VirtualOrganisationRole";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION_CAPABILITY = "VirtualOrganisationCapability";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN = "vomsHostDN";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL = "vomsServerURL";
    public static final String PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT = "vomsServerPort";
    public static final String PREFERENCE_LFC_SERVER = "LfcServer";
    public static final String PREFERENCE_LFC_SERVER_PORT = "LfcServerPort";
    public static final String PREFERENCE_BDII_URI = "bdiiURI";
    public static final String PREFERENCE_PREFERRED_SE_ID = "preferredSEID";
    public static final String PREFERENCE_POLL_INTERVAL_SECS = "glite.pollIntervalSecs";
    public static final String PREFERENCE_VOMS_MIN_LIFETIME = "glite.minproxytime";
    public static final String PREFERENCE_VOMS_NEW_LIFETIME = "glite.newproxytime";
    public static final String PREFERENCE_VOMS_CREATE_NEW_PROXY = "glite.proxycreation";
    public static final String PREFERENCE_PROXY_PATH = "glite.vomsproxypath";
    public static final String PREFERENCE_VOMS_PROXY_DIRECTORY = "glite.vomsProxyDirectory";
    public static final String PREFERENCE_PROXY_USER_ID = "glite.vomsProxyUserId";
    public static final String PREFERENCE_DELETE_JDL = "glite.deleteJDL";
    public static final String PREFERENCE_JOB_STOP_POSTSTAGE = "job.stop.poststage";
    public static final String PREFERENCE_JOB_STOP_ON_EXIT = "job.stop.on.exit";
    public static final String PREFERENCE_SYNCH_LFC_DPM_PERMS = "glite.synch.lfc.dpm.permissions";
    
    private GliteConstants() {
        // do not instantiate.
    }

    public static String getVO(GATContext context) {
        String vo = (String) context.getPreferences().get(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
        if (vo == null) {
            vo = System.getenv("LCG_GFAL_VO");
        }
        return vo;
    }
}
