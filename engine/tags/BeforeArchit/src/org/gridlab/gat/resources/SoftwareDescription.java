/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.net.URI;
import java.util.Map;

import org.gridlab.gat.io.File;

/**
 * @author rob
 * 
 * An instance of this class is a description of a piece of software (component)
 * which is to be submitted as a job. It currently takes a table describing this
 * piece of software's attributes to any underlying job submission system.
 * 
 * The GAT-API defines a minimum set of supported name/value pairs to be
 * included in the Map used to construct a SoftwareDescription instance, as
 * listed in table below.
 * 
 * Name Type Description location Location Software location arguments array of
 * Strings Software arguments. environment Map Software environment,
 * names/values are Strings. stdin File Stdin from which the component reads.
 * stdout File Stdout to which the component writes. stderr File Stderr to which
 * the component writes. pre-staged files array of Files Files which should be
 * staged to the resource before the component is invoked. post-staged files
 * array of Files Files which should be staged from the resource after the
 * component is finished.
 *  
 */

public class SoftwareDescription implements java.io.Serializable {
	private URI location;

	private String[] arguments;

	private Map environment;

	private File stdin;

	private File stdout;

	private File stderr;

	private File[] preStaged;

	private File[] postStaged;

	private Map attributes;

	public SoftwareDescription(Map attributes) {
		this.attributes = attributes;

		location = (URI) attributes.get("location");
		arguments = (String[]) attributes.get("arguments");
		environment = (Map) attributes.get("environment");
		stdin = (File) attributes.get("stdin");
		stdout = (File) attributes.get("stdout");
		stderr = (File) attributes.get("stderr");
		preStaged = (File[]) attributes.get("pre-staged files");
		postStaged = (File[]) attributes.get("post-staged files");
	}

	/**
	 * Tests this GATSoftwareDescription for equality with the passed GATObject.
	 * GATSoftwareDescription are equal if they have equivalent entries in the
	 * description table.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof SoftwareDescription))
			return false;
		SoftwareDescription other = (SoftwareDescription) o;
		return other.attributes.equals(attributes);
	}
}