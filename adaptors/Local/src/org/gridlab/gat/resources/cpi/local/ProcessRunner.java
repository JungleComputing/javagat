package org.gridlab.gat.resources.cpi.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;

import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRunner implements Runnable {
    
    private static Logger logger = LoggerFactory
            .getLogger(ProcessRunner.class);
    
    private ProcessBuilder builder = new ProcessBuilder();
    private Process process = null;
    private int exitStatus;
    private boolean done = false;

    private int processID;

    public ProcessRunner(String exe, String[] args, File dir, Map<String, Object> env) {
	builder.command().add(exe);
	if (args != null) {
	    for (String arg : args) {
		builder.command().add(arg);
	    }
	}
	builder.directory(dir);
	if (env != null) {
            Map<String, String> e = builder.environment();
            e.clear();
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                builder.environment().put(entry.getKey(),
                        (String) entry.getValue());
            }
	}
	if (logger.isDebugEnabled()) {
	    logger.debug("Created local ProcessRunner for: " +  builder.command());
	}
    }
    
    public void startProcess() throws IOException {
	if (logger.isDebugEnabled()) {
	    logger.debug("Starting local process: " +  builder.command());
	}
	process = builder.start();
        Field f = null;
        try {
            f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            processID = Integer.parseInt(f.get(process).toString());
            // ignore exceptions
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
	ScheduledExecutor.schedule(this, 0, 50);
    }
    
    public OutputStream getStdin() {
	return process.getOutputStream();
    }
    
    public InputStream getStdout() {
	return process.getInputStream();
    }
    
    public InputStream getStderr() {
	return process.getErrorStream();
    }

    @Override
    public void run() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            // ignore
        }
        try {
            exitStatus = process.exitValue();
        } catch (Throwable e) {
            // ignore
        }
	if (logger.isDebugEnabled()) {
	    logger.debug("Local process done: " +  builder.command());
	}
        synchronized(this) {
            done = true;
            notifyAll();
        }
        ScheduledExecutor.remove(this);
    }
        
    public synchronized int getExitStatus() {
	while (! done) {
	    try {
		wait();
	    } catch(Throwable e) {
		// ignore
	    }
	}
        return exitStatus;
    }

    public void kill() {
	if (process != null) {
	    process.destroy();
	}
    }
    
    public synchronized boolean done() {
	return done;
    }
    
    public synchronized void waitFor() {
	while (! done) {
	    try {
		wait();
	    } catch(Throwable e) {
		// ignore
	    }
	}
    }
    
    public int getProcessID() {
        return processID;
    }
}
