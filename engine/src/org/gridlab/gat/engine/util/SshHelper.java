package org.gridlab.gat.engine.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.ssh.SshSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility functions for use with the ssh command.
 */
public class SshHelper {  
    
    protected static Logger logger = LoggerFactory.getLogger(SshHelper.class);
    
    
    private final GATContext gatContext;
    private final URI location;
    private final String adaptorName;
    private boolean windows = false;
    private int ssh_port;
    private final String strictHostKeyChecking;
    private final Map<String, String> securityInfo;
    
    /**
     * Creates an ssh helper object for the specified URI and context.
     * 
     * @param gatContext the GAT context.
     * @param location the location to be used.
     * @param adaptorName the name of the adaptor that uses this helper, for instance "commandlinessh".
     * @param prefPortNo the name of the preference that specifies a port number, or null.
     * @param prefStrict the name of the preference that specifies whether strict host key checking
     *        is to be used, or null.
     * @throws GATObjectCreationException is thrown when the security information could not be loaded
     *        for some reason.
     */
    public SshHelper(GATContext gatContext, URI location, String adaptorName, String prefPortNo, String prefStrict) throws GATObjectCreationException {
	this.gatContext = gatContext;
	this.location = location;
	this.adaptorName = adaptorName;

	// Running on windows?
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            windows = true;
        }
        
        // Allow port override.
        ssh_port = -1;
        if (location.getPort() != -1) {
            ssh_port = location.getPort();
        } else if (prefPortNo != null) {
            String port = (String) gatContext.getPreferences().get(prefPortNo);
            if (port != null) {
                ssh_port = Integer.parseInt(port);
            }
        }
        
        // check if strict host key checking is requested.
        if (prefStrict != null) {
            String s = (String) gatContext.getPreferences().get(prefStrict);
            if ("true".equals(s)) {
        	strictHostKeyChecking="yes";
            } else {
        	strictHostKeyChecking="no";
            }
        } else {
            strictHostKeyChecking = null;
        }
               
        // Obtain security info.
        securityInfo = SshSecurityUtils.getSecurityInfo(gatContext, location, adaptorName, ssh_port);
    }
    
    /**
     * Returns the user name, in case one was specified, either in the security context
     * or in the location. If not, return <code>null</code>.
     * 
     * @return the user name, or <code>null</code>.
     */
    public String getUserName() {
	
	// Check if the location contains user info.
	String user = location.getUserInfo();
        if (user != null) {
            return user;
        }
        
        // Check if the securityInfo contains a user name,
        // but only if it is not a default (JavaGAT-inserted) security info.
        if (! securityInfo.containsKey("default")) {
            return securityInfo.get("username");
        }
	return null;
    }
    
    /**
     * Returns a port number, in case one was specified, either in the location or
     * in the preferences. If not, return -1;
     * 
     * @return a port number, or -1.
     */
    public int getPort() {
	return ssh_port;
    }
    
    /**
     * Returns the password, in case one was specified in the security context used.
     * 
     * @return the password, or null if not present.
     */
    public String getPassword() {
	return securityInfo.get("password");
    }
    
    /**
     * Returns whether we are running on windows.
     * 
     * @return <code>true</code> if we are running on windows, <code>false</code> otherwise.
     */
    public boolean onWindows() {
	return windows;
    }
    
    /**
     * Runs the specified command with ssh, synchronously. The returned CommandRunner
     * can be used to obtain stdout and stderr.
     * 
     * @param params the command to be executed.
     * @return the CommandRunner, which can be used to obtain output.
     * @throws GATInvocationException in case of failure.
     */
    public CommandRunner runSshCommand(String... params) throws GATInvocationException {
	
	return runSshCommand(false, params);
    }
    
    /**
     * Runs the specified command with ssh, synchronously. The returned CommandRunner
     * can be used to obtain stdout and stderr.

     * @param stoppable when set, a pseudo tty is allocated, but this has the side-effect that
     *        stderr now appears on stdout.
     * @param params the command to be executed.
     * @return the CommandRunner, which can be used to obtain output.
     * @throws GATInvocationException in case of failure.
     */
    public CommandRunner runSshCommand(boolean stoppable, String... params) throws GATInvocationException {

	return runSshCommand(getSshCommand(stoppable), params);
    }
    
    
    /**
     * Runs the specified command with ssh, synchronously. The returned CommandRunner
     * can be used to obtain stdout and stderr.
     * 
     * @param command the initial part of the ssh command.
     * @param params the command to be executed remotely.
     * @return the CommandRunner, which can be used to obtain output.
     * @throws GATInvocationException in case of failure.
     */
    public CommandRunner runSshCommand(List<String> command, String... params) throws GATInvocationException {
	
	// Add the remote command, but protect against shell expansion.
        for (String p : params) {
            command.add(protectAgainstShellMetas(p));
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("running command = " + command);
        }
        
        // Start the command. CommandRunner will catch the stdout and stderr.
        CommandRunner runner = new CommandRunner(command);
        
        // CommandRunner.getExitCode() implies a wait.
        int exitVal = runner.getExitCode();
        
        if (logger.isDebugEnabled()) {
            logger.debug("exitCode=" + exitVal);
        }
        
        if (exitVal == 255) {
            // 255 means ssh error and not false as command response.
            if (logger.isInfoEnabled()) {
                logger.info("command failed, error=" + runner.getStderr());
            }
            throw new GATInvocationException("invocation error");
        }
        
        return runner;
    }
    
    /**
     * Starts the specified command with ssh, asynchronously. The returned Process
     * can be used to obtain stdout and stderr, and also to obtain the exit status
     * or kill the job.
     * 
     * @param command the initial part of the ssh command.
     * @param params the command to be executed remotely.
     * @return the Process.
     * @throws CommandNotFoundException in case of failure.
     */
    public Process startSshCommand(List<String> command, String[] params) throws CommandNotFoundException {

	if (params != null) {
	    for (String arg : params) {
		command.add(protectAgainstShellMetas(arg));
	    }
	}

        ProcessBuilder builder = new ProcessBuilder(command);

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + command);
        }
       
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            throw new CommandNotFoundException(
                    "builder.start() fails", e);
        }
	return p;
    }
         
    /**
     * Generates a "version" of the parameter that survives shell meta
     * characters.
     * 
     * @param s the parameter to be protected.
     * @return the protected string.
     */
    public static String protectAgainstShellMetas(String s) {
        char[] chars = s.toCharArray();
        StringBuffer b = new StringBuffer();
        b.append('\'');
        for (char c : chars) {
            if (c == '\'') {
                b.append('\'');
                b.append('\\');
                b.append('\'');
            }
            b.append(c);
        }
        b.append('\'');
        return b.toString();
    }

    
    /* Not now. We'll need this when we start supporting windows ssh servers.
    // Protect against special characters for the windows command line interpreter.
    private static String protectAgainstWindowsMetas(String s) {
        char[] chars = s.toCharArray();
        StringBuffer b = new StringBuffer();
        for (char c : chars) {
            if ("\"&()^;| ".indexOf(c) >= 0) {
                b.append('^');
            }
            b.append(c);
        }
        return b.toString();
    }
    */
    
    /**
     * Returns the initial part of an ssh command. Only the command to be executed remotely
     * needs to be added to obtain a full command line.
     * 
     * @param stoppable whether to allocate a pseudo tty.
     * @return the initial part of an ssh command.
     */
    public ArrayList<String> getSshCommand(boolean stoppable) {
        
        ArrayList<String> command = new ArrayList<String>();
	
        if (windows) {
            // TODO: for which ssh client is this? Looks like Bitvise Tunnelier.
            command.add("sexec");
            if (getUserName() != null) {
        	command.add(getUserName() + "@" + location.getAuthority());
            } else {
        	command.add(location.getAuthority());
            }
            command.add("-unat=yes");
            if (ssh_port != -1) {
                command.add("-P");
                command.add("" + ssh_port);
            }
            int privateKeySlot = -1;
            try {
                String v = securityInfo.get("privatekeyslot");
                if (v != null) {
                    privateKeySlot = Integer.parseInt(v);
                }
            } catch (NumberFormatException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to parse private key slot: " + e);
                }
            }
            if (getPassword() == null) { // public/private key
                int slot = privateKeySlot;
                if (slot == -1) { // not set by the user, assume he only has
                    // one key
                    slot = 0;
                }
                command.add(" -pk=" + slot);
            } else { // password
                command.add(" -pw=" + getPassword());
            }
   
            return command;
        }

        command.add("ssh");
        if (ssh_port != -1) {
            command.add("-p");
            command.add("" + ssh_port);
        }
        command.add("-o");
        command.add("BatchMode=yes");
        
        // Add pseudo terminal if you want to be able to kill the job.
        if (stoppable) {
            command.add("-t");
            command.add("-t");
        }
        
        if (strictHostKeyChecking != null) {
            command.add("-o");
            command.add("StrictHostKeyChecking=" + strictHostKeyChecking);
        }

        String username = getUserName();
        if (username != null) {
            command.add(username + "@" + location.resolveHost());
        } else {
            command.add(location.resolveHost());
        }
        return command;
    }
    
    /**
     * Returns the first part of an scp command. Only the files have to be added.
     * Note: this method should be called from an SshHelper instance that represents
     * the remote side!
     * 
     * @return the first part of an scp command.
     */
    public ArrayList<String> getScpCommand()  {

        ArrayList<String> command = new ArrayList<String>();
        
        command.add("scp");
        
        if (windows) {
            // TODO: for which ssh client is this?
            command.add("-unat=yes");
            if (ssh_port != -1) {
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
            if (ssh_port != -1) {
                command.add("-P");
                command.add("" + ssh_port);
            }
            command.add("-o");
            command.add("BatchMode=yes");
            
            if (strictHostKeyChecking != null) {
                command.add("-o");
                command.add("StrictHostKeyChecking=" + strictHostKeyChecking);
            }

            command.add("-p");  // preserve time/mode
        }
        return command;
    }
}
