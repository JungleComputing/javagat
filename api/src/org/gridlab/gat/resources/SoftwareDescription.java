/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

/**
 * An instance of this class is a description of a piece of software (component)
 * which is to be submitted as a job. It currently takes a table describing this
 * piece of software's attributes to any underlying job submission system.
 * <p>
 * The following attributes are defined in the specification and should be
 * recognized by {@link ResourceBroker} adaptors.
 * <p>
 * <TABLE border="2" frame="box" rules="groups" summary="Minimum set of
 * supported attributes"> <CAPTION>Minimum set of supported name/value pairs
 * </CAPTION> <COLGROUP align="left"> <COLGROUP align="center"> <COLGROUP
 * align="left" > <THEAD valign="top">
 * <TR>
 * <TH>Name
 * <TH>Type
 * <TH>Description <TBODY>
 * <TR>
 * <TD>directory
 * <TD>{@link String}
 * <TD>working directory
 * <TR>
 * <TD>count (<b>deprecated</b> use {@link JobDescription#setProcessCount(int)})
 * <TD>{@link Integer}/{@link String}
 * <TD>number of executables to run
 * <TR>
 * <TD>host.count (<b>deprecated</b> use
 * {@link JobDescription#setResourceCount(int)})
 * <TD>{@link Integer}/{@link String}
 * <TD>number of hosts to distribute on
 * <TR>
 * <TD>time.max
 * <TD>{@link Long}/{@link String}
 * <TD>The maximum walltime or cputime for a single execution of the
 * executable. The units is in minutes.
 * <TR>
 * <TD>walltime.max
 * <TD>{@link Long}/{@link String}
 * <TD>the maximum walltime in minutes
 * <TR>
 * <TD>cputime.max
 * <TD>{@link Long}/{@link String}
 * <TD>the maximum cputime in minutes
 * <TR>
 * <TD>job.type
 * <TD>{@link String}
 * <TD>single|multiple|mpi|condor|...
 * <TR>
 * <TD>project
 * <TD>{@link String}
 * <TD>project to use, for accounting purposes
 * <TR>
 * <TD>dry.run
 * <TD>{@link String}
 * <TD>if set, don't submit but return success
 * <TR>
 * <TD>memory.min
 * <TD>{@link String}
 * <TD>minimal required memory in MB
 * <TR>
 * <TD>memory.max
 * <TD>{@link String}
 * <TD>maximal required memory in MB
 * <TR>
 * <TD>save.state
 * <TD>{@link Boolean}/{@link String}
 * <TD>keep job data persistent for restart
 * <TR>
 * <TD>restart
 * <TD>{@link String}
 * <TD>restart job with given ID
 * <TR></TBODY> </TABLE>
 * 
 * @author rob
 */
@SuppressWarnings("serial")
public class SoftwareDescription implements java.io.Serializable {

    private String executable;

    private String[] arguments;

    private HashMap<String, Object> environment;

    private File stdinFile;

    private File stdoutFile;

    private File stderrFile;

    private HashMap<File, File> preStagedFiles; // contains (src, dest) tuples

    private HashMap<File, File> postStagedFiles; // contains (src, dest)

    // tuples

    private ArrayList<File> deletedFiles; // contains Files, filenames of

    // files to be removed after the
    // run.

    private ArrayList<File> wipedFiles; // contains Files, filenames of files to

    // be wiped and removed after the run.

    private HashMap<String, Object> attributes;

    private boolean deletePreStaged;

    private boolean deletePostStaged;

    private boolean wipePreStaged;

    private boolean wipePostStaged;

    private boolean streamingStderr = false;

    private boolean streamingStdout = false;

    private boolean streamingStdin = false;

    /**
     * Create a {@link SoftwareDescription}, which describes the application
     * you want to run.
     */
    public SoftwareDescription() {
        attributes = new HashMap<String, Object>();
        preStagedFiles = new HashMap<File, File>();
        postStagedFiles = new HashMap<File, File>();
        deletedFiles = new ArrayList<File>();
        wipedFiles = new ArrayList<File>();
    }

    /**
     * To be implemented.
     * 
     * @param jsdlString
     */
    public SoftwareDescription(String jsdlString) {
    }

    /**
     * Create a {@link SoftwareDescription}, which describes the application
     * you want to run.
     * 
     * @param attributes
     *                See the comment above for a list of known attributes.
     */
    @SuppressWarnings("unchecked")
    public SoftwareDescription(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);

        executable = (String) attributes.get("executable");
        arguments = (String[]) attributes.get("arguments");
        environment = new HashMap<String, Object>(
                (Map<String, Object>) attributes.get("environment"));
        stdinFile = (File) attributes.get("stdin");
        stdoutFile = (File) attributes.get("stdout");
        stderrFile = (File) attributes.get("stderr");
    }

    /**
     * Tests this {@link SoftwareDescription} for equality with the passed
     * {@link Object}. {@link SoftwareDescription}s are equal if they have
     * equivalent entries in the description table.
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

    /**
     * Returns the hashcode of this {@link SoftwareDescription}
     * 
     * @return the hashcode of this {@link SoftwareDescription}
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return attributes.hashCode();
    }

    /**
     * Returns the arguments of the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> it will return a
     * {@link String}[]{"hello", "world", ">", "out"}
     * 
     * @return Returns the commandline arguments.
     */
    public String[] getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments of the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> the {@link String}[]{"hello",
     * "world", ">", "out"} contains the arguments.
     * 
     * @param arguments
     *                The commandline arguments to set.
     */
    public void setArguments(String... arguments) {
        this.arguments = arguments;
    }

    /**
     * Gets the attributes of this {@link SoftwareDescription}. This method
     * converts the well known attributes as listed in the table of
     * {@link SoftwareDescription} to their specific type (which means that if
     * an value can be a String or an Integer, it's cast to the non String
     * type).
     * 
     * @return the attributes.
     */
    public Map<String, Object> getAttributes() {
        // For known keys, resolve value strings to the correct type.
        Set<String> s = attributes.keySet();
        Iterator<String> i = s.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            Object tmp = attributes.get(key);
            if (!(tmp instanceof String))
                continue;
            String val = (String) tmp;
            if (key.equalsIgnoreCase("time.max")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("walltime.max")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("cputime.max")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("dry.run")) {
                attributes.put(key, new Boolean(val));
            } else if (key.equalsIgnoreCase("memory.min")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("memory.max")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("save.state")) {
                attributes.put(key, new Boolean(val));
            }
        }

        return attributes;
    }

    /**
     * Set the attributes to the attributes specified in the {@link Map}. This
     * will create a set of attributes only containing the attributes from the
     * {@link Map}. Use addAttribute to add attributes to an existing set of
     * attributes.
     * 
     * @param attributes
     *                The attributes to set. See the comment above for a list of
     *                known attributes. Note that some adaptors may also support
     *                other attributes.
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);
    }

    /**
     * Add an attribute to the existing attribute set. See the comment above for
     * a list of known attributes. Note that some adaptors may also support
     * other attributes.
     * 
     * @param key
     *                the key of the attribute
     * @param value
     *                the value of the attribute
     */
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Remove an attribute from the attribute set.
     * 
     * @param key
     *                the key of the attribute
     * @return the value belonging to this key
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    /**
     * Returns the environment of the executable. The environment of the
     * executable consists of a {@link Map} of environment variables with their
     * values (for instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @return the environment
     */
    public Map<String, Object> getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment of the executable. The environment of the executable
     * consists of a {@link Map} of environment variables with their values (for
     * instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @param environment
     *                The environment to set.
     */
    public void setEnvironment(Map<String, Object> environment) {
        this.environment = new HashMap<String, Object>(environment);
    }

    /**
     * Returns the path to the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> it will return a
     * {@link String} "/bin/cat".
     * 
     * @return the path to the executable.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Sets the path to the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> the {@link String} "/bin/cat"
     * should be provided.
     * 
     * @param executable
     *                The path to the executable.
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Returns the pre staged file set. This a {@link Map} with the source
     * {@link File}s as keys and the destination {@link File}s as values. This
     * method returns the files that should be pre staged regardless of whether
     * they are already pre staged or not.
     * 
     * @return the pre staged file set.
     */
    public Map<File, File> getPreStaged() {
        return preStagedFiles;
    }

    /**
     * Sets the pre staged file set. Any former pre staged files added to the
     * pre staged file set are no longer part of the pre staged file set. More
     * {@link File}s can be added using the <code>addPreStagedFile</code>
     * methods. See these methods for a table stating at which locations the
     * {@link File}s will end up after the pre staging.
     * 
     * @param files
     *                An array of files that should be pre staged.
     */
    public void setPreStaged(File... files) {
        preStagedFiles = new HashMap<File, File>();
        for (int i = 0; i < files.length; i++) {
            addPreStagedFile(files[i]);
        }
    }

    /**
     * Add a single pre stage file. This is similar to
     * <code>addPreStagedFile(src, null)</code>.
     * 
     * @param src
     *                the file that should be pre staged.
     */
    public void addPreStagedFile(File src) {
        addPreStagedFile(src, null);
    }

    /**
     * Add a single pre stage file that should be pre staged to the given
     * destination. The table below shows where the pre stage files will end up
     * after pre staging.
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="pre staging
     * overview" cellpadding="2"> <CAPTION>where do the pre staged files end up
     * </CAPTION> <COLGROUP align="left"> <COLGROUP align="center"> <COLGROUP
     * align="left" > <THEAD valign="top">
     * 
     * <TR>
     * <TH> source file
     * <TH> destination file
     * <TH> location after pre staging<TBODY>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>sandbox/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>sandbox/other/path/to/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>sandbox/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>sandbox/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code> </TABLE>
     * 
     * @param src
     *                the {@link File} that should be pre staged (may not be
     *                <code>null</code>)
     * @param dest
     *                the {@link File} that should exist after the pre staging
     *                (may be <code>null</code>, see table).
     */
    public void addPreStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                    "the source file cannot be null when adding a preStaged file");
        }
        preStagedFiles.put(src, dest);
    }

    /**
     * Returns the post stage file set. The key {@link File}s are the source
     * files on the execution site, the values are the {@link File}s with the
     * destination of the post staging. This method returns the files that
     * should be post staged regardless of whether they are already post staged
     * or not.
     * 
     * @return the post stage file set
     */
    public Map<File, File> getPostStaged() {
        return postStagedFiles;
    }

    /**
     * Sets the post staged file set. Any former post staged files added to the
     * post staged file set are no longer part of the post staged file set. More
     * {@link File}s can be added using the <code>addPostStagedFile</code>
     * methods. See these methods for a table stating at which locations the
     * {@link File}s will end up after the post staging.
     * 
     * @param files
     *                An array of files that should be pre staged.
     */
    public void setPostStaged(File... files) {
        postStagedFiles = new HashMap<File, File>();
        for (int i = 0; i < files.length; i++) {
            addPostStagedFile(files[i]);
        }
    }

    /**
     * Add a single post stage file. This is similar to
     * <code>addPostStagedFile(src, null)</code>.
     * 
     * @param src
     *                the file that should be post staged.
     */
    public void addPostStagedFile(File src) {
        addPostStagedFile(src, null);
    }

    /**
     * Add a single post stage file that should be post staged to the given
     * destination. The table below shows where the post stage files will end up
     * after post staging.
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="post staging
     * overview" cellpadding="2"> <CAPTION>where do the post staged files end up
     * </CAPTION> <COLGROUP align="left"> <COLGROUP align="center"> <COLGROUP
     * align="left" > <THEAD valign="top">
     * 
     * <TR>
     * <TH> source file
     * <TH> destination file
     * <TH> location after post staging<TBODY>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>cwd/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>cwd/other/path/to/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>cwd/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>cwd/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code> </TABLE>
     * 
     * @param src
     *                the {@link File} that should be post staged (may not be
     *                <code>null</code>)
     * @param dest
     *                the {@link File} that should exist after the post staging
     *                (may be <code>null</code>, see table).
     */
    public void addPostStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                    "the destination file cannot be null when adding a postStaged file");
        }

        postStagedFiles.put(src, dest);
    }

    /**
     * Returns a {@link List} of the {@link File}s that should be deleted after
     * the run.
     * 
     * @return the list of files to be deleted after the run. elements are of
     *         type File.
     */
    public List<File> getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * Adds a {@link File} to the set of {@link File}s that should be deleted
     * after the run. Normally the {@link ResourceBroker} will delete the
     * sandbox with all its contents after the run and it isn't necessary to
     * specify {@link File}s using this method to be deleted. However, there
     * are two cases where this method will be applicable.
     * <p>
     * First of all, {@link File}s outside the sandbox are not automatically
     * deleted by the {@link ResourceBroker}. Those {@link File}s should be
     * explicitly added to be deleted using this method.
     * <p>
     * Second, it's possible to specify that sandbox isn't deleted after the job
     * run, using the attribute "sandbox.delete" set to "false". In the case
     * that sandbox isn't deleted but some files inside the sandbox should be
     * deleted, use this method.
     * 
     * 
     * @param file
     *                the file to be deleted after the run.
     */
    public void addDeletedFile(File file) {
        deletedFiles.add(file);
    }

    /**
     * Returns a {@link List} of the {@link File}s that should be wiped and
     * deleted after the run.
     * 
     * @return the list of files to be deleted after the run. elements are of
     *         type File.
     */
    public List<File> getWipedFiles() {
        return wipedFiles;
    }

    /**
     * Adds a {@link File} to the set of {@link File}s that should be wiped
     * after the run. When a {@link File} gets deleted, it's possible that some
     * data that was in the file remains on the disk. To be sure these data is
     * also removed, the wiping of a {@link File} consists of first overwriting
     * the {@link File}, in order to delete the contents of the File. And then
     * deleting the File, in order to free up the disk space.
     * 
     * @param file
     *                the file to be wiped (overwritten) and deleted after the
     *                run.
     */
    public void addWipedFile(File file) {
        wipedFiles.add(file);
    }

    /**
     * Returns the stderr {@link File}.
     * 
     * @return the stderr {@link File}
     */
    public File getStderr() {
        return stderrFile;
    }

    /**
     * Enable or disable streaming standard error.
     * 
     * @param enabled
     *                <code>true</code> if streaming standard error should be
     *                enabled, <code>false</code> otherwise.
     */
    public void enableStreamingStderr(boolean enabled) {
        this.streamingStderr = enabled;
    }

    /**
     * Returns whether streaming standard error is enabled.
     * 
     * @return whether streaming standard error is enabled.
     */
    public boolean streamingStderrEnabled() {
        return this.streamingStderr;
    }

    /**
     * Enable or disable streaming standard output.
     * 
     * @param enabled
     *                <code>true</code> if streaming standard output should be
     *                enabled, <code>false</code> otherwise.
     */
    public void enableStreamingStdout(boolean enabled) {
        this.streamingStdout = enabled;
    }

    /**
     * Returns whether streaming standard output is enabled.
     * 
     * @return whether streaming standard output is enabled.
     */
    public boolean streamingStdoutEnabled() {
        return this.streamingStdout;
    }

    /**
     * Enable or disable streaming standard input.
     * 
     * @param enabled
     *                <code>true</code> if streaming standard input should be
     *                enabled, <code>false</code> otherwise.
     */
    public void enableStreamingStdin(boolean enabled) {
        this.streamingStdin = enabled;
    }

    /**
     * Returns whether streaming standard input is enabled.
     * 
     * @return whether streaming standard input is enabled.
     */
    public boolean streamingStdinEnabled() {
        return this.streamingStdin;
    }

    /**
     * Sets the stderr {@link File}. Note that stderr will be redirected to
     * either a {@link File} or a {@link OutputStream}. The last invocation of
     * <code>setStderr()</code> determines whether the destination of the
     * output.
     * 
     * @param stderr
     *                The {@link File} where stderr is redirected to.
     */
    public void setStderr(File stderr) {
        this.stderrFile = stderr;
    }

    /**
     * Returns the stdin {@link File}.
     * 
     * @return the stdin {@link File}.
     */
    public File getStdin() {
        return stdinFile;
    }

    /**
     * Sets the {@link File} where stdin is redirected from.
     * 
     * @param stdin
     *                The {@link File} where stdin is redirected from.
     */
    public void setStdin(File stdin) {
        this.stdinFile = stdin;
    }

    /**
     * Returns the stdout {@link File}.
     * 
     * @return the stdout {@link File}.
     */
    public File getStdout() {
        return stdoutFile;
    }

    /**
     * Sets the stdout {@link File}. Note that stdout will be redirected to
     * either a {@link File} or a {@link OutputStream}. The last invocation of
     * <code>setStdout()</code> determines whether the destination of the
     * output.
     * 
     * @param stdout
     *                The {@link File} where stdout is redirected to.
     */
    public void setStdout(File stdout) {
        this.stdoutFile = stdout;
    }

    public String toString() {
        String res = "SoftwareDescription(";
        res += "executable: " + executable;
        res += ", arguments: {";
        if (getArguments() != null) {
            for (String argument : getArguments()) {
                res += argument + ",";
            }
        } else {
            res += "null";
        }
        res += "}";
        res += ", stdin: "
                + (stdinFile == null ? "null" : stdinFile.toString());
        res += ", stdout: "
                + (stdoutFile == null ? "null" : stdoutFile.toString());
        res += ", stderr: "
                + (stderrFile == null ? "null" : stderrFile.toString());

        res += ", environment: "
                + (environment == null ? "null" : environment.toString());
        res += ", preStaged: "
                + (preStagedFiles == null ? "null" : preStagedFiles.toString());
        res += ", postStaged: "
                + (postStagedFiles == null ? "null" : postStagedFiles
                        .toString());
        res += ", attributes: "
                + (attributes == null ? "null" : attributes.toString());

        res += ")";

        return res;
    }

    /**
     * Returns whether the {@link File}s in the post stage file set should be
     * deleted after the post staging.
     * 
     * @return TRUE if post stage files will be deleted, FALSE if they won't be
     *         deleted.
     */
    public boolean deletePostStaged() {
        return deletePostStaged;
    }

    /**
     * Sets the value which is used to determine whether the {@link File}s in
     * the post stage file set should be deleted after the post staging. (TRUE
     * if the post stage files should be deleted, FALSE if they shouldn't be
     * deleted).
     * 
     * @param deletePostStaged
     *                delete the post stage {@link File}s after post staging.
     */
    public void setDeletePostStaged(boolean deletePostStaged) {
        this.deletePostStaged = deletePostStaged;
    }

    /**
     * Returns whether the {@link File}s in the pre stage file set should be
     * deleted after the post staging.
     * 
     * @return TRUE if pre stage files will be deleted, FALSE if they won't be
     *         deleted.
     */
    public boolean deletePreStaged() {
        return deletePreStaged;
    }

    /**
     * Sets the value which is used to determine whether the {@link File}s in
     * the pre stage file set should be deleted after the post staging. (TRUE if
     * the pre stage files should be deleted, FALSE if they shouldn't be
     * deleted).
     * 
     * @param deletePreStaged
     *                delete the pre stage {@link File}s after post staging.
     */
    public void setDeletePreStaged(boolean deletePreStaged) {
        this.deletePreStaged = deletePreStaged;
    }

    /**
     * Returns whether the {@link File}s in the post stage file set should be
     * wiped after the post staging.
     * 
     * @return TRUE if post stage files will be wiped, FALSE if they won't be
     *         wiped.
     */
    public boolean wipePostStaged() {
        return wipePostStaged;
    }

    /**
     * Sets the value which is used to determine whether the {@link File}s in
     * the post stage file set should be wiped after the post staging. (TRUE if
     * the post stage files should be wiped, FALSE if they shouldn't be wiped).
     * 
     * @param wipePostStaged
     *                wipe the post stage {@link File}s after post staging.
     */
    public void setWipePostStaged(boolean wipePostStaged) {
        this.wipePostStaged = wipePostStaged;
    }

    /**
     * Returns whether the {@link File}s in the pre stage file set should be
     * wiped after the post staging.
     * 
     * @return TRUE if pre stage files will be wiped, FALSE if they won't be
     *         wiped.
     */
    public boolean wipePreStaged() {
        return wipePreStaged;
    }

    /**
     * Sets the value which is used to determine whether the {@link File}s in
     * the pre stage file set should be wiped after the post staging. (TRUE if
     * the pre stage files should be wiped, FALSE if they shouldn't be wiped).
     * 
     * @param wipePreStaged
     *                wipe the pre stage {@link File}s after post staging.
     */
    public void setWipePreStaged(boolean wipePreStaged) {
        this.wipePreStaged = wipePreStaged;
    }

    /**
     * Returns the int value of an attribute. If no value (or the value
     * <code>null</code>) can be found for the given <code>name</code>,
     * the <code>defaultVal</code> is returned. If there exist a value for the
     * key, but this value cannot be parsed to an int, this method throws an
     * {@link Error}.
     * 
     * @param name
     *                the key of the attribute
     * @param defaultVal
     *                the default value
     * @return the int value of the attribute indicated by the <code>name</code>.
     */
    public int getIntAttribute(String name, int defaultVal) {
        Object val = (Integer) attributes.get(name);
        if (val == null)
            return defaultVal;

        if (val instanceof Integer) {
            Integer ival = (Integer) val;
            return ival.intValue();
        } else if (val instanceof String) {
            return Integer.parseInt((String) val);
        } else {
            throw new Error("illegal int value: " + val);
        }
    }

    /**
     * Returns the long value of an attribute. If no value (or the value
     * <code>null</code>) can be found for the given <code>name</code>,
     * the <code>defaultVal</code> is returned. If there exist a value for the
     * key, but this value cannot be parsed to a long, this method throws an
     * {@link Error}.
     * 
     * @param name
     *                the key of the attribute
     * @param defaultVal
     *                the default value
     * @return the long value of the attribute indicated by the
     *         <code>name</code>.
     */
    public long getLongAttribute(String name, long defaultVal) {
        Object val = attributes.get(name);
        if (val == null)
            return defaultVal;

        if (val instanceof Long) {
            Long lval = (Long) val;
            return lval.longValue();
        } else if (val instanceof String) {
            return Long.parseLong((String) val);
        } else {
            throw new Error("illegal long value: " + val);
        }
    }

    /**
     * Returns the {@link String} value of an attribute. If no value (or the
     * value <code>null</code>) can be found for the given <code>name</code>,
     * the <code>defaultVal</code> is returned.
     * 
     * @param name
     *                the key of the attribute
     * @param defaultVal
     *                the default value
     * @return the {@link String} value of the attribute indicated by the
     *         <code>name</code>.
     */
    public String getStringAttribute(String name, String defaultVal) {
        String val = (String) attributes.get(name);
        if (val == null)
            return defaultVal;
        return val;
    }

    /**
     * Returns the {@link Object} value of an attribute. If no value (or the
     * value <code>null</code>) can be found for the given <code>name</code>,
     * <code>null</code> is returned.
     * 
     * @param name
     *                the key of the attribute
     * @return the {@link Object} value of the attribute indicated by the
     *         <code>name</code>.
     */
    public Object getObjectAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Returns the boolean value of an attribute. If no value (or the value
     * <code>null</code>) can be found for the given <code>name</code>,
     * the <code>defaultVal</code> is returned. If there exist a value for the
     * key, but this value cannot be parsed to a boolean, this method throws an
     * {@link Error}.
     * 
     * @param name
     *                the key of the attribute
     * @param defaultVal
     *                the default value
     * @return the boolean value of the attribute indicated by the
     *         <code>name</code>.
     */
    public boolean getBooleanAttribute(String name, boolean defaultVal) {
        Object val = attributes.get(name);

        if (val == null)
            return defaultVal;

        if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        } else if (val instanceof String) {
            return ((String) val).equalsIgnoreCase("true");
        } else {
            throw new Error("illegal type for boolean attribute: " + name
                    + ": " + val);
        }
    }

    /**
     * <b>Do not use this method. This method is for internal use of JavaGAT.</b>
     * The attribute "globus.exitvalue.enable" can be set to "true" to make
     * JavaGAT use the wrapper script.
     * <p>
     * It creates a wrapper script out of this {@link SoftwareDescription} in
     * order to be able to retrieve the exit code of a Globus job, it stores the
     * output value in a file.
     * <p>
     * 
     * @param context
     *                the {@link GATContext} used to create the wrapper file
     * @param wrapperFileName
     *                the wrapper file name
     * @param exitValueFileName
     *                the file name where the exit value is written to
     * @throws GATInvocationException
     */
    public void toWrapper(GATContext context, String wrapperFileName,
            String exitValueFileName) throws GATInvocationException {
        // assemble the content
        String newline = System.getProperty("line.separator");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("#!/bin/sh");
            sb.append(newline);
            sb.append("# wrapper script for " + executable);
            sb.append(newline);
            sb.append("typeset int rc");
            sb.append(newline);
            sb.append("rcfile=" + exitValueFileName);
            sb.append(newline);
            sb.append("cmd=\"");
            sb.append(executable);
            if (arguments != null) {
                for (String argument : arguments) {
                    sb.append(" ");
                    sb.append(argument);
                }
            }
            sb.append("\"");
            sb.append(newline);
            sb.append("$cmd");
            sb.append(newline);
            sb.append("rc=$?");
            sb.append(newline);
            sb.append("echo $rc > $rcfile");
            sb.append(newline);
            sb.append("exit $rc");

            // write out the content of the wrapper file
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
                    wrapperFileName));
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        } catch (IOException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        }
        // OK, now we've created the wrapper script and written it to the local
        // disk. We have to add the wrapper script to prestage fileset and set
        // the executable to '/bin/sh' and the arguments to the wrapperscript.
        this.executable = "/bin/sh";
        this.arguments = new String[] { wrapperFileName };
        try {
            addPreStagedFile(GAT.createFile(context, new URI(wrapperFileName)));
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        }
        try {
            addPostStagedFile(GAT.createFile(context,
                    new URI(exitValueFileName)));
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        SoftwareDescription sd = new SoftwareDescription();
        sd.executable = executable;
        sd.stdinFile = stdinFile;
        sd.stdoutFile = stdoutFile;
        sd.stderrFile = stderrFile;
        sd.streamingStderr = streamingStderr;
        sd.streamingStdin = streamingStdin;
        sd.streamingStdout = streamingStdout;
        sd.preStagedFiles = preStagedFiles;
        sd.postStagedFiles = postStagedFiles;
        sd.deletedFiles = deletedFiles;
        sd.wipedFiles = wipedFiles;
        sd.deletePostStaged = deletePostStaged;
        sd.deletePreStaged = deletePreStaged;
        sd.wipePostStaged = wipePostStaged;
        sd.wipePreStaged = wipePreStaged;
        if (arguments != null) {
            sd.arguments = new String[arguments.length];
            System.arraycopy(arguments, 0, sd.arguments, 0, arguments.length);
        }
        if (attributes != null) {
            sd.attributes = (HashMap<String, Object>) attributes.clone();
        }
        if (environment != null) {
            sd.environment = (HashMap<String, Object>) environment.clone();
        }
        return sd;

    }

    /**
     * to be implemented.
     * 
     * @return the JSDL representation of this {@link SoftwareDescription}
     */
    public String getJSDL() {
        return null;
    }

}
