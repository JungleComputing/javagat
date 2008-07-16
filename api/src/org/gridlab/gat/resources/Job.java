package org.gridlab.gat.resources;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An instance of this class represents a job.
 * <p>
 * A job is the entire process of (1) getting an application to execute on some
 * location, (2) the actual execution of the application and (3) the cleanup
 * after the execution.
 * <p>
 * A job will be constructed by a {@link ResourceBroker} according to a
 * description of it, the {@link JobDescription}.
 * <p>
 * A job object always has an associated state and can be polled for this state.
 * Examples of state sequences are:
 * <ul>
 * <li>INITIAL, PRE_STAGING, SCHEDULED, RUNNING, POST_STAGING, STOPPED</li>
 * <li>INITIAL, RUNNING, ON_HOLD, RUNNING, STOPPED</li>
 * <li>PRE_STAGING, SUBMISSION_ERROR</li>
 * </ul>
 * The following relations are true for the state order:
 * <ul>
 * <li>PRE_STAGING < RUNNING </li>
 * <li>SCHEDULED < RUNNING </li>
 * <li>RUNNING < POST_STAGING</li>
 * <li>POST_STAGING < STOPPED</li>
 * <li>POST_STAGING < SUBMISSION_ERROR</li>
 * </ul>
 * It depends on the implementation of the Job in the adaptor which states are
 * supported for that adaptor. For instance a local job might never be in the
 * state SCHEDULED.
 * <p>
 * It's also possible to add a listener to a Job object which can listen to a
 * metric that the job provides. Most Jobs provide at least the metric
 * "job.status", which will indicate state changes. Note that it is possible to
 * receive a state twice, or that some states may never occur.
 * <p>
 * Furthermore, if you want to listen to metric that's firing metrics during the
 * creation of the Job object (which is done by the {@link ResourceBroker}),
 * you can add the listener and the metric name to the submitJob method in the
 * {@link ResourceBroker}.
 * <p>
 * Note that adaptors might not implement the whole functionality.
 */
public interface Job extends Monitorable, Advertisable {

    /**
     * An instance of this enumeration indicates the state of a {@link Job}.
     * 
     * @author rkemp
     */
    public enum JobState {
        /**
         * Initial state indicator.
         * 
         * The {@link Job} has been constructed.
         */
        INITIAL,
        /**
         * Scheduled state indicator.
         * 
         * The {@link Job} has been submitted to a resource broker and is
         * scheduled to be executed.
         */
        SCHEDULED,
        /**
         * Running state indicator.
         * 
         * The {@link Job} is executing.
         */
        RUNNING,
        /**
         * Stopped state indicator.
         * 
         * The {@link Job} has properly run. All the cleanup and administration
         * of the {@link Job} is completely done.
         */
        STOPPED,
        /**
         * Submission error state indicator.
         * 
         * The {@link Job} hasn't properly run. All the cleanup and
         * administration of the {@link Job} is completely done.
         */
        SUBMISSION_ERROR,
        /**
         * The {@link Job} has been paused.
         * 
         * The {@link Job} has been paused by the user.
         */
        ON_HOLD,
        /**
         * The input files of the {@link Job} are being pre staged.
         */
        PRE_STAGING,
        /**
         * The output files of the {@link Job} are being post staged.
         */
        POST_STAGING,
        /**
         * The {@link Job} state is unknown for some reason. It might be a
         * network problem.
         */
        UNKNOWN
    };

    /**
     * Returns the {@link AbstractJobDescription} that was used to create this
     * Job.
     * 
     * @return the JobDescription that belongs to this Job
     */
    public AbstractJobDescription getJobDescription();

    /**
     * Stops the associated physical job. Upon a successful call to this method
     * the associated physical job is forcibly terminated. This method can only
     * be called on a job in the SCHEDULED or RUNNING state.
     * 
     * 
     * @deprecated Deprecated, because there is a race condition here. The job
     *             state can change between the call to getState and the call to
     *             stop/unSchedule. Use stop instead.
     * 
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void unSchedule() throws GATInvocationException;

    /**
     * Stops the associated physical job. Upon a successful call to this method
     * the associated physical job is forcibly terminated. This method can be
     * called in any state. Note that the termination of jobs may take some
     * time.
     * 
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void stop() throws GATInvocationException;

    /**
     * This method returns the state of the Job. This is one of the associated
     * public member variables INITIAL, SCHEDULED, RUNNING, STOPPED, ON_HOLD,
     * PRE_STAGING, POST_STAGING, UNKNOWN or SUBMISSSION_ERROR.
     * 
     * @return This method returns the state of the associated Job
     */
    public JobState getState();

    /**
     * This method returns an instance of the class {@link java.util.Map} which
     * contains information about the associated Job. This {@link java.util.Map}
     * contains a set of key/value pairs the key, a {@link java.lang.String},
     * being the name of the information and the value being the value of the
     * associated named information. The minimum set of keys which the returned
     * {@link java.util.Map} contains is as follows:
     * <ul>
     * <li><em>state</em></li>
     * <li><em>hostname</em></li>
     * <li><em>submissiontime</em></li>
     * <li><em>starttime</em></li>
     * <li><em>stoptime</em></li>
     * <li><em>poststage.exception</em></li>
     * </ul>
     * <p>
     * <em>state</em> The key state corresponds to a {@link java.lang.String}
     * value which is the name of the state the job is in. The state strings are
     * literally the same as the name of the constants used, e.g., "RUNNING", or
     * "STOPPED".
     * <p>
     * <em>hostname</em> The key hostname corresponds to a
     * {@link java.lang.String} value which is the name of the host on which the
     * physical job is running, if the job is in the RUNNING state. If the
     * associated job is not in the RUNNING state, the value is null.
     * <p>
     * <em>submissiontime</em> The key submissiontime corresponds to a long
     * value which is the number of milliseconds after January 1, 1970, 00:00:00
     * GMT when the associated physical job was submitted. This value is null
     * for a job in the INITIAL state otherwise it is not null.
     * <p>
     * <em>starttime</em> The key starttime corresponds to a long value which
     * is the number of milliseconds after January 1, 1970, 00:00:00 GMT when
     * the associated physical job was started. This value is null for a job in
     * the SCHEDULED or INITIAL states otherwise it is not null.
     * <p>
     * <em>stoptime</em> The key stoptime corresponds to a long value which is
     * the number of milliseconds after January 1, 1970, 00:00:00 GMT when the
     * associated physical job stopped. This value is not null for a job in the
     * STOPPED state otherwise it is null.
     * <p>
     * <em>poststage.exception</em> This key is present in the map if the
     * application did run, but one or more files could not be post staged. The
     * data value attached to this key is the exception that occurred while post
     * staging.
     * <p>
     * Other key/value pairs will be in future added to the list of key/value
     * pairs returned in this {@link java.util.Map} as the need develops.
     * 
     * @return An instance of the class {@link java.util.Map} which presents
     *         information about the associated job.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public Map<String, Object> getInfo() throws GATInvocationException;

    /**
     * This method returns the job id, a globally unique identifier for the
     * physical job corresponding to this instance. Some adaptors provide an
     * adaptor specific job id. This can be retrieved by getting the value
     * belonging to the key 'adaptor.job.id' in the Map returned by
     * {@link #getInfo()}.
     * 
     * @return An integer which represents the job ID
     */
    public int getJobID();

    /**
     * @deprecated Deprecated, this method doesn't fit in the API anymore
     * 
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void checkpoint() throws GATInvocationException;

    /**
     * @deprecated Deprecated, this method doesn't fit in the API anymore
     * 
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public void migrate() throws GATInvocationException;

    /**
     * @deprecated Deprecated, this method doesn't fit in the API anymore
     * 
     * @param hardwareResourceDescription
     *                A description of the hardware resource to which the
     *                physical job corresponding to this {@link Job} should be
     *                migrated, a {@link HardwareResourceDescription}
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void migrate(HardwareResourceDescription hardwareResourceDescription)
            throws GATInvocationException;

    /**
     * @deprecated Deprecated, this method doesn't fit in the API anymore
     * 
     * @param resource
     *                HarwareResource to run the job on (null means any
     *                resource)
     * @return the clone of the job
     */
    public Job cloneJob(HardwareResource resource);

    /**
     * Put a job on hold, pause it. This can be called in SCHEDULED or RUNNING
     * state.
     */
    public void hold() throws GATInvocationException;

    /**
     * Resume a job that was paused with the "hold" method. This can be called
     * only in the ON_HOLD state.
     */
    public void resume() throws GATInvocationException;

    /**
     * Returns the exit status of a job.
     * 
     * @return the exit status of a job.
     */
    public int getExitStatus() throws GATInvocationException;

    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException;

    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException;

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException;

    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException;

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException;

    public String toString();

    public InputStream getStdout() throws GATInvocationException;

    public InputStream getStderr() throws GATInvocationException;

    public OutputStream getStdin() throws GATInvocationException;
}
