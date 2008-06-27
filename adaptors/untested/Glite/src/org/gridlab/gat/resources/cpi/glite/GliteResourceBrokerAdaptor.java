////////////////////////////////////////////////////////////////////
//
// GliteResourceBrokerAdaptor.java
// 
// Contributor(s):
// Dec/2007 - Andreas Havenstein 
//     for Max Planck Institute for Gravitational Physics
//     (Albert Einstein Institute) 
//     Astrophysical Relativity / eScience
// 
//
////////////////////////////////////////////////////////////////////

package org.gridlab.gat.resources.cpi.glite;

import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;


public class GliteResourceBrokerAdaptor extends ResourceBrokerCpi {
	GATContext context = null;
	URI brokerURI = null;

	public GliteResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException {
		super(gatContext, brokerURI);
		context = gatContext;
		this.brokerURI = brokerURI;
	}

	public List findResources(ResourceDescription resourceDescription) {
		String voName = (String) context.getPreferences().get("VirtualOrganisation");
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public Reservation reserveResource(ResourceDescription resourceDescription,
			TimePeriod timePeriod) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public Job submitJob(JobDescription jobDescription, MetricListener listener, String metricDefinitionName )
			throws GATInvocationException {
		GliteJob job = new GliteJob(context, jobDescription, null, brokerURI.toString());
		return job;
	}
}