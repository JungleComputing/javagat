package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.CoScheduleJob;
import org.gridlab.gat.resources.CoScheduleJobDescription;

public abstract class CoScheduleJobCpi extends JobCpi implements CoScheduleJob {

    protected final CoScheduleJobDescription jobDescription;

    protected CoScheduleJobCpi(GATContext gatContext,
            CoScheduleJobDescription jobDescription) {
        super(gatContext);
        this.jobDescription = jobDescription;
    }
}
