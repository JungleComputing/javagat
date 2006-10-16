/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

/**
 * @author rob
 * 
 * An instance of this class is a description of a piece of software (component)
 * which is to be submitted as a job. It currently takes a table describing this
 * piece of software's attributes to any underlying job submission system.
 * 
 * The following attributes are defined in the specification and should be 
 * recognized by ResourceBroker adaptors. 
 * 
 * <ul>
 * <li> directory (String): working directory.
 * <li> executable String): executable location.
 * <li> count (Integer/String): number of executables to run.
 * <li> hostCount (Integer/String): number of hosts to distribute on.
 * <li> maxTime (Long/String): maximal time in minutes.
 * <li> maxWallTime (Long/String): maximal WALL time in minutes.
 * <li> maxCPUTime (Long/String): maximal CPU time in minutes.
 * <li> jobType (String): single|multiple|mpi|condor|...
 * <li> queue (String): target queue name.
 * <li> project (String): project account to use.
 * <li> dryRun (Boolean/String): if set, dont submit but return success.
 * <li> minMemory (Integer/String): minimal required memory in MB.
 * <li> maxMemory (Integer/String): maximal required memory in MB.
 * <li> saveState (Boolean/String): keep job data persistent for restart.
 * <li> restart=ID (String): restart job with given ID.
 * </ul>
 */
public class SoftwareDescription implements java.io.Serializable {
    private URI location;

    private String[] arguments;

    private HashMap environment;

    private File stdin;

    private File stdout;

    private File stderr;

    private HashMap preStagedFiles; // contains (src, dest) tuples

    private HashMap postStagedFiles; // contains (src, dest) tuples

    private ArrayList deletedFiles; // contains Files, filenames of files to be removed after the run.
    
    private ArrayList wipedFiles; // contains Files, filenames of files to be wiped and removed after the run.

    private HashMap attributes;

    private boolean deletePreStaged;
    private boolean deletePostStaged;
    private boolean wipePreStaged;
    private boolean wipePostStaged;
    
    
    public SoftwareDescription() {
        attributes = new HashMap();
        preStagedFiles = new HashMap();
        postStagedFiles = new HashMap();
        deletedFiles = new ArrayList();
        wipedFiles = new ArrayList();
    }

    public SoftwareDescription(Map attributes) {
        this.attributes = new HashMap(attributes);

        location = (URI) attributes.get("location");
        arguments = (String[]) attributes.get("arguments");
        environment = new HashMap((Map) attributes.get("environment"));
        stdin = (File) attributes.get("stdin");
        stdout = (File) attributes.get("stdout");
        stderr = (File) attributes.get("stderr");
    }

    /**
     * Tests this GATSoftwareDescription for equality with the passed GATObject.
     * GATSoftwareDescription are equal if they have equivalent entries in the
     * description table.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof SoftwareDescription)) {
            return false;
        }

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
        // For known keys, resolve value strings to the correct type. 
        Set s = attributes.keySet();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            Object tmp = attributes.get(key);
            if (!(tmp instanceof String)) continue;
            String val = (String) tmp;
            if (key.equalsIgnoreCase("count")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("hostCount")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("maxTime")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("maxWallTime")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("maxCPUTime")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("dryRun")) {
                attributes.put(key, new Boolean(val));
            } else if (key.equalsIgnoreCase("minMemory")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("maxMemory")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("saveState")) {
                attributes.put(key, new Boolean(val));
            }
        }

        return attributes;
    }

    /**
     * @param attributes
     *            The attributes to set.
     */
    public void setAttributes(Map attributes) {
        this.attributes = new HashMap(attributes);
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
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

    /**
     * @return Returns the pre staged files.
     */
    public Map getPreStaged() {
        return preStagedFiles;
    }

    /** Add a prestaged file. The file will have the same name in the CWD at the remote machine. */
    public void addPreStagedFile(File src) {
        addPreStagedFile(src, null);
    }

    /** Dest can be null, this means file with the same name in the CWD at the remote machine */
    public void addPreStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                "the source file cannot be null when adding a preStaged file");
        }

        preStagedFiles.put(src, dest);
    }

    /**
     * @return Returns the postStaged files. the order inside a tuple in this map is (src, dest)
     */
    public Map getPostStaged() {
        return postStagedFiles;
    }

    /** Add a file to poststage. 
     * The file will be post staged to the current working directory, and will 
     * have the same name as the remote file. */
    public void addPostStagedFile(File src) {
        addPostStagedFile(src, null);
    }

    /** dest can be null, this means file with the same name in the CWD at the remote machine */
    public void addPostStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                "the destination file cannot be null when adding a postStaged file");
        }

        postStagedFiles.put(src, dest);
    }

    /** 
     * @return the list of files to be deleted after the run.
     * elements are of type File.
     */
    public ArrayList getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * 
     * @param f the file to be wiped (overwritten) after the run.
     */
    public void addDeletedFile(File f) {
        deletedFiles.add(f);
    }
    
    /** 
     * @return the list of files to be wiped (overwritten) and deleted after the run.
     * elements are of type File.
     */
    public ArrayList getWipedFiles() {
        return wipedFiles;
    }

    /**
     * 
     * @param f the file to be wiped (overwritten) and deleted after the run.
     */
    public void addWipedFile(File f) {
        wipedFiles.add(f);
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

    public String toString() {
        String res = "SoftwareDescription(";
        res += "location: " + (location == null ? "null" : location.toString());

        res += ", arguments: ";
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                res += arguments[i];
                if (i != arguments.length - 1) res += ", ";
            }
        } else {
            res += "null";
        }

        res += ", stdin: " + (stdin == null ? "null" : stdin.toString());
        res += ", stdout: " + (stdout == null ? "null" : stdout.toString());
        res += ", stderr: " + (stderr == null ? "null" : stderr.toString());

        res +=
                ", environment: "
                    + (environment == null ? "null" : environment.toString());
        res +=
                ", preStaged: "
                    + (preStagedFiles == null ? "null" : preStagedFiles
                        .toString());
        res +=
                ", postStaged: "
                    + (postStagedFiles == null ? "null" : postStagedFiles
                        .toString());
        res +=
                ", attributes: "
                    + (attributes == null ? "null" : attributes.toString());

        res += ")";

        return res;
    }

    public boolean deletePostStaged() {
        return deletePostStaged;
    }

    public void setDeletePostStaged(boolean deletePostStaged) {
        this.deletePostStaged = deletePostStaged;
    }

    public boolean deletePreStaged() {
        return deletePreStaged;
    }

    public void setDeletePreStaged(boolean deletePreStaged) {
        this.deletePreStaged = deletePreStaged;
    }

    public boolean wipePostStaged() {
        return wipePostStaged;
    }

    public void setWipePostStaged(boolean wipePostStaged) {
        this.wipePostStaged = wipePostStaged;
    }

    public boolean wipePreStaged() {
        return wipePreStaged;
    }

    public void setWipePreStaged(boolean wipePreStaged) {
        this.wipePreStaged = wipePreStaged;
    }
}
