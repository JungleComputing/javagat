package org.gridlab.gat.io.cpi.commandlineSsh;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.gridlab.gat.AdaptorNotSelectedException;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshFileAdaptor;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;
import org.gridlab.gat.util.OutputForwarder;

public class CommandlineSshFileAdaptor extends FileCpi {
    public static final int SSH_PORT = 22;

    private boolean windows = false;

    /** We use an ssh adaptor for all operations, except copy.
     * This is done this way, because the auto optimizing of adaptor ordering by the 
     * engine does not select this adaptor anymore if another call is done before 
     * the copy.
     */
    private SshFileAdaptor sshAdaptor;
    
    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public CommandlineSshFileAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("ssh")) {
            throw new AdaptorNotSelectedException("cannot handle this URI");
        }

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) windows = true;
        
        sshAdaptor = new SshFileAdaptor(gatContext, preferences, location);
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     *
     * @param destination
     *            The new location
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     */
    public void copy(URI dest) throws GATInvocationException {
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (dest.refersToLocalHost() && (toURI().refersToLocalHost())) {
            throw new GATInvocationException(
                "commandlineSsh cannot copy local files");
        }

        // create a seperate file object to determine whether the source
        // is a directory. This is needed, because the source might be a local
        // file, and commandlineSsh might not be installed locally.
        // This goes wrong for local -> remote copies.
        try {
            File f = GAT.createFile(gatContext, preferences, toURI());

            if (f.isDirectory()) {
                copyDirectory(gatContext, preferences, toURI(), dest);

                return;
            }
        } catch (Exception e) {
            throw new GATInvocationException("commandlineSsh", e);
        }

        if (dest.refersToLocalHost()) {
            if (GATEngine.DEBUG) {
                System.err.println("commandlineSsh file: copy remote to local");
            }

            copyToLocal(fixURI(toURI(), "commandlineSsh"), fixURI(dest,
                "commandlineSsh"));

            return;
        }

        if (toURI().refersToLocalHost()) {
            if (GATEngine.DEBUG) {
                System.err.println("commandlineSsh file: copy local to remote");
            }

            copyToRemote(fixURI(toURI(), "commandlineSsh"), fixURI(dest,
                "commandlineSsh"));

            return;
        }

        // source is remote, dest is remote.
        if (GATEngine.DEBUG) {
            System.err.println("commandlineSsh file: copy remote to remote");
        }

        copyThirdParty(fixURI(toURI(), "commandlineSsh"), fixURI(dest,
            "commandlineSsh"));
    }

    protected void copyThirdParty(URI src, URI dest)
            throws GATInvocationException {
        throw new GATInvocationException("not implemented");
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        SshUserInfo sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                "ssh", src, SSH_PORT);
        } catch (Exception e) {
            System.out
                .println("CommandlineSshFileAdaptor: failed to retrieve credentials"
                    + e);
        }

        if (sui == null) {
            throw new GATInvocationException(
                "Unable to retrieve user info for authentication");
        }

        if (sui.privateKeyfile != null) {
            if (GATEngine.DEBUG) {
                System.err.println("key file argument not supported yet");
            }
        }

        //to be modified, this part goes inside the SSHSecurityUtils
        if (src.getUserInfo() != null) {
            sui.username = dest.getUserInfo();
        }

        /*allow port override*/
        int port = src.getPort();
        /*it will always return -1 for user@host:path*/
        if (port == -1) {
            port = SSH_PORT;
        }

        if (GATEngine.DEBUG) {
            System.err.println("CommandlineSsh: Prepared session for location "
                + src + " with username: " + sui.username + "; host: "
                + src.getHost());
        }

        String command = null;
        if (windows) {
            command = "scp -unat=yes ";
            if (sui.getPassword() == null) { // public/private key
                int slot = sui.getPrivateKeySlot();
                if (slot == -1) { // not set by the user, assume he only has one key
                    slot = 0;
                }
                command += " -pk=" + slot;
            } else { // password
                command += " -pw=" + sui.getPassword();
            }

            command += sui.username + "@" + src.getHost() + ":" + src.getPath()
                + " " + dest.getPath();
        } else {
            command = "scp -r " 
                + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                + sui.username + "@" + src.getHost() + ":"
                + src.getPath() + " " + dest.getPath();
        }

        if (GATEngine.VERBOSE) {
            System.err.println("CommandlineSsh: running command: " + command);
        }

        int exitValue = runCommand(command.toString());
        if (exitValue != 0) {
            throw new GATInvocationException("CommandlineSsh command failed");
        }
    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {
        SshUserInfo sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                "ssh", dest, SSH_PORT);
        } catch (Exception e) {
            System.out
                .println("CommandlineSshFileAdaptor: failed to retrieve credentials"
                    + e);
        }

        if (sui == null) {
            throw new GATInvocationException(
                "Unable to retrieve user info for authentication");
        }

        if (sui.privateKeyfile != null) {
            if (GATEngine.DEBUG) {
                System.err.println("key file argument not supported yet");
            }
        }

        //to be modified, this part goes inside the SSHSecurityUtils
        if (dest.getUserInfo() != null) {
            sui.username = src.getUserInfo();
        }

        /*allow port override*/
        int port = dest.getPort();
        /*it will always return -1 for user@host:path*/
        if (port == -1) {
            port = SSH_PORT;
        }

        if (GATEngine.DEBUG) {
            System.err.println("CommandlineSsh: Prepared session for location "
                + dest + " with username: " + sui.username + "; host: "
                + dest.getHost());
        }

        String command = null;
        if (windows) {
            command = "scp -unat=yes ";
            if (sui.getPassword() == null) { // public/private key
                int slot = sui.getPrivateKeySlot();
                if (slot == -1) { // not set by the user, assume he only has one key
                    slot = 0;
                }
                command += " -pk=" + slot;
            } else { // password
                command += " -pw=" + sui.getPassword();
            }

            command += src.getPath() + " " + sui.username + "@" + dest.getHost() + ":" + dest.getPath();
        } else {
            command = "scp -r " 
                + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                + src.getPath() + " " + sui.username + "@" + dest.getHost() + ":"
                + dest.getPath();
        }

        if (GATEngine.VERBOSE) {
            System.err.println("CommandlineSsh: running command: " + command);
        }

        int exitValue = runCommand(command.toString());
        if (exitValue != 0) {
            throw new GATInvocationException("CommandlineSsh command failed");
        }
    }

    /** run a command, discard output. Exit code is returned */
    protected int runCommand(String command) throws GATInvocationException {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command.toString());
        } catch (IOException e) {
            throw new CommandNotFoundException("commandlineSsh file", e);
        }

        // close stdin.
        try {
            p.getOutputStream().close();
        } catch (Throwable e) {
            // ignore
        }

        // we must always read the output and error streams to avoid deadlocks
        OutputForwarder out = new OutputForwarder(p.getInputStream(), false); // throw away output
        OutputForwarder err = new OutputForwarder(p.getErrorStream(), false); // throw away output

        try {
            int exitValue = p.waitFor();

            // Wait for the output forwarders to finish!
            // You may lose output if you don't -- Jason
            out.waitUntilFinished();
            err.waitUntilFinished();

            return exitValue;
        } catch (InterruptedException e) {
            // Cannot happen
            return 1;
        }
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#canRead()
     */
    public boolean canRead() {
        return sshAdaptor.canRead();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#canWrite()
     */
    public boolean canWrite() {
        return sshAdaptor.canWrite();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#createNewFile()
     */
    public boolean createNewFile() throws GATInvocationException {
        return sshAdaptor.createNewFile();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#delete()
     */
    public boolean delete() {
        return sshAdaptor.delete();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#exists()
     */
    public boolean exists() throws GATInvocationException {
        return sshAdaptor.exists();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#getAbsoluteFile()
     */
    public File getAbsoluteFile() throws GATInvocationException {
        return sshAdaptor.getAbsoluteFile();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#getAbsolutePath()
     */
    public String getAbsolutePath() throws GATInvocationException {
        return sshAdaptor.getAbsolutePath();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#getCanonicalFile()
     */
    public File getCanonicalFile() throws GATInvocationException {
        return sshAdaptor.getCanonicalFile();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#getCanonicalPath()
     */
    public String getCanonicalPath() throws GATInvocationException {
        return sshAdaptor.getCanonicalPath();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#getParent()
     */
    public String getParent() {
        return sshAdaptor.getParent();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#getParentFile()
     */
    public File getParentFile() throws GATInvocationException {
        return sshAdaptor.getParentFile();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#isAbsolute()
     */
    public boolean isAbsolute() {
        return sshAdaptor.isAbsolute();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#isDirectory()
     */
    public boolean isDirectory() {
        return sshAdaptor.isDirectory();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#isFile()
     */
    public boolean isFile() {
        return sshAdaptor.isFile();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#isHidden()
     */
    public boolean isHidden() {
        return sshAdaptor.isHidden();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#lastModified()
     */
    public long lastModified() throws GATInvocationException {
        return sshAdaptor.lastModified();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#length()
     */
    public long length() throws GATInvocationException {
        return sshAdaptor.length();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#list()
     */
    public String[] list() throws GATInvocationException {
        return sshAdaptor.list();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#list(java.io.FilenameFilter)
     */
    public String[] list(FilenameFilter arg0) throws GATInvocationException {
        return sshAdaptor.list(arg0);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#list(org.gridlab.gat.io.cpi.FilenameFilter)
     */
    public String[] list(org.gridlab.gat.io.cpi.FilenameFilter filter) throws GATInvocationException {
        return sshAdaptor.list(filter);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#listFiles()
     */
    public File[] listFiles() throws GATInvocationException {
        return sshAdaptor.listFiles();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#listFiles(java.io.FileFilter)
     */
    public File[] listFiles(FileFilter arg0) throws GATInvocationException {
        return sshAdaptor.listFiles(arg0);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#listFiles(org.gridlab.gat.io.cpi.FileFilter)
     */
    public File[] listFiles(org.gridlab.gat.io.cpi.FileFilter filter) throws GATInvocationException {
        return sshAdaptor.listFiles(filter);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#listFiles(java.io.FilenameFilter)
     */
    public File[] listFiles(FilenameFilter arg0) throws GATInvocationException {
        return sshAdaptor.listFiles(arg0);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#listFiles(org.gridlab.gat.io.cpi.FilenameFilter)
     */
    public File[] listFiles(org.gridlab.gat.io.cpi.FilenameFilter filter) throws GATInvocationException {
        return sshAdaptor.listFiles(filter);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#mkdir()
     */
    public boolean mkdir() throws GATInvocationException {
        return sshAdaptor.mkdir();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#mkdirs()
     */
    public boolean mkdirs() {
        return sshAdaptor.mkdirs();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#move(org.gridlab.gat.URI)
     */
    public void move(URI destination) throws GATInvocationException {
        sshAdaptor.move(destination);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#renameTo(java.io.File)
     */
    public boolean renameTo(java.io.File arg0) throws GATInvocationException {
        return sshAdaptor.renameTo(arg0);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#renameTo(org.gridlab.gat.io.File)
     */
    public boolean renameTo(File arg0) throws GATInvocationException {
        return sshAdaptor.renameTo(arg0);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#renameTo(org.gridlab.gat.URI)
     */
    public void renameTo(URI destination) throws GATInvocationException {
        sshAdaptor.renameTo(destination);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#setLastModified(long)
     */
    public boolean setLastModified(long arg0) {
        return sshAdaptor.setLastModified(arg0);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.ssh.SshFileAdaptor#setReadOnly()
     */
    public boolean setReadOnly() {
        return sshAdaptor.setReadOnly();
    }
}
