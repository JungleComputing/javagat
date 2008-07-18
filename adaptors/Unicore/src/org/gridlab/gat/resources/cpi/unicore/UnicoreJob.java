/**
 * UnicoreJob.java
 *
 * Created on July 7, 2008
 *
 */

package org.gridlab.gat.resources.cpi.unicore;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
//import org.gridlab.gat.resources.cpi.sge.SgeJob.jobStopListener;

/**
 * The Unicore / HiLA staff
 */

import de.fzj.hila.*;
import de.fzj.hila.common.jsdl.JSDL;
import de.fzj.hila.exceptions.HiLAException;
import de.fzj.hila.exceptions.HiLALocationSyntaxException;

/**
 * 
 * @author Alexander Beck-Ratzka, AEI.
 */

public class UnicoreJob extends JobCpi {

    private String jobID;
    private String hostname;
    private MetricDefinition statusMetricDefinition;
    Metric statusMetric;
    Site site;
    JSDL jsdl;
    private Task task;
    private Hashtable time;

    /**
     * constructor of UnicoreJob 
     * @param gatContext The gatContext
     * @param jobDescription
     * @param sandbox
     */
    protected UnicoreJob(GATContext gatContext,
            JobDescription jobDescription, Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);

        state = INITIAL;

        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
    }
    
    protected void setHostname(String hostname) {
        this.hostname = hostname;
    }

    protected void setState(int state) {
        this.state = state;
    }
    
    /**
     * The jobStartListener runs in a thread and checks the job's state. When it
     * detects a state transition from SCHEDULED to RUN, it writes the time and
     * exits
     */
    private class jobStartListener implements Runnable  {

        int state = 0x00;
        final int SLEEP = 250;

        TaskStatus Status=null;
        String jobID = null;
        Task task = null;
        
        Hashtable time = null;

        public jobStartListener(Task task, String jobID,
                Hashtable time) {
//            this.session = session;
        	
            this.jobID = jobID;
            this.time = time;
            this.task = task;
        }

        public void run() {
        	try {
        		while (!task.status().equals(Status.RUNNING)) {
        			if (task.status().equals(Status.FAILED)) {
        				System.out.println("Job submission failed");
        				break;
                    } else if (task.status().equals(Status.CANCELLED)) {
                    	System.out.println("Job submission cancelled");
                        break;
                    }
        		Thread.sleep(SLEEP);
        		} 
        	} catch (HiLAException e) {
        		e.printStackTrace();
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	}
            // Now we're in RUNNING state - set the time and start the
            // jobStopListener
            time.put("start_time", new Long(System.currentTimeMillis()));

            jobStopListener jsl = new jobStopListener(this.task, this.jobID,  time);
            new Thread(jsl).start();
        }
    }

    /**
     * The jobStopListener runs in a thread and checks the job's state. When it
     * detects a state transition from RUN to STOP, it writes the time and exits
     */
    private class jobStopListener implements Runnable {

        int state = 0x00;
        final int SLEEP = 250;
        Task task=null;
        TaskStatus Status=null;

        String jobID = null;
        Hashtable time = null;

        public jobStopListener(Task task, String jobID,
                Hashtable time) {
            this.task = task;
            this.jobID = jobID;
            this.time = time;
        }

        public void run() {
        	try {
        		while (!task.status().equals(Status.FINISHED)) {
        			if (task.status().equals(Status.FAILED)) {
        				System.out.println("HilA job " + jobID + "failed");
        				break;
        				}
        			else if (task.status().equals(Status.CANCELLED)) {
        				System.out.println("HilA job " + jobID + "cancelled");
        				break;
        			    }
        			Thread.sleep(SLEEP);
        			}
        		// Now we're in STOPPED state - set the time and exit
        		
        		time.put("stop_time", new Long(System.currentTimeMillis()));
        		} catch  (HiLAException e) {
            		e.printStackTrace();
            	} catch (InterruptedException e) {
            		e.printStackTrace();
            	}
        }
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

    protected void setTask(Task task) {
        this.task = task;
    }
    
    protected void setState() {
    	TaskStatus Status=null;
   
        try {
            TaskStatus status = task.status();

            if (status.equals(Status.RUNNING)) {
            	state = RUNNING;
            }

           if (status.equals(Status.FAILED)) {
        	  state = SUBMISSION_ERROR;
           }

           if (status.equals(Status.FINISHED)) {
         	  state = STOPPED;
            }

            /* Job is active but suspended */

           if (status.equals(Status.NEW)) {
          	  state = SCHEDULED;
             }
           
           if (status.equals(Status.PENDING)) {
           	  state = SCHEDULED;
              }
         } catch (HiLAException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("-- UNICOREJob EXCEPTION --");
                logger
                        .debug("Got an exception while retrieving resource manager status:");
                logger.debug(e);
            }
        }
    }
    
    public void stop() throws GATInvocationException {
        if ((getState() != RUNNING) || (getState() != ON_HOLD)
                || (getState() != SCHEDULED)) {
            throw new GATInvocationException(
                    "Cant stop(): job is not in a running state");
        } else {
            try {
            	task.abort();
                state = INITIAL;
            } catch (HiLAException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("-- UnicoreJob EXCEPTION --");
                    logger
                            .debug("Got an exception while trying to TERMINATE job:");
                    logger.debug(e);
                }
                throw new GATInvocationException("Can't stop Unicore Job" + jobID, e);
            }
        }
    }

}
            	