package org.gridlab.gat.io.cpi.commandlineSsh;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.ssh.SshSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

@SuppressWarnings("serial")
public class CommandlineSshFileAdaptor extends FileCpi {

    protected static Logger logger = Logger
            .getLogger(CommandlineSshFileAdaptor.class);

    public static final int SSH_PORT = 22;

    private boolean windows = false;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public CommandlineSshFileAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("ssh") && !location.isCompatible("file")) {
            throw new AdaptorNotApplicableException("cannot handle this URI");
        }

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows"))
            windows = true;
    }
    
    private boolean runSshCommand(String params) throws GATInvocationException {
        return runSshCommand(params, false);
    }
    
    /* if second parameter is true than stderr will be written to log4j (it is for commands where
     * we think that failing is an error (like mkdir))
    */ 
    private boolean runSshCommand(String params, boolean writeError) throws GATInvocationException {
        String command = getSshCommand() + params;
        CommandRunner runner = new CommandRunner(command.toString());
        if (logger.isDebugEnabled()) {
            logger.debug("running command = " + command);
        }
        int exitVal = runner.getExitCode();
        if (logger.isDebugEnabled()) {
            logger.debug("exitCode=" + exitVal);
        }
        if (exitVal > 1) {
            // bigger than 1 means ssh error and not false as command response
            throw new GATInvocationException("invocation error");
        }
        if (exitVal == 1 && writeError) {
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error=" + runner.getStderr());
            }
        }
        return exitVal == 0;
    }
    

    /* [wojciech]
     * 
     * Those four methods work for *nix system on the other side
     * 
     * Merge into trunk only if you really want to :).
     */
    

    @Override
    public boolean mkdir() throws GATInvocationException {
        if (location.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("location=" + location + " refers to local in mkdir");
            }
            return new java.io.File(location.getPath()).mkdir();
        }
        return runSshCommand("/bin/mkdir " + getPath(), true);
    }

    @Override
    public boolean delete() throws GATInvocationException {
        if (location.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("location=" + location + " refers to local in delete");
            }
            return new java.io.File(location.getPath()).delete();
        }
        return runSshCommand("rm -rf " + getPath(), true);
    }
    
    @Override
    public boolean isDirectory() throws GATInvocationException {
        if (1 < 2) {
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("isDirectory() called");
        }
        if (location.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("location=" + location + " refers to local in isDirectory");
            }
            return new java.io.File(location.getPath()).isDirectory();
        }
        return runSshCommand("find " + getPath() + " -type d");
    }
    
    @Override
    public boolean exists() throws GATInvocationException {
        if (location.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("location=" + location + " refers to local in exists");
            }
            return new java.io.File(location.getPath()).exists();
        }
        return runSshCommand("find " + getPath());
    }

// TODO [wojciech] add some sense here
    private String getSshCommand() throws GATInvocationException {
        return "ssh " + location.resolveHost() + " ";
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param destination
     *                The new location
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public void copy(URI dest) throws GATInvocationException {
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (dest.refersToLocalHost() && (toURI().refersToLocalHost())) {
            throw new GATInvocationException(
                    "commandlineSsh cannot copy local files");
        }

        // @@@ if we find a solution for the "scp -r" problem, this code should
        // be commented out
        // create a seperate file object to determine whether the source
        // is a directory. This is needed, because the source might be a local
        // file, and gridftp might not be installed locally.
        // This goes wrong for local -> remote copies.
        try {
            if (determineIsDirectory()) {
                copyDirectory(gatContext, preferences, toURI(), dest);
                return;
            }

        } catch (GATInvocationException e) {
            throw new GATInvocationException("commandlineSSH", e);
        }

        if (dest.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("commandlineSsh file: copy remote to local");
            }

            copyToLocal(location, dest);
            return;
        }

        if (toURI().refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("commandlineSsh file: copy local to remote");
            }

            copyToRemote(location, dest);
            return;
        }

        // source is remote, dest is remote.
        throw new GATInvocationException(
                "commandlineSsh: cannot do third party copy");
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        SshUserInfo sui = null;

        try {
            sui = SshSecurityUtils.getSshCredential(gatContext, preferences,
                    "ssh", src, SSH_PORT);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (sui == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        if (sui.privateKeyfile != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }

        /* allow port override */
        int port = src.getPort();
        /* it will always return -1 for user@host:path */
        if (port == -1) {
            port = SSH_PORT;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("CommandlineSsh: Prepared session for location " + src
                    + " with username: " + sui.username + "; host: "
                    + src.resolveHost());
        }

        String command = null;
        if (windows) {
            command = "scp -unat=yes ";
            if (sui.getPassword() == null) { // public/private key
                int slot = sui.getPrivateKeySlot();
                if (slot == -1) { // not set by the user, assume he only has
                    // one key
                    slot = 0;
                }
                command += " -pk=" + slot;
            } else { // password
                command += " -pw=" + sui.getPassword();
            }

            command += sui.username + "@" + src.resolveHost() + ":"
                    + src.getPath() + " " + dest.getPath();
        } else {
            File remote = null;
            boolean dir = false;
            try {
                remote = GAT.createFile(gatContext, preferences, src);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("commandlineSsh", e);
            }
            if (remote.getFileInterface().exists()) {
                if (remote.getFileInterface().isDirectory()) {
                    dir = true;
                }
            } else {
                throw new GATInvocationException(
                        "the remote file does not exist.");
            }

            if (dir) {
                java.io.File local = new java.io.File(dest.getPath());
                if (local.exists()) {
                    if (!local.isDirectory()) {
                        throw new GATInvocationException(
                                "local destination already exists, and it is not a directory");
                    }
                } else {
                    if (!local.mkdir()) {
                        throw new GATInvocationException(
                                "could not create local dir");
                    }
                }

                command = "scp -r "
                        + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                        + sui.username + "@" + src.resolveHost() + ":"
                        + src.getPath() + "/* " + dest.getPath();
            } else {
                command = "scp "
                        + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                        + sui.username + "@" + src.resolveHost() + ":"
                        + src.getPath() + " " + dest.getPath();
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("CommandlineSsh: running command: " + command);
        }
        CommandRunner runner = new CommandRunner(command.toString());
        if (logger.isInfoEnabled()) {
            logger.info("\nstderr: " + runner.getStderr() + "\nstdout: "
                + runner.getStdout());
        }
        int exitValue = runner.getExitCode();
        if (exitValue != 0) {
            throw new GATInvocationException("CommandlineSsh command failed: " + runner.getStderr());
        }
    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {
        SshUserInfo sui = null;

        try {
            sui = SshSecurityUtils.getSshCredential(gatContext, preferences,
                    "ssh", dest, SSH_PORT);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (sui == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        if (sui.privateKeyfile != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }

        // to be modified, this part goes inside the SSHSecurityUtils
        if (dest.getUserInfo() != null) {
            sui.username = dest.getUserInfo();
        }

        /* allow port override */
        int port = dest.getPort();
        /* it will always return -1 for user@host:path */
        if (port == -1) {
            port = SSH_PORT;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("CommandlineSsh: Prepared session for location "
                    + dest + " with username: " + sui.username + "; host: "
                    + dest.resolveHost());
        }

        String command = null;
        if (windows) {
            command = "scp -unat=yes ";
            if (sui.getPassword() == null) { // public/private key
                int slot = sui.getPrivateKeySlot();
                if (slot == -1) { // not set by the user, assume he only has
                    // one key
                    slot = 0;
                }
                command += " -pk=" + slot;
            } else { // password
                command += " -pw=" + sui.getPassword();
            }

            command += src.getPath() + " " + sui.username + "@"
                    + dest.resolveHost() + ":" + dest.getPath();
        } else {
            if (determineIsDirectory()) {
                File remote = null;
                try {
                    remote = GAT.createFile(gatContext, preferences, dest);
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException("commandlineSsh", e);
                }
                if (remote.getFileInterface().exists()) {
                    if (!remote.getFileInterface().isDirectory()) {
                        throw new GATInvocationException(
                                "remote destination already exists, and it is not a directory");
                    }
                } else {
                    if (!remote.getFileInterface().mkdir()) {
                        throw new GATInvocationException(
                                "could not create remote dir");
                    }
                }

                command = "scp -r "
                        + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                        + src.getPath() + "/* " + sui.username + "@"
                        + dest.resolveHost() + ":" + dest.getPath();
            } else {
                command = "scp "
                        + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                        + src.getPath() + " " + sui.username + "@"
                        + dest.resolveHost() + ":" + dest.getPath();
            }
        }

        // @@@ this does not work because the * is not understood by scp
        if (logger.isInfoEnabled()) {
            logger.info("CommandlineSsh: running command: " + command);
        }

        CommandRunner runner = new CommandRunner(command.toString());
        if (logger.isInfoEnabled()) {
            logger.info("\nstderr: " + runner.getStderr() + "\nstdout: "
                + runner.getStdout());
        }
        int exitValue = runner.getExitCode();
        if (exitValue != 0) {
            throw new GATInvocationException("CommandlineSsh command failed: " + runner.getStderr());
        }
    }

}
