package org.gridlab.gat.resources;

import java.util.ArrayList;
import java.util.List;

public class CoScheduleJobDescription extends AbstractJobDescription {

    /**
     * 
     */
    private static final long serialVersionUID = -6670271235157948175L;

    private List<JobDescription> jobDescriptions = new ArrayList<JobDescription>();

    public CoScheduleJobDescription(JobDescription jobDescription) {
        add(jobDescription);
    }

    public CoScheduleJobDescription(JobDescription[] jobDescriptions) {
        add(jobDescriptions);
    }

    public void add(JobDescription jobDescription) {
        jobDescriptions.add(jobDescription);
    }

    public void add(JobDescription[] jobDescriptions) {
        for (JobDescription jobDescription : jobDescriptions) {
            this.jobDescriptions.add(jobDescription);
        }
    }
    
    public List<JobDescription> getJobDescriptions() {
        return jobDescriptions;
    }

}
