package org.gridlab.gat.scheduler;

import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;


/**
 * An instance of this interface allows on to examine the various properties of
 * the scheduler resource to which this instance corresponds.
 */
public interface Scheduler extends Monitorable, Advertisable {
	
	/**
	 * Return a {@link List} of {@link QueueDescription} associated to this scheduler.
	 * 
	 *  @return a {@link List} of {@link QueueDescription}
	 */
	public List<Queue> getQueues() throws GATInvocationException;
	
	/**
	 * returns the type of the scheduler.
	 * 
	 * @return the type of the scheduler.
	 */
	public String getSchedulerType() throws GATInvocationException;
}
