/*
 * SGEJob.java
 *
 * Created on May 16, 2006, 11:24 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.lang.String;
import java.util.Map;
import java.util.HashMap;
import org.ggf.drmaa.*;

import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.GATInvocationException;

import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

/**
 *
 * @author ole.weidner
 */
public class SGEJob extends Job{
    
    private String jobID;    
    private MetricDefinition statusMetricDefinition;
    private Metric statusMetric;
    private JobDescription jobDescription;
    private Session session;
    
    public SGEJob(SGEBrokerAdaptor broker, JobDescription jobDescription,
            Session session, String id) {
        
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

    public JobDescription getJobDescription() {
        return jobDescription;
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
                    state = SCHEDULED;
                    break;
                case Session.USER_ON_HOLD:
                    state = SCHEDULED;
                    break;
                case Session.USER_SYSTEM_ON_HOLD:
                    state = SCHEDULED;
                    break;
                default:
                    System.err.println("WARNING: SGE Job: unknown DRMAA state: "
                            + status);
            }
            
        } catch(DrmaaException e) {
            
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
            if (GATEngine.VERBOSE) {
                System.err.println("got an exception while retrieving job " +
                        "information: "+ e);
            }
        }
        
        return m;
    }
    
    public void stop() throws GATInvocationException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Cant stop(): job is not running");
        }

        try {  
            
            session.control (jobID, Session.TERMINATE);
            state = INITIAL;            
            
        } catch(DrmaaException e) {
            System.err.println("Execption in SGEBRokerAdaptor");
            System.err.println(e);            
        }
    }

    public void unSchedule() throws GATInvocationException {
        if (getState() != SCHEDULED) {
            throw new GATInvocationException("Can't unSchedule(): job is not scheduled");
        }
        
        try {
            session.control(jobID, Session.TERMINATE);
            state = INITIAL;

        } catch(DrmaaException e) {
            System.err.println("Execption in SGEBRokerAdaptor");
            System.err.println(e);        
        }
    }

}
