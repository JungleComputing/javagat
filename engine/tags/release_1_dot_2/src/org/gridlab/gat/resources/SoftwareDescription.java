/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

/**
 * @author rob
 */

/** An instance of this class is a description of a piece of software (component)
 * which is to be submitted as a job. It currently takes a table describing this
 * piece of software's attributes to any underlying job submission system.
 */
public class SoftwareDescription implements java.io.Serializable {
    private URI location;

    private String[] arguments;

    private HashMap environment;

    private File stdin;

    private File stdout;

    private File stderr;

    private HashMap preStagedFiles; // contains (src, dest) tuples

    private HashMap postStagedFiles; // contains (dest,src) tuples !!! (key cannot be null)

    private HashMap attributes;

    public SoftwareDescription() {
        attributes = new HashMap();
        preStagedFiles = new HashMap();
        postStagedFiles = new HashMap();
    }

    public SoftwareDescription(Map attributes) {
        this.attributes = new HashMap(attributes);

        location = (URI) attributes.get("location");
        arguments = (String[]) attributes.get("arguments");
        environment = new HashMap((Map) attributes.get("environment"));
        stdin = (File) attributes.get("stdin");
        stdout = (File) attributes.get("stdout");
        stderr = (File) attributes.get("stderr");

        // @@@ read them from the map
        //		preStagedSrc = (File[]) attributes.get("pre-staged files");
        //		postStagedDest = (File[]) attributes.get("post-staged files");
    }

    /**
     * Tests this GATSoftwareDescription for equality with the passed GATObject.
     * GATSoftwareDescription are equal if they have equivalent entries in the
     * description table.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof SoftwareDescription)) return false;
        SoftwareDescription other = (SoftwareDescription) o;
        return other.attributes.equals(attributes);
    }

    public int hashCode() {
        return attributes.hashCode();
    }

    /**
     * @return Returns the commandline arguments.
     */
    public String[] getArguments() {
        return arguments;
    }

    /**
     * @param arguments
     * The commandline arguments to set.
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
     * @param attributes
     *            The attributes to set.
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
     * @param environment
     *            The environment to set.
     */
    public void setEnvironment(Map environment) {
        this.environment = new HashMap(environment);
    }

    /**
     * @return Returns the location of the executable.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * @param location
     *            The location of the executable.
     */
    public void setLocation(URI location) {
        this.location = location;
    }

    /**
     * @param location
     *            The location of the executable.
     */
    public void setLocation(String location) throws URISyntaxException {
        this.location = new URI(location);
    }

    /* dest can be null, this means file with the same name in the PWD at the remote machine */
    public void addPreStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                "the source file cannot be null when adding a preStaged file");
        }
        preStagedFiles.put(src, dest);
    }

    /* src can be null, this means file with the same name in the PWD at the remote machine */
    public void addPostStagedFile(File src, File dest) {
        if (dest == null) {
            throw new NullPointerException(
                "the destination file cannot be null when adding a postStaged file");
        }
        postStagedFiles.put(dest, src);
    }

    /**
     * @return Returns the postStaged files. the order inside a tuple in this map is (dest,src)
     */
    public Map getPostStaged() {
        return postStagedFiles;
    }

    /**
     * @param postStaged
     *            The files to post stage.
     */
    public void setPostStaged(File[] postStaged) {
        postStagedFiles = new HashMap();

        for (int i = 0; i < postStaged.length; i++) {
            addPostStagedFile(null, postStaged[i]);
        }
    }

    /**
     * @param postStaged
     *            The files to post stage.
     */
    public void setPostStaged(File postStaged) {
        File[] tmp = new File[1];
        tmp[0] = postStaged;
        setPostStaged(tmp);
    }

    /**
     * @return Returns the pre staged files.
     */
    public Map getPreStaged() {
        return preStagedFiles;
    }

    /**
     * @param preStaged
     *            The files to pre stage.
     */
    public void setPreStaged(File preStaged) {
        File[] tmp = new File[1];
        tmp[0] = preStaged;
        setPreStaged(tmp);
    }

    /**
     * @param preStaged
     *            The files to pre stage.
     */
    public void setPreStaged(File[] preStaged) {
        preStagedFiles = new HashMap();

        for (int i = 0; i < preStaged.length; i++) {
            addPreStagedFile(preStaged[i], null);
        }
    }

    /**
     * @return Returns the stderr file.
     */
    public File getStderr() {
        return stderr;
    }

    /**
     * @param stderr
     *            The file where stderr is redirected to.
     */
    public void setStderr(File stderr) {
        this.stderr = stderr;
    }

    /**
     * @return Returns the stdin file.
     */
    public File getStdin() {
        return stdin;
    }

    /**
     * @param stdin
     *            The file where stdin is redirected from.
     */
    public void setStdin(File stdin) {
        this.stdin = stdin;
    }

    /**
     * @return Returns the stdout file.
     */
    public File getStdout() {
        return stdout;
    }

    /**
     * @param stdout
     *            The file where stdout is redirected to.
     */
    public void setStdout(File stdout) {
        this.stdout = stdout;
    }
}
