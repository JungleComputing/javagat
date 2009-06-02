package org.gridlab.gat.resources;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class describes an coschedule job to be run.
 * 
 * A coschedule job consists of multiple jobs that should be started together.
 * Some adaptors, like the Koala adaptor, can handle
 * {@link CoScheduleJobDescription}s.
 * 
 * @author rkemp
 */
public class CoScheduleJobDescription extends AbstractJobDescription {

    /**
     * 
     */
    private static final long serialVersionUID = -6670271235157948175L;

    private List<JobDescription> jobDescriptions = new ArrayList<JobDescription>();

    /**
     * Creates a {@link CoScheduleJobDescription} with one
     * {@link JobDescription} of a job that will be included in the
     * coscheduling.
     * 
     * @param jobDescription
     *                the description of the job that will be included in the
     *                coscheduling.
     */
    public CoScheduleJobDescription(JobDescription jobDescription) {
        add(jobDescription);
    }

    /**
     * Creates a {@link CoScheduleJobDescription} with a set of
     * {@link JobDescription}s of a jobs that will be included in the
     * coscheduling.
     * 
     * @param jobDescriptions
     *                the descriptions of the jobs that will be included in the
     *                coscheduling.
     */
    public CoScheduleJobDescription(JobDescription[] jobDescriptions) {
        add(jobDescriptions);
    }

    /**
     * Adds a single {@link JobDescription} to the set of {@link JobDescription}s
     * that will be included in the coscheduling.
     * 
     * @param jobDescription
     *                the {@link JobDescription} to be included in the
     *                coscheduling
     */
    public void add(JobDescription jobDescription) {
        jobDescriptions.add(jobDescription);
    }

    /**
     * Adds a set of {@link JobDescription}s to the set of
     * {@link JobDescription}s that will be included in the coscheduling.
     * 
     * @param jobDescriptions
     *                the {@link JobDescription}s to be included in the
     *                coscheduling
     */
    public void add(JobDescription[] jobDescriptions) {
        for (JobDescription jobDescription : jobDescriptions) {
            this.jobDescriptions.add(jobDescription);
        }
    }

    /**
     * Returns the set of {@link JobDescription}s that will be included in the
     * coscheduling.
     * 
     * @return the set of {@link JobDescription}s that will be included in the
     *         coscheduling.
     */
    public List<JobDescription> getJobDescriptions() {
        return jobDescriptions;
    }

}
