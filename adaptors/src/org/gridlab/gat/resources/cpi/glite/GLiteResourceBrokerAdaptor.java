/*
 * Created on July 25, 2007
 */
package org.gridlab.gat.resources.cpi.glite;

import java.util.List;

import org.glite.jdl.JobAd;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author anna
 */

public class GLiteResourceBrokerAdaptor extends ResourceBrokerCpi {

	public GLiteResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        super(gatContext, preferences);
	}
	
	private WMProxyAPI connectToWMProxy() {
		WMProxyAPI client = null;
		try {
			// TODO: where to find host, port and proxy file?
			String host = "mu12.matrix.sara.nl";
		    String port = "7443";
		    String proxyFile = "/tmp/x509up_u1380";
		    
			client = new WMProxyAPI("https://" + host + ":" + port + "/glite_wms_wmproxy_server" , proxyFile);
			// delegation of user credentials
			// TODO: change id
			String delegationId = "anna";
			String proxy = client.grstGetProxyReq(delegationId);
			client.grstPutProxy(delegationId, proxy);
		} catch (Exception e) {
			throw GATInvocationException("Unable to connect to WMProxy service", e);
		}
		return client;
	}
	
	private JobAd createJobDescription(JobDescription description) {
		JobAd jobAd;
		
		return jobAd;
	}
	
	private JobAd createJobDescription(ResourceDescription resourceDescription) {
		JobAd jobAd;
		
		return jobAd;
	}
	
	/*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
     */
	public List findResources(ResourceDescription resourceDescription)
			throws GATInvocationException {
		//throw new UnsupportedOperationException("Not implemented");
		WMProxyAPI client = connectToWMProxy();
		List resources = null;
		JobAd jobAd = createJobDescription(resourceDescription);
		String jdlString = jobAd.toString();
		//CE Ids satisfying the job Requirements specified in the JDL, ordered according to the decreasing Rank
		StringAndLongList matchingResources = proxyAPI.jobListMatch(jdlString, delegationId);
		if (matchingResources != null) {
			resources = new List();
			StringAndLongType[] matchingResourcesList = matchingResources.getFile();
			for (int i = 0; i < matchingResourcesList.length; i++) {
				resources.add(matchingResourcesList[i].getName());
			}
		}
		//CE Ids satisfying the job Requirements specified in the JDL
		return resources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description)
			throws GATInvocationException {
		throw new UnsupportedOperationException("Not implemented");
		WMProxyAPI client = connectToWMProxy();
	}
	
}
