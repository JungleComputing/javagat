/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.net.URI;
import java.util.HashMap;
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

	private HashMap environment;

	private File stdin;

	private File stdout;

	private File stderr;

	private File[] preStaged;

	private File[] postStaged;

	private HashMap attributes;

	public SoftwareDescription() {
		attributes = new HashMap();
	}
	
	public SoftwareDescription(Map attributes) {
		this.attributes = new HashMap(attributes);

		location = (URI) attributes.get("location");
		arguments = (String[]) attributes.get("arguments");
		environment = new HashMap((Map) attributes.get("environment"));
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
	
	public int hashCode() {
		return attributes.hashCode();
	}
	
	/**
	 * @return Returns the arguments.
	 */
	public String[] getArguments() {
		return arguments;
	}
	/**
	 * @param arguments The arguments to set.
	 */
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
	/**
	 * @return Returns the attributes.
	 */
	public Map getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes The attributes to set.
	 */
	public void setAttributes(Map attributes) {
		this.attributes = new HashMap(attributes);
	}
	/**
	 * @return Returns the environment.
	 */
	public Map getEnvironment() {
		return environment;
	}
	/**
	 * @param environment The environment to set.
	 */
	public void setEnvironment(Map environment) {
		this.environment = new HashMap(environment);
	}
	/**
	 * @return Returns the location.
	 */
	public URI getLocation() {
		return location;
	}
	/**
	 * @param location The location to set.
	 */
	public void setLocation(URI location) {
		this.location = location;
	}
	/**
	 * @return Returns the postStaged.
	 */
	public File[] getPostStaged() {
		return postStaged;
	}
	/**
	 * @param postStaged The postStaged to set.
	 */
	public void setPostStaged(File[] postStaged) {
		this.postStaged = postStaged;
	}
	/**
	 * @return Returns the preStaged.
	 */
	public File[] getPreStaged() {
		return preStaged;
	}
	/**
	 * @param preStaged The preStaged to set.
	 */
	public void setPreStaged(File[] preStaged) {
		this.preStaged = preStaged;
	}
	/**
	 * @return Returns the stderr.
	 */
	public File getStderr() {
		return stderr;
	}
	/**
	 * @param stderr The stderr to set.
	 */
	public void setStderr(File stderr) {
		this.stderr = stderr;
	}
	/**
	 * @return Returns the stdin.
	 */
	public File getStdin() {
		return stdin;
	}
	/**
	 * @param stdin The stdin to set.
	 */
	public void setStdin(File stdin) {
		this.stdin = stdin;
	}
	/**
	 * @return Returns the stdout.
	 */
	public File getStdout() {
		return stdout;
	}
	/**
	 * @param stdout The stdout to set.
	 */
	public void setStdout(File stdout) {
		this.stdout = stdout;
	}
}