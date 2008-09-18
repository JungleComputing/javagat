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

public class GliteResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", false);
        capabilities.put("endMultiJob", false);
        capabilities.put("findResources", false);
        capabilities.put("reserveResource", false);
        capabilities.put("submitJob", true);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences.put("VirtualOrganisation", "true");
        preferences.put("vomsHostDN", "true");
        preferences.put("vomsServerURL", "true");
        preferences.put("vomsServerPort", "true");
        preferences.put("vomsLifetime", "true");
        preferences.put("glite.createNewProxy", "true");
        preferences.put("glite.pollIntervalSecs", "true");
        preferences.put("glite.deleteJDL", "true");
        preferences.put("job.stop.poststage", "false");
        preferences.put("job.stop.on.exit", "false");
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
                LDAPResourceFinder finder = new LDAPResourceFinder(brokerURI);
                String vo = (String) gatContext.getPreferences().get(
                        "VirtualOrganisation");
                List<String> brokerURIs = finder.fetchWMSServers(vo);
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

    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented yet!");
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