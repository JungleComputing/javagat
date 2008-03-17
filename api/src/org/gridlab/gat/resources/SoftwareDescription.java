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
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
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
 * <li> count (Integer/String): number of executables to run.
 * <li> host.count (Integer/String): number of hosts to distribute on.
 * <li> time.max (Long/String): The maximum walltime or cputime for a single
 * execution of the executable. The units is in minutes.
 * <li> walltime.max (Long/String): maximal WALL time in minutes.
 * <li> cputime.max (Long/String): maximal CPU time in minutes.
 * <li> job.type (String): single|multiple|mpi|condor|...
 * <li> globus.queue (String): target queue name.
 * <li> project (String): project to use, for accounting purposes.
 * <li> dryrun (Boolean/String): if set, dont submit but return success.
 * <li> memory.min (Integer/String): minimal required memory in MB.
 * <li> memory.max (Integer/String): maximal required memory in MB.
 * <li> savestate (Boolean/String): keep job data persistent for restart.
 * <li> restart=ID (String): restart job with given ID.
 * </ul>
 * 
 * 
 */
@SuppressWarnings("serial")
public class SoftwareDescription implements java.io.Serializable {

    private String executable;

    private String[] arguments;

    private HashMap<String, Object> environment;

    private File stdin;

    private File stdout;
    private OutputStream stdoutStream;
    private boolean stdoutIsStreaming = false;

    private File stderr;
    private OutputStream stderrStream;
    private boolean stderrIsStreaming = false;

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

    private String virtualOrganisation;

    /**
     * Create a software description, which describes the application you want
     * to run.
     * 
     */
    public SoftwareDescription() {
        attributes = new HashMap<String, Object>();
        preStagedFiles = new HashMap<File, File>();
        postStagedFiles = new HashMap<File, File>();
        deletedFiles = new ArrayList<File>();
        wipedFiles = new ArrayList<File>();
    }

    /**
     * Create a software description, which describes the application you want
     * to run.
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
     *                The commandline arguments to set.
     */
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * @return Returns the attributes.
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
            if (key.equalsIgnoreCase("count")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("host.count")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("time.max")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("walltime.max")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("cputime.max")) {
                attributes.put(key, new Long(val));
            } else if (key.equalsIgnoreCase("dryrun")) {
                attributes.put(key, new Boolean(val));
            } else if (key.equalsIgnoreCase("memory.min")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("memory.max")) {
                attributes.put(key, new Integer(val));
            } else if (key.equalsIgnoreCase("savestate")) {
                attributes.put(key, new Boolean(val));
            }
        }

        return attributes;
    }

    /**
     * @param attributes
     *                The attributes to set. See the comment above for a list of
     *                known attributes.
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);
    }

    /**
     * Add an attribute to the attribute set. See the comment above for a list
     * of known attributes.
     * 
     * @param key
     * @param value
     */
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Remove an attribute from the attribute set.
     * 
     * @param key
     * @return the value belonging to this key
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    /**
     * @return Returns the environment.
     */
    public Map<String, Object> getEnvironment() {
        return environment;
    }

    /**
     * @param environment
     *                The environment to set.
     */
    public void setEnvironment(Map<String, Object> environment) {
        this.environment = new HashMap<String, Object>(environment);
    }

    /**
     * @return Returns the path to the executable.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * @param executable
     *                The path to the executable.
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * @return Returns the pre staged files.
     */
    public Map<File, File> getPreStaged() {
        return preStagedFiles;
    }

    public void setPreStaged(File[] files) {
        preStagedFiles = new HashMap<File, File>();
        for (int i = 0; i < files.length; i++) {
            addPreStagedFile(files[i]);
        }
    }

    /**
     * Add a prestaged file. The file will have the same name in the CWD at the
     * remote machine.
     */
    public void addPreStagedFile(File src) {
        addPreStagedFile(src, null);
    }

    /**
     * Dest can be null, this means file with the same name in the CWD at the
     * remote machine
     */
    public void addPreStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                    "the source file cannot be null when adding a preStaged file");
        }
        preStagedFiles.put(src, dest);
    }

    /**
     * @return Returns the postStaged files. the order inside a tuple in this
     *         map is (src, dest)
     */
    public Map<File, File> getPostStaged() {
        return postStagedFiles;
    }

    public void setPostStaged(File[] files) {
        postStagedFiles = new HashMap<File, File>();
        for (int i = 0; i < files.length; i++) {
            addPostStagedFile(files[i]);
        }
    }

    /**
     * Add a file to poststage. The file will be post staged to the current
     * working directory, and will have the same name as the remote file.
     */
    public void addPostStagedFile(File src) {
        addPostStagedFile(src, null);
    }

    /**
     * dest can be null, this means file with the same name in the CWD at the
     * local machine
     */
    public void addPostStagedFile(File src, File dest) {
        if (src == null) {
            throw new NullPointerException(
                    "the destination file cannot be null when adding a postStaged file");
        }

        postStagedFiles.put(src, dest);
    }

    /**
     * @return the list of files to be deleted after the run. elements are of
     *         type File.
     */
    public ArrayList<File> getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * 
     * @param f
     *                the file to be wiped (overwritten) after the run.
     */
    public void addDeletedFile(File f) {
        deletedFiles.add(f);
    }

    /**
     * @return the list of files to be wiped (overwritten) and deleted after the
     *         run. elements are of type File.
     */
    public ArrayList<File> getWipedFiles() {
        return wipedFiles;
    }

    /**
     * 
     * @param f
     *                the file to be wiped (overwritten) and deleted after the
     *                run.
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
     * @return Returns the stderr stream.
     */
    public OutputStream getStderrStream() {
        return stderrStream;
    }

    /**
     * @param stderr
     *                The file where stderr is redirected to.
     */
    public void setStderr(File stderr) {
        stderrIsStreaming = false;
        this.stderr = stderr;
    }
    
    /**
     * @param stderrStream
     *                The stream where stderr is redirected to.
     */
    public void setStderr(OutputStream stderrStream) {
        stderrIsStreaming = true;
        this.stderrStream = stderrStream; 
    }
    
    public boolean getStderrIsStreaming() {
        return stderrIsStreaming;
    }

    /**
     * @return Returns the stdin file.
     */
    public File getStdin() {
        return stdin;
    }

    /**
     * @param stdin
     *                The file where stdin is redirected from.
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
     * @return Returns the stdout stream.
     */
    public OutputStream getStdoutStream() {
        return stdoutStream;
    }

    /**
     * @param stdout
     *                The file where stdout is redirected to.
     */
    public void setStdout(File stdout) {
        stdoutIsStreaming = false;
        this.stdout = stdout;
    }
    
    /**
     * @param stdoutStream
     *                The stream where stdout is redirected to.
     */
    public void setStdout(OutputStream stdoutStream) {
        stdoutIsStreaming = true;
        this.stdoutStream = stdoutStream; 
    }
    
    public boolean getStdoutIsStreaming() {
        return stdoutIsStreaming;
    }

    public String toString() {
        String res = "SoftwareDescription(";
        // res += "location: " + (location == null ? "null" :
        // location.toString());
        res += "executable: " + executable;

        res += ", arguments: ";
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                res += arguments[i];
                if (i != arguments.length - 1)
                    res += ", ";
            }
        } else {
            res += "null";
        }

        res += ", stdin: " + (stdin == null ? "null" : stdin.toString());
        res += ", stdout: " + (stdout == null ? "null" : stdout.toString());
        res += ", stderr: " + (stderr == null ? "null" : stderr.toString());

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

    public String getStringAttribute(String name, String defaultVal) {
        String val = (String) attributes.get(name);
        if (val == null)
            return defaultVal;
        return val;
    }

    /** returns the associated object, or null if it is not set * */
    public Object getObjectAttribute(String name) {
        return attributes.get(name);
    }

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

    public void setVirtualOrganisation(String vo) {
        this.virtualOrganisation = vo;
    }

    public String getVirtualOrganisation() {
        return virtualOrganisation;
    }

    /**
     * Creates a wrapper script to retrieve the exit code of a Globus job, and
     * stores it in a file.
     */
    public void toWrapper(GATContext context, Preferences preferences,
            String wrapperFileName, String exitValueFileName)
            throws GATInvocationException {
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
            // sb.append("/bin/sh $cmd");
            sb.append("$cmd");
            sb.append(newline);
            sb.append("rc=$?");
            sb.append(newline);
            // sb.append("echo 'test echo' >> /tmp/proc_gat_test_log.test");
            // sb.append(newline);
            // sb.append("echo 'test echo rc of date_script: `echo $rc` >>
            // /tmp/proc_gat_test_log.test");
            // sb.append(newline);
            // sb.append("echo 'test echo date: `/bin/date` >>
            // /tmp/proc_gat_test_log.test");
            // sb.append(newline);
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
            addPreStagedFile(GAT.createFile(context, preferences, new URI(
                    wrapperFileName)));
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        }
        try {
            addPostStagedFile(GAT.createFile(context, preferences, new URI(
                    exitValueFileName)));
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("SoftwareDescription toWrapper", e);
        }

    }

}
