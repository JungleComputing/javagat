package org.gridlab.gat.engine.util;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Simplified interface to a single <code>ScheduledThreadPoolExecutor</code>,
 * to be used for f.i. job polling.
 */
public class ScheduledExecutor {
      
    /** The executor, to be created lazily. */
    private ScheduledThreadPoolExecutor executor = null;
    
    private HashMap<Runnable, Future<?>> map = null;

    private static ScheduledExecutor scheduledExecutor;
    
    /**
     * Creates and executes a periodic action that becomes enabled first after the
     * given initial delay, and subsequently with the given delay between the
     * termination of one execution and the commencement of the next.
     * If any execution of the task encounters an exception, subsequent executions
     * are suppressed. Otherwise, the task will only terminate via removal or
     * termination of the executor. 
     * @param r the task to run repeatedly.
     * @param initialDelay the time to delay first execution
     * @param delay the delay between executions
     */
    public static synchronized void schedule(Runnable r, long initialDelay, long delay) {
        if (scheduledExecutor == null) {
            scheduledExecutor = new ScheduledExecutor();
        }
        scheduledExecutor.addJob(r, initialDelay, delay);
    }
    
    private ScheduledExecutor() {
        executor = new ScheduledThreadPoolExecutor(0);
        map = new HashMap<Runnable, Future<?>>();
        executor.setKeepAliveTime(5, TimeUnit.SECONDS);
    }
    
    private void addJob(Runnable r, long initialDelay, long delay) {
        map.put(r,
                executor.scheduleWithFixedDelay(r, initialDelay,
                        delay, TimeUnit.MILLISECONDS));
    }
    
    private void shutdown() {
        executor.shutdownNow();
    }
    
    /**
     * Ends the executor.Typically called when the GATEngine ends.
     */
    public synchronized static void end() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }
    
    private void cancel(Runnable r) {
        Future<?> f = map.get(r);
        f.cancel(true);
        executor.remove(r);
    }

    /**
     * Removes and cancels the specified task from the executor.
     * @param r the task to remove.
     */
    public static synchronized void remove(Runnable r) {
        if (scheduledExecutor != null) {
            scheduledExecutor.cancel(r);
        }
    }
}
