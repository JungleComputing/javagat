package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.SoftwareResourceDescription;

/**
 * Capability provider interface to the SimpleJob class.
 * <p>
 * Capability provider wishing to provide the functionality of the SimpleJob
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this SimpleJob class and will be used to implement the
 * corresponding method in the SimpleJob class at runtime.
 */
public abstract class JobCpi extends Job {

	GATContext gatContext;

	Preferences preferences;

	SoftwareResourceDescription softwareResourceDescription;

	/**
	 * Constructs a SimpleJobCpi instance corresponding to the passed
	 * SoftwareResourceDescription and GATContext
	 * 
	 * @param gatContext
	 *            A GATContext used to broker resources
	 * @param softwareResourceDescription
	 *            A SoftwareResourceDescription describing the simple job's
	 *            executable
	 */
	public JobCpi(GATContext gatContext, Preferences preferences,
			SoftwareResourceDescription softwareResourceDescription) {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.softwareResourceDescription = softwareResourceDescription;
	}
}