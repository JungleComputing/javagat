package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;

/**
 * This class provides bounded parallellism. Use this to submit a number
 * of jobs by calling submit, and then wait until they are done by
 * calling waitForAll.
 * Can only be used once.
 */
public class Threader extends Thread {

    /** The job queue. */
    private ArrayList jobs = new ArrayList();

    /** Maximum amount of parallellism. */
    private int maxThreads;

    /** Number of running jobs. */
    private int running;

    /** Set to true when we are done. */
    private boolean done = false;

    private static class Encaps implements Runnable {
        Runnable r;
        Threader t;
        public Encaps(Runnable r, Threader t) {
            this.r = r;
            this.t = t;
        }
        public void run() {
            try {
                r.run();
            } finally {
                t.jobDone();
            }
        }
    }

    private Threader(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public static Threader createThreader(int maxThreads) {
        Threader threader = new Threader(maxThreads);
        threader.setName("Threader");
        threader.start();
        return threader;
    }
    
    public synchronized void submit(Runnable r) {
        jobs.add(r);
        if (running < maxThreads) {
            notifyAll();
        }
    }

    public synchronized void jobDone() {
        running--;
        if (jobs.size() > 0 || running == 0) {
            notifyAll();
        }
    }

    public synchronized void waitForAll() {
        if (done) {
            return;
        }
        while (jobs.size() > 0 || running > 0) {
            try {
                wait();
            } catch(Exception e) {
                // ignored
            }
        }
        done = true;
        notifyAll();
    }

    public synchronized void run() {
        for (;;) {
            while (running >= maxThreads) {
                try {
                    wait();
                } catch(Exception e) {
                    // ignored
                }
            }
            while (jobs.size() == 0 && ! done) {
                try {
                    wait();
                } catch(Exception e) {
                    // ignored
                }
            }
            if (done) {
                return;
            }
            Runnable job = (Runnable) jobs.remove(0);
            running++;
            Encaps e = new Encaps(job, this);
            Thread t = new Thread(e);
            t.setName("Encaps");
            t.start();
        }
    }
}
