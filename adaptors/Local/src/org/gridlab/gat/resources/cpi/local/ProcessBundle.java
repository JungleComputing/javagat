package org.gridlab.gat.resources.cpi.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.gridlab.gat.engine.util.StreamForwarder;

public class ProcessBundle implements Runnable {
    
    private static class OutputStreamSplitter extends OutputStream {
	
	private OutputStream[] streams;
	
	public OutputStreamSplitter(ProcessRunner[] bundle) {
	    streams = new OutputStream[bundle.length];
	    for (int i = 0; i < streams.length; i++) {
		streams[i] = bundle[i].getStdin();
	    }
	}

	@Override
	public void close() throws IOException {
	    IOException ex = null;
	    for (OutputStream s : streams) {
		try {
		    s.close();
		} catch(IOException e) {
		    if (ex == null) {
			ex = e;
		    }
		}
	    }
	    if (ex != null) {
		throw ex;
	    }
	}

	@Override
	public void flush() throws IOException {
	    IOException ex = null;
	    for (OutputStream s : streams) {
		try {
		    s.flush();
		} catch(IOException e) {
		    if (ex == null) {
			ex = e;
		    }
		}
	    }
	    if (ex != null) {
		throw ex;
	    }
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
	    IOException ex = null;
	    for (OutputStream s : streams) {
		try {
		    s.write(b, off, len);
		} catch(IOException e) {
		    if (ex == null) {
			ex = e;
		    }
		}
	    }
	    if (ex != null) {
		throw ex;
	    }
	}

	@Override
	public void write(byte[] b) throws IOException {
	    write(b, 0, b.length);
	}

	@Override
	public void write(int arg0) throws IOException {
	    IOException ex = null;
	    for (OutputStream s : streams) {
		try {
		    s.write(arg0);
		} catch(IOException e) {
		    if (ex == null) {
			ex = e;
		    }
		}
	    }
	    if (ex != null) {
		throw ex;
	    }
	}

    }
    

    private static class MergingInputStream extends InputStream { 

	private Reader[] readerThreads;
	private int index;
	private int closedStreams = 0;

	public MergingInputStream(InputStream... streams) 
	{ 
	    readerThreads = new Reader[streams.length];
	    for (int i = 0; i < streams.length; i++) { 
		readerThreads[i] = new Reader(this, streams[i]);
		ScheduledExecutor.schedule(readerThreads[i], 0, 50);
	    }
	    index = 0;
	}

	public synchronized int read() throws IOException { 
	    while (closedStreams != readerThreads.length) { 
		for (int i = 0; i < readerThreads.length; i++) { 
		    Reader rd = readerThreads[index];
		    if (rd.available > 0) { 
			return rd.read();
		    }
		    index += 1;
		    if (index == readerThreads.length) { 
			index = 0;
		    }
		} 
		try { 
		    wait();
		} catch(InterruptedException ex) { 
		    // ignore
		}
	    }
	    return -1;
	}

	public synchronized int read(byte b[], int off, int len) throws IOException
	{
	    while (closedStreams != readerThreads.length) { 
		for (int i = 0; i < readerThreads.length; i++) { 
		    Reader rd = readerThreads[index];
		    if (rd.available > 0) { 
			return rd.read(b, off, len);
		    }
		    index += 1;
		    if (index == readerThreads.length) { 
			index = 0;
		    }
		} 	
		try { 
		    wait();
		} catch(InterruptedException ex) { 
		    // ignored
		}
	    }
	    return -1;
	} 

	public synchronized void close() throws IOException { 
	    for (Reader r : readerThreads) {
		if (r.available >= 0) {
		    r.close();
		}
	    }
	    closedStreams = readerThreads.length;
	}
    }

    private static class Reader implements Runnable { 

	private int available = 0;
	private int pos = 0;
	private final byte[] buffer = new byte[4096]; 
	private final InputStream stream;
	private IOException exception = null;
	private final MergingInputStream merger;

	Reader(MergingInputStream merger, InputStream stream) { 
	    this.stream = stream;
	    this.merger = merger;
	}

	public synchronized void run() { 
	    for (;;) {
		int len;
		try { 
		    len = stream.read(buffer);
		} catch(IOException ex) {
		    exception = ex;
		    len = -1;
		}
		available = len;
		pos = 0;

		synchronized (merger) {
		    merger.notify();
		    if (len < 0) { 	
			try {
			    close();
			} catch (IOException e) {
			    // ignore
			}
			merger.closedStreams++;
			break;
		    }
		}
		do { 
		    try { 
			wait();
		    } catch(InterruptedException ex) { 
			// ignore
		    }
		} while (available != 0);
	    }
	    ScheduledExecutor.remove(this);
	}

	synchronized int read() throws IOException { 
	    if (exception != null) { 
		throw exception;
	    }
	    int ch = buffer[pos] & 0xFF;
	    if (++pos == available) { 
		available = 0;
		notify();
	    }
	    return ch;
	}

	synchronized int read(byte[] b, int off, int len) throws IOException { 
	    if (exception != null) { 
		throw exception;
	    }
	    if (available - pos <= len) { 
		len = available - pos;
		available = 0;
		notify();
	    }
	    System.arraycopy(buffer, pos, b, off, len);
	    pos += len;
	    return len;
	}

	synchronized void close() throws IOException {
	    stream.close();
	    available = 0;
	    notify();
	}
    }

    private ProcessRunner[] processes;
    private OutputStream stdin = null;
    private InputStream stdout = null;
    private InputStream stderr = null;
    private boolean done = false;
    private int processID;

    public ProcessBundle(int count, String exe, String[] args, File dir, Map<String, Object> env) {
	processes = new ProcessRunner[count];
	for (int i = 0; i < count; i++) {
	    processes[i] = new ProcessRunner(exe, args, dir, env);
	}
    }
    
    public void startBundle() throws IOException {
	for (int i = 0; i < processes.length; i++) {
	    try {
		processes[i].startProcess();
	    } catch (IOException e) {
		// Kill already started processes before rethrowing.
		if (i > 0) {
		    for (int j = 0; j < i; j++) {
			processes[j].kill();
		    }
		}
		throw e;
	    }
	}
	processID = processes[0].getProcessID();
	ScheduledExecutor.schedule(this, 0, 50);
    }
    
    public void closeInput() {
	if (stdin != null) {
	    try {
		stdin.close();
	    } catch (IOException e) {
		// ignore
	    }
	}
    }
    
    public OutputStream getStdin() {
	if (stdin == null) {
	    if (processes.length == 1) {
		stdin = processes[0].getStdin();
	    } else {
		stdin = new OutputStreamSplitter(processes);
	    }
	}
	return stdin;
    }
    
    public void setStdin(String name, InputStream in) {
	new StreamForwarder(in, getStdin(), name + " [stdin]");
    }
    
    public InputStream getStdout()  {
	if (stdout == null) {
	    if (processes.length == 1) {
		stdout = processes[0].getStdout();
	    } else {
		InputStream[] streams = new InputStream[processes.length];
		for (int i = 0; i < streams.length; i++) {
		    streams[i] = processes[i].getStdout();
		}
		stdout = new MergingInputStream(streams);
	    }
	}
	return stdout;
    }

    public InputStream getStderr()  {
	if (stderr == null) {
	    if (processes.length == 1) {
		stderr = processes[0].getStderr();
	    } else {
		InputStream[] streams = new InputStream[processes.length];
		for (int i = 0; i < streams.length; i++) {
		    streams[i] = processes[i].getStderr();
		}
		stderr = new MergingInputStream(streams);
	    }
	}
	return stderr;
    }

    public int getExitStatus() {
	waitFor();
	return processes[0].getExitStatus();
    }
        
    public int getProcessID() {
        return processID;
    }

    @Override
    public void run() {
	for (ProcessRunner r : processes) {
	    r.waitFor();
	}
	ScheduledExecutor.remove(this);
	synchronized(this) {
	    done = true;
	    notifyAll();
	}
	if (stdout != null) {
	    try {
		stdout.close();
	    } catch (Throwable e) {
		// ignore
	    }
	}
	if (stderr != null) {
	    try {
		stderr.close();
	    } catch (Throwable e) {
		// ignore
	    }
	}
    }
    
    public void kill() {
	for (ProcessRunner r : processes) {
	    r.kill();
	}
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
}
