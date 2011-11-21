package org.gridlab.gat.engine.util;

import java.util.HashMap;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.File;

/**
 * This class allows one to wait for the appearance of a file. A single thread will
 * serve all waiters in a specific directory.
 */
public class FileWaiter implements Runnable {
    
    private final File directory;
    
    private static HashMap<String, FileWaiter> scanners = new HashMap<String, FileWaiter>();
    
    private static class Entry {
	private String file;
	
	public Entry(String file) {
	    this.file = file;
	}
	
	public boolean equals(Object e) {
	    if (! (e instanceof Entry)) {
		return false;
	    }
	    Entry entry = (Entry) e;
	    return entry.file.equals(file);
	}
	
	public int hashCode() {
	    return file.hashCode();
	}
    }
    
    private final HashMap<String, Entry> entries = new HashMap<String, Entry>();

    private boolean stopped;
    
    public synchronized static FileWaiter createFileWaiter(File directory) throws GATInvocationException {
	String name = directory.toGATURI().toString();
	FileWaiter w = scanners.get(name);
	if (w == null) {
	    w = new FileWaiter(directory);
	    scanners.put(name, w);
	}
	return w;
    }
    
    public synchronized static void end() {
	for (FileWaiter w :  scanners.values()) {
	    ScheduledExecutor.remove(w);
	    w.stop();
	}
	scanners.clear();
    }
    
    private FileWaiter(File directory) throws GATInvocationException {
	if (! directory.isDirectory()) {
	    throw new GATInvocationException("Not a directory");
	}
	this.directory = directory;
    }

    @Override
    public void run() {
	if (stopped) {
	    synchronized(this) {
		for (Entry e : entries.values()) {
		    synchronized(e) {
			e.notifyAll();
		    }
		}
		entries.clear();
	    }
	    ScheduledExecutor.remove(this);
	    return;
	}
	String[] list = directory.list();
	if (list != null) {
	    for (String s : list) {
		Entry e;
		synchronized(this) {
		    e = entries.get(s);
		}
		if (e != null) {
		    synchronized(e) {
			e.notifyAll();
		    }
		}
	    }
	}
    }
    
    private synchronized void stop() {
	stopped = true;
    }
    
    public void waitFor(String file) {

	Entry e = new Entry(file);
	synchronized(this) {
	    if (stopped) {
		return;
	    }
	    if (entries.size() == 0) {
		ScheduledExecutor.schedule(this, 1000, 1000);
	    }
	    entries.put(file, e);
	}
	synchronized(e) {
	    try {
		e.wait();
	    } catch (InterruptedException e1) {
		// ignore
	    }
	}
	synchronized(this) {
	    entries.remove(file);
	    if (entries.size() == 0) {
		ScheduledExecutor.remove(this);
	    }
 	}
    }
}
