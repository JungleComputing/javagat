/*
 * Created on Apr 22, 2004
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 * 
 * An instance of this class describes a job to be run. It consists of a
 * description of the "executable" (a GATSoftwareDescription), and of a
 * description of the resource requirements of the job. The latter can be given
 * as either a GATResourceDescription, or as a specific GATResource; only one of
 * these may be specified.
 */
public class JobDescriptionCpi implements JobDescription, java.io.Serializable {

	GATContext gatContext;

	Preferences preferences;

	SoftwareDescription softwareDescription;

	ResourceDescription resourceDescription;

	Resource resource;

	public JobDescriptionCpi(GATContext gatContext, Preferences preferences,
			SoftwareDescription softwareDescription,
			ResourceDescription resourceDescription) {
		this.gatContext = gatContext;
		this.softwareDescription = softwareDescription;
		this.resourceDescription = resourceDescription;
	}

	public JobDescriptionCpi(GATContext gatContext, Preferences preferences,
			SoftwareDescription softwareDescription, Resource resource) {
		this.gatContext = gatContext;
		this.softwareDescription = softwareDescription;
		this.resource = resource;
	}
}