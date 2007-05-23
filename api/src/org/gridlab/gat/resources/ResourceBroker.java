package org.gridlab.gat.resources;

import java.io.IOException;
import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.TimePeriod;

/**
 * An instance of this class is used to reserve resources or to submit jobs.
 * <p>
 * A resource can either be a hardware resource or a software resource. A
 * software resource is simply an executable it makes little sense to reserve
 * such. Thus an instance of this class can currently only reserve a hardware
 * resource.
 * <p>
 * If one wishes to reserve a hardware resource, one must first describe it.
 * This is accomplished by
 * creating an instance of the class HardwareResourceDescription. After creating such an
 * instance, one must specify the time period for which
 * one wishes to reserve the hardware resource. This is accomplished by creating
 * an instance of the class TimePeriod which specifies the time period for which
 * one wishes to reserve the hardware resource. Finally, one must obtain a
 * reservation for the desired hardware resource for the desired time period.
 * This is accomplished by calling the method reserveHardwareResource() on an
 * instance of the class ResourceBroker with the appropriate instance of
 * HardwareResourceDescription and the appropriate instance of TimePeriod.
 * <p>
 * In addition, an instance of this class can be used to find hardware resources.
 * This is
 * accomplished by creating an instance of the class HardwareResourceDescription
 * which describes the hardware resource that one wishes to find. After creating
 * such an instance, one must find the corresponding
 * hardware resource. This is accomplished by calling the method
 * findHardwareResources() on an instance of the class ResourceBroker with the
 * appropriate instance of HardwareResourceDescription.
 * <p>
 * Finally, jobs can be submitted using this class. To do this, a JobDescription
 * must fist be created. Next, submitJob is called with this description.
 */
public interface ResourceBroker {
    /**
     * This method attempts to reserve the specified resource for the specified
     * time period. Upon reserving the specified resource this method returns a
     * Reservation. Upon failing to reserve the specified resource this method
     * returns an error.
     *
     * @param resourceDescription
     *            A description, a ResourceDescription, of the hardware resource
     *            to reserve
     * @param timePeriod
     *            The time period, a TimePeriod , for which to reserve the
     *            hardware resource Thrown upon problems accessing the remote
     *            instance
     * @return the reservation object that describes the reserved resource
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     * @throws GATInvocationException
     *             a remote problem occurred
     */
    public abstract Reservation reserveResource(
            ResourceDescription resourceDescription, TimePeriod timePeriod)
            throws GATInvocationException;

    /**
     * This method attempts to reserve the specified resource for the specified
     * time period. Upon reserving the specified resource this method returns a
     * Reservation. Upon failing to reserve the specified resource this method
     * returns an error.
     *
     * @param resource
     *            The resource to reserve.
     * @param timePeriod
     *            The time period, a TimePeriod , for which to reserve the
     *            hardware resource
     * @return the reservation object that describes the reserved resource
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     * @throws GATInvocationException
     *             a remote problem occurred
     */
    public abstract Reservation reserveResource(Resource resource,
            TimePeriod timePeriod) throws GATInvocationException;

    /**
     * This method attempts to find one or more matching hardware resources.
     * Upon finding the specified hardware resource(s) this method returns a
     * java.util.List of HardwareResource instances. Upon failing to find the
     * specified hardware resource this method returns an error.
     *
     * @param resourceDescription
     *            A description, a ResoucreDescription, of the resource(s) to
     *            find
     * @return java.util.List of Resources upon success
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     */
    public abstract List findResources(ResourceDescription resourceDescription)
            throws GATInvocationException;

    /**
     * This operation takes a JobDescription, and submits the specified job to
     * some underlying resource management or allocation system. On success, a
     * GATJob instance is returned, which represents the job. Upon failing to
     * submit the job, an error is issued.
     *
     * Some resource broker adaptors will check the "ResourceBroker.jobmanager"
     * property to see to which jobmanager (e.g., fork, pbs, condor) should be
     * used.
     *
     * @param description
     *            The job description.
     * @return a job object that is a handle to the task that was submitted
     * @throws IOException
     *             Upon non-remote IO problem
     * @throws GATInvocationException
     *             a remote problem occurred
     */
    public abstract Job submitJob(JobDescription description)
            throws GATInvocationException;
}
