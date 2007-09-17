/*
 * Created on May 16, 2007 by rob
 */
package org.gridlab.gat.resources.cpi.globus;

import org.apache.log4j.Logger;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.cpi.commandlineSshPrun.CommandlineSshPrunResourceBrokerAdaptor;

/**
 * This thread actively polls the globus state of a job. this is needed in case
 * of firewalls.
 * 
 * @author rob
 * 
 */
class JobPoller extends Thread {

	protected static Logger logger = Logger.getLogger(JobPoller.class);

	private GlobusJob j;

	private boolean die = false;

	JobPoller(GlobusJob j) {
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
