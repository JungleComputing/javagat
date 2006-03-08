package org.gridlab.gat.resources.cpi;

/**
 * A utility class used to determine when a Process has stopped.
 */
public class StopThread extends Thread {

	/** Stop time of the Process */
	protected Long stopTime = null;

	/** The Process */
	protected Process process = null;

	/**
	 * Constructs a StopThread to associated with this Process
	 * 
	 * @param process
	 *            The Process to find the stop time for
	 */
	public StopThread(Process process) {
		this.process = process;
	}

	/**
	 * Method which determines when the Process stopped
	 */
	public void run() {
		try {
			process.waitFor();
			stopTime = new Long(System.currentTimeMillis());
		} catch (InterruptedException interruptedException) {
			// Ignore InterruptedException
		}
	}

	/**
	 * Returns the stop time of the corresponding Process
	 * 
	 * @return The stop time or null
	 */
	public Long getStopTime() {
		return stopTime;
	}
}