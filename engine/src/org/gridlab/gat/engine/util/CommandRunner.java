package org.gridlab.gat.engine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GATInvocationException;

public class CommandRunner {

    protected static Logger logger = LoggerFactory.getLogger(CommandRunner.class);

    private int exitCode;

    private OutputForwarder out;

    private OutputForwarder err;

    private static final String[] path;

    static {
        String PATH = System.getenv("PATH");
        if (PATH != null) {
            StringTokenizer s = new StringTokenizer(PATH, File.pathSeparator);
            int n = s.countTokens();
            path = new String[n];
            for (int i = 0; i < n; i++) {
                path[i] = s.nextToken();
            }
        } else {
            path = null;
        }
    }

    /** synchronously run a command */
    public CommandRunner(String... command) throws GATInvocationException {
        exitCode = runCommand(command, null);
    }

    public CommandRunner(List<String> command) throws GATInvocationException {
        exitCode = runCommand(command.toArray(new String[command.size()]), null);
    }

    public CommandRunner(List<String> command, String workingDir)
            throws GATInvocationException {
        exitCode = runCommand(command.toArray(new String[command.size()]),
                workingDir);
    }

    public String getStdout() {
        return out.getResult().toString();
    }

    public String getStderr() {
        return err.getResult().toString();
    }

    public int getExitCode() {
        return exitCode;
    }

    private String getExeFile(String exe) {
        if (path != null) {
            for (String s : path) {
                if (! "".equals(s)) {
                    String e = s  + File.separator + exe;
                    File f = new File(e);
                    if (f.exists()) {
                        // TODO: if we migrate to java 6, we can use f.canExecute().
                        return e;
                    }
                } else {
                    File f = new File(exe);
                    if (f.exists()) {
                        // TODO: if we migrate to java 6, we can use f.canExecute().
                        return exe;
                    }
                }
            }
        }
        return exe;
    }

    /** run a command. Exit code is returned */
    private int runCommand(String[] command, String dir)
            throws GATInvocationException {
        if (command.length == 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "runCommand: command array has length 0");
        }
        command[0] = getExeFile(command[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("CommandRunner running: " + Arrays.toString(command));
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        if (dir != null) {
            builder.directory(new File(dir));
        }
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            throw new CommandNotFoundException("CommandRunner", e);
        }

        // close stdin.
        try {
            p.getOutputStream().close();
        } catch (Throwable e) {
            // ignore
        }

        // we must always read the output and error streams to avoid deadlocks
        out = new OutputForwarder(p.getInputStream(), true);
        err = new OutputForwarder(p.getErrorStream(), true);

        try {
            int exitValue = p.waitFor();

            // Wait for the output forwarders to finish!
            // You may lose output if you don't -- Jason
            out.waitUntilFinished();
            err.waitUntilFinished();

            if (logger.isDebugEnabled()) {
                logger.debug("CommandRunner out: " + out.getResult() + "\n"
                        + "CommandRunner err: " + err.getResult());
            }

            return exitValue;
        } catch (InterruptedException e) {
            // Cannot happen
            return 1;
        }
    }
    
    /**
     * Returns either the stdout or the stderr, depending on the exit status of the job,
     * as an array of strings, one entry for each line.
     * 
     * @return a list of strings, one entry for each line.
     * @throws IOException on IO error.
     */
    public List<String> outputAsLines() throws IOException {
	
	BufferedReader br = null;
	ArrayList<String> result = new ArrayList<String>();
	try {
	    String line;	   
	    if (getExitCode() == 0) {
		br = new BufferedReader(new StringReader(getStdout()));
	    } else {
		br = new BufferedReader(new StringReader(getStderr()));
	    }
	    while ((line = br.readLine()) != null) {
		result.add(line);
	    }
	} finally {
	    if (br != null) {
		br.close();
	    }
	}
	
	if (logger.isDebugEnabled()) {
	    for (String s : result) {
		logger.debug("Line: " + s);
	    }
	}
	return result;
    }
}
