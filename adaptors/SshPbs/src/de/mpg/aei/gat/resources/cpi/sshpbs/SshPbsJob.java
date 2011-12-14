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
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.SshHelper;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedSimpleJobBase;
import org.gridlab.gat.resources.cpi.SimpleJobBase;

/**
 * @author Alexander Beck-Ratzka, AEI, June 10th 2010, created
 */

public class SshPbsJob extends SimpleJobBase {

    // private static String regexString = "[ ][ ]";
    private static String regexString = "\\s\\s*";

    private static final long serialVersionUID = 1L;
    
    private final SshHelper jobHelper;

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
	    Sandbox sandbox, SshHelper jobHelper, String returnValueFile) {
	super(gatContext, brokerURI, jobDescription, sandbox, returnValueFile);
	this.jobHelper = jobHelper;
    }

    /**
     * Constructor for unmarshalled jobs.
     */

    public SshPbsJob(GATContext gatContext, SerializedSimpleJobBase sj)
	    throws GATObjectCreationException {
	super(gatContext, sj);
	jobHelper = new SshHelper(gatContext, brokerURI, "sshpbs",
		SshPbsResourceBrokerAdaptor.SSH_PORT_STRING,
		SshPbsResourceBrokerAdaptor.SSH_STRICT_HOST_KEY_CHECKING);
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

    protected synchronized void getJobState(String jobID) throws GATInvocationException {
	
        if (state == JobState.POST_STAGING || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }

	logger.debug("Getting task status in setState()");

	//  getting the status via ssh ... qstat
	
        CommandRunner cmd = jobHelper.runSshCommand("qstat");
        if (cmd.getExitCode() != 0) {
            throw new GATInvocationException("qstat gives exitcode " +  cmd.getExitCode());
        }
	JobState s;
	try {
	    s = mapPbsStatetoGAT(cmd.outputAsLines());
	    if (s != JobState.STOPPED) {
		setState(s);
	    } else {
		setState(JobState.POST_STAGING);
	    }
	} catch (IOException e) {
	    logger.debug("retrieving job status sshpbsjob failed");
	    throw new GATInvocationException(
		    "Unable to retrieve the Job Status", e);
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
		// For SGE, format is:
		// JobID Prio JobName JobOwner JobState .....
		// For PBS, format is
		// JobID JobName JobOwner CpuTime JobState ....
		// So, in both cases, the 5'th column gives the job state.
		// Below is combined job-state determination for SGE and PBS ...
		if (splits.length < 5) {
		    return JobState.UNKNOWN;
		}
		String status = splits[4];
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

    protected synchronized void kill(String jobID) {
	try {
	    jobHelper.runSshCommand("qdel", jobID);
	} catch (Throwable e) {
	    logger.info("Failed to stop sshPbs job: " + jobID, e);
	    // TODO: what to do here?
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
}
