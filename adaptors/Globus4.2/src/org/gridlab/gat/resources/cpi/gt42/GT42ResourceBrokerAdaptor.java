package org.gridlab.gat.resources.cpi.gt42;

import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;


public class GT42ResourceBrokerAdaptor extends ResourceBrokerCpi {

	   public static Map<String, Boolean> getSupportedCapabilities() {
	        Map<String, Boolean> capabilities = ResourceBrokerCpi
	                .getSupportedCapabilities();
	        capabilities.put("beginMultiJob", true);
	        capabilities.put("endMultiJob", true);
	        capabilities.put("submitJob", true);

	        return capabilities;
	    }
	
	   public static Preferences getSupportedPreferences() {
	        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
	        preferences.put("GT42.sandbox.gram", "false");
	        preferences.put("GT42.factory.type", "<FORK CONSTANT>");
	        return preferences;
	    }

	   protected static Logger logger = LoggerFactory
       .getLogger(GT42newResourceBrokerAdaptor.class);
	   
	   protected GSSCredential getCred() throws GATInvocationException {
	        GSSCredential cred = null;
	        URI location = null;
	        try {
	            location = new URI(getHostname());
	        } catch (Exception e) {
	            throw new GATInvocationException(
	                    "GT4.2 Job: getSecurityContext, initialization of location failed, "
	                            + e);
	        }
	        try {
	            cred = GlobusSecurityUtils.getGlobusCredential(gatContext,
	                    "ws-gram", location, ResourceManagerContact.DEFAULT_PORT);
	        } catch (Exception e) {
	            throw new GATInvocationException(
	                    "WSGT4Job: could not initialize credentials, " + e);
	        }
	        return cred;
	    }
	   
	
	protected GT42ResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
			throws GATObjectCreationException {
		super(gatContext, brokerURI);
		// TODO Auto-generated constructor stub
	}
	
	public Job submitJob(AbstractJobDescription description,
            MetricListener listener, String metricName)
            throws GATInvocationException {
        
		throw new UnsupportedOperationException("Not implemented");
    }

}
