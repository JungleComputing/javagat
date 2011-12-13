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
    
    public SshHelper(GATContext gatContext, URI location, String adaptorName, String prefPortNo, String prefStrict) throws GATObjectCreationException {
	this.gatContext = gatContext;
	this.location = location;
	this.adaptorName = adaptorName;

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            windows = true;
        }
        
        ssh_port = -1;
        /* allow port override */
        if (location.getPort() != -1) {
            ssh_port = location.getPort();
        } else if (prefPortNo != null) {
            String port = (String) gatContext.getPreferences().get(prefPortNo);
            if (port != null) {
                ssh_port = Integer.parseInt(port);
            }
        }
        
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
               
        securityInfo = SshSecurityUtils.getSecurityInfo(gatContext, location, adaptorName, ssh_port);
    }
    
    /**
     * Returns the user name, in case one was specified, either in the security context
     * of in the location. If not, return <code>null</code>.
     * @return the user name, or <code>null</code>.
     */
    public String getUserName() {
	String user = location.getUserInfo();
        if (user != null) {
            return user;
        }
        if (! securityInfo.containsKey("default")) {
            user = securityInfo.get("username");
            if (user != null) {
        	return user;
            }
        }
	return null;
    }
    
    /**
     * Returns a port number, in case one was specified, either in the location or
     * in the preferences. If not, return -1;
     * @return a port number, or -1.
     */
    public int getPort() {
	return ssh_port;
    }
    
    public String getPassword() {
	if (securityInfo.containsKey("password")) {
	    return securityInfo.get("password");
	}
	return null;
    }
    
    public String getSecurityItem(String item) {
	return securityInfo.get(item);
    }
    
    public boolean onWindows() {
	return windows;
    }
    
    /**
     * Runs the specified command with ssh.
     * @param params the command to be executed.
     * @return the CommandRunner, which can be used to obtain output.
     * @throws GATInvocationException in case of failure.
     */
    public CommandRunner runSshCommand(String... params) throws GATInvocationException {
	
	return runSshCommand(false, params);
    }
    
    /**
     * Runs the specified command with ssh.
     * @param stoppable when set, a pseudo tty is allocated, but this has the side-effect that
     *        stderr now appears on stdout.
     * @param params the command to be executed.
     * @return the CommandRunner, which can be used to obtain output.
     * @throws GATInvocationException in case of failure.
     */
    public CommandRunner runSshCommand(boolean stoppable, String... params) throws GATInvocationException {

	ArrayList<String> command = getSshCommand(stoppable);

	return runSshCommand(command, params);
    }
    
    
    /**
     * Runs the specified command with ssh.
     * @param command the initial part of the ssh command.
     * @param params the command to be executed remotely.
     * @return the CommandRunner, which can be used to obtain output.
     * @throws GATInvocationException in case of failure.
     */
    public CommandRunner runSshCommand(List<String> command, String... params) throws GATInvocationException {
	
        for (String p : params) {
            command.add(protectAgainstShellMetas(p));
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
    
    public Process startSshCommand(List<String> cmd, String[] args) throws CommandNotFoundException {

	if (args != null) {
	    for (String arg : args) {
		cmd.add(protectAgainstShellMetas(arg));
	    }
	}

        ProcessBuilder builder = new ProcessBuilder(cmd);

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + cmd);
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
     * @param stoppable whether to allocate a pseudo tty.
     * @param extra extra ssh flags to be added.
     * @return the initial part of an ssh command.
     */
    public ArrayList<String> getSshCommand(boolean stoppable, String... extra) {
        
        ArrayList<String> cmd = new ArrayList<String>();
	
        if (windows) {
            // TODO: for which ssh client is this?
            cmd.add("sexec");
            if (getUserName() != null) {
        	cmd.add(getUserName() + "@" + location.getAuthority());
            } else {
        	cmd.add(location.getAuthority());
            }
            cmd.add("-unat=yes");
            if (ssh_port != -1) {
                cmd.add("-P");
                cmd.add("" + ssh_port);
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
                cmd.add(" -pk=" + slot);
            } else { // password
                cmd.add(" -pw=" + getPassword());
            }
            return cmd;
        }

        cmd.add("/usr/bin/ssh");
        if (ssh_port != -1) {
            cmd.add("-p");
            cmd.add("" + ssh_port);
        }
        cmd.add("-o");
        cmd.add("BatchMode=yes");
        
        if (stoppable) {
            cmd.add("-t");
            cmd.add("-t");
        }
        
        if (strictHostKeyChecking != null) {
            cmd.add("-o");
            cmd.add("StrictHostKeyChecking=" + strictHostKeyChecking);
        }

        for (String e : extra) {
            cmd.add(e);
        }
        
        String username = getUserName();
        if (username != null) {
            cmd.add(username + "@" + location.resolveHost());
        } else {
            cmd.add(location.resolveHost());
        }
        return cmd;
    }
    
    /**
     * Returns the first part of an scp command. Only the files have to be added.
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
