/*
 * Created on Apr 22, 2004
 */
package org.gridlab.gat.resources;

/**
 * @author rob
 * 
 * An instance of this class describes a job to be run. It consists of a
 * description of the "executable" (a GATSoftwareDescription), and of a
 * description of the resource requirements of the job. The latter can be given
 * as either a GATResourceDescription, or as a specific GATResource; only one of
 * these may be specified.
 */
public class JobDescription implements java.io.Serializable {

	SoftwareDescription softwareDescription;

	ResourceDescription resourceDescription;

	Resource resource;

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
}