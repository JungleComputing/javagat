package org.gridlab.gat.engine.util;

import java.io.IOException;

import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;

public class CommandRunner {

    private int exitCode;

    private OutputForwarder out;

    private OutputForwarder err;

    /** synchronously run a command */
    public CommandRunner(String command) throws GATInvocationException {
        exitCode = runCommand(command);
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

    /** run a command. Exit code is returned */
    private int runCommand(String command) throws GATInvocationException {
        if (GATEngine.DEBUG) {
            System.err.println("CommandRunner running: " + command);
        }
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command.toString());
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

            if (GATEngine.DEBUG) {
                System.err.println("CommandRunner out: " + out.getResult());
                System.err.println("CommandRunner err: " + err.getResult());
            }

            return exitValue;
        } catch (InterruptedException e) {
            // Cannot happen
            return 1;
        }
    }
}
