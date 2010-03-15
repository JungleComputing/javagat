package org.gridlab.gat.scheduler;

/**
 * DTO-Bean for Queues.
 * 
 * @author Stefan Bozic
 * @author Bastian Boegel
 */
public class Queue {

	private String name = null;

	private String lrm = null;

	private String lrmVersion = null;
	
	private String uniqueId = null;
	
	private String gramVersion = null;
	
	private long totalCpus = 0;
	
	private long totalJobs = 0;
	
	private long runningJobs = 0;
	
	private long waitingJobs = 0;
	
	private long freeCpus = 0;
	
	private String status = null;
	
	private int maxCPUTime = -2;
	
	private int maxWallTime = -2;
	
	private int maxRunningJobs = -2;
	
	private int MaxTotalJobs = -2;
	
	private int priority = -2;
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the lrmVersion
	 */
	public String getLrmVersion() {
		return lrmVersion;
	}

	/**
	 * @param lrmVersion the lrmVersion to set
	 */
	public void setLrmVersion(String lrmVersion) {
		this.lrmVersion = lrmVersion;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the gramVersion
	 */
	public String getGramVersion() {
		return gramVersion;
	}

	/**
	 * @param gramVersion the gramVersion to set
	 */
	public void setGramVersion(String gramVersion) {
		this.gramVersion = gramVersion;
	}

	/**
	 * @return the totalCpus
	 */
	public long getTotalCpus() {
		return totalCpus;
	}

	/**
	 * @param totalCpus the totalCpus to set
	 */
	public void setTotalCpus(long totalCpus) {
		this.totalCpus = totalCpus;
	}

	/**
	 * @return the totalJobs
	 */
	public long getTotalJobs() {
		return totalJobs;
	}

	/**
	 * @param totalJobs the totalJobs to set
	 */
	public void setTotalJobs(long totalJobs) {
		this.totalJobs = totalJobs;
	}

	/**
	 * @return the runningJobs
	 */
	public long getRunningJobs() {
		return runningJobs;
	}

	/**
	 * @param runningJobs the runningJobs to set
	 */
	public void setRunningJobs(long runningJobs) {
		this.runningJobs = runningJobs;
	}

	/**
	 * @return the waitingJobs
	 */
	public long getWaitingJobs() {
		return waitingJobs;
	}

	/**
	 * @param waitingJobs the waitingJobs to set
	 */
	public void setWaitingJobs(long waitingJobs) {
		this.waitingJobs = waitingJobs;
	}

	/**
	 * returns the number of freeCpus for this queue.
	 * @return number of freeCpus for this queue.
	 */
	public long getFreeCpus() {
		return freeCpus;
	}

	/**
	 * @param freeCpus the freeCpus to set
	 */
	public void setFreeCpus(long freeCpus) {
		this.freeCpus = freeCpus;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}	
	
	/**
	 * @return the lrm
	 */
	public String getLrm() {
		return lrm;
	}

	/**
	 * @param lrm the lrm to set
	 */
	public void setLrm(String lrm) {
		this.lrm = lrm;
	}
	
	/**
	 * Getter for the maximum CPU time of a job in this queue (in minutes).
	 * @return {@link #maxCPUTime}.
	 */
	public int getMaxCPUTime() {
		return maxCPUTime;
	} // public int getMaxCPUTime()
	
	/**
	 * Setter for the maximum CPU time of a job (in minutes)
	 * @param maxCPUTime
	 */
	public void setMaxCPUTime(int maxCPUTime) {
		this.maxCPUTime = maxCPUTime;
	} // public void setMaxCPUTime(int maxCPUTime)
	
	public int getMaxWallTime() {
		return maxWallTime;
	} // public int getMaxWallTime()
	
	public void setMaxWallTime(int maxWallTime) {
		this.maxWallTime = maxWallTime;
	} // public void setMaxWallTime(int maxWallTime)
	
	public int getMaxRunningJobs() {
		return maxRunningJobs;
	} // public int getMaxRunningJobs()
	
	public void setMaxRunningJobs(int maxRunningJobs) {
		this.maxRunningJobs = maxRunningJobs;
	} // public void setMaxRunningJobs(int maxRunningJobs)
	
	public int getMaxTotalJobs() {
		return MaxTotalJobs;
	} // public int getMaxTotalJobs()
	
	public void setMaxTotalJobs(int maxTotalJobs) {
		MaxTotalJobs = maxTotalJobs;
	} // public void setMaxTotalJobs(int maxTotalJobs)
	
	public int getPriority() {
		return priority;
	} // public int getPriority()
	
	public void setPriority(int priority) {
		this.priority = priority;
	} // public void setPriority(int priority)
	
} // public class Queue
