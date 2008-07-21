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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;


public class GliteResourceBrokerAdaptor extends ResourceBrokerCpi {
	private GATContext context = null;
	private URI brokerURI = null;

	public GliteResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException {
		super(gatContext, brokerURI);
		context = gatContext;
		this.brokerURI = brokerURI;
	}

	public List findResources(ResourceDescription resourceDescription) {
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
		return new GliteJob(context, jobDescription, null, brokerURI.toString());
	}
}