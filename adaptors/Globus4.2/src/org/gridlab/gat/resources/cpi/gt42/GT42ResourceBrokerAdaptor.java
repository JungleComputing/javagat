package org.gridlab.gat.resources.cpi.gt42;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

public class GT42ResourceBrokerAdaptor extends ResourceBrokerCpi {

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
