package org.gridlab.gat.resources.cpi.gt4;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;

class GT4StatusListener implements StatusListener {
    Task task;
    GT4Job job;
    public GT4StatusListener(Task t, GT4Job j) {
	task = t;
	job = j;
    }
    public void statusChanged(StatusEvent event) {
	Status status = event.getStatus();
	switch(status.getStatusCode()) {
	case Status.ACTIVE: 
	    job.setState(job.RUNNING);
	    break;
	case Status.CANCELED:
	    job.setState(job.STOPPED);
	    break;
	case Status.COMPLETED:
	    job.setState(job.STOPPED);
	case Status.FAILED:
	    job.setState(job.SUBMISSION_ERROR);
	    break;
	case Status.RESUMED:
	    job.setState(job.RUNNING);
	    break;
	case Status.SUBMITTED:
	    job.setState(job.SCHEDULED);
	    break;
	case Status.SUSPENDED:
	    job.setState(job.ON_HOLD);
	    break;
	case Status.UNKNOWN:
	    job.setState(job.UNKNOWN);
	    break;
	case Status.UNSUBMITTED:
	    job.setState(job.UNKNOWN);
	    break;
	default:
	    job.setState(job.UNKNOWN);
	}
    }
}

public class GT4Job extends JobCpi {
    GT4ResourceBrokerAdaptor broker;
    Task task;
    public GT4Job(GATContext gatContext, Preferences preferences,
		  JobDescription jobDescription,
		  Sandbox sandbox,
		  JobSpecification spec,
		  Service service) 
	throws GATInvocationException {
        super(gatContext, preferences, jobDescription, sandbox);
	task = new TaskImpl("gatgt4job", Task.JOB_SUBMISSION);
	// Maybe setProvider is not necessary
	task.setProvider("GT4.0.0");
	task.setSpecification(spec);
	task.setService(Service.JOB_SUBMISSION_SERVICE, service);
	TaskHandler handler = new ExecutionTaskHandler();
	GT4StatusListener listener = new GT4StatusListener(task, this);
	task.addStatusListener(listener);
	try {
	    handler.submit(task);
	} catch(Exception e) {
	    throw new GATInvocationException("GT4Job: " + e);
	}
    }
    protected synchronized void setState(int s) {
	state = s;
    }
}

