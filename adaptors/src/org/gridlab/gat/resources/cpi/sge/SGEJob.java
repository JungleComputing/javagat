/*
 * SGEJob.java
 *
 * Created on May 16, 2006, 11:24 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.Session;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 *
 * @author ole.weidner
 */
@SuppressWarnings("serial")
public class SGEJob extends JobCpi {
    
    private String jobID;
    private MetricDefinition statusMetricDefinition;
//    private Metric statusMetric;
    private JobDescription jobDescription;
    private Session session;
    private Hashtable<String, Long> time;
    
    /**
     * The jobStartListener runs in a thread and checks the job's state.
     * When it detects a state transition from SCHEDULED to RUN, it writes
     * the time and exits
     */
    private class jobStartListener implements Runnable {
        
        int state = 0x00;
        final int SLEEP = 250;
        
        Session session = null;
        String jobID    = null;
        Hashtable<String, Long> time  = null;

        public jobStartListener(Session session, String jobID, Hashtable<String, Long> time) {
            this.session = session;
            this.jobID = jobID;
            this.time = time;
        }
        
        public void run() {
            while (state != Session.RUNNING) {
                try {
                    state = session.getJobProgramStatus(jobID);
                    if( state == Session.FAILED) {
                        //TODO
                    }
                    else if( state == Session.DONE ) {
                        //TODO
                    }
                    Thread.sleep(SLEEP);
                }
                catch (DrmaaException e) {
                    //TODO
                }
                catch (InterruptedException e) {
                    //TODO
                }
            }
            // Now we're in RUNNING state - set the time and start the
            // jobStopListener
            time.put( "start_time", new Long(System.currentTimeMillis()) );
            
            jobStopListener jsl = new jobStopListener(this.session, this.jobID, time);
            new Thread(jsl).start();            
        }
    }
    
    /**
     * The jobStopListener runs in a thread and checks the job's state.
     * When it detects a state transition from RUN to STOP, it writes
     * the time and exits 
     */
    private class jobStopListener implements Runnable {
        
        int state = 0x00;
        final int SLEEP = 250;
        
        Session session = null;
        String jobID    = null;
        Hashtable<String, Long> time  = null;

        public jobStopListener(Session session, String jobID, Hashtable<String, Long> time) {
            this.session = session;
            this.jobID = jobID;
            this.time = time;
        }
        
        public void run() {
            while (state != Session.DONE) {
                try {
                    state = session.getJobProgramStatus(jobID);
                    if( state == Session.FAILED) {
                        //TODO
                    }
                    Thread.sleep(SLEEP);
                }
                catch (DrmaaException e) {
                    //TODO
                }
                catch (InterruptedException e) {
                    //TODO
                }
            }            
            // Now we're in STOPPED state - set the time and exit
            time.put( "stop_time", new Long(System.currentTimeMillis()) );
        }
    }
    
    public SGEJob(GATContext gatContext, Preferences preferences, SGEResourceBrokerAdaptor broker, JobDescription jobDescription,
            Session session, String id, Sandbox sandbox, MetricListener listener, Metric metric) {
        super(gatContext, preferences, jobDescription, sandbox, listener, metric);
        
        jobID = id;
        this.jobDescription = jobDescription;
        this.session = session;
        state = INITIAL;
        
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
//        statusMetric = statusMetricDefinition.createMetric(null);
        
        time = new Hashtable<String, Long>();
        
        jobStartListener jsl = new jobStartListener(this.session, this.jobID, time);
        new Thread(jsl).start();
    }
    
    public String getJobID() {
        return jobID;
    }
    
    public synchronized int getState() {
        setState();
        return state;
    }
    
    public String marshal() {
        throw new Error("Not implemented");
    }
    
    protected void setState() {
        try {
            int status = session.getJobProgramStatus(jobID);
            
            switch(status) {
                
                case Session.RUNNING:
                    state = RUNNING;
                    break;
                    
                case Session.FAILED:
                    state = SUBMISSION_ERROR;
                    break;
                    
                case Session.DONE:
                    state = STOPPED;
                    break;
                    
                    /* Job is active but suspended */
                    
                case Session.SYSTEM_SUSPENDED:
                    state = STOPPED;
                    break;
                case Session.USER_SUSPENDED:
                    state = STOPPED;
                    break;
                case Session.USER_SYSTEM_SUSPENDED:
                    state = STOPPED;
                    break;
                    
                    /* Job is in the queue states */
                    
                case Session.QUEUED_ACTIVE:
                    state = SCHEDULED;
                    break;
                case Session.SYSTEM_ON_HOLD:
                    state = ON_HOLD;
                    break;
                case Session.USER_ON_HOLD:
                    state = ON_HOLD;
                    break;
                case Session.USER_SYSTEM_ON_HOLD:
                    state = ON_HOLD;
                    break;
                default:
                    System.err.println("WARNING: SGE Job: unknown DRMAA state: "
                            + status);
            }
            
        } catch(DrmaaException e) {
            System.err.println("-- SGEJob EXCEPTION --");
            System.err.println("Got an exception while retrieving resource manager status:");
            System.err.println(e);
        }
    }
    
    public Map<String, Object> getInfo() {
        
        HashMap<String, Object> m = new HashMap<String, Object>();
        setState();
        
        try {
            m.put(  "hostname",
                    jobDescription.getSoftwareDescription().getLocation().toASCIIString());
            m.put("checkpointable", "0");
            m.put("scheduletime",   null);
            m.put("resManName","Sun Grid Engine");
            m.put("state",getStateString(state));
            m.put("resManState", Integer.toString(session.getJobProgramStatus(jobID)));
            m.put("jobID", jobID);
            m.put("starttime", time.get("start_time"));
            m.put("stoptime",  time.get("stop_time"));
             
            
        } catch (DrmaaException e) {
            System.err.println("-- SGEJob EXCEPTION --");
            System.err.println("Got an exception while retrieving resource manager status:");
            System.err.println(e);
        }
        
        return m;
    }
    
    public int getExitStatus() {
        
        JobInfo info = null;
        int retVal = -255;
        
        try {
            info = session.wait(jobID, Session.TIMEOUT_NO_WAIT);
        } catch (ExitTimeoutException ete) {
            /* This exception is OK - it's always thrown, when the job is still
             * running. Just ignore and do nothing...*/
        } catch(DrmaaException e) {
            /* This kind of exception is NOT OK - something ugly happened...*/
            System.err.println("-- SGEJob EXCEPTION --");
            System.err.println("Got an exception while retrieving JobInfo:");
            System.err.println(e);
        }
        
        if( info != null ) {
            if( info.hasExited() ) {
                retVal = info.getExitStatus();
            } else {
                retVal = -255;
            }
        }
        
        return retVal;
    }
    
    public void stop() throws GATInvocationException {
        if ((getState() != RUNNING) || (getState() != ON_HOLD) || (getState() != SCHEDULED) ) {
            throw new GATInvocationException("Cant stop(): job is not in a running state");
        } else {
            try {
                session.control(jobID, Session.TERMINATE);
                state = INITIAL;
            } catch(DrmaaException e) {
                System.err.println("-- SGEJob EXCEPTION --");
                System.err.println("Got an exception while trying to TERMINATE job:");
                System.err.println(e);
            }
        }
    }
}
