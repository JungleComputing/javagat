package org.gridlab.gat.resources.cpi;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
import org.gridlab.gat.scheduler.Queue;
import org.gridlab.gat.scheduler.Scheduler;

/**
 * This abstract class defines an interface to obtain information about a
 * scheduler and the queues to a resource.
 * 
 * @author Stefan Bozic
 */
public abstract class SchedulerCpi extends MonitorableCpi implements Scheduler {

	private List<Queue> queues;

	/**
	 * The {@link GATContext} for security issues etc.
	 */
	protected GATContext gatContext;

	/**
	 * The {@link URI} of the information system.
	 */
	protected URI informationSystemUri;

	/**
	 * Constructor.
	 * 
	 * @param uri the location of the scheduler
	 * @param gatContext the gatContext.
	 */
	protected SchedulerCpi(GATContext gatContext, URI uri) {
		this.gatContext = gatContext;
		this.informationSystemUri = uri;

		queues = new ArrayList<Queue>();
	}

	/**
	 * Returns the {@link URI} to an information system where to obtain the scheduler data.
	 * 
	 * @return the {@link URI} to an information system
	 */
	public URI getInformationSystemUri() {
		return informationSystemUri;
	}

	/**
	 * Sets the {@link URI} to the information system where to obtain the scheduler data.
	 * 
	 * @param isUri the {@link URI} to set
	 */
	public void setInformationSystemUri(URI isUri) {
		this.informationSystemUri = isUri;
	}

	/**
	 * @see Scheduler#getQueues()
	 */
	@Override
	public List<Queue> getQueues() throws GATInvocationException{
		throw new UnsupportedOperationException("getQueues() is not supported by this adaptor");
	}

	/**
	 * @see Scheduler#getSchedulerType()
	 */
	@Override
	public String getSchedulerType() throws GATInvocationException{
		throw new UnsupportedOperationException("getSchedulerType() is not supported by this adaptor");
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		throw new UnsupportedOperationException("marshalling of this object is not supported by this adaptor");
	}

}
