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
			// TODO Appropriate action
		}
		if (!monitor.isAlive()) {
			monitor.start();
		}
		return fileName;
	}

	private static class PreStageDoneFileMonitor extends Thread {

		public void run() {
			while (!queue.isEmpty()) {
				String fileName = null;
				try {
					fileName = queue.take();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				java.io.File file = new java.io.File(fileName);
				try {
					file.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (file.exists()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
