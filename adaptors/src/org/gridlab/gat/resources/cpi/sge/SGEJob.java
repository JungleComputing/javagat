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
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 *
 * @author ole.weidner
 */
public class SGEJob extends JobCpi {
    
    private String jobID;
    private MetricDefinition statusMetricDefinition;
//    private Metric statusMetric;
    private JobDescription jobDescription;
    private Session session;
    private Hashtable time;
    
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
        Hashtable time  = null;

        public jobStartListener(Session session, String jobID, Hashtable time) {
            this.session = session;
            this.jobID = jobID;
            this.time = time;
        }
        
        public void run() {
            while (state != Session.RUNNING) {
                try {
                    state = session.getJobProgramStatus(jobID);
                    if( state == Session.FAILED) 
                    {
                        System.out.println("drmaa.Session got the state FAILED");
                        break;
                    }
                    else if( state == Session.DONE ) 
                    {
                        break;
                    }
                    Thread.sleep(SLEEP);
                }
                catch (DrmaaException e) 
                {
                	e.printStackTrace();
                    break;
                }
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                    break;
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
        Hashtable time  = null;

        public jobStopListener(Session session, String jobID, Hashtable time) {
            this.session = session;
            this.jobID = jobID;
            this.time = time;
        }
        
        public void run() {
            while (state != Session.DONE) {
                try {
                    state = session.getJobProgramStatus(jobID);
                    
                    switch (state)
                    {
                    case Session.FAILED: 	                
                    	System.out.println("Job failed");
                    	break;
                    case Session.HOLD: 	                    
                    	System.out.println("Job hold");
                    	break;
                    case Session.QUEUED_ACTIVE:             
                    	System.out.println("Job queued active");
                    	break;
                    case Session.RELEASE:                   
                    	System.out.println("Job released");
                    	break;
                    case Session.RESUME:                    
                    	System.out.println("Job resumed");
                    	break;
                    case Session.RUNNING:                   
                    	System.out.println("Job running");
                    	break;
//                    case Session.SUSPEND:                   System.out.println("Job suspended");
                    case Session.SYSTEM_ON_HOLD:            
                    	System.out.println("Job system on hold");
                    	break;
                    case Session.SYSTEM_SUSPENDED:          
                    	System.out.println("Job system suspended");
                    	break;
                    case Session.TERMINATE:                 
                    	System.out.println("Job terminate");
                    	break;
                    case Session.UNDETERMINED:              
                    	System.out.println("Job undetermined");
                    	break;
                    case Session.USER_ON_HOLD:              
                    	System.out.println("Job user on hold");
                    	break;
                    case Session.USER_SUSPENDED:            
                    	System.out.println("Job user suspended");
                    	break;
                    case Session.USER_SYSTEM_ON_HOLD:       
                    	System.out.println("Job user system on hold");
                    	break;
                    case Session.USER_SYSTEM_SUSPENDED:     
                    	System.out.println("Job user system suspended");
                    	break;
                    	
                    default: 		System.out.println("job: state " + state + "not found in drmaa");
                    }
                    
                    System.out.println("state: " + state + " Session.UNDETERMINED: " + Session.UNDETERMINED);
                    if( state == Session.FAILED) 
                    {
                       System.out.println("state has the value failed");
                       break;
                    }
                    Thread.sleep(SLEEP);
                }
                catch (DrmaaException e) 
                {
                	e.printStackTrace();
                	break;
                    //TODO
                }
                catch (InterruptedException e) 
                {
                    break;
                }
            }            
            // Now we're in STOPPED state - set the time and exit
            time.put( "stop_time", new Long(System.currentTimeMillis()) );
        }
    }
    
    public SGEJob(GATContext gatContext, Preferences preferences, SGEBrokerAdaptor broker, JobDescription jobDescription,
            Session session, String id, Sandbox sandbox) {
        super(gatContext, preferences, jobDescription, sandbox);
        
        jobID = id;
        this.jobDescription = jobDescription;
        this.session = session;
        state = INITIAL;
        
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
//        statusMetric = statusMetricDefinition.createMetric(null);
        
        time = new Hashtable();
        
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
            e.printStackTrace();
        }
    }
    
    public Map getInfo() {
        
        HashMap m = new HashMap();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }
}
