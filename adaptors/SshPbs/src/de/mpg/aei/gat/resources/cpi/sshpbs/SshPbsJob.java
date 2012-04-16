/**
 * SshPbsJob.java
 *
 * Created on June 10th, 2010
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedSimpleJobBase;
import org.gridlab.gat.resources.cpi.SimpleJobBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshPbsJob extends SimpleJobBase implements MetricListener {

    protected static Logger logger = LoggerFactory
            .getLogger(SshPbsJob.class);

    // private static String regexString = "[ ][ ]";
    private static String regexString = "\\s\\s*";

    private static final long serialVersionUID = 1L;
    
    private final ResourceBroker jobHelper;

    private final GATContext subContext;

    /**
     * constructor of SshPbsJob
     * 
     * @param gatContext
     *            The gatContext
     * @param jobDescription
     * @param sandbox
     */
    protected SshPbsJob(GATContext gatContext,
	    URI brokerURI, JobDescription jobDescription,
	    Sandbox sandbox, ResourceBroker jobHelper, String returnValueFile) {
	super(gatContext, brokerURI, jobDescription, sandbox, returnValueFile);
	subContext = SshPbsResourceBrokerAdaptor.getSubContext(gatContext);
	this.jobHelper = jobHelper;
    }

    /**
     * Constructor for unmarshalled jobs.
     */

    public SshPbsJob(GATContext gatContext, SerializedSimpleJobBase sj)
	    throws GATObjectCreationException {
	super(gatContext, sj);
	subContext = SshPbsResourceBrokerAdaptor.getSubContext(gatContext);
	try {
	    jobHelper = SshPbsResourceBrokerAdaptor.subResourceBroker(subContext, brokerURI);
	} catch (URISyntaxException e) {
	    throw new GATObjectCreationException("Could not create broker to get PBS job status", e);
	}

	startListener();
    }

    protected synchronized void setJobID(String jobID) {
	super.setJobID(jobID);
	notifyAll();
    }

    public static Advertisable unmarshal(GATContext context, String s)
	    throws GATObjectCreationException {
	return SimpleJobBase.unmarshal(context, s, SshPbsJob.class.getClassLoader());
    }

    protected void setSoft(SoftwareDescription Soft) {
	super.setSoft(Soft);
    }
    
    protected void setState(JobState state) {
	super.setState(state);
    }
    
    protected void startListener() {
	super.startListener();
    }

    boolean jobStateBusy = false;
    
    protected void getJobState(String jobID) throws GATInvocationException {
	synchronized(this) {
	    while (jobStateBusy) {
	        try {
	            wait();
	        } catch(InterruptedException e) {
	            // ignore
	        }
	    }
	    jobStateBusy = true;
	}
	
        
        JobState resultState;
        
	if (logger.isDebugEnabled()) {
	    logger.debug("Entering getJobState");
	}
	
	try {
	    if (state == JobState.POST_STAGING || state == JobState.STOPPED
	            || state == JobState.SUBMISSION_ERROR) {
	        if (logger.isDebugEnabled()) {
	            logger.debug("returning from getJobState, state = " + state);
	        }
	        return;
	    }

	    logger.debug("Getting task status in setState()");

	    java.io.File qstatResultFile = null;
	    ArrayList<String> result = new ArrayList<String>();

	    try {    
	        // Create qstat job
	        SoftwareDescription sd = new SoftwareDescription();
	        // Indirection via /bin/sh so that $USER argument gets expanded.
	        sd.setExecutable("/bin/sh");
	        sd.setArguments("-c", "qstat -u $USER");
	        sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
	        sd.addAttribute(SoftwareDescription.SANDBOX_ROOT, sandbox.getSandboxPath());
	        qstatResultFile = java.io.File.createTempFile("GAT", "tmp");
	        try {
	            sd.setStdout(GAT.createFile(subContext, qstatResultFile.getAbsolutePath()));
	        } catch (GATObjectCreationException e1) {
	            throw new GATInvocationException("Could not create GAT object for temporary " + qstatResultFile.getAbsolutePath(), e1);
	        }
	        JobDescription jd = new JobDescription(sd);
	        Job job = jobHelper.submitJob(jd, this, "job.status");
	        synchronized (job) {
	            while (job.getState() != Job.JobState.STOPPED && job.getState() != Job.JobState.SUBMISSION_ERROR) {
	                synchronized(this) {
	                    try {
	                        job.wait();
	                    } catch(InterruptedException e) {
	                        // ignore
	                    }
	                }
	            }
	        }
	        if (job.getState() != Job.JobState.STOPPED || job.getExitStatus() != 0) {
	            throw new GATInvocationException("Could not submit qstat job");
	        }

	        // qstat success.
	        BufferedReader in = new BufferedReader(new FileReader(qstatResultFile.getAbsolutePath()));
	        String str;
	        while ((str = in.readLine()) != null) {
	            if (logger.isDebugEnabled()) {
	                logger.debug("qstat  line: " + str);
	            }
	            result.add(str);
	        }
	        resultState = mapPbsStatetoGAT(result);
	    } catch (IOException e) {
	        logger.debug("retrieving job status sshpbsjob failed");
	        throw new GATInvocationException(
	                "Unable to retrieve the Job Status", e);
	    } finally {
	        qstatResultFile.delete();
	    }
	} finally {
	    synchronized(this) {
	        jobStateBusy = false;
	        notifyAll();
	    }
	}

	if (resultState != JobState.STOPPED) {
	    setState(resultState);
	} else {
	    setState(JobState.POST_STAGING);
	}

	if (logger.isDebugEnabled()) {
	    logger.debug("returning from getJobState, state = " + state);
	}
    } 


    private boolean sawJob = false;
    private int missedJob = 0;
    
    /**
     * mapPbsStateToGAT maps a job status of PBS to a GAT job status.
     * 
     * @param pbsState the output of qstat.
     * @return JobState (GAT)
     * @author A. Beck-Ratzka, AEI, 14.09.2010
     */
    
    private JobState mapPbsStatetoGAT(List<String> pbsState) {

	String[] splits = null;

	if (logger.isDebugEnabled()) {
	    logger.debug("enter mapPbsStatetoGAT");
	}
	if (pbsState == null) {
	    logger.error("Error in mapPbsStatetoGAT: no PbsState returned");
	    return JobState.UNKNOWN;
	} else {
	    for (String s : pbsState) {
		// Remove leading and trailing spaces, and split.
		splits = s.trim().split(regexString);
		// Note: PBS qstat sometimes does not print the complete job identifier.
		// On lisa.sara.nl, for example, if the job identifier is 5823458.batch1.irc.sara.nl,
		// qstat only prints 5823458.batch1. --Ceriel
		if (logger.isDebugEnabled()) {
		    logger.debug("Saw job " + splits[0]);
		}
		if (this.jobID.startsWith(splits[0])) {
		    if (logger.isDebugEnabled()) {
			logger.debug("Found job: " + splits[0] + ", JobID = " + this.jobID);
		    }
		    sawJob = true;
		    break;
		}
		splits = null;
	    }
	    if (splits == null) {
		if (logger.isDebugEnabled()) {
		    logger.debug("no job status information for '" + this.jobID
			    + "' found.");
		}
		// if we saw it before, assume it is finished now.
		if (sawJob) {
		    if (logger.isDebugEnabled()) {
			logger.debug("But is was present earlier, so we assume it finished.");
		    }
		    return JobState.STOPPED;
		}
		missedJob++;
		if (missedJob > 1) {
		    // arbitrary threshold. Allow for a small gap between successful
		    // submission of the job and its appearance in qstat output. But it may
		    // also not appear because it is already finished ...
		    if (logger.isDebugEnabled()) {
			logger.debug("But is was not present for a while, so we assume it finished.");
		    }
		    return JobState.STOPPED;
		}
		// Return current state.
		return state;
	    } else {
		// PBS alternative output format. 10'th column gives the job state.
		
		if (splits.length < 10) {
		    return JobState.UNKNOWN;
		}
		String status = splits[9];
		if (status.indexOf('t') >= 0 || status.indexOf('T') >= 0) {	// transfer
		    return JobState.SCHEDULED;
		}
		if (status.indexOf('w') >= 0 || status.indexOf('W') >= 0) {	// waiting
		    return JobState.SCHEDULED;
		}
		if (status.indexOf('Q') >= 0) { // Queued
		    return JobState.SCHEDULED;
		}
		if (status.indexOf('h') >= 0 || status.indexOf('H') >= 0) {	// hold
		    return JobState.ON_HOLD;
		}
		if (status.indexOf('r') >= 0) {	// running
		    return JobState.RUNNING;
		}
		if (status.indexOf('R') >= 0) {	// restarted
		    return JobState.RUNNING;
		}
		if (status.indexOf('s') >= 0 || status.indexOf('S') >= 0) {	// suspended
		    return JobState.RUNNING;
		}
		if (status.indexOf('x') >= 0 || status.indexOf('C') >= 0) {	// exit
		    return JobState.STOPPED;
		}
		if (status.indexOf('E') >= 0) {	// error or exiting
		    return JobState.STOPPED;
		}
		return JobState.UNKNOWN;
	    }
	}
    }

    protected void kill(String jobID) {
	try {
	    // Create qdel job
            SoftwareDescription sd = new SoftwareDescription();
            sd.setExecutable("qdel");
            sd.setArguments(jobID);
            sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
            sd.addAttribute(SoftwareDescription.SANDBOX_ROOT, sandbox.getSandboxPath());
            sd.addAttribute(SoftwareDescription.STOP_ON_EXIT, "false");
            JobDescription jd = new JobDescription(sd);
            Job job = jobHelper.submitJob(jd, this, "job.status");
            synchronized(job) {
                while (job.getState() != Job.JobState.STOPPED && job.getState() != Job.JobState.SUBMISSION_ERROR) {
                    try {
                        job.wait();
                    } catch(InterruptedException e) {
                        // ignore
                    }
                }
            }
            if (job.getState() != Job.JobState.STOPPED || job.getExitStatus() != 0) {
                throw new GATInvocationException("Could not submit qdel job");
            }
        } catch (Throwable e) {
            logger.info("Failed to stop sshPbs job: " + jobID, e);
	}
    }

    protected synchronized Integer retrieveExitStatus(String returnValueFile) {

	String marker = "retvalue = ";
	String line = null;
	int rc = -1;

	BufferedReader rExit = null;
	java.io.File fi = new java.io.File(returnValueFile);
	try {
	    rExit = new BufferedReader(new FileReader(fi));

	    line = rExit.readLine().toString();

	    String rc_String = line.substring(marker.length());
	    if (rc_String != null) {
		rc = Integer.parseInt(rc_String);
	    }
	    return new Integer(rc);
	} catch (FileNotFoundException e) {
	    logger.debug("SshPbs adaptor: exit value file " + returnValueFile
		    + " not found!");
	    return null;
	} catch (IOException e) {
	    return null;
	} finally {
	    try {
		rExit.close();
	    } catch(Throwable e) {
		// ignore
	    }
	    fi.delete();
	}
    }
    
    @Override
    public void processMetricEvent(MetricEvent event) {
        if (event.getValue().equals(Job.JobState.STOPPED) || event.getValue().equals(Job.JobState.SUBMISSION_ERROR)) {
            synchronized(event.getSource()) {
                event.getSource().notifyAll();
            }
        }       
    }

}
