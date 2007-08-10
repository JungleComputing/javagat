/*
 * Created on July 25, 2007
 */
package org.gridlab.gat.resources.cpi.glite;

import java.util.HashMap;

import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.InvalidArgumentFaultException;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.JobUnknownFaultException;
import org.glite.wms.wmproxy.OperationNotAllowedFaultException;
import org.glite.wms.wmproxy.ServiceException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.WMProxyAPI;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * This thread actively checks if the output files are ready to copy. If not the
 * exception is caught. This is not the most elegant way but it is not possible
 * to poll the state of gLite job right now.
 * 
 * @author anna
 * 
 */
class GLiteJobPoller extends Thread {
	private GLiteJob job;

	private boolean die = false;

	GLiteJobPoller(GLiteJob j) {
		this.job = j;
		setDaemon(true);
	}

	public void run() {
		while (true) {
			if (job.getState() == Job.STOPPED)
				return;
			if (job.getState() == Job.SUBMISSION_ERROR)
				return;
			job.tryGetOutputFiles();
			if (job.getState() == Job.STOPPED)
				return;
			if (job.getState() == Job.SUBMISSION_ERROR)
				return;

			synchronized (this) {
				try {
					wait(20 * 1000);
				} catch (Exception e) {
					// Ignore
				}
				if (die) {
					if (GATEngine.DEBUG) {
						System.err.println("gLite job poller killed");
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

/**
 * @author anna
 */
public class GLiteJob extends JobCpi {

	private WMProxyAPI client;

	private String jobID;

	private JobIdStructType jobStruct;

	private long startTime;

	private MetricDefinition statusMetricDefinition;

	private Metric statusMetric;

	private GLiteJobPoller poller;

	// accessed only in synchronized block!!! (just like state)
	private boolean postStageStarted = false;
	private boolean postStageFinished = false;

	private String outputStorage = null;
	
	public GLiteJob(GATContext gatContext, Preferences preferences,
			JobDescription jobDescription, Sandbox sandbox, WMProxyAPI client,
			long startTime, JobIdStructType jobStruct) {

		super(gatContext, preferences, jobDescription, sandbox);
		this.client = client;
		this.startTime = startTime;
		this.jobStruct = jobStruct;
		this.jobID = jobStruct.getId();
		// it is not yet possible to retrieve job state
		// we just check if we can already copy output files
		// so I use now 3 states: UNKNOWN, POST_STAGING and STOPPED
		state = Job.UNKNOWN;
		// Tell the engine that we provide job.status events

		HashMap returnDef = new HashMap();
		returnDef.put("status", String.class);
		statusMetricDefinition = new MetricDefinition("job.status",
				MetricDefinition.DISCRETE, "String", null, null, returnDef);
		GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
		statusMetric = statusMetricDefinition.createMetric(null);

		poller = new GLiteJobPoller(this);
		poller.start();
	}

	public String getJobID() throws GATInvocationException {
		return jobID;
	}

	/* we use this method to check if the job has already ended */
	void tryGetOutputFiles() {
		// TODO: code this
		// careful synchronization with stop() method!!!
		try {
			StringAndLongList list = client.getOutputFileList(jobID);
			setState(POST_STAGING);
			// TODO: file copying
			setState(STOPPED);
		} catch (AuthorizationFaultException afe) {
			// the client is not authorized to perform this operation
			setState(SUBMISSION_ERROR);
		} catch (AuthenticationFaultException aufe) {
			// a generic authentication problem occurred
			setState(SUBMISSION_ERROR);
		} catch (JobUnknownFaultException jufe) {
			// the given job has not been registered to the system
			setState(SUBMISSION_ERROR);
		} catch (InvalidArgumentFaultException iafe) {
			// the given job Id is not valid
			setState(SUBMISSION_ERROR);
		} catch (OperationNotAllowedFaultException onafe) {
			// the current job status does not allow requested operation
			// we don't know if the job is scheduled or already running
			setState(UNKNOWN);
		} catch (ServiceException se) {
			// other error occured during the execution of the remote method
			// call to the WMProxy server
			setState(SUBMISSION_ERROR);
		} catch (Exception e) {
			// some other exception caught; lets wait
			setState(UNKNOWN);
		}
	}

	public boolean 
	
	public void stop() throws GATInvocationException {
		String stateString = null;
		synchronized (this) {
			// we don't want to postStage twice (can happen with jobpoller)
			if (postStageStarted)
				return;
			postStageStarted = true;
			state = POST_STAGING;
			stateString = getStateString(state);
		}
		MetricValue v = new MetricValue(this, stateString, statusMetric, System
				.currentTimeMillis());
		if (GATEngine.DEBUG) {
			System.err.println("glite job stop: firing event: " + v);
		}
		GATEngine.fireMetric(this, v);

		GATInvocationException exception = null;
		try {
            if (job != null)
                cancelGLiteJob();
        } catch (GATInvocationException e) {
        	// shouldn't we do something more when an exception occurs?
            if (GATEngine.VERBOSE) {
                System.err.println("got an exception while cancelling job: "
                        + e);
            }
            exception = e;
        }

        if (GATEngine.VERBOSE) {
            System.err.println("glite job stop: delete/wipe starting");
        }

        // do cleanup, callback handler has been uninstalled
        // TODO: it was copied from GLobusJob - check it!!!
        if (sandbox != null)
        	sandbox.retrieveAndCleanup(this);

        synchronized (this) {
            postStageFinished = true;

            if (GATEngine.VERBOSE) {
                System.err.println("glite job stop: post stage finished");
            }

            state = STOPPED;
            stateString = getStateString(state);
        }

        MetricValue v2 =
                new MetricValue(this, stateString, statusMetric, System
                        .currentTimeMillis());
        if (GATEngine.DEBUG) {
            System.err.println("glite job stop: firing event: " + v2);
        }
        GATEngine.fireMetric(this, v2);

        finished();
	}

	private boolean cancelGLiteJob() throws GATInvocationException {
		try {
			client.jobCancel(jobID);
			return true;
		} catch (AuthorizationFaultException afe) {
			throw new GATInvocationException(
					"gLite job stop: The client is not authorized to perform this operation",
					afe);
		} catch (AuthenticationFaultException aufe) {
			throw new GATInvocationException(
					"gLite job stop: A generic authentication problem occurred", aufe);
		} catch (JobUnknownFaultException jufe) {
			throw new GATInvocationException(
					"gLite job stop: The given job has not been registered to the system", jufe);
		} catch (InvalidArgumentFaultException iafe) {
			throw new GATInvocationException("gLite job stop: The given job Id is not valid "
					+ jobID, iafe);
		} catch (OperationNotAllowedFaultException onafe) {
			throw new GATInvocationException(
					"gLite job stop: The current job status does not allow requested operation",
					onafe);
		} catch (ServiceException se) {
			throw new GATInvocationException(
					"gLite job stop: Unknown error occured during the execution of the remote method call to the WMProxy server",
					se);
		} catch (Exception e) {
			throw new GATInvocationException(
					"gLite job stop: Unknown error occured during the execution of the remote method call to the WMProxy server",
					e);
		}
	}

	protected synchronized void setState(int newState) {
		state = newState;
	}
}
