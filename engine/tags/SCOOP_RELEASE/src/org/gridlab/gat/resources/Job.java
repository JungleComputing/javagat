package org.gridlab.gat.resources;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An instance of this class represents a job.
 * <p>
 * A job is what one normally considers when referring to a starting a program.
 * Thus, a job is primarily described by the software which is to execute during
 * this job. The description of the software which is to execute during this job
 * is given by an instance of the class SoftwareResourceDescription. As detailed
 * in the description section of the documentation for the class
 * SoftwareResourceDescription, an instance of the class
 * SoftwareResourceDescription describes a software component. uses this
 * description to describe the software which is to execute during this simple
 * job.
 * <p>
 * Upon creating an instance of the class the associated physical job is not
 * immediately running. An instance of the class has various states which it can
 * be in, only one of which maps to a running physical job. In particular the
 * various state are as follows
 * <ul>
 * <li><em>INITIAL</em></li>
 * <li><em>SCHEDULED</em></li>
 * <li><em>RUNNING</em></li>
 * <li><em>STOPPED</em></li>
 * <li><em>SUBMISSION_ERROR</em></li>
 * </ul>
 * A description of the various states diagrammed in figure below is as follows:
 * <center><img src="doc-files/SimpleJob.jpg" height="600" width="150">
 * </center> <em>INITIAL</em> An instance of the class has been constructed,
 * but the method Submit has not yet been successfully called on this instance.
 * <p>
 * <em>SCHEDULED</em> The method Submit has been successfully called on an
 * instance of while the instance was in the Constructed state.
 * <p>
 * <em>RUNNING</em> The physical job corresponding to an instance of a is
 * running.
 * <p>
 * <em>STOPPED</em> The physical job corresponding to an instance of a was
 * running but is not currently running due to a successful call to the method
 * Stop, or the physical job corresponding to an instance of a was running but
 * is not currently running due to the physical job completing.
 * <p>
 * <em>SUBMISSION_ERROR</em> An error occurred while submitting this job.
 * <p>
 * <p>
 * In addition a allows one or more Metrics to be monitored.
 */
public abstract class Job implements Monitorable, Advertisable {

	/** Constructed state indicator */
	public static final int INITIAL = 0;

	/** Submitted state indicator */
	public static final int SCHEDULED = 1;

	/** Running state indicator */
	public static final int RUNNING = 2;

	/** Stopped state indicator */
	public static final int STOPPED = 3;

	/** Submission error state indicator */
	public static final int SUBMISSION_ERROR = 4;

	protected int state = INITIAL;

	protected GATContext gatContext;

	protected Preferences preferences;

	protected String getStateString() {
		switch (state) {
		case INITIAL:
			return "INITIAL";
		case SCHEDULED:
			return "SCHEDULED";
		case RUNNING:
			return "RUNNING";
		case STOPPED:
			return "STOPPED";
		case SUBMISSION_ERROR:
			return "SUBMISSION_ERROR";
		default:
			return "UNKNOWN_STATE";
		}
	}

	public JobDescription getJobDescription() {
		throw new Error("Not implemented");
	}

	/**
	 * Stops the associated physical job. Upon a successful call to this method
	 * the associated physical job is forcibly terminated. This method can only
	 * be called on a in the SCHEDULED state.
	 * 
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void unSchedule() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * Stops the associated physical job. Upon a successful call to this method
	 * the associated physical job is forcibly terminated. This method can only
	 * be called on a in the Running state.
	 * 
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void stop() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * This method returns the state of the associated . This is one of the
	 * associated public member variables INITIAL, SCHEDULED,
	 * RUNNING, STOPPED, or SUBMISSSION_ERROR.
	 * 
	 * @return This method returns the state of the associated , one of the
	 *         associated member variables
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public int getState() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * This method returns an instance of the class java.util.Map which contains
	 * information about the associated . This java.util.Map contains a set of
	 * key/value pairs the key, a java.lang.String, being the name of the
	 * information and the value being the value of the associated named
	 * information. The minimum set of keys which the returned java.util.Map
	 * contains is as follows:
	 * <ul>
	 * <li><em>hostname</em></li>
	 * <li><em>submissiontime</em></li>
	 * <li><em>starttime</em></li>
	 * <li><em>stoptime</em></li>
	 * </ul>
	 * <p>
	 * <em>hostname</em> The key hostname corresponds to a java.lang.String
	 * value which is the name of the host on which the physical job is running,
	 * if is in the Running state. If the associated is not in the Running  
	 * state, the value is null.
	 * <p>
	 * <em>submissiontime</em> The key submissiontime corresponds to a long
	 * value which is the number of milliseconds after January 1, 1970, 00:00:00
	 * GMT when the associated physical job was submitted. This value is null
	 * for a in the Constructed state otherwise it is not null.
	 * <p>
	 * <em>starttime</em> The key starttime corresponds to a long value which
	 * is the number of milliseconds after January 1, 1970, 00:00:00 GMT when
	 * the associated physical job was started. This value is null for a in the
	 * Submitted or Constructed states otherwise it is not null.
	 * <p>
	 * <em>stoptime<em>
	 * The key stoptime corresponds to a long value which is the
	 * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
	 * associated physical job stopped. This value is not null for a
	 *  in the Stopped state otherwise it is null.
	 * <p>
	 * <em>postStageError<em>
	 * This key is present in the map if the application did run, but one or more files could not be post staged.
	 * The data value attached to this key is the exception that occurred while poststageing.
	 * 
	 * Other key/value pairs will be in future added to the list of key/value
	 * pairs returned in this java.util.Map as the need develops.
	 *
	 * @return An instance of the class java.util.Map which presents 
	 * information about the associated .
	 * @throws GATInvocationException Thrown upon problems 
	 * accessing the remote instance
	 * @throws java.io.IOException Upon non-remote IO problem
	 */
	public Map getInfo() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * This method returns the job id, a globally unique identifier for the
	 * physical job corresponding to this instance. This method should be called
	 * on an instance of this class only when the instance is in a Running or
	 * Submitted state or an error will be returned.
	 * 
	 * @return An instance of the class java.lang.String which represents the
	 *         job ID
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public String getJobID() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * Checkpoints the associated physical job. A physical job is said to be
	 * checkpointed when it writes its current state information to a long term
	 * storage medium. A physical job when checkpointing must write its current
	 * state information to a long term storage medium in such a manner so that
	 * it can, at a later date, by using the state information stored in the
	 * long term storage medium continue running "from the same point" it was at
	 * before checkpointing. This is useful for physical jobs which involve
	 * significant data processing and can not, for any number of reasons,
	 * process all of this data in a single run; also, it is useful for physical
	 * jobs which involve significant data processing and are, for any number of
	 * reasons unstable. In both of these cases checkpointing allows the
	 * physical job to process some data now, then, at a later date, continue
	 * running. This method can only be called on a Job in the Running state or
	 * an error will occur.
	 * 
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void checkpoint() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * This method is equivalent to calling Checkpoint, Stop, then Submit on
	 * this instance of Job.
	 * 
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void migrate() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * Migrate a job.
	 * <p>
	 * This method is equivalent to calling Checkpoint then Stop on this
	 * instance of Job, then calling Submit on an instance of Job identical to
	 * this instance except that
	 * <ul>
	 * <li>The Job when it first enters the Running state will be in the state
	 * specified by the state information stored during the previous call to the
	 * Checkpoint method.</li>
	 * <li>The Job when it first enters the Running state will be running on a
	 * hardware resource described in the passed HardwareResourceDescription.
	 * </li>
	 * </ul>
	 * 
	 * @param hardwareResourceDescription
	 *            A description of the hardware resource to which the physical
	 *            job corresponding to this Job should be migrated, a
	 *            HardwareResourceDescription
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void migrate(HardwareResourceDescription hardwareResourceDescription)
			throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/**
	 * The Clone operation creates a copy of the GATJob.
	 * 
	 * The resulting GATJob has the same GATSoftwareDescription in its
	 * GATJobDescription, but the GATResourceDescriptions or GATResources of its
	 * GATJobDescription may be altered.
	 * 
	 * This operation upon success completes the following steps:
	 * <LI>Constructs a new GATJobDescription instance with the
	 * GATSoftwareResourceDescription used to construct this GATJob instance.
	 * <LI>Complete the new GATJobDescription instance with the optional
	 * GATHardwareResource passed to the Clone operation.
	 * <LI>If no GATHardwareResource is given, use the
	 * GATHardwareResourceDescription or GATHardwareResource used to construct
	 * the current job.
	 * <LI>Configures the new GATJobDescription instance so that when it begins
	 * running it will have the same state as the state saved in the last call
	 * to checkpoint on this GATJob instance.
	 * <LI>Returns this new GATJob instance to the caller.
	 * 
	 * This operation can only be called on a GATJob instance on which the
	 * operation Checkpoint has been successfully called at least once,
	 * otherwise an error will be issued.
	 * 
	 * @param resource
	 *            HarwareResource to run the job on (null means any resource)
	 * @return the clone of the job
	 */
	public Job cloneJob(HardwareResource resource) {
		throw new Error("Not implemented");
	}

	public MetricValue getMeasurement(Metric metric)
			throws GATInvocationException {
		if (metric.getDefinition().getMeasurementType() == MetricDefinition.DISCRETE) {
			return GATEngine.getMeasurement(this, metric);
		}

		throw new Error("Not implemented");
	}

	public final List getMetricDefinitions() throws GATInvocationException {
		return GATEngine.getMetricDefinitions(this);
	}

	public final MetricDefinition getMetricDefinitionByName(String name)
			throws GATInvocationException {
		return GATEngine.getMetricDefinitionByName(this, name);
	}

	public final void addMetricListener(MetricListener metricListener,
			Metric metric) throws GATInvocationException {
		GATEngine.addMetricListener(this, metricListener, metric);
	}

	public final void removeMetricListener(MetricListener metricListener,
			Metric metric) throws GATInvocationException {
		GATEngine.removeMetricListener(this, metricListener, metric);
	}
}