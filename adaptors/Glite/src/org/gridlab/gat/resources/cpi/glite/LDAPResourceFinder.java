////////////////////////////////////////////////////////////////////
//
// LDAPResourceFinger.java
//
// Contributor(s):
// Jan/2012 - Stefan Verhoeven
//     for Nederlands eScience Center
//     grid.sara.nl srm.
// Jun,Jul/2008 - Thomas Zangerl
//      for Distributed and Parallel Systems Research Group
//      University of Innsbruck
// Jan/2009 - Max Berger
//      for Distributed and Parallel Systems Research Group
//      University of Innsbruck
//      SE/LFC implementations.
//
////////////////////////////////////////////////////////////////////

package org.gridlab.gat.resources.cpi.glite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;

public class LDAPResourceFinder {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LDAPResourceFinder.class);

    public static final String DEFAULT_LDAP_SERVER_NAME = "ldap://bdii.ce-egee.org";
    public static final int DEFAULT_LDAP_SERVER_PORT = 2170;

    private static final String START_DN = "Mds-Vo-name=local,o=Grid";

    private final DirContext ctx;

    private final SearchControls globalSearchControls;

    private final String preferredSEID;

    /**
     * Create a new LDAPResourceFinder with the given BDII.
     * 
     * @param gatContext
     *            GatContext with additional information.
     * @param ldapResource
     *            URI to a BDII, if null a default value will be used.
     * @throws NamingException
     *             if an LDAP error occurs.
     */
    public LDAPResourceFinder(GATContext gatContext, URI ldapResource)
            throws NamingException {

        this.preferredSEID = (String) gatContext.getPreferences().get(
                GliteConstants.PREFERENCE_PREFERRED_SE_ID);

        String ldapContact;
        if (ldapResource == null) {
            ldapContact = (String) gatContext.getPreferences().get(
                    GliteConstants.PREFERENCE_BDII_URI);
            if (ldapContact == null) {
                ldapContact = DEFAULT_LDAP_SERVER_NAME + ":"
                        + DEFAULT_LDAP_SERVER_PORT;
            }
        } else {
            final String host = ldapResource.getHost();
            final int port = ldapResource.getPort(DEFAULT_LDAP_SERVER_PORT);
            if (host == null) {
                ldapContact = DEFAULT_LDAP_SERVER_NAME + ":" + port;
            } else {
                ldapContact = "ldap://" + host + ":" + port;
            }
        }

        globalSearchControls = new SearchControls();
        // search everything within the given context
        globalSearchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // return all found elements
        globalSearchControls.setCountLimit(0);
        // take as much time as you need
        globalSearchControls.setTimeLimit(0);
        // return all attributes
        globalSearchControls.setReturningAttributes(null);

        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapContact);

        // construct the ldap connection from the environment params
        ctx = new InitialLdapContext(env, null);
    }

    /**
     * Create a new LDAPResourceFinder.
     * 
     * @param context
     *            GatContext with additional information.
     * @throws NamingException
     *             if an LDAP error occurs.
     */
    public LDAPResourceFinder(GATContext context) throws NamingException {
        this(context, null);
    }

    private String getSafeStringAttr(SearchResult result, String name)
            throws NamingException {
        Attribute attr = result.getAttributes().get(name);
        if (attr != null) {
            return (String) attr.get();
        } else
            return null;
    }

    /**
     * Retrieve a list of WMSs for the given VO.
     * 
     * @param voName
     *            name of the VO. Must not be null.
     * @return a List&lt;String&gt; of WMS URIs
     * @throws NamingException
     *             if an LDAP error occurs.
     */
    public List<String> fetchWMSServers(final String voName)
            throws NamingException {
        ArrayList<String> wmsServers = new ArrayList<String>();

        String filter = "(&(GlueServiceType=org.glite.wms.WMProxy)(GlueServiceOwner="
                + voName + "))";
        NamingEnumeration<SearchResult> results = ctx.search(START_DN, filter,
                globalSearchControls);

        while (results.hasMore()) {
            SearchResult result = results.nextElement();
            String wmsServer = getSafeStringAttr(result, "GlueServiceEndpoint");
            if (wmsServer == null) {
                wmsServer = getSafeStringAttr(result,
                        "GlueServiceAccessPointURL");
            }
            if (wmsServer != null) {
                wmsServers.add(wmsServer);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retrieved the following WMS server from LDAP: "
                            + wmsServer);
                }
            }
        }

        return wmsServers;
    }

    /**
     * Retrieve a list of CEs for the given VO.
     * 
     * @param voName
     *            name of the VO. Must not be null.
     * @return a List&lt;String&gt; of CE Ids
     * @throws NamingException
     *             if an LDAP error occurs.
     */
    public List<String> fetchCEs(final String voName) throws NamingException {
        ArrayList<String> results = new ArrayList<String>();

        String filter = "(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:"
                + voName + "))";
        NamingEnumeration<SearchResult> clusters = ctx.search(START_DN, filter,
                globalSearchControls);

        while (clusters.hasMore()) {
            SearchResult result = clusters.nextElement();
            String ceURL = getSafeStringAttr(result, "GlueCEInfoContactString");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Retrieved the following CE from LDAP: " + ceURL);
            }
            if (ceURL != null)
                results.add(ceURL);
        }

        return results;
    }

    /**
     * Retrieve a list of LFCs for the given VO.
     * 
     * @param voName
     *            name of the VO. Must not be null.
     * @return a List&lt;String&gt; of LFC endpoints. Under normal circumstances
     *         this list should contain exactly one entry.
     * @throws NamingException
     *             if an LDAP error occurs.
     */
    public List<String> fetchLFCs(final String voName) throws NamingException {
        ArrayList<String> results = new ArrayList<String>();

        String filter = "(&(objectClass~=GlueService)(GlueServiceType=lcg-file-catalog)(GlueServiceOwner="
                + voName + "))";
        NamingEnumeration<SearchResult> searchResults = ctx.search(START_DN,
                filter, globalSearchControls);
        while (searchResults.hasMore()) {
            SearchResult result = searchResults.nextElement();
            String endpoint = getSafeStringAttr(result, "GlueServiceEndpoint");
            results.add(endpoint);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Retrieved the following LFC from LDAP: " + endpoint);
            }
        }
        return results;
    }

    /**
     * Contains information about a storage element (SE).
     */
    public class SEInfo {
        final String seUniqueId;
        final String path;
        final String space;

        private SEInfo(String id, String pat, String spac) {
            this.seUniqueId = id;
            this.path = pat;
            this.space = spac;
        }

        /**
         * @return the uniqieID of the SE.
         */
        public String getSeUniqueId() {
            return seUniqueId;
        }

        /**
         * @return the storage path on the SE.
         */
        public String getPath() {
            return path;
        }

        /**
         * @return the amount of free space on the SE.
         */
        public String getSpace() {
            return space;
        }

        @Override
        public String toString() {
            return "{" + seUniqueId + "," + path + "," + space + "}";
        }
    }

    /**
     * Retrieve a list of SEs for the given VO that have a minimal free space.
     * The list is ordered in terms of preference:
     * <ol>
     * <li>The SE given by the {@link GliteConstants#PREFERENCE_PREFERRED_SE_ID}
     * Gat context property.</li>
     * <li>The SE given by the VO_VONAME_DEFAULT_SE environment variable</li>
     * <li>All other SEs for the given VO.</li>
     * </ol>
     * 
     * @param voName
     *            name of the VO. Must not be null.
     * @param fileSize
     *            The minimal size that must be available in the SEs
     * @return a List&lt;{@link SEInfo}&gt; of SEs.
     * @throws NamingException
     *             if an LDAP error occurs.
     */
    public List<SEInfo> fetchSEs(final String voName, final long fileSize)
            throws NamingException {
        List<SEInfo> unsortedList = fetchAllSEs(voName, fileSize);
        List<SEInfo> orderedList = new ArrayList<SEInfo>(unsortedList.size());
        findAndMoveSE(preferredSEID, unsortedList, orderedList);
        findAndMoveSE(System.getenv("VO_" + voName.toUpperCase(Locale.ENGLISH)
                + "_DEFAULT_SE"), unsortedList, orderedList);
        Collections.shuffle(unsortedList);
        orderedList.addAll(unsortedList);
        return orderedList;
    }

    private void findAndMoveSE(String seid, List<SEInfo> sourceList,
            List<SEInfo> targetList) {
        if (seid == null) {
            return;
        }
        Iterator<SEInfo> it = sourceList.iterator();
        while (it.hasNext()) {
            SEInfo info = it.next();
            if (seid.equalsIgnoreCase(info.getSeUniqueId())) {
                targetList.add(info);
                it.remove();
                return;
            }
        }
    }

    private List<SEInfo> fetchAllSEs(final String voName, final long fileSize)
            throws NamingException {

        ArrayList<SEInfo> results = new ArrayList<SEInfo>();

        final String filter = "(&(objectClass~=GlueSA)(|(GlueSALocalID=" + voName
                + ")(GlueSAAccessControlBaseRule=VO*:"+ voName
                + ")(GlueSAAccessControlBaseRule="+ voName
                + ")))";
        final NamingEnumeration<SearchResult> searchResults = ctx.search(
                START_DN, filter, globalSearchControls);

        while (searchResults.hasMore()) {
            final SearchResult result = searchResults.nextElement();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("searchresult: " + result.toString());
            }
            String pathName = result.getName();
            String seUniqueId = null;
            int pos = pathName.indexOf("GlueSEUniqueID");
            if (pos < 0) {
                pathName = getSafeStringAttr(result, "GlueChunkKey");
                if (pathName != null)
                    pos = pathName.indexOf("GlueSEUniqueID");
            }
            if (pathName != null && pos >= 0) {
                String st = pathName.substring(pos + 15);
                int posEnd = st.indexOf(',');
                if (posEnd < 0)
                    posEnd = st.length();
                seUniqueId = st.substring(0, posEnd);
            }

            String path = getSafeStringAttr(result, "GlueSAPath");

            if (path == null) {
            	path = fetchVOInfoPath(voName, seUniqueId);
            }

            String space = getSafeStringAttr(result,
                    "GlueSAStateAvailableSpace");

            if (seUniqueId != null) {
                boolean valid;
                valid = space == null || Long.parseLong(space) > fileSize;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found SE: " + seUniqueId + " with path " + path
                            + " and space " + (space == null ? "not specified" : space)
                            + " Valid: " + valid);
                }
                if (valid) {
                    results.add(new SEInfo(seUniqueId, path, space));
                }
            }
        }
        return results;
    }

    private String fetchVOInfoPath(final String voName, final String seUniqueId)
    		throws NamingException {
    	final String filter = "(&(objectClass~=GlueVOInfo)(GlueVOInfoAccessControlBaseRule=VO*:" + voName
    			+ ")(GlueChunkKey=*"+ seUniqueId
    			+ "))";
    	final NamingEnumeration<SearchResult> searchResults = ctx.search(
    			START_DN, filter, globalSearchControls);

    	if (searchResults.hasMore()) {
    		final SearchResult result = searchResults.nextElement();
    		return getSafeStringAttr(result, "GlueVOInfoPath");
    	} else {
    		return null;
    	}
    }

    // only used for testing
    // public static void main(final String [] args) throws Exception {
    // SoftwareResourceDescription srd = new SoftwareResourceDescription();
    // HardwareResourceDescription hrd = new HardwareResourceDescription();
    //
    // List<String> gliteOSNames = new ArrayList<String>();
    // gliteOSNames.add("ScientificSL");
    // gliteOSNames.add("ScientificCERNSLC");
    // gliteOSNames.add("Scientific Linux CERN");
    // srd.addResourceAttribute("glite.OS", gliteOSNames);
    //
    // List<String> gliteProcessorNames = new ArrayList<String>();
    // gliteProcessorNames.add("PIV");
    // gliteProcessorNames.add("P4");
    // gliteProcessorNames.add("PIII");
    // srd.addResourceAttribute("glite.Processor", gliteProcessorNames);
    //
    // srd.addResourceAttribute("memory.size", new Float(1.0));
    //
    // LDAPResourceFinder finder = new LDAPResourceFinder();
    // //finder.findResources("voce");
    // finder.fetchCEs("compchem");
    // }

}
