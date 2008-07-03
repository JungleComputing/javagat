package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.CoScheduleJob;
import org.gridlab.gat.resources.CoScheduleJobDescription;

public abstract class CoScheduleJobCpi extends JobCpi implements CoScheduleJob {

    protected CoScheduleJobDescription jobDescription;

    protected CoScheduleJobCpi(GATContext gatContext,
            CoScheduleJobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

}
