package org.gridlab.gat.io.cpi.commandlineSsh;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.gridlab.gat.engine.util.SshHelper;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;

@SuppressWarnings("serial")
public class CommandlineSshFileAdaptor extends FileCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("canRead", true);
        capabilities.put("canWrite", true);
        capabilities.put("copy", true);
        capabilities.put("createNewFile", true);  
        capabilities.put("delete", true);
        capabilities.put("exists", true);
        capabilities.put("getAbsolutePath", true);
        capabilities.put("getAbsoluteFile", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("lastModified", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        capabilities.put("mkdirs", true);
        capabilities.put("setLastModified", true);
        return capabilities;
    }
    
    private static final String SSH_PORT_STRING = "commandlinessh.ssh.port";
    
    private static final String SSH_STRICT_HOST_KEY_CHECKING = "commandlinessh.StrictHostKeyChecking";
    
    public static final int SSH_PORT = 22;

    public static String getDescription() {
        return "The CommandlineSsh File Adaptor implements the File object using ssh commands.";
    }
   
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

    private final URI fixedURI;
    
    private final SshHelper locationUtils;
    
    private final java.io.File localFile;
    
    /**
     * @param gatContext
     * @param location
     */
    public CommandlineSshFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        fixedURI = fixURI(location, null);
        
        if (fixedURI.isCompatible("file") && fixedURI.refersToLocalHost()) {
            localFile = new java.io.File(fixedURI.getPath());
        } else {
            localFile = null;
        }
        
        locationUtils = new SshHelper(gatContext, location, "commandlinessh", SSH_PORT_STRING, SSH_STRICT_HOST_KEY_CHECKING);

        // TODO: test if remote machine is windows, and if so, fail.	    
    }

    private boolean runSshCommand(String... params)
            throws GATInvocationException {
	return locationUtils.runSshCommand(params).getExitCode() == 0;
    }
    
    public long lastModified() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.lastModified();
	}
	
	CommandRunner command = locationUtils.runSshCommand("stat", "-c", "%Y", fixedURI.getPath());
	
	if (command.getExitCode() != 0) {
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error = " + command.getStderr());
            }
            // command failed, but ssh did not --> assume non-existing.
            return 0;
        }
	String result = command.getStdout();
        return Long.parseLong(result.replaceAll("\\s", "")) * 1000L;
    }
    
    private String toTouchDateFormat(long date) {
        Date d = new Date(date);
        // see man touch for the date format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
        return formatter.format(d);
    }
    
    public boolean setLastModified(long lastModified)
            throws GATInvocationException {
	if (! exists()) {
	    return false;
	}
        if (localFile != null) {
            return localFile.setLastModified(lastModified);
        }
        return runSshCommand("touch", "-c", "-t", toTouchDateFormat(lastModified), fixedURI.getPath());
    }


    public long length() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.length();
	}
        CommandRunner command = locationUtils.runSshCommand("wc","-c", fixedURI.getPath());
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
	if (localFile != null) {
	    return localFile.list();
	}
        CommandRunner command = locationUtils.runSshCommand("ls","-1", fixedURI.getPath());
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
    
    public File getAbsoluteFile()
            throws GATInvocationException {
        String absUri = fixedURI.toString().replace(fixedURI.getPath(),
                getAbsolutePath());
        try {
            return GAT.createFile(gatContext, new URI(absUri));
        } catch (Exception e) {
            return null; // never executed
        }
    }

    public String getAbsolutePath() throws GATInvocationException {
	
	if (localFile != null) {
	    return localFile.getAbsolutePath();
	}
	
	String fixed = fixedURI.getPath();
	if (fixed.startsWith("/")) {
	    return fixed;
	}

	CommandRunner command = locationUtils.runSshCommand("echo","~");
	if (command.getExitCode() != 0) {
	    if (logger.isInfoEnabled()) {
		logger.info("command failed, error = " + command.getStderr());
	    }
	    throw new GATInvocationException("Could not execute \"echo ~\"");
	}
	
	String result = command.getStdout();

	return result.replace("\n", "") + "/" + fixed;        
    }

    public boolean mkdir() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.mkdir();
	}
        return runSshCommand("mkdir", fixedURI.getPath());
    }
    
    public boolean mkdirs() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.mkdirs();
	}
        return runSshCommand("mkdir", "-p", fixedURI.getPath());
    }
    
    public boolean createNewFile() throws GATInvocationException {
	if (localFile != null) {
	    try {
		return localFile.createNewFile();
	    } catch (IOException e) {
		throw new GATInvocationException("local createNewFile failed", e);
	    }
	}
        // TODO: this is not atomic.
        if (exists()) {
            return false;
        }
        return runSshCommand("touch", fixedURI.getPath());
    }

    public boolean delete() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.delete();
	}
        if (!exists()) {
            return false;
        }
        return runSshCommand("rm",  "-rf", fixedURI.getPath());
    }

    public boolean isDirectory() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.isDirectory();
	}
        return runSshCommand("test",  "-d", fixedURI.getPath());
    }

    public boolean isFile() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.isFile();
	}
        return runSshCommand("test",  "-f", fixedURI.getPath());
    }

    public boolean exists() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.exists();
	}
        // Use "/usr/bin/test", not "test". Solaris version of /bin/sh does not
        // recognize the -e option ... --Ceriel
        return runSshCommand("/usr/bin/test", "-e", fixedURI.getPath());
    }
    
    public boolean canRead() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.canRead();
	}
        return runSshCommand("/usr/bin/test", "-r", fixedURI.getPath());
    }
    
    public boolean canWrite() throws GATInvocationException {
	if (localFile != null) {
	    return localFile.canWrite();
	}
        return runSshCommand("/usr/bin/test", "-w", fixedURI.getPath());
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
        if (localFile != null && dest.isCompatible("file") && dest.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("commandlineSsh file: copy local to local");
            }
            copyLocaltoLocal(dest);
            return;
        }

        if (dest.isCompatible("file") && dest.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("commandlineSsh file: copy remote to local");
            }

            copyToLocal(dest);
            return;
        }

        if (localFile != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("commandlineSsh file: copy local to remote");
            }

            copyToRemote(dest);
            return;
        }

        // source is remote, dest is remote.
        throw new GATInvocationException(
                "commandlineSsh: cannot do third party copy");
    }

    protected void copyLocaltoLocal(URI dest)
            throws GATInvocationException {

        if (gatContext.getPreferences().containsKey("file.create")) {
            if (((String) gatContext.getPreferences().get("file.create"))
                    .equalsIgnoreCase("true")) {
        	java.io.File destFile = new java.io.File(dest.getPath());
        	java.io.File destinationParentFile = destFile.getParentFile();
        	if (destinationParentFile != null) {
        	    destinationParentFile.mkdirs();
        	}
            }
        }

        ArrayList<String> command = new ArrayList<String>();
        command.add("cp");

        boolean dir = false;

        if (exists()) {
            if (isDirectory()) {
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

        command.add(getPath());
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
 
        if (exitValue != 0) {
            throw new GATInvocationException("CommandlineSsh command failed: "
                    + runner.getStderr());
        }
    }

    protected void copyToLocal(URI dest) throws GATInvocationException {

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
            logger.debug("CommandlineSsh: Prepared session for location " + location
                    + "; host: " + location.resolveHost());
        }

        ArrayList<String> command = locationUtils.getScpCommand();

        boolean dir = false;

        if (exists()) {
            if (isDirectory()) {
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
        
        String  username = locationUtils.getUserName();
        
        if (username != null) {
            command.add(username + "@" + location.resolveHost() + ":" + location.getPath());
        } else {
            command.add(location.resolveHost() + ":" + location.getPath());
        }
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

    protected void copyToRemote(URI dest)
            throws GATInvocationException {

        if (! recognizedScheme(dest.getScheme(), getSupportedSchemes())) {
            throw new GATInvocationException("CommandlineSshFileAdaptor: unrecognized scheme");
        }
        
	SshHelper destUtils;
	
	try {
	    destUtils = new SshHelper(gatContext, dest, "commandlinessh", SSH_PORT_STRING, SSH_STRICT_HOST_KEY_CHECKING);
	} catch (GATObjectCreationException e1) {
	    Throwable e = e1.getCause();
	    if (e1 != null) {
		throw new GATInvocationException(e1.getSuperMessage(), e);
	    } else {
		throw new GATInvocationException(e1.getSuperMessage(), e1);
	    }
	}

	String username = destUtils.getUserName();

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
                    + dest + "; host: "
                    + dest.resolveHost());
        }

        ArrayList<String> command = destUtils.getScpCommand();
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
        command.add(location.getPath());
        if (username != null) {
            command.add(username + "@" + dest.resolveHost() + ":" + dest.getPath());
        } else {
            command.add(dest.resolveHost() + ":" + dest.getPath());
        }

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
