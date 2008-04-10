/*
 * Created on May 16, 2007 by rob
 */
package org.gridlab.gat.resources.cpi.gt4;

import org.apache.log4j.Logger;
import org.gridlab.gat.resources.Job;

/**
 * This thread actively polls the globus state of a job. this is needed in case
 * of firewalls.
 * 
 * @author rob
 * 
 */
class GT4JobPoller extends Thread {

	protected static Logger logger = Logger.getLogger(GT4JobPoller.class);

	private GT4Job j;

	private boolean die = false;

	GT4JobPoller(GT4Job j) {
		setName("GT4 Job Poller");
		this.j = j;
		setDaemon(true);
	}

	public void run() {
		while (true) {
			if (j.getState() == Job.STOPPED)
				return;
			if (j.getState() == Job.SUBMISSION_ERROR)
				return;
			j.getStateActive();
			if (j.getState() == Job.STOPPED)
				return;
			if (j.getState() == Job.SUBMISSION_ERROR)
				return;

			synchronized (this) {
				try {
					wait(5 * 1000);
				} catch (Exception e) {
					// Ignore
				}
				if (die) {
					if (logger.isDebugEnabled()) {
						logger.debug("Job poller killed");
					}
					return;
				}
			}
		}
	}

	synchronized void die() {
		die = true;
		notifyAll();
	}
}
