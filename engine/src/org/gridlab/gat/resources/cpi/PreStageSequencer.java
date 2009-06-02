package org.gridlab.gat.resources.cpi;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class PreStageSequencer {

    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    private static PreStageDoneFileMonitor monitor = new PreStageDoneFileMonitor();

    private static int id = 0;

    public synchronized static String createPreStageMonitor() {
        String fileName = ".JavaGAT-prestagedone-" + (id++) + "-"
                + Math.random();
        try {
            queue.put(fileName);
        } catch (InterruptedException e) {
            // ignore
        }
        if (!monitor.isAlive()) {
            monitor.start();
        }
        return new java.io.File(fileName).getAbsolutePath();
    }

    private static class PreStageDoneFileMonitor extends Thread {

        public PreStageDoneFileMonitor() {
            setDaemon(true);
            setName("PreStageDoneFileMonitor thread");
        }

        public void run() {
            // setDaemon(true);
            while (!queue.isEmpty()) {
                String fileName = null;
                try {
                    fileName = queue.take();
                } catch (InterruptedException e) {
                    // ignore
                }
                java.io.File file = new java.io.File(fileName);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    // ignore
                }
                while (file.exists()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }
    }

}
