/*
 * Created on May 20, 2007
 */
package org.gridlab.gat.resources.cpi.glite;

import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author anna
 */
public class gLiteResourceBrokerAdaptor extends ResourceBrokerCpi {

	
	/*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
     */
	public List findResources(ResourceDescription resourceDescription)
			throws GATInvocationException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description)
			throws GATInvocationException {
		throw new UnsupportedOperationException("Not implemented");
	}

}
