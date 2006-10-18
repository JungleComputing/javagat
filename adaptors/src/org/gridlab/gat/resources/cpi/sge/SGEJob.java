/*
 * SGEJob.java
 *
 * Created on May 16, 2006, 11:24 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.util.HashMap;
import java.util.Map;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.Session;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;

/**
 *
 * @author ole.weidner
 */
public class SGEJob extends JobCpi {
    
    private String jobID;    
    private MetricDefinition statusMetricDefinition;
    private Metric statusMetric;
    private Session session;
    
    public SGEJob(SGEBrokerAdaptor broker, JobDescription jobDescription,
            Session session, String id) {
        super(jobDescription, null, null);
        
        jobID = id;
        this.jobDescription = jobDescription;
        this.session = session;
        state = INITIAL;
        
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null); 
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
            
        }
        catch(DrmaaException e) {
            System.err.println("-- SGEJob EXCEPTION --");
            System.err.println("Got an exception while retrieving resource manager status:");
            System.err.println(e);      
        }
    }
    
    public Map getInfo() {

        HashMap m = new HashMap();
        setState();
        
        try {
            m.put("resManName","Sun Grid Engine");
            m.put("state",getStateString(state));
            m.put("resManState", Integer.toString(session.getJobProgramStatus(jobID)));
            m.put("resManContact", session.getContact());
            m.put("jobID", jobID);
            
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
        }
        catch (ExitTimeoutException ete) {
            /* This exception is OK - it's always thrown, when the job is still
             * running. Just ignore and do nothing...*/
        }
        catch(DrmaaException e) {
            System.err.println("-- SGEJob EXCEPTION --");
            System.err.println("Got an exception while retrieving JobInfo:");
            System.err.println(e);
        }
        
        if( info != null ) {
            if( info.hasExited() ) {
                retVal = info.getExitStatus();
            }
            else {
                retVal = -255;
            }
        }
        
        return retVal;
    }
    
    public void stop() throws GATInvocationException {
        if ((getState() != RUNNING) || (getState() != ON_HOLD) || (getState() != SCHEDULED) ) {
            throw new GATInvocationException("Cant stop(): job is not in a running state");
        }
        else {
            try {  
                session.control (jobID, Session.TERMINATE);
                state = INITIAL;            
            } 
            catch(DrmaaException e) {
                System.err.println("-- SGEJob EXCEPTION --");
                System.err.println("Got an exception while trying to TERMINATE job:");
                System.err.println(e);
            }
        }
    }
}
