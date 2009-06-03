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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

//	   protected static Logger logger = LoggerFactory
 //      .getLogger(GT42newResourceBrokerAdaptor.class);
	   
	
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
