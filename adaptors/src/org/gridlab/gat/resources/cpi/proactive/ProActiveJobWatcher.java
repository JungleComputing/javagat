package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;

/**
 * This class provides a thread that keeps track of the status of all
 * ProActiveJobs. We cannot use a separate thread for each job, because
 * there can be many simultaneous jobs. On the other hand, this may be
 * too slow. We'll see.
 */
class ProActiveJobWatcher extends Thread {

    ArrayList jobs = new ArrayList();     // An array of ProActiveJobs.

    ProActiveJobWatcher() {
        setDaemon(true);
        start();
    }

    public void run() {
        while (true) {
            ProActiveJob[] jbs;
            synchronized(this) {
                jbs = (ProActiveJob[]) jobs.toArray(new ProActiveJob[0]);
            }
            int finishedCount = 0;
            for (int i = 0; i < jbs.length; i++) {
                int status = jbs[i].getState();
                if (status != ProActiveJob.RUNNING) {
                    if (status == ProActiveJob.POST_STAGING) {
                        jbs[i].initiatePostStaging();
                    }
                    synchronized(this) {
                        jobs.remove(i - finishedCount);
                        finishedCount++;
                    }
                }
            }
            try {
                Thread.sleep(10*1000);
            } catch(Exception e) {
                // Ignored
            }
        }
    }

    public synchronized void addJob(ProActiveJob job) {
        jobs.add(job);
    }
}
