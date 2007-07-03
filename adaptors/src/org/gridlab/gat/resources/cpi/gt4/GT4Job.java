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
/**
 * Implements the JavaCog <code>StatusListener</code> interface.
 * It listens a submitted job, and is notified at status changes.
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
class GT4StatusListener implements StatusListener {
    Task task;
    GT4Job job;
    /**
     * Initializes the <code>GT4StatusListener</code> object.
     * @param j a <code>GT4Job</code> object. The state of the job
     * changes every time when the submitted job state is changed.
     */
    public GT4StatusListener(GT4Job j) {
	job = j;
    }

    /**
     * This callback method is invoked when the submitted job's state is
     * changed. The matches between the JavaCog states and JavaGAT states are
     * arbitrary.
     * @param event the passed <code>StatusEvent</code> has the <code>State</code>
     * object.
     */
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
	    try {
		job.stop();
	    } catch(GATInvocationException e) {
		//fix it
	    }
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

/**
 * Implements JobCpi abstract class. Wrappers a JavaCog task.
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
public class GT4Job extends JobCpi {
    GT4ResourceBrokerAdaptor broker;
    Task task;
    /**
     * Initializes a job. Creates a task, sets up the listener
     * and submits it.
     */
    public GT4Job(GATContext gatContext, Preferences preferences,
		  JobDescription jobDescription,
		  Sandbox sandbox,
		  JobSpecification spec,
		  Service service) 
	throws GATInvocationException {
        super(gatContext, preferences, jobDescription, sandbox);
	task = new TaskImpl("gatgt4jobtest", Task.JOB_SUBMISSION);
	// Maybe setProvider is not necessary
	task.setProvider("GT4.0.0");
	task.setSpecification(spec);
	task.setService(Service.JOB_SUBMISSION_SERVICE, service);
	TaskHandler handler = new ExecutionTaskHandler();
	GT4StatusListener listener = new GT4StatusListener(this);
	task.addStatusListener(listener);
	try {
	    handler.submit(task);
	} catch(IllegalSpecException e) {
	    throw new GATInvocationException("GT4Job illegal spec: " + e);
	} catch(InvalidSecurityContextException e) {
	    throw new GATInvocationException("GT4Job invalid security: " + e);
	} catch(InvalidServiceContactException e) {
	    throw new GATInvocationException("GT4Job invalid service: " + e);
	} catch(TaskSubmissionException e) {
	    throw new GATInvocationException("GT4Job task submission: " + e);
	}

    }
    /**
     * The <code>GT4StatusListener</code> calls this function to set the
     * state of the job.
     * @param state
     */
    
    public void stop() throws GATInvocationException {
	System.out.println("done");
	sandbox.retrieveAndCleanup(this);
    }

    protected synchronized void setState(int state) {
	this.state = state;
    }
}

