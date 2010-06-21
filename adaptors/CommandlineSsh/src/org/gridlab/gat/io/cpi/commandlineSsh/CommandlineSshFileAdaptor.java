package org.gridlab.gat.io.cpi.commandlineSsh;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        capabilities.put("createNewFile", true);  
        capabilities.put("delete", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        capabilities.put("exists", true);
        return capabilities;
    }
    
    private static final String SSH_PORT_STRING = "commandlinessh.ssh.port";
    
    private static final String SSH_STRICT_HOST_KEY_CHECKING = "commandlinessh.StrictHostKeyChecking";
    
    public static final int SSH_PORT = 22;
   
    public static Preferences getSupportedPreferences() {
        Preferences p = FileCpi.getSupportedPreferences();
        p.put(SSH_PORT_STRING, "" + SSH_PORT);
        p.put(SSH_STRICT_HOST_KEY_CHECKING, "false");
        return p;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "commandlinessh", "ssh", "file", ""};
    }
    
    protected static Logger logger = LoggerFactory
            .getLogger(CommandlineSshFileAdaptor.class);

    private boolean windows = false;

    private final int ssh_port;
    
    private final boolean strictHostKeyChecking;
    
    private URI fixedURI;
    
    private Map<String, String> securityInfo = null;
    
    /**
     * @param gatContext
     * @param location
     */
    public CommandlineSshFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        fixedURI = fixURI(location, null);

        String osname = System.getProperty("os.name");
        
        // local machine is windows?
        if (osname.startsWith("Windows"))
            windows = true;
        
        // TODO: test if remote machine is windows, and if so, fail.

        /* allow port override */
        if (location.getPort() != -1) {
            ssh_port = location.getPort();
        } else {
            String port = (String) gatContext.getPreferences().get(SSH_PORT_STRING);
            if (port != null) {
                ssh_port = Integer.parseInt(port);
            } else {
                ssh_port = SSH_PORT;
            }
        }
        
        strictHostKeyChecking = ((String) gatContext.getPreferences().get(SSH_STRICT_HOST_KEY_CHECKING, "false"))
                .equalsIgnoreCase("");
        
        try {
            securityInfo = CommandlineSshSecurityUtils.getSshCredential(
                    gatContext, "commandlinessh", fixedURI, ssh_port);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (securityInfo == null) {
            throw new GATObjectCreationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
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
        
        CommandRunner runner = runSshCommand(params);

        int exitVal = runner.getExitCode();

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
    
    private CommandRunner runSshCommand(String... params) throws GATInvocationException {
        ArrayList<String> command = getSshCommand();
        for (String p : params) {
            command.add(p);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("running command = " + command);
        }
        
        CommandRunner runner = new CommandRunner(command);

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
        
        return runner;
    }
 
    
    public long length() throws GATInvocationException {
        CommandRunner command = runSshCommand("wc","-c", fixedURI.getPath());
        if (command.getExitCode() != 0) {
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error = " + command.getStderr());
            }
            // command failed, but ssh did not --> assume non-existing.
            return 0;
        }

        String result = command.getStdout();
        return Long.parseLong(result.replaceAll(" .*\\s?", ""));
    }
    
    public String[] list() throws GATInvocationException {
        CommandRunner command = runSshCommand("ls","-1", fixedURI.getPath());
        if (command.getExitCode() != 0) {
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error = " + command.getStderr());
            }
            // command failed, but ssh did not --> assume non-directory.
            return null;
        }

        String result = command.getStdout();
        if (result == null || result.equals("")) {
            return new String[0];
        }
        return result.split("\n");
    }
    
    public File[] listFiles() throws GATInvocationException {
        try {
            String[] f = list();
            if (f == null) {
                return null;
            }
            File[] res = new File[f.length];

            for (int i = 0; i < f.length; i++) {
                String uri = fixedURI.toString();

                if (!uri.endsWith("/")) {
                    uri += "/";
                }

                uri += f[i];
                res[i] = GAT.createFile(gatContext, new URI(uri));
            }

            return res;
        } catch (Exception e) {
            throw new GATInvocationException("file cpi", e);
        }
    }

    public boolean mkdir() throws GATInvocationException {
        return runSshCommand(true, "mkdir", fixedURI.getPath());
    }
    
    public boolean createNewFile() throws GATInvocationException {
        // TODO: this is not atomic.
        if (exists()) {
            return false;
        }
        return runSshCommand(true, "touch", fixedURI.getPath());
    }

    public boolean delete() throws GATInvocationException {
        if (!exists()) {
            return false;
        }
        return runSshCommand(true, "rm",  "-rf", fixedURI.getPath());
    }

    public boolean isDirectory() throws GATInvocationException {
        return runSshCommand(true, "test",  "-d", fixedURI.getPath());
    }

    public boolean isFile() throws GATInvocationException {
        return runSshCommand(true, "test",  "-f", fixedURI.getPath());
    }

    public boolean exists() throws GATInvocationException {
        // Use "/usr/bin/test", not "test". Solaris version of /bin/sh does not
        // recognize the -e option ... --Ceriel
        return runSshCommand(true, "/usr/bin/test", "-e", fixedURI.getPath());
    }

    private ArrayList<String> getSshCommand() throws GATInvocationException {
        
        if (windows) {
            throw new UnsupportedOperationException("Not implemented");
        }
        ArrayList<String> cmd = new ArrayList<String>();

        cmd.add("ssh");
        if (ssh_port != SSH_PORT) {
            cmd.add("-p");
            cmd.add("" + ssh_port);
        }
        cmd.add("-o");
        cmd.add("BatchMode=yes");
        cmd.add("-o");
        cmd.add("StrictHostKeyChecking=" + (strictHostKeyChecking ? "yes" : "no"));
        
        String username = securityInfo.get("username");
        if (location.getUserInfo() != null) {
            username = location.getUserInfo();
        }

        cmd.add(username + "@" + location.resolveHost());
        return cmd;
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param dest
     *                The new location
     */
    public void copy(URI dest) throws GATInvocationException {
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        dest = fixURI(dest, null);
        if (fixedURI.refersToLocalHost() && dest.refersToLocalHost()) {
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

        if (fixedURI.refersToLocalHost()) {
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
                    + "; host: localhost");
        }

        ArrayList<String> command = new ArrayList<String>();
        
        command.add("scp");
        
        if (windows) {
            command.add("-unat=yes");
            if (ssh_port != SSH_PORT) {
                command.add("-P");
                command.add("" + ssh_port);
            }
            
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
            if (ssh_port != SSH_PORT) {
                command.add("-P");
                command.add("" + ssh_port);
            }
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=" + (strictHostKeyChecking ? "yes" : "no"));
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

        String username = securityInfo.get("username");
        if (src.getUserInfo() != null) {
            username = src.getUserInfo();
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
            if (ssh_port != SSH_PORT) {
                command.add("-P");
                command.add("" + ssh_port);
            }
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
            if (ssh_port != SSH_PORT) {
                command.add("-P");
                command.add("" + ssh_port);
            }
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=" + (strictHostKeyChecking ? "yes" : "no"));
            command.add("-p");  // preserve time/mode
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
        Map<String, String> destSecurityInfo = null;
        if (! recognizedScheme(dest.getScheme(), getSupportedSchemes())) {
            throw new GATInvocationException("CommandlineSshFileAdaptor: unrecognized scheme");
        }
        try {
            destSecurityInfo = CommandlineSshSecurityUtils.getSshCredential(
                    gatContext, "commandlinessh", dest, ssh_port);
        } catch (Exception e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
        }

        if (destSecurityInfo == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        if (destSecurityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }

        // overwrite the username with the username of the dest, if possible.
        String username = destSecurityInfo.get("username");
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
            if (!destSecurityInfo.containsKey("password")) { // public/private
                // key
                int slot = 0;
                try {
                    slot = Integer.parseInt(destSecurityInfo.get("privatekeyslot"));
                } catch (Exception e) {
                    // ignore, use the default value.
                }
                command.add("-pk=" + slot);
            } else { // password               
                command.add(" -pw=" + destSecurityInfo.get("password"));
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
            command.add("StrictHostKeyChecking=" + (strictHostKeyChecking ? "yes" : "no"));
            command.add("-p");  // preserve time/mode
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
