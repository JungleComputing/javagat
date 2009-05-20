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
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

import de.fzj.hila.HiLAFactory;
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
    
    private static final String homeDir = System.getProperty("user.home");
    
    private String jobID;
    private String hostname;
    private MetricDefinition statusMetricDefinition;
    Metric statusMetric;
    Site site;
    JSDL jsdl;
    private Task task;
    private SoftwareDescription Soft;

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
     * Constructor for unmarshalled jobs.
     */
    private UnicoreJob(GATContext gatContext, SerializedUnicoreJob sj)
            throws GATObjectCreationException {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());
        
        if (logger.isDebugEnabled()) {
            logger.debug("reconstructing UnicoreJob: " + sj);
        }

        this.starttime = sj.getStarttime();
        this.stoptime = sj.getStoptime();
        this.submissiontime = sj.getSubmissiontime();
        
        // Set the context classloader to the classloader that loaded the UnicoreJob
        // class, otherwise the HiLA libraries will not find any HiLA implementations.
        // --Ceriel
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {            
            // Note: the job id used here is actually a Location.
            this.task = (Task) HiLAFactory.getInstance().locate(new de.fzj.hila.Location(sj.getJobId()));
            if (this.task == null) {
                throw new GATObjectCreationException("Unmarshalling UnicoreJob: task = null");
            }

            this.jobID = this.task.getID();
        } catch(HiLAException e) {
            throw new GATObjectCreationException("Got HiLAException: ", e);
        } finally {
            Thread.currentThread().setContextClassLoader(saved);
        }
        
        // reconstruct enough of the software description to be able to
        // poststage.
        Soft = new SoftwareDescription();
        String s = sj.getStdout();
        if (s != null) {
            Soft.setStdout(GAT.createFile(gatContext, s));
        }
        s = sj.getStderr();
        if (s != null) {
            Soft.setStderr(GAT.createFile(gatContext, s));
        }
        
        String[] toStageOut = sj.getToStageOut();
        String[] stagedOut = sj.getStagedOut();
        if (toStageOut != null) {
            for (int i = 0; i < toStageOut.length; i++) {
                Soft.addPostStagedFile(GAT.createFile(gatContext, toStageOut[i]),
                        GAT.createFile(gatContext, stagedOut[i]));
            }
        }
       
        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
        startListener();
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

        public jobStartListener(Task task, String jobID,
                SoftwareDescription Soft) {
//            this.session = session;
        	
            this.jobID = jobID;
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
            starttime = System.currentTimeMillis();

            try {
                if (!task.status().equals(TaskStatus.FINISHED)) {
                    jobStopListener jsl = new jobStopListener(this.task, this.jobID,  this.Soft);
                    Thread t = new Thread(jsl);
                    t.setDaemon(true);
                    t.start();
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

        public jobStopListener(Task task, String jobID, 
                SoftwareDescription Soft) {
            this.task = task;
            this.jobID = jobID;
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
        		stoptime = System.currentTimeMillis();
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
    protected synchronized void setJobID(String jobID) {
        this.jobID = jobID;
        notifyAll();
    }

    protected void startListener() {
        jobStartListener jsl = new jobStartListener(this.task, this.jobID,
                this.Soft);
        Thread t = new Thread(jsl);
        t.setDaemon(true);
        t.start();
    }

    public synchronized JobState getState() {
//        setState();
        return state;
    }

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        SerializedUnicoreJob sj;
        synchronized (this) {

            // Wait until initial stages are passed.
            while (state == JobState.INITIAL || state == JobState.PRE_STAGING
                  || state == JobState.SCHEDULED) {
                try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj = new SerializedUnicoreJob(jobDescription, sandbox, task.getLocation().toString(),
                    submissiontime, starttime, stoptime, Soft);
        }
        String res = GATEngine.defaultMarshal(sj);
        if (logger.isDebugEnabled()) {
            logger.debug("marshalled seralized job: " + res);
        }
        return res;
    }


    public static Advertisable unmarshal(GATContext context, String s)
            throws GATObjectCreationException {
        if (logger.isDebugEnabled()) {
            logger.debug("unmarshalled seralized job: " + s);
        }

        SerializedUnicoreJob sj = (SerializedUnicoreJob) GATEngine.defaultUnmarshal(
            SerializedUnicoreJob.class, s);

        // if this job was created within this JVM, just return a reference to
        // the job
        synchronized (JobCpi.class) {
            for (int i = 0; i < jobList.size(); i++) {
                JobCpi j = (JobCpi) jobList.get(i);
                if (j instanceof UnicoreJob) {
                    UnicoreJob gj = (UnicoreJob) j;
                    if (sj.getJobId().equals(gj.task.getLocation().toString())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("returning existing job: " + gj);
                        }
                        return gj;
                    }
                }
            }
        }
        return new UnicoreJob(context, sj);
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
        
        JobState oldState = state;

        logger.debug("Getting task status in setState()");
   
        try {
            TaskStatus status = task.status();

            if (submissiontime == 0L) {
                setSubmissionTime();
            }
            if (status.equals(TaskStatus.RUNNING)) {
                if (starttime == 0L) {
                    setStartTime();
                }
            	state = JobState.RUNNING;
            }

           if (status.equals(TaskStatus.FAILED)) {
        	  state = JobState.SUBMISSION_ERROR;
           }

           if (status.equals(TaskStatus.FINISHED)) {
               if (stoptime == 0L) {
                   setStopTime();
               }
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
         if (state != oldState) {
             notifyAll();
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
                if (stoptime == 0L) {
                    stoptime = System.currentTimeMillis();
                }
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
			File stdout = sd.getStdout();
			if (stdout != null) {
			    stdoutFileName = stdout.getAbsolutePath();
			    localStdoutFile = new File(stdoutFileName);
			    logger.debug("stdout: " + stdoutFileName + "||" + localStdoutFile.getName());      
			}
			File stderr = sd.getStderr();
			if (stderr != null) {
			    stderrFileName = sd.getStderr().getAbsolutePath();
			    localStderrFile = new File(stderrFileName);
			    logger.debug("stderr: " + stderrFileName + "||" + localStderrFile.getName());
			}

			// Why make file objects if you only use the AbsolutePath? --Ceriel
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
                m.put("starttime", starttime);
            }
            if (state != JobState.STOPPED) {
                m.put("stoptime", null);
            } else {
                m.put("stoptime", stoptime);
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

            	