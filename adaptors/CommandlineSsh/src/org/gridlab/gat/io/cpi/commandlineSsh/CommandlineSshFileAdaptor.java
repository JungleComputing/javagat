package org.gridlab.gat.io.cpi.commandlineSsh;

import java.util.ArrayList;
import java.util.Map;

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
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.commandlinessh.CommandlineSshSecurityUtils;

@SuppressWarnings("serial")
public class CommandlineSshFileAdaptor extends FileCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("delete", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("mkdir", true);
        capabilities.put("exists", true);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences p = FileCpi.getSupportedPreferences();
        p.put("commandlinesshfile.ssh.port", "22");
        return p;
    }

    protected static Logger logger = Logger
            .getLogger(CommandlineSshFileAdaptor.class);

    public static final int SSH_PORT = 22;

    private boolean windows = false;

    private final int ssh_port;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public CommandlineSshFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        if (!location.isCompatible("ssh") && !location.isCompatible("file")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows"))
            windows = true;

        // Allow different port, so that this adaptor can be used over an
        // ssh tunnel. --Ceriel
        String port = (String) gatContext.getPreferences().get(
                "CommandlineSshFile.ssh.port");
        if (port != null) {
            ssh_port = Integer.parseInt(port);
        } else {
            ssh_port = SSH_PORT;
        }
    }

    private boolean runSshCommand(boolean writeError, String... params)
            throws GATInvocationException {
        return runSshCommand(params, writeError, false);
    }

    /*
     * if second parameter is true than stderr will be written to log4j (it is
     * for commands where we think that failing is an error (like mkdir))
     */
    private boolean runSshCommand(String[] params, boolean writeError,
            boolean nonEmptyOutputMeansSuccess) throws GATInvocationException {
        ArrayList<String> command = getSshCommand();
        for (String p : params) {
            command.add(p);
        }
        CommandRunner runner = new CommandRunner(command);
        if (logger.isDebugEnabled()) {
            logger.debug("running command = " + command);
        }
        int exitVal = runner.getExitCode();
        if (logger.isDebugEnabled()) {
            logger.debug("exitCode=" + exitVal);
        }
        if (exitVal > 1) {
            // bigger than 1 means ssh error and not false as command response
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error=" + runner.getStderr());
            }
            throw new GATInvocationException("invocation error");
        }
        if (exitVal == 1 && writeError) {
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error=" + runner.getStderr());
            }
        }
        if (exitVal == 0 && nonEmptyOutputMeansSuccess) {
            return runner.getStdout().length() != 0
                    || runner.getStderr().length() != 0;
        }
        return exitVal == 0;
    }

    public boolean mkdir() throws GATInvocationException {
        return runSshCommand(true, "mkdir", getPathFixed());
    }

    // This method modifies the path if the path is on the local host and is
    // relative, any ssh command would be relative to the ssh entry point
    // ($HOME), but we want it to be relative to the cwd of the user. --roelof
    private String getPathFixed() {
        if (isAbsolute() || !location.refersToLocalHost()) {
            return getPath();
        }
        return System.getProperty("user.dir") + File.separator + getPath();
    }

    public boolean delete() throws GATInvocationException {
        if (windows) {
            throw new UnsupportedOperationException("Not implemented");
        }
        if (!exists()) {
            return false;
        }
        return runSshCommand(true, "rm",  "-rf",  getPathFixed());
    }

    public boolean isDirectory() throws GATInvocationException {
        if (windows) {
            throw new UnsupportedOperationException("Not implemented");
        }
        return runSshCommand(true, "test",  "-d", getPathFixed());
    }

    public boolean isFile() throws GATInvocationException {
        if (windows) {
            throw new UnsupportedOperationException("Not implemented");
        }
        return runSshCommand(true, "test",  "-f",  getPathFixed());
    }

    public boolean exists() throws GATInvocationException {
        if (windows) {
            throw new UnsupportedOperationException("Not implemented");
        }
        return runSshCommand(true, "test", "-e", getPathFixed());
    }

    private ArrayList<String> getSshCommand() throws GATInvocationException {
        ArrayList<String> cmd = new ArrayList<String>();
        int p = location.getPort();
        String portString = "" + ssh_port;
        if (p != -1) {
            portString = "" + p;
        }
        cmd.add("ssh");
        cmd.add("-p");
        cmd.add("" + portString);
        cmd.add(location.resolveHost());
        return cmd;
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
            if (logger.isDebugEnabled()) {
                logger.debug("commandlineSsh file: copy local to local");
            }
            copyLocaltoLocal(location, dest);
            return;
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

    protected void copyLocaltoLocal(URI src, URI dest)
            throws GATInvocationException {
        Map<String, String> securityInfo = null;

        try {
            securityInfo = CommandlineSshSecurityUtils.getSshCredential(
                    gatContext, "commandlinessh", dest, ssh_port);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (securityInfo == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }

        String username = securityInfo.get("username");

        /* allow port override */
        int port = src.getPort();
        /* it will always return -1 for user@host:path */
        if (port == -1) {
            port = ssh_port;
        }

        if (gatContext.getPreferences().containsKey("file.create")) {
            if (((String) gatContext.getPreferences().get("file.create"))
                    .equalsIgnoreCase("true")) {
                try {
                    FileInterface destFile = GAT.createFile(gatContext,
                            gatContext.getPreferences(), dest)
                            .getFileInterface();
                    File destinationParentFile = destFile.getParentFile();
                    if (destinationParentFile != null) {
                        destinationParentFile.getFileInterface().mkdirs();
                    }
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException(
                            "CommandlineSshFileAdaptor", e);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("CommandlineSsh: Prepared session for location " + src
                    + " with username: " + username + "; host: localhost");
        }

        ArrayList<String> command = new ArrayList<String>();
        
        command.add("scp");
        
        if (windows) {
            command.add("-unat=yes");
            command.add("-P");
            command.add("" + port);
            
            if (!securityInfo.containsKey("password")) { // public/private
                // key
                int slot = 0;
                try {
                    slot = Integer.parseInt(securityInfo.get("privatekeyslot"));
                } catch (Exception e) {
                    // ignore, use the default value.
                }
                command.add("-pk=" + slot);
            } else { // password               
                command.add(" -pw=" + securityInfo.get("password"));
            }
        } else {
            File source = null;
            boolean dir = false;
            try {
                source = GAT.createFile(gatContext, src);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("commandlineSsh", e);
            }
            if (source.getFileInterface().exists()) {
                if (source.getFileInterface().isDirectory()) {
                    dir = true;
                }
            } else {
                throw new GATInvocationException(
                        "the source file does not exist.");
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
                command.add("-r");
            }
            command.add("-P");
            command.add("" + port);
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=yes");
        }
        command.add(src.getPath());
        command.add(dest.getPath());
            
        if (logger.isInfoEnabled()) {
            logger.info("CommandlineSsh: running command: " + command);
        }
        CommandRunner runner = new CommandRunner(command);
        if (logger.isInfoEnabled()) {
            logger.info("\nstderr: " + runner.getStderr() + "\nstdout: "
                    + runner.getStdout());
        }
        int exitValue = runner.getExitCode();
        // scp exit value seems to be buggy, so only if the exit status > 1 AND
        // there's something in the stderr consider it as a failure. --roelof
        if (exitValue != 0 && runner.getStderr().length() > 0) {
            throw new GATInvocationException("CommandlineSsh command failed: "
                    + runner.getStderr());
        }
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        Map<String, String> securityInfo = null;

        try {
            securityInfo = CommandlineSshSecurityUtils.getSshCredential(
                    gatContext, "commandlinessh", dest, ssh_port);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (securityInfo == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }

        String username = securityInfo.get("username");

        /* allow port override */
        int port = src.getPort();
        /* it will always return -1 for user@host:path */
        if (port == -1) {
            port = ssh_port;
        }

        if (gatContext.getPreferences().containsKey("file.create")) {
            if (((String) gatContext.getPreferences().get("file.create"))
                    .equalsIgnoreCase("true")) {
                try {
                    FileInterface destFile = GAT.createFile(gatContext, dest)
                            .getFileInterface();
                    File destinationParentFile = destFile.getParentFile();
                    if (destinationParentFile != null) {
                        destinationParentFile.getFileInterface().mkdirs();
                    }
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException(
                            "CommandlineSshFileAdaptor", e);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("CommandlineSsh: Prepared session for location " + src
                    + " with username: " + username + "; host: "
                    + src.resolveHost());
        }

        ArrayList<String> command = new ArrayList<String>();
        command.add("scp");
        if (windows) {
            command.add("-unat=yes");           
            command.add("-P");
            command.add("" + port);
            if (!securityInfo.containsKey("password")) { // public/private
                // key
                int slot = 0;
                try {
                    slot = Integer.parseInt(securityInfo.get("privatekeyslot"));
                } catch (Exception e) {
                    // ignore, use the default value.
                }
                command.add("-pk=" + slot);
            } else { // password               
                command.add(" -pw=" + securityInfo.get("password"));
            }
        } else {
            File remote = null;
            boolean dir = false;
            try {
                remote = GAT.createFile(gatContext, src);
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
                command.add("-r");
            }
            command.add("-P");
            command.add("" + port);
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=yes");
        }
        command.add(username + "@" + src.resolveHost() + ":" + src.getPath());
        command.add(dest.getPath());

        if (logger.isInfoEnabled()) {
            logger.info("CommandlineSsh: running command: " + command);
        }
        CommandRunner runner = new CommandRunner(command);
        if (logger.isInfoEnabled()) {
            logger.info("\nstderr: " + runner.getStderr() + "\nstdout: "
                    + runner.getStdout());
        }
        int exitValue = runner.getExitCode();
        // scp exit value seems to be buggy, so only if the exit status > 1 AND
        // there's something in the stderr consider it as a failure. --roelof
        if (exitValue != 0 && runner.getStderr().length() > 0) {
            throw new GATInvocationException("CommandlineSsh command failed: "
                    + runner.getStderr());
        }

    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {
        Map<String, String> securityInfo = null;

        try {
            securityInfo = CommandlineSshSecurityUtils.getSshCredential(
                    gatContext, "commandlinessh", dest, ssh_port);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (securityInfo == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }

        // overwrite the username with the username of the dest, if possible.
        String username = securityInfo.get("username");
        if (dest.getUserInfo() != null) {
            username = dest.getUserInfo();
        }

        /* allow port override */
        int port = dest.getPort();
        /* it will always return -1 for user@host:path */
        if (port == -1) {
            port = ssh_port;
        }

        if (gatContext.getPreferences().containsKey("file.create")) {
            if (((String) gatContext.getPreferences().get("file.create"))
                    .equalsIgnoreCase("true")) {
                try {
                    FileInterface destFile = GAT.createFile(gatContext, dest)
                            .getFileInterface();
                    File destinationParentFile = destFile.getParentFile();
                    if (destinationParentFile != null) {
                        destinationParentFile.getFileInterface().mkdirs();
                    }
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException(
                            "CommandlineSshFileAdaptor", e);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("CommandlineSsh: Prepared session for location "
                    + dest + " with username: " + username + "; host: "
                    + dest.resolveHost());
        }

        ArrayList<String> command = new ArrayList<String>();
        command.add("scp");
        if (windows) {
            command.add("-unat=yes");           
            command.add("-P");
            command.add("" + port);
            if (!securityInfo.containsKey("password")) { // public/private
                // key
                int slot = 0;
                try {
                    slot = Integer.parseInt(securityInfo.get("privatekeyslot"));
                } catch (Exception e) {
                    // ignore, use the default value.
                }
                command.add("-pk=" + slot);
            } else { // password               
                command.add(" -pw=" + securityInfo.get("password"));
            }
        } else {
            if (determineIsDirectory()) {
                File remote = null;
                try {
                    remote = GAT.createFile(gatContext, dest);
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
                command.add("-r");
            }
            command.add("-P");
            command.add("" + port);
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=yes");
        }
        command.add(src.getPath());
        command.add(username + "@" + dest.resolveHost() + ":" + dest.getPath());

        // @@@ this does not work because the * is not understood by scp
        if (logger.isInfoEnabled()) {
            logger.info("CommandlineSsh: running command: " + command);
        }

        CommandRunner runner = new CommandRunner(command);
        if (logger.isInfoEnabled()) {
            logger.info("\nstderr: " + runner.getStderr() + "\nstdout: "
                    + runner.getStdout());
        }
        int exitValue = runner.getExitCode();
        // scp exit value seems to be buggy, so only if the exit status > 1 AND
        // there's something in the stderr consider it as a failure. --roelof
        if (exitValue != 0 && runner.getStderr().length() > 0) {
            throw new GATInvocationException("CommandlineSsh command failed: "
                    + runner.getStderr());
        }

    }

}
