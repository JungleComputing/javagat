/*
 * Created on Apr 22, 2004
 */
package org.gridlab.gat.resources;

/**
 * @author rob
 */
/** An instance of this class describes a job to be run.
 *
 * It consists of a
 * description of the "executable" (a SoftwareDescription), and of a
 * description of the resource requirements of the job. The latter can be given
 * as either a ResourceDescription, or as a specific Resource; only one of
 * these may be specified.
 */
public class JobDescription implements java.io.Serializable {
    SoftwareDescription softwareDescription;

    ResourceDescription resourceDescription;

    Resource resource;

    /**
     * Create a job description with no resource description.
     * I.e., we don't care where it runs.
     * @param softwareDescription
     */
    public JobDescription(SoftwareDescription softwareDescription) {
        this.softwareDescription = softwareDescription;
    }

    public JobDescription(SoftwareDescription softwareDescription,
            ResourceDescription resourceDescription) {
        this.softwareDescription = softwareDescription;
        this.resourceDescription = resourceDescription;
    }

    public JobDescription(SoftwareDescription softwareDescription,
            Resource resource) {
        this.softwareDescription = softwareDescription;
        this.resource = resource;
    }

    /**
     * @return Returns the resource.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @param resource
     *            The resource to set.
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * @return Returns the resourceDescription.
     */
    public ResourceDescription getResourceDescription() {
        return resourceDescription;
    }

    /**
     * @param resourceDescription
     *            The resourceDescription to set.
     */
    public void setResourceDescription(ResourceDescription resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    /**
     * @return Returns the softwareDescription.
     */
    public SoftwareDescription getSoftwareDescription() {
        return softwareDescription;
    }

    /**
     * @param softwareDescription
     *            The softwareDescription to set.
     */
    public void setSoftwareDescription(SoftwareDescription softwareDescription) {
        this.softwareDescription = softwareDescription;
    }
    
    public String toString() {
        String res = " JobDescription(";

        res += "softwareDescription: " + (softwareDescription == null ?  "null" : softwareDescription.toString());
        res += ", resourceDescription: " + (resourceDescription == null ?  "null" : resourceDescription.toString());
        res += ", resource: " + (resource == null ?  "null" : resource.toString());
        
        res += ")";
        
        return res;
    }
}
