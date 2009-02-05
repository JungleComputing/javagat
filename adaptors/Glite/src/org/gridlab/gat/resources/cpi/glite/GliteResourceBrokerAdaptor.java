////////////////////////////////////////////////////////////////////
//
// GliteResourceBrokerAdaptor.java
// 
// Contributor(s):
// Dec/2007 - Andreas Havenstein 
//     for Max Planck Institute for Gravitational Physics
//     (Albert Einstein Institute) 
//     Astrophysical Relativity / eScience
// Jun,Jul/2008 - Thomas Zangerl 
//      for Distributed and Parallel Systems Research Group
//      University of Innsbruck
//      major enhancements
//
////////////////////////////////////////////////////////////////////

package org.gridlab.gat.resources.cpi.glite;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * Adapter for the Glite Job Submission (WMS) for JavaGAT.
 * <p>
 * Please read the documentation in <tt>/doc/GliteAdaptor</tt> for further
 * information!
 * 
 */
public class GliteResourceBrokerAdaptor extends ResourceBrokerCpi {

    private static final String GLITE_RESOURCE_BROKER_ADAPTOR = "GliteResourceBrokerAdaptor";

    private LDAPResourceFinder ldapResourceFinder;

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", false);
        capabilities.put("endMultiJob", false);
        capabilities.put("findResources", true);
        capabilities.put("reserveResource", false);
        capabilities.put("submitJob", true);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION, "true");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_GROUP,
                "true");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_ROLE,
                "true");
        preferences.put(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN,
                "true");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL,
                "true");
        preferences.put(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL,
                "true");
        preferences.put(GliteConstants.PREFERENCE_VOMS_LIFETIME, "true");
        preferences
                .put(GliteConstants.PREFERENCE_VOMS_CREATE_NEW_PROXY, "true");
        preferences.put(GliteConstants.PREFERENCE_POLL_INTERVAL_SECS, "true");
        preferences.put(GliteConstants.PREFERENCE_DELETE_JDL, "true");
        preferences.put(GliteConstants.PREFERENCE_JOB_STOP_POSTSTAGE, "false");
        preferences.put(GliteConstants.PREFERENCE_JOB_STOP_ON_EXIT, "false");
        return preferences;
    }

    /**
     * Construct a GliteResourceBrokerAdaptor for the given broker URI Instead
     * of a WMS address also an ldap address may be given. In this case the
     * resource broker adaptor will pick some random suitable WMS for the
     * virtual organisation set in the preference VirtualOrganisation
     * 
     * @param gatContext
     * @param brokerURI
     * @throws GATObjectCreationException
     */
    public GliteResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);

        // if not a broker URI itself but an LDAP address is given, retrieve a
        // broker URI
        if (brokerURI.getScheme().equals("ldap")
                || brokerURI.getScheme().equals("ldaps")) {
            try {
                ldapResourceFinder = new LDAPResourceFinder(gatContext,
                        brokerURI);
                String vo = (String) gatContext.getPreferences().get(
                        GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
                List<String> brokerURIs = ldapResourceFinder
                        .fetchWMSServers(vo);
                int randomPos = (int) (Math.random() * brokerURIs.size());
                String brokerURIStr = brokerURIs.get(randomPos);
                this.brokerURI = new URI(brokerURIStr);
            } catch (NamingException e) {
                throw new GATObjectCreationException(
                        "Could not find suitable WMS!", e);
            } catch (URISyntaxException e) {
                throw new GATObjectCreationException(
                        "Could not find suitable WMS!", e);
            }
        }

        if (!(this.brokerURI.isCompatible("http") || this.brokerURI
                .isCompatible("https"))) {
            throw new GATObjectCreationException("cannot handle scheme: "
                    + brokerURI.getScheme());
        }
    }

    private void ensureLdapFinderExists() throws NamingException {
        if (ldapResourceFinder == null) {
            ldapResourceFinder = new LDAPResourceFinder(gatContext);
        }
    }

    /** {@inheritDoc} */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription)
            throws GATInvocationException {
        if (resourceDescription != null) {
            if (!resourceDescription.getDescription().isEmpty())
                throw new GATInvocationException(
                        "gLite findResources does not support any arguments");
        }
        try {
            ensureLdapFinderExists();
            String vo = (String) gatContext.getPreferences().get(
                    GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
            List<String> queNames = ldapResourceFinder.fetchCEs(vo);
            List<HardwareResource> retList = new ArrayList<HardwareResource>(
                    queNames.size());
            for (String queName : queNames) {
                retList
                        .add(new GliteHardwareResource(this.gatContext, queName));
            }
            return retList;
        } catch (NamingException e) {
            throw new GATInvocationException(GLITE_RESOURCE_BROKER_ADAPTOR, e);
        }
    }

    public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Job submitJob(AbstractJobDescription jobDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {

        try {

            GliteJob job = new GliteJob(gatContext,
                    (JobDescription) jobDescription, null, brokerURI.toString());

            if (listener != null && metricDefinitionName != null) {
                Metric metric = job.getMetricDefinitionByName(
                        metricDefinitionName).createMetric(null);
                job.addMetricListener(listener, metric);
            }

            return job;

        } catch (GATObjectCreationException e) {
            throw new GATInvocationException(e.getMessage());
        }

    }
}