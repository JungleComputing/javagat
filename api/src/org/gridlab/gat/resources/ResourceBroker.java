package org.gridlab.gat.resources;

import java.io.IOException;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * An instance of this class is used to submit {@link Job}s or to reserve
 * {@link Resource}s. A {@link ResourceBroker} can be created using the factory
 * methods of the {@link GAT} object (for instance
 * GAT.createResourceBroker(gatContext, preferences, brokerURI)). Each
 * {@link ResourceBroker} has a location which can be expressed using a
 * {@link URI}. This {@link URI} should be passed to the factory method.
 * <p>
 * First of all, the {@link ResourceBroker} can be used to submit {@link Job}s.
 * A {@link Job} is described by a {@link JobDescription}. Using the one of the
 * submitJob methods of the {@link ResourceBroker}, one can create a
 * {@link Job} out of the {@link JobDescription}.
 * <p>
 * The {@link ResourceBroker} can also be used to reserve {@link Resource}s. A
 * {@link Resource} can either be a {@link HardwareResource} or a
 * {@link SoftwareResource}. A {@link SoftwareResource} is simply an
 * executable, therefore it makes little sense to reserve such. Thus, an
 * instance of this class can currently only reserve a hardware resource.
 * <p>
 * If one wishes to reserve a {@link HardwareResource}, one must first describe
 * it. This is accomplished by creating an instance of the class
 * {@link HardwareResourceDescription}. After creating such an instance, one
 * must specify the time period for which one wishes to reserve the
 * {@link HardwareResource}. This is accomplished by creating an instance of
 * the class {@link TimePeriod}, which specifies the time period for which one
 * wishes to reserve the {@link HardwareResource}. Finally, one must obtain a
 * reservation for the desired {@link HardwareResource} for the desired
 * {@link TimePeriod}. This is accomplished by calling the method
 * reserveHardwareResource() on an instance of the class {@link ResourceBroker}
 * with the appropriate instance of {@link HardwareResourceDescription} and the
 * appropriate instance of {@link TimePeriod}.
 * <p>
 * In addition, an instance of this class can be used to find
 * {@link HardwareResource}s. This is accomplished by creating an instance of
 * the class {@link HardwareResourceDescription} which describes the
 * {@link HardwareResource} that one wishes to find. After creating such an
 * instance, one must find the corresponding {@link HardwareResource}. This is
 * accomplished by calling the method findHardwareResources() on an instance of
 * the class {@link ResourceBroker} with the appropriate instance of
 * {@link HardwareResourceDescription}.
 */
public interface ResourceBroker {
    /**
     * This method attempts to reserve the specified {@link Resource} for the
     * specified {@link TimePeriod}. Upon reserving the specified
     * {@link Resource} this method returns a {@link Reservation}. Upon failing
     * to reserve the specified resource this method returns an error.
     * 
     * @param resourceDescription
     *                A description, a {@link ResourceDescription}, of the
     *                {@link HardwareResource} to reserve
     * @param timePeriod
     *                The time period, a {@link TimePeriod}, for which to
     *                reserve the {@link HardwareResource}
     * @return the {@link Reservation} object that describes the reserved
     *         {@link Resource}
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     * @throws GATInvocationException
     *                 a remote problem occurred
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) throws GATInvocationException;

    /**
     * This method attempts to reserve the specified {@link Resource} for the
     * specified {@link TimePeriod}. Upon reserving the specified
     * {@link Resource} this method returns a {@link Reservation}. Upon failing
     * to reserve the specified resource this method returns an error.
     * 
     * @param resource
     *                The {@link Resource} to reserve.
     * @param timePeriod
     *                The time period, a {@link TimePeriod}, for which to
     *                reserve the {@link HardwareResource}
     * @return the {@link Reservation} object that describes the reserved
     *         {@link Resource}
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     * @throws GATInvocationException
     *                 a remote problem occurred
     */
    public abstract Reservation reserveResource(Resource resource,
            TimePeriod timePeriod) throws GATInvocationException;

    /**
     * This method attempts to find one or more matching
     * {@link HardwareResource}s. Upon finding the specified
     * {@link HardwareResource}(s) this method returns a {@link java.util.List}
     * of {@link HardwareResource} instances. Upon failing to find the specified
     * {@link HardwareResource} this method returns an error.
     * 
     * @param resourceDescription
     *                A description, a {@link ResourceDescription}, of the
     *                {@link Resource}(s) to find
     * @return {@link java.util.List} of {@link Resource}s upon success
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public abstract List<HardwareResource> findResources(
            ResourceDescription resourceDescription)
            throws GATInvocationException;

    /**
     * This operation takes a {@link JobDescription}, and submits the specified
     * job to some underlying resource management or allocation system. On
     * success, a {@link Job} instance is returned, which represents the job.
     * Upon failing to submit the job, an exception is issued.
     * <p>
     * In former releases some resource broker adaptors would check the
     * "resourcebroker.jobmanager" property to see to which jobmanager (e.g.,
     * fork, pbs, condor) should be used. This has been deprecated. Use the
     * brokerURI which you have to set while creating the {@link ResourceBroker}
     * instead.
     * 
     * @param description
     *                The job description ({@link JobDescription}).
     * @return a {@link Job} object that is a handle to the task that was
     *         submitted
     * @throws IOException
     *                 Upon non-remote IO problem
     * @throws GATInvocationException
     *                 a remote problem occurred
     */
    public Job submitJob(AbstractJobDescription description)
            throws GATInvocationException;

    /**
     * This operation takes a {@link JobDescription}, and submits the specified
     * job to some underlying resource management or allocation system. On
     * success, a {@link Job} instance is returned, which represents the job.
     * Upon failing to submit the job, an exception is issued. The
     * {@link MetricListener} listening to the {@link Metric} defined by the
     * metricDefinitionName is attached to the {@link Job} as soon as it is
     * created. This has the big advantage that {@link Metric}s that are fired
     * <i>during</i> the submitJob can be caught in the provided listener.
     * <p>
     * If one, for instance, would like to listen to the state of a {@link Job},
     * using the "job.status" metricDefinitionName, during the submitJob the
     * {@link Metric} containing the state PRE_STAGING will be fired. This state
     * can only be caught in the listener, if the listener is directly provided
     * with the submitJob invocation.
     * <p>
     * In former releases some resource broker adaptors would check the
     * "resourcebroker.jobmanager" preference to see to which jobmanager (e.g.,
     * fork, pbs, condor) should be used. This has been deprecated. Use the
     * brokerURI which you have to set while creating the {@link ResourceBroker}
     * instead.
     * 
     * @param description
     *                The job description.
     * @param listener
     *                A metric listener that listens to the returned job
     * @param metricDefinitionName
     *                The name of the metric the listener listens to
     * @return a job object that is a handle to the task that was submitted
     * @throws IOException
     *                 Upon non-remote IO problem
     * @throws GATInvocationException
     *                 a remote problem occurred
     */
    public Job submitJob(AbstractJobDescription description,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException;



}
