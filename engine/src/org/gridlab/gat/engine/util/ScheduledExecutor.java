package org.gridlab.gat.engine.util;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simplified interface to a single <code>ScheduledThreadPoolExecutor</code>,
 * to be used for f.i. job polling.
 */
public class ScheduledExecutor implements RejectedExecutionHandler {
      
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
            int sz = 16;
            String poolSize = System.getProperty("gat.threadpool.size");
            if (poolSize != null) {
                try {
                    sz = Integer.parseInt(poolSize);
                } catch(Throwable e) {
                    System.err.println("Warning: could not parse value of gat.threadpool.size property");
                }
            }
            scheduledExecutor = new ScheduledExecutor(sz);
        }
        scheduledExecutor.addJob(r, initialDelay, delay);
    }
    
    /**
     * Creates and executes a delayed action that becomes enabled first after the
     * given delay.
     * @param r the task to run..
     * @param delay the delay until the execution.
     */
    public static synchronized void schedule(Runnable r, long delay) {
        if (scheduledExecutor == null) {
            int sz = 16;
            String poolSize = System.getProperty("gat.threadpool.size");
            if (poolSize != null) {
                try {
                    sz = Integer.parseInt(poolSize);
                } catch(Throwable e) {
                    System.err.println("Warning: could not parse value of gat.threadpool.size property");
                }
            }
            scheduledExecutor = new ScheduledExecutor(sz);
        }
        scheduledExecutor.addJob(r, delay);
    }
    
    private ScheduledExecutor(int size) {
        executor = new ScheduledThreadPoolExecutor(size, new MyThreadFactory(), this);
        map = new HashMap<Runnable, Future<?>>();
    }
    
    private void addJob(Runnable r, long initialDelay, long delay) {
        map.put(r,
                executor.scheduleWithFixedDelay(r, initialDelay,
                        delay, TimeUnit.MILLISECONDS));
    }
    
    
    private void addJob(Runnable r, long delay) {
	executor.schedule(r, delay, TimeUnit.MILLISECONDS);
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
        if (f != null) {
            f.cancel(true);
            map.remove(r);
        }
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

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.err.println("Warning: rejected scheduled execution of " + r);
        map.remove(r);
    }
    
    // Thread factory that creates daemon threads.
    static class MyThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        MyThreadFactory() {
            namePrefix = "pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r,
                                  namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
