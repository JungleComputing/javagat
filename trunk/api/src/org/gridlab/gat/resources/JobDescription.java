/*
 * Created on Apr 22, 2004
 */
package org.gridlab.gat.resources;

/**
 * @author rob
 */
/**
 * An instance of this class describes a job to be run.
 * 
 * It consists of a description of the "executable" (a
 * {@link SoftwareDescription}), and of a description of the resource
 * requirements of the job. The latter can be given as either a
 * {@link ResourceDescription}, or as a specific {@link Resource}; only one of
 * these may be specified.
 */
@SuppressWarnings("serial")
public class JobDescription extends AbstractJobDescription {

    private int processCount = 1;

    private int resourceCount = 1;

    SoftwareDescription softwareDescription;

    ResourceDescription resourceDescription;

    Resource resource;

    /**
     * Create a job description with no resource description. I.e., we don't
     * care where it runs, the {@link ResourceBroker} will choose the location.
     * 
     * @param softwareDescription
     *                the {@link SoftwareDescription} of the executable.
     */
    public JobDescription(SoftwareDescription softwareDescription) {
        this.softwareDescription = softwareDescription;
    }

    /**
     * Create a {@link JobDescription} out of a {@link SoftwareDescription} and
     * a {@link ResourceDescription}.
     * 
     * @param softwareDescription
     *                the {@link SoftwareDescription} of the executable.
     * @param resourceDescription
     *                the {@link ResourceDescription} of the resource where the
     *                executable should run.
     */
    public JobDescription(SoftwareDescription softwareDescription,
            ResourceDescription resourceDescription) {
        this.softwareDescription = softwareDescription;
        this.resourceDescription = resourceDescription;
    }

    /**
     * Create a {@link JobDescription} out of a {@link SoftwareDescription} and
     * a {@link Resource}.
     * 
     * @param softwareDescription
     *                the {@link SoftwareDescription} of the executable.
     * @param resource
     *                the Resource where the executable should run on.
     */
    public JobDescription(SoftwareDescription softwareDescription,
            Resource resource) {
        this.softwareDescription = softwareDescription;
        this.resource = resource;
    }

    /**
     * Returns the {@link Resource} associated with this {@link JobDescription}.
     * 
     * @return the {@link Resource} associated with this {@link JobDescription}.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Set the {@link Resource} associated with this {@link JobDescription}.
     * 
     * @param resource
     *                The {@link Resource} to set.
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Returns the {@link ResourceDescription} associated with this
     * {@link JobDescription}.
     * 
     * @return the associated {@link ResourceDescription}.
     */
    public ResourceDescription getResourceDescription() {
        return resourceDescription;
    }

    /**
     * Set the {@link ResourceDescription} associated with this
     * {@link JobDescription}.
     * 
     * @param resourceDescription
     *                The {@link ResourceDescription} to set.
     */
    public void setResourceDescription(ResourceDescription resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    /**
     * Returns the {@link SoftwareDescription} associated with this
     * {@link JobDescription}.
     * 
     * @return the associated {@link SoftwareDescription}.
     */
    public SoftwareDescription getSoftwareDescription() {
        return softwareDescription;
    }

    /**
     * Set the {@link SoftwareDescription} associated with this
     * {@link JobDescription}.
     * 
     * @param softwareDescription
     *                The {@link SoftwareDescription} to set.
     */
    public void setSoftwareDescription(SoftwareDescription softwareDescription) {
        this.softwareDescription = softwareDescription;
    }

    public String toString() {
        String res = " JobDescription(";

        res += "softwareDescription: "
                + (softwareDescription == null ? "null" : softwareDescription
                        .toString());
        res += ", resourceDescription: "
                + (resourceDescription == null ? "null" : resourceDescription
                        .toString());
        res += ", resource: "
                + (resource == null ? "null" : resource.toString());

        res += ")";

        return res;
    }

    public Object clone() {
        JobDescription description = new JobDescription(
                (SoftwareDescription) softwareDescription.clone());
        if (resource != null) {
            description.setResource(resource);
        }
        if (resourceDescription != null) {
            description.setResourceDescription(resourceDescription);
        }
        return description;
    }

    /**
     * Get the number of processes, which is the total number of times the
     * executable should be run.
     * 
     * @return the number of processes
     */
    public int getProcessCount() {
        return processCount;
    }

    /**
     * Set the number of processes, which is the total number of times the
     * executable should be run.
     * 
     * @param processCount
     *                the number of processes
     */
    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    /**
     * Get the number of resources, which is the total number of resources where
     * the number of processes should be distributed on.
     * 
     * @return the number of resources
     */
    public int getResourceCount() {
        return resourceCount;
    }

    /**
     * Set the number of resources, which is the total number of resources where
     * the number of processes should be distributed on.
     * 
     * @param resourceCount
     *                the number of resources
     */
    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

}
