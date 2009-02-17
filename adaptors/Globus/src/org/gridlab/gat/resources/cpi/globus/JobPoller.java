/*
 * Created on May 16, 2007 by rob
 */
package org.gridlab.gat.resources.cpi.globus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.resources.Job;

/**
 * This thread actively polls the globus state of a job. this is needed in case
 * of firewalls.
 * 
 * @author rob
 * 
 */
class JobPoller extends Thread {

    protected static Logger logger = LoggerFactory.getLogger(JobPoller.class);

    private GlobusJob j;

    private boolean die = false;

    JobPoller(GlobusJob j) {
        super("GlobusJob poller thread");
        this.j = j;
        setDaemon(true);
    }

    public void run() {
        while (true) {
            if (j.getState() == Job.JobState.STOPPED)
                return;
            if (j.getState() == Job.JobState.SUBMISSION_ERROR)
                return;
            j.getStateActive();
            if (j.getState() == Job.JobState.STOPPED)
                return;
            if (j.getState() == Job.JobState.SUBMISSION_ERROR)
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
