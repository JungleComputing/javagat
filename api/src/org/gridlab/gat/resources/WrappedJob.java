package org.gridlab.gat.resources;

/**
 * An instance of this class represents a wrapped job.
 * <p>
 * A {@link WrappedJob} is a {@link Job} object that reflects to another
 * {@link Job} object that in turn reflects to the real job. A
 * {@link WrappedJob} will be created if you submit a {@link JobDescription}
 * with a {@link SoftwareDescription} that contains this attribute:
 * "wrapper.enable", "true".
 * <p>
 * Instead of creating a normal {@link Job} out of the {@link JobDescription},
 * the {@link ResourceBroker} creates a Job that executes a JavaGAT application,
 * the Wrapper. When the Wrapper gets executed, it creates a new
 * {@link ResourceBroker} and it submits the job described by the
 * {@link JobDescription}. The {@link WrappedJob} has a one-to-one relationship
 * with the {@link Job} object held by the Wrapper. All state changes that are
 * received by the Wrapper application are forwarded to the {@link WrappedJob}.
 * <p>
 * There are two main reasons for using the Wrapper instead of normal job
 * submission. First of all the Wrapper pulls its prestage files and pushes its
 * poststage files, where in the normal case without the wrapper the prestage
 * files are pushed and the poststage files are pulled. The advantage of having
 * the Wrapper doing the staging is that the Wrapper knows on which node it's
 * executing. Therefore it doesn't necessarily have to put the files on a shared
 * file system, it can also use the local file system. Typically nodes have a
 * local scratch directory available on their local file system with a much
 * larger size than the size of the user's home directory on the shared file
 * system. Another advantage of using the local file system, is that access
 * times are much smaller.
 * </p>
 * <p>
 * The second reason of using a Wrapper instead of a normal Job is that the
 * {@link ResourceBroker} that is used by the Wrapper can have more features
 * than the one used to submit the Wrapper itself. For instance a globus
 * {@link ResourceBroker} might not be able to start multiple different jobs on
 * a single node, but a local {@link ResourceBroker} can do this. Then
 * submitting the Wrapper using the globus {@link ResourceBroker} will result in
 * the Wrapper starting on a node of a cluster. Then the Wrapper itself submits
 * multiple different jobs to this node using the local {@link ResourceBroker}.
 * </p>
 * <p>
 * The drawback of using the Wrapper is it's dependency on JavaGAT. JavaGAT
 * needs to be available at the remote side. Either it has to be preinstalled
 * (and the {@link SoftwareDescription} should contain an attribute
 * "wrapper.remotegat.location", "path/to/location") or it is staged during the
 * submission of the Wrapper (and thereby causes some overhead).
 * </p>
 * <p>
 * The Wrapper communicates with the {@link WrappedJob} by writing the state
 * changes of the real job in a File on the submission host. The name of this
 * file is constructed as follows: ".JavaGATstatusXYZ", where XYZ is the job id.
 * </p>
 */
public interface WrappedJob {

    /**
     * Gets the {@link Job} associated with the Wrapper application.
     * 
     * The Wrapper application has a {@link Job} associated. This method will
     * return this {@link Job}. Note that invoking the stop method on this
     * method will cause the {@link WrappedJob} also to stop, but the state
     * change of the {@link WrappedJob} can't be sent, because the Wrapper is
     * already finished.
     * 
     * @return the wrapper job
     */
    public Job getWrapperJob();
}
