package org.gridlab.gat.resources;

import org.gridlab.gat.GATInvocationException;

public interface WrapperJob extends Job {

    public Job getJob(JobDescription description) throws GATInvocationException;

}
