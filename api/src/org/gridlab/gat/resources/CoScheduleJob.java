package org.gridlab.gat.resources;

/**
 * An instance of this class represents a coschedule job.
 * 
 * A coschedule job will be returned by a submitJob invocation with a
 * {@link CoScheduleJobDescription}. The {@link CoScheduleJob} is an object
 * that has multiple {@link Job}s which were coallocated. A single {@link Job}
 * out of this collection can be retrieved using the
 * {@link #getJob(JobDescription)} method.
 * 
 * 
 * @author rkemp
 */
public interface CoScheduleJob extends Job {

    /**
     * Returns a {@link Job} corresponding to the {@link JobDescription}.
     * 
     * @param description
     *                the description of the job
     * @return a {@link Job} corresponding to the {@link JobDescription}, or
     *         <code>null</code> if no {@link Job} corresponds to the
     *         {@link JobDescription}
     */
    public Job getJob(JobDescription description);

}
