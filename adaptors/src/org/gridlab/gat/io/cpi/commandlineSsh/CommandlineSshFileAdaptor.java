package org.gridlab.gat.io.cpi.commandlineSsh;

import java.io.IOException;

import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

public class CommandlineSshFileAdaptor extends FileCpi {
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
                    throw new GATObjectCreationException("cannot handle this URI: " + location);
                }
                
		String osname = System.getProperty("os.name");
		if (osname.startsWith("Windows"))
			windows = true;
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

                // @@@ if we find a solution for the "scp -r" problem, this code should
                // be commented out
                // create a seperate file object to determine whether the source
                // is a directory. This is needed, because the source might be a local
                // file, and gridftp might not be installed locally.
                // This goes wrong for local -> remote copies.
                if (determineIsDirectory()) {
                    copyDirectory(gatContext, preferences, toURI(), dest);
                    return;
                }
                
		if (dest.refersToLocalHost()) {
			if (GATEngine.DEBUG) {
				System.err.println("commandlineSsh file: copy remote to local");
			}

			copyToLocal(location, dest);
			return;
		}

		if (toURI().refersToLocalHost()) {
			if (GATEngine.DEBUG) {
				System.err.println("commandlineSsh file: copy local to remote");
			}

			copyToRemote(location, dest);
			return;
		}

		// source is remote, dest is remote.
		throw new GATInvocationException("commandlineSsh: cannot do third party copy");
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

		// to be modified, this part goes inside the SSHSecurityUtils
		if (src.getUserInfo() != null) {
			sui.username = src.getUserInfo();
		}

		/* allow port override */
		int port = src.getPort();
		/* it will always return -1 for user@host:path */
		if (port == -1) {
			port = SSH_PORT;
		}

		if (GATEngine.DEBUG) {
			System.err.println("CommandlineSsh: Prepared session for location "
					+ src + " with username: " + sui.username + "; host: "
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

			command += sui.username + "@" + src.resolveHost() + ":" + src.getPath()
					+ " " + dest.getPath();
		} else {

                    
                    
                    File remote = null;
                    boolean dir = false;
                    try {
                        remote = GAT.createFile(gatContext, preferences, src);
                    } catch (GATObjectCreationException e) {
                        throw new GATInvocationException("commandlineSsh", e);
                    }
                    if(remote.exists()) {
                        if(remote.isDirectory()) {
                            dir = true;
                        }
                    } else {
                        throw new GATInvocationException("the remote file does not exist.");
                    }

                    if(dir) {
                        java.io.File local = new java.io.File(dest.getPath());
                        if(local.exists()) {
                            if(!local.isDirectory()) {
                                throw new GATInvocationException("local destination already exists, and it is not a directory");
                            }
                        } else {
                            if(!local.mkdir()) {
                                throw new GATInvocationException("could not create local dir");
                            }
                        }
                        
                        command = "scp -r "
                                + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                                + sui.username + "@" + src.resolveHost() + ":" + src.getPath()
                                + "/* " + dest.getPath();
                    } else {
                        command = "scp "
					+ "-o BatchMode=yes -o StrictHostKeyChecking=yes "
					+ sui.username + "@" + src.resolveHost() + ":" + src.getPath()
					+ " " + dest.getPath();
                    }
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

		if (GATEngine.DEBUG) {
			System.err.println("CommandlineSsh: Prepared session for location "
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
                    if(determineIsDirectory()) {
                        File remote = null;
                        try {
                            remote = GAT.createFile(gatContext, preferences, dest);
                        } catch (GATObjectCreationException e) {
                            throw new GATInvocationException("commandlineSsh", e);
                        }
                        if(remote.exists()) {
                            if(!remote.isDirectory()) {
                                throw new GATInvocationException("remote destination already exists, and it is not a directory");
                            }
                        } else {
                            if(!remote.mkdir()) {
                                throw new GATInvocationException("could not create remote dir");
                            }
                        }
                        
                        command = "scp -r "
                                + "-o BatchMode=yes -o StrictHostKeyChecking=yes "
                                + src.getPath() + "/* " + sui.username + "@" + dest.resolveHost()
                                + ":" + dest.getPath();                        
                    } else {
			command = "scp "
					+ "-o BatchMode=yes -o StrictHostKeyChecking=yes "
					+ src.getPath() + " " + sui.username + "@" + dest.resolveHost()
					+ ":" + dest.getPath();
                    }
		}

                
//                @@@ this does not work because the * is not understood by scp
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
		OutputForwarder out = new OutputForwarder(p.getInputStream(), true); // throw
																				// away
																				// output
		OutputForwarder err = new OutputForwarder(p.getErrorStream(), true); // throw
																				// away
																				// output

		try {
			int exitValue = p.waitFor();

			// Wait for the output forwarders to finish!
			// You may lose output if you don't -- Jason
			out.waitUntilFinished();
			err.waitUntilFinished();

                        System.err.println("OUT: " + out.getResult());
                        System.err.println("ERR: " + err.getResult());
                        
			return exitValue;
		} catch (InterruptedException e) {
			// Cannot happen
			return 1;
		}
	}
}
