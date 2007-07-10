package org.gridlab.gat.resources;

import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An instance of this class represents a job.
 * <p>
 * A job is what one normally considers when referring to starting a program.
 * Thus, a job is primarily described by the software which is to execute during
 * this job. The description of the software which is to execute during this job
 * is given by an instance of the class SoftwareResourceDescription.
 * <p>
 * Upon creating an instance of the class the associated physical job is not
 * immediately running. An instance of the class has various states which it can
 * be in, only one of which maps to a running physical job.
 * <p>
 * In addition, a Job allows one or more Metrics to be monitored.
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
    
    /** The job has been paused */
    public static final int ON_HOLD = 5;

    /** The input files of the job are being pre-staged */
    public static final int PRE_STAGING = 6;

    /** The output files of the job are being post-staged */
    public static final int POST_STAGING = 7;
    
    /** The job state is unkown for some reason. May be a network problem. */
    public static final int UNKNOWN = 8;

    public static String getStateString(int state) {
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
        case ON_HOLD:
            return "ON_HOLD";
        case PRE_STAGING:
            return "PRE_STAGING";
        case POST_STAGING:
            return "POST_STAGING";
        case UNKNOWN:
            return "UNKNOWN";
        default:
            throw new RuntimeException("unknown job state in getStateString");
        }
    }

    public abstract JobDescription getJobDescription();

    /**
     * Stops the associated physical job. Upon a successful call to this method
     * the associated physical job is forcibly terminated. This method can only
     * be called on a job in the SCHEDULED or RUNNING state.
     * 
     *
     * @deprecated Deprecated, because there is a race condition here. 
     * The job state can change between the call to getState and the call to 
     * stop/unSchedule. Use stop instead.
     * 
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     */
    public final void unSchedule() throws GATInvocationException {
        stop();
    }

    /**
     * Stops the associated physical job. Upon a successful call to this method
     * the associated physical job is forcibly terminated. This method can be called in 
     * any state.
     *
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     */
    public void stop() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * This method returns the state of the associated . This is one of the
     * associated public member variables INITIAL, SCHEDULED,
     * RUNNING, STOPPED, UNKNOWN or SUBMISSSION_ERROR.
     *
     * @return This method returns the state of the associated , one of the
     *         associated member variables
     */
    public abstract int getState();

    /**
     * This method returns an instance of the class java.util.Map which contains
     * information about the associated . This java.util.Map contains a set of
     * key/value pairs the key, a java.lang.String, being the name of the
     * information and the value being the value of the associated named
     * information. The minimum set of keys which the returned java.util.Map
     * contains is as follows:
     * <ul>
     * <li><em>state</em></li>
     * <li><em>hostname</em></li>
     * <li><em>submissiontime</em></li>
     * <li><em>starttime</em></li>
     * <li><em>stoptime</em></li>
     * </ul>
     * <p>
     * <em>state</em> The key state corresponds to a java.lang.String
     * value which is the name of the state the job is in. The state strings
     * are literally the same as the name of the constants used,
     * e.g., "RUNNING", or "STOPPED".
     * <p>
     * <em>hostname</em> The key hostname corresponds to a java.lang.String
     * value which is the name of the host on which the physical job is running,
     * if the job is in the Running state. If the associated job is not in the
     * Running state, the value is null.
     * <p>
     * <em>submissiontime</em> The key submissiontime corresponds to a long
     * value which is the number of milliseconds after January 1, 1970, 00:00:00
     * GMT when the associated physical job was submitted. This value is null
     * for a job in the Constructed state otherwise it is not null.
     * <p>
     * <em>starttime</em> The key starttime corresponds to a long value which
     * is the number of milliseconds after January 1, 1970, 00:00:00 GMT when
     * the associated physical job was started. This value is null for a job in
     * the Submitted or Constructed states otherwise it is not null.
     * <p>
     * <em>stoptime</em>
     * The key stoptime corresponds to a long value which is the
     * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
     * associated physical job stopped. This value is not null for a
     * job in the Stopped state otherwise it is null.
     * <p>
     * <em>postStageError</em>
     * This key is present in the map if the application did run, but one or
     * more files could not be post staged.
     * The data value attached to this key is the exception that occurred while 
     * poststageing.
     *
     * Other key/value pairs will be in future added to the list of key/value
     * pairs returned in this java.util.Map as the need develops.
     *
     * @return An instance of the class java.util.Map which presents
     * information about the associated job.
     * @throws GATInvocationException Thrown upon problems
     * accessing the remote instance
     */
    public Map getInfo() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
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
     */
    public String getJobID() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
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
     */
    public void checkpoint() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
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
    public void migrate() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
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
     */
    public void migrate(HardwareResourceDescription hardwareResourceDescription)
            throws GATInvocationException {
        throw new RuntimeException("Not implemented");
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
        throw new RuntimeException("Not implemented");
    }

    /** Put a job on hold, pause it.
     * This can be called in SCHEDULED or RUNNING state.
     */
    public void hold() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
    }
    
    /** Resume a job that was paused with the "hold" method.
     * This can be called only in the ON_HOLD state. 
     */
    public void resume() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
    }
    
    /** Returns the exit status of a job. 
     * @return the exit status of a job.
     */
    public int getExitStatus() throws GATInvocationException {
        throw new RuntimeException("Not implemented");
    }
    
    public MetricValue getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new RuntimeException("Not implemented");
    }

    public List getMetricDefinitions() throws GATInvocationException {
        throw new RuntimeException("Not implemented");    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        throw new RuntimeException("Not implemented");    }

    public void addMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new RuntimeException("Not implemented");    }

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new RuntimeException("Not implemented");    }
    
    public String toString() {
        String res = "gat job";

        String id = null;
        try {
            id = getJobID();
        } catch (Exception e) {
            // ignore
        }
        if(id != null) res += ", id is " + id;
        else {
            res += ", " + "not initialized";
        }

        return res;
    }    
}
