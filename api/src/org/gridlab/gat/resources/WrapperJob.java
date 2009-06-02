package org.gridlab.gat.resources;

import org.gridlab.gat.GATInvocationException;

/**
 * An instance of this class represents a wrapper job.
 * 
 * A wrapper job will be returned by a submitJob invocation with a
 * {@link WrapperJobDescription}. The {@link WrapperJob} is a {@link Job}
 * itself and has one or more {@link Job}s which were submitted by this.
 * 
 * A single {@link Job} out of this collection can be retrieved using the
 * {@link #getJob(JobDescription)} method.
 * 
 * 
 * @author rkemp
 */
public interface WrapperJob extends Job {

    /**
     * Returns a {@link Job} corresponding to the {@link JobDescription}.
     * 
     * @param description
     *                the description of the job
     * @return a {@link Job} corresponding to the {@link JobDescription}, or
     *         <code>null</code> if no {@link Job} corresponds to the
     *         {@link JobDescription}
     */
    public Job getJob(JobDescription description) throws GATInvocationException;

    /**
     * Returns a boolean indicating whether all wrapped jobs are finished pre
     * staging. This is a convenience method and iterates over all wrapped jobs
     * to see if they're not in state INITIAL or PRE_STAGING.
     * 
     * @return a boolean indicating whether all wrapped jobs are finished pre
     *         staging.
     * @throws GATInvocationException
     */
    public boolean wrappedJobsFinishedPreStaging()
            throws GATInvocationException;

}
