/**
 * UnicoreJob.java
 *
 * Created on July 7, 2008
 *
 */

package org.gridlab.gat.resources.cpi.unicore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

import de.fzj.hila.Location;
import de.fzj.hila.Site;
import de.fzj.hila.Storage;
import de.fzj.hila.Task;
import de.fzj.hila.TaskStatus;
import de.fzj.hila.common.jsdl.JSDL;
import de.fzj.hila.exceptions.HiLAException;

/**
 * @author  Alexander Beck-Ratzka, AEI.
 */

public class UnicoreJob extends JobCpi {

    private static final long serialVersionUID = 1L;
    
    private final String homeDir = System.getProperty("user.home");
    private String jobID;
    private String hostname;
    private MetricDefinition statusMetricDefinition;
    Metric statusMetric;
    Site site;
    JSDL jsdl;
    private Task task;
    private SoftwareDescription Soft;
    private Hashtable<String, Long> time = new Hashtable<String, Long>();

    /**
     * constructor of UnicoreJob 
     * @param gatContext The gatContext
     * @param jobDescription
     * @param sandbox
     */
    protected UnicoreJob(GATContext gatContext,
            JobDescription jobDescription, Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);

        state = JobState.INITIAL;

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);
    }
    
    /**
	 * @param hostname
	 * @uml.property  name="hostname"
	 */
    protected void setHostname(String hostname) {
        this.hostname = hostname;
    }

    protected synchronized void setState(JobState state) {
        this.state = state;
    }
    
    /**
     * The jobStartListener runs in a thread and checks the job's state. When it
     * detects a state transition from SCHEDULED to RUN, it writes the time and
     * exits
     */
    private class jobStartListener implements Runnable  {

        int state = 0x00;
        final int SLEEP = 250;

        TaskStatus Status=null;
        String jobID = null;
        Task task = null;
        SoftwareDescription Soft=null;
        
        Hashtable<String, Long> time;

        public jobStartListener(Task task, String jobID,
                SoftwareDescription Soft, Hashtable<String, Long>  time) {
//            this.session = session;
        	
            this.jobID = jobID;
            this.time = time;
            this.task = task;
            this.Soft = Soft;
        }

        public void run() {
        	try {
        		while (!task.status().equals(TaskStatus.RUNNING)) {
        			if (task.status().equals(TaskStatus.FAILED)) {
        				logger.warn("Job submission failed");
        				task.getOutcomeFiles();
        				break;
                    } else if (task.status().equals(TaskStatus.CANCELLED)) {
                    	logger.info("Job submission cancelled");
                    	task.getOutcomeFiles();
                        break;
                    }  else if (task.status().equals(TaskStatus.FINISHED)) {
                    	Thread.sleep(500);
                    	setState();
                    	if (task.status().equals(TaskStatus.FINISHED)) {
                        	logger.debug("Job finished suddenly");
                        	task.getOutcomeFiles();
                        	break;
                    	}
                    }
        			task.status();
        			setState();
        			Thread.sleep(SLEEP);
        		} 
        	} catch (HiLAException e) {
        		logger.error("HilaException caught in thread jobStartListener");
        		e.printStackTrace();
        	} catch (InterruptedException e) {
        		logger.error("InterruptedException caught in thread jobStartListener");
        		e.printStackTrace();
        	}
            // Now we're in RUNNING state - set the time and start the
            // jobStopListener
            time.put("start_time", new Long(System.currentTimeMillis()));

            try {
                if (!task.status().equals(TaskStatus.FINISHED)) {
                    jobStopListener jsl = new jobStopListener(this.task, this.jobID,  this.Soft, time);
                    new Thread(jsl).start();
                }
                else
                {
                	logger.debug("will get poststagefiles from jobstartlistener");
                	poststageFiles(Soft,task);
                }
            } catch (HiLAException e) {
        		logger.error("HilaException caught in thread jobStartListener");
        		e.printStackTrace();
            } catch (GATInvocationException ee) {
        		logger.error("GATInvocationException caught in thread jobStartListener");
        		ee.printStackTrace();
            }
        }
    }

    /**
     * The jobStopListener runs in a thread and checks the job's state. When it
     * detects a state transition from RUN to STOP, it writes the time and exits
     */
    private class jobStopListener implements Runnable {

        int state = 0x00;
        final int SLEEP = 250;
        Task task=null;
        TaskStatus Status=null;
        SoftwareDescription Soft=null;

        String jobID = null;
        Hashtable<String, Long> time;

        public jobStopListener(Task task, String jobID, 
                SoftwareDescription Soft, Hashtable<String, Long> time) {
            this.task = task;
            this.jobID = jobID;
            this.time = time;
            this.Soft= Soft;
        }

        public void run() {
        	try {
        		while (!task.status().equals(TaskStatus.FINISHED)) {
        			if (task.status().equals(TaskStatus.FAILED)) {
        				logger.error("HilA job " + jobID + "failed");
        				break;
        				}
        			else if (task.status().equals(TaskStatus.CANCELLED)) {
        				logger.debug("HilA job " + jobID + "cancelled");
        				break;
        			    }
        			logger.debug("Unicore Job still running");
        			setState();
        			Thread.sleep(SLEEP);
        			}
        		// Now we're in STOPPED state - set the time and exit
        		
        		setState(JobState.POST_STAGING);
//				task.getOutcomeFiles();
				logger.debug("will get poststagefiles from jobstoplistener");
        		poststageFiles(Soft, task);
        		time.put("stop_time", new Long(System.currentTimeMillis()));
        		setState(JobState.STOPPED);
        		
        		} catch  (HiLAException e) {
            		logger.debug("HilaException caught in thread jobStopListener");
            		setState(JobState.SUBMISSION_ERROR);
            		e.printStackTrace();
            	} catch (InterruptedException e) {
            		logger.debug("InterruptedException caught in thread jobStopListener");
            		setState(JobState.SUBMISSION_ERROR);
            		e.printStackTrace();
            	} catch (GATInvocationException e) {
            		logger.debug("GATInvocationException caught in thread jobStopListener");
            		System.out.println("UNICORE Adaptor: error in posstaging");
            		setState(JobState.SUBMISSION_ERROR);
            		e.printStackTrace();
            	}
        }
    }
 
    /*public String getJobID() {
        return jobID;
    }*/

    /**
	 * @param jobID
	 * @uml.property  name="jobID"
	 */
    protected void setJobID(String jobID) {
        this.jobID = jobID;
    }

    protected void startListener() {
        jobStartListener jsl = new jobStartListener(this.task, this.jobID,
                this.Soft, time);
        new Thread(jsl).start();
    }

    public synchronized JobState getState() {
//        setState();
        return state;
    }

    public String marshal() {
        throw new Error("Not implemented");
    }

    /**
	 * @param task
	 * @uml.property  name="task"
	 */
    protected void setTask(Task task) {
        this.task = task;
    }
    

    /**
	 * @param Soft
	 * @uml.property  name="soft"
	 */
    protected void setSoft(SoftwareDescription Soft) {
        this.Soft = Soft;
    }


    protected synchronized void  setState() {

        logger.debug("Getting task status in setState()");
   
        try {
            TaskStatus status = task.status();

            if (status.equals(TaskStatus.RUNNING)) {
            	state = JobState.RUNNING;
            }

           if (status.equals(TaskStatus.FAILED)) {
        	  state = JobState.SUBMISSION_ERROR;
           }

           if (status.equals(TaskStatus.FINISHED)) {
         	  state = JobState.STOPPED;
            }

            /* Job is active but suspended */

           if (status.equals(TaskStatus.NEW)) {
          	  state = JobState.SCHEDULED;
             }
           
           if (status.equals(TaskStatus.PENDING)) {
           	  state = JobState.SCHEDULED;
              }
         } catch (HiLAException e) {
            if (logger.isDebugEnabled()) {
                logger.error("-- UNICOREJob EXCEPTION --");
                logger.error("Got an exception while retrieving resource manager status:");
                logger.error("", e);
            }
        }
    }
    
    public synchronized void stop() throws GATInvocationException {
        if ((getState() != JobState.RUNNING) && (getState() != JobState.ON_HOLD)
                && (getState() != JobState.SCHEDULED)) {
            throw new GATInvocationException(
                    "Cant stop(): job is not in a running state");
        } else {
            try {
            	task.abort();
                state = JobState.STOPPED;
                logger.debug("Unicore Job " + task.getID() + " stopped by user");
            } catch (HiLAException e) {
                if (logger.isDebugEnabled()) {
                    logger.error("-- UnicoreJob EXCEPTION --");
                    logger.error("Got an exception while trying to TERMINATE job:");
                    logger.error("", e);
                }
                throw new GATInvocationException("Can't stop Unicore Job" + jobID, e);
            }
        }
    }
    
    private synchronized void poststageFiles (SoftwareDescription sd, Task task) throws GATInvocationException {
    	
    	File remoteStdoutFile=null;
    	File remoteStderrFile=null;
    	File localStdoutFile=null;
    	File localStderrFile=null;
    	
    	String stdoutFileName = null;
    	String stderrFileName = null;

    	//    	File localStderrFile=new File("/home/alibeck/.hila/strderr-gat.test");
   	
    	Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> postStaged=null;
		Storage wd;
		try {
			
			/**
			 * poststage stderr and stdout, if desired...
			 */
			
			stdoutFileName = sd.getStdout().getAbsolutePath();
			stderrFileName = sd.getStderr().getAbsolutePath();

			localStdoutFile = new File(stdoutFileName);
			localStderrFile = new File(stderrFileName);
			logger.debug("stdout: " +localStdoutFile.getAbsolutePath() + "||" + localStdoutFile.getName());		
			logger.debug("stderr: " +localStderrFile.getAbsolutePath() + "||" + localStderrFile.getName());

			if (localStdoutFile!=null) {
				remoteStdoutFile = task.getStdOut();
				if (remoteStdoutFile!=null) {
					moveFile (remoteStdoutFile.getAbsolutePath(), localStdoutFile.getAbsolutePath());
				}
			}
			if (localStderrFile!=null) {
				remoteStderrFile = task.getStdErr();
				if (remoteStderrFile!=null) { 
					moveFile (remoteStderrFile.getAbsolutePath(), localStderrFile.getAbsolutePath());
				}
			}
			
			wd = (Storage) task.getLocatableChild(Location.WD);
			postStaged=sd.getPostStaged();
		
			if (postStaged!=null) {
				de.fzj.hila.File wdFile = wd.asFile("/");
				List<de.fzj.hila.File> wdFiles = wdFile.ls();
				for (de.fzj.hila.File file : wdFiles) {
				    for (java.io.File srcFile : postStaged.keySet()) {
			    		java.io.File destFile = postStaged.get(srcFile);
			    		logger.debug("PoststageFiles: srcFile: '" + srcFile.getName() + "' destFile (name , path): '" + destFile.getName() + "', " + destFile.getAbsolutePath() + "'");
			    		if (file.getName().compareTo(srcFile.getName()) == 0 ) { // maybe getName should be used here
			    			file.exportToLocalFile(destFile, true).block();
							}
							
						}
					}
			}
		} catch (HiLAException e) {
			e.printStackTrace();
			throw new GATInvocationException("UNICORE Adptor: loading Storage for poststaging failes");
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
 
    public int getExitStatus() throws GATInvocationException {
    	
    	/**
    	 * retrieve the exit status from the file
    	 * UNICORE_SCRIPT_EXIT_CODE which must be poststaged from the execution host first
    	 * 
    	 * That's the way of how to do it within HiLA
    	 */

		int rc=-1;

		try {
			rc=task.getExitCode();
		} catch (HiLAException e) {
			e.printStackTrace();
			rc=-1;
			throw new GATInvocationException("UNICORE Adptor: loading Storage for poststaging exit value file failed");
		}
		
		return rc;
    	
/*		try {
			wd = (Storage) task.getLocatableChild(Location.WD);
			de.fzj.hila.File wdFile = wd.asFile("/");
			List<de.fzj.hila.File> wdFiles = wdFile.ls();
			for (de.fzj.hila.File file : wdFiles) {
				if (file.getName().compareTo("UNICORE_SCRIPT_EXIT_CODE") == 0 ) {
					file.exportToLocalFile(new File(homeDir + "/.hila/data/__" + jobID +"__UNICORE_SCRIPT_EXIT_CODE"), true).block();
				}
			}*/
			
			/**
			 * read the exitstatus over the dataset UNICORE_SCRIPT_EXIT_CODE
			 */
			
			
/*			String fileName = homeDir + "/.hila/data/__" + jobID +"__UNICORE_SCRIPT_EXIT_CODE";
			
			BufferedReader rExit=null;
			try {
				rExit = new BufferedReader(new FileReader(fileName));
				
				line = rExit.readLine().toString();
				if (line!=null) {
					rc=Integer.parseInt(line);
				}
				return rc;
				
			} catch (FileNotFoundException e) {
				logger.error("UNICORE adaptor: exit value file " + fileName + " not found!");
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				try {
					rExit.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.error("UNICORE adaptor: Close error after read error on " + fileName);
					e1.printStackTrace();
				}
				e.printStackTrace();
				return -1;
			}
		} catch (HiLAException e) {
			e.printStackTrace();
			throw new GATInvocationException("UNICORE Adptor: loading Storage for poststaging exit value file failed");
		}*/
    }
    
    public int getExitStatus_old() {
    	
    	/**
    	 * retrieve the exit status from the file
    	 * $HOME/.hila.data/<JOB ID>/UNICORE_SCRIPT_EXIT_CODE
    	 * 
    	 * That's the way of how to do it within HiLA
    	 */
    	
		String line=null;
		int rc=-1;
		
		/**
		 * read the exitstatus over the dataset UNICORE_SCRIPT_EXIT_CODE
		 */
		
		
		String fileName = homeDir + "/.hila/data/" + jobID + "/UNICORE_SCRIPT_EXIT_CODE";
		
		BufferedReader rExit=null;
		try {
			rExit = new BufferedReader(new FileReader(fileName));
			
			line = rExit.readLine().toString();
			if (line!=null) {
				rc=Integer.parseInt(line);
			}
			return rc;
			
		} catch (FileNotFoundException e) {
			logger.error("UNICORE adaptor: exit value file " + fileName + " not found!");
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			try {
				rExit.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				logger.error("UNICORE adaptor: Close error after read error on " + fileName);
				e1.printStackTrace();
			}
			e.printStackTrace();
			return -1;
		}
    }
  
    /**
     * somewhat as a dummy; gets the outcome files (for test purposes)
     */
    
    public Map<String, Object> getInfo() throws GATInvocationException {

        HashMap<String, Object> m = new HashMap<String, Object>();
        setState();
        try {
            m.put("adaptor.job.id", task.getID());
            m.put("hostname", hostname);
            if (state == JobState.INITIAL || state == JobState.UNKNOWN
                    || state == JobState.SCHEDULED) {
                m.put("starttime", null);
            } else {
                m.put("starttime", time.get("start_time"));
            }
            if (state != JobState.STOPPED) {
                m.put("stoptime", null);
            } else {
                m.put("stoptime", time.get("stop_time"));
            }
            
            m.put("state", state.toString());
                
            task.getOutcomeFiles();    

        } catch (HiLAException e) {
        	logger.error("HilaException in getInfo()");
        	e.printStackTrace();
        	throw new GATInvocationException("HilaException in getInfo()" + e);
        	}

        return m;
    }
    
    private void moveFile (String inFile, String outFile) throws IOException {
    	
    	File inputFile = new File(inFile);
        File outputFile = new File(outFile);

        FileReader in = new FileReader(inputFile);
        FileWriter out = new FileWriter(outputFile);
        int c;

        while ((c = in.read()) != -1)
          out.write(c);

        in.close();
        out.close();
        
        /**
         * delete the input file to finish up the move...
         */
        
        inputFile.delete();
    }
}

            	