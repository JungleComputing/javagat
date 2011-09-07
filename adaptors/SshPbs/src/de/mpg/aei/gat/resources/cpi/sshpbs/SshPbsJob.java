/**
 * SshPbsJob.java
 *
 * Created on June 10th, 2010
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.URI;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import de.mpg.aei.gat.resources.cpi.sshpbs.SshPbsSecurityUtils;

import de.mpg.aei.gat.resources.cpi.sshpbs.SshPbsResourceBrokerAdaptor;

/**
 * @author  Alexander Beck-Ratzka, AEI, June 10th 2010, created
 */

public class SshPbsJob extends JobCpi {

	private static String regexString="[ ][ ]";

    private static final long serialVersionUID = 1L;
    
    private static final String homeDir = System.getProperty("user.home");
    
    private static final String SSH_PORT_STRING = "sshpbs.ssh.port";    

    public static final int SSH_PORT = 22;
    
    /**
     * the global boolean b_PostStage is used for marking the poststage for a job having taken 
     * place. This is necessary to avoid a secondary poststaging which will occur with offline monitoring,
     * because the unmarschalling and the getInfo method try to poststage.
     */
    
    private boolean b_PostStage = false; 
    
    private String jobID;
    private String hostname;
    private MetricDefinition statusMetricDefinition;
    Metric statusMetric;
    private SoftwareDescription Soft;
    
    int ssh_port;


    /**
     * security stuff for offline monitoring 
     */
    
    private SshPbsResourceBrokerAdaptor broker;
    private URI brokerURI;
    private Map<String, String> securityInfo;


    /**
     * constructor of SshPbsJob 
     * @param gatContext The gatContext
     * @param jobDescription
     * @param sandbox
     */
    protected SshPbsJob(GATContext gatContext, SshPbsResourceBrokerAdaptor broker,
            JobDescription jobDescription, Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);

        /**
         * broker necessary for security context which is required for 
         * getting a ssh connection for offline monitoring.
         */
        
        this.broker = broker;
        
        brokerURI = broker.getBrokerURI();
  
        if (brokerURI.getPort() != -1) {
            ssh_port = brokerURI.getPort();
        } else {
            String port = (String) gatContext.getPreferences().get(SSH_PORT_STRING);
            if (port != null) {
                ssh_port = Integer.parseInt(port);
            } else {
                ssh_port = SSH_PORT;
            }
        }        
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
    
    private SshPbsJob(GATContext gatContext, SerializedSshPbsJob sj)
            throws GATObjectCreationException {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());
        if (sandbox != null) {
            sandbox.setContext(gatContext);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("reconstructing SshPbsJob: " + sj);
        }

        this.jobID = sj.getJobId();
        this.starttime = sj.getStarttime();
        this.stoptime = sj.getStoptime();
        this.submissiontime = sj.getSubmissiontime();
        
        
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      
             
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

        final int SLEEP = 250;

        String jobID = null;
        SoftwareDescription Soft=null;

        public jobStartListener(String jobID,
                SoftwareDescription Soft) {
//            this.session = session;
        	
            this.jobID = jobID;
            this.Soft = Soft;
        }

        public void run() {
            try {
                while (state!=JobState.RUNNING  && state!=JobState.SCHEDULED) {
                	logger.debug("Job Status is:  " + state.toString());
                    if (state==JobState.SUBMISSION_ERROR) {
                        logger.warn("Job submission failed");
//                        task.getOutcomeFiles();
                        break;
                    }  else if (state==JobState.STOPPED) {
                        Thread.sleep(500);
                        setState();
                        if (state==JobState.STOPPED) {
                            logger.debug("Job finished suddenly");
                            break;
                        }
                    }
                    setState();
                    Thread.sleep(SLEEP);
                } 
            } catch (InterruptedException e) {
                logger.error("InterruptedException caught in thread jobStartListener");
                e.printStackTrace();
            } catch (GATObjectCreationException e) {
            	logger.error("setState failed in jobStartListener running thread");
            	e.printStackTrace();
            } catch (GATInvocationException e) {
            	logger.error("setState failed in jobStartListener running thread");
            	e.printStackTrace();
            }

            // Now we're in RUNNING state - set the time and start the
            // jobStopListener

            starttime = System.currentTimeMillis();

            if (state!=JobState.STOPPED) {
			    jobStopListener jsl = new jobStopListener(this.jobID,  this.Soft);
			    Thread t = new Thread(jsl);
			    t.setDaemon(true);
			    t.start();
			}
			else
			{
			    logger.debug("will get poststagefiles from jobstartlistener");
//                  poststageFiles(Soft);
			}
        }
    }

    /**
     * The jobStopListener runs in a thread and checks the job's state. When it
     * detects a state transition from RUN to STOP, it writes the time and exits
     */
    private class jobStopListener implements Runnable {

        final int SLEEP = 250;
        SoftwareDescription Soft=null;

        String jobID = null;

        public jobStopListener(String jobID, 
                SoftwareDescription Soft) {
            this.jobID = jobID;
            this.Soft= Soft;
        }

        public void run() {
        	try {
        		while (state!=JobState.STOPPED) {
        			if (state==JobState.SUBMISSION_ERROR) {
        				logger.error("SshPbs job " + jobID + "failed");
        				break;
        				}
        			logger.debug("SshPbs Job still running");
        			try {
						setState();
					} catch (GATObjectCreationException e) {
						logger.error("setState failed in run");
						e.printStackTrace();
					}
        			Thread.sleep(SLEEP);
        			}

        		// Now we're in STOPPED state - set the time and exit
        		
        		setState(JobState.POST_STAGING);
//				task.getOutcomeFiles();
				logger.debug("will get poststagefiles from jobstoplistener");
        		poststageFiles(Soft);
        		stoptime = System.currentTimeMillis();
        		setState(JobState.STOPPED);
        		
        		} catch (InterruptedException e) {
            		logger.debug("InterruptedException caught in thread jobStopListener");
            		setState(JobState.SUBMISSION_ERROR);
            		e.printStackTrace();
            	} catch (GATInvocationException e) {
            		logger.debug("GATInvocationException caught in thread jobStopListener");
            		System.out.println("SshPbs Adaptor: error in posstaging");
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
        jobStartListener jsl = new jobStartListener(this.jobID, this.Soft);
        Thread t = new Thread(jsl);
        t.setDaemon(true);
        t.start();
    }

    public synchronized JobState getState() {
        try {
			setState();
		} catch (GATObjectCreationException e) {
			logger.error("setState failed in getState");
			e.printStackTrace();
		} catch (GATInvocationException e) {
			logger.error("setState failed in getState");
		}
        return state;
    }

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        SerializedSshPbsJob sj;
        synchronized (this) {

            // Wait until initial stages are passed.
            while (state == JobState.INITIAL || state == JobState.PRE_STAGING) {
//                  || state == JobState.SCHEDULED) {
                try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj = new SerializedSshPbsJob(
                    getClass().getName(),
                    jobDescription, sandbox, jobID,
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

        SerializedSshPbsJob sj = (SerializedSshPbsJob) GATEngine.defaultUnmarshal(
                SerializedSshPbsJob.class, s);

        // if this job was created within this JVM, just return a reference to
        // the job
        synchronized (JobCpi.class) {
            for (int i = 0; i < jobList.size(); i++) {
                JobCpi j = (JobCpi) jobList.get(i);
                if (j instanceof SshPbsJob) {
                    SshPbsJob gj = (SshPbsJob) j;
                    if (sj.getJobId().equals(gj.getJobID())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("returning existing job: " + gj);
                        }
                        return gj;
                    }
                }
            }
        }
        return new SshPbsJob(context, sj);
    }


    /**
	 * @param Soft
	 * @uml.property  name="soft"
	 */
    protected void setSoft(SoftwareDescription Soft) {
        this.Soft = Soft;
    }


    protected synchronized void  setState() throws GATObjectCreationException, GATInvocationException {
        
        JobState oldState = state;

        logger.debug("Getting task status in setState()");
   
        
        try {
            securityInfo = SshPbsSecurityUtils.getSshCredential(
                    gatContext, "sshpbs", brokerURI, broker.SSH_PORT);
        } catch (Throwable e) {
            logger
                    .info("SshPbsResourceBrokerAdaptor: failed to retrieve credentials"
                            + e);
            securityInfo = null;
        }

        if (securityInfo == null) {
            throw new GATObjectCreationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }  
        /**
         * getting the status via ssh ... qstat
        */ 
        
    	String username = securityInfo.get("username");
        String host = broker.pbsHost();
        
        if (host == null) {
            host = "localhost";
        }
        ArrayList<String> command = new ArrayList<String>();
        
        command.add("/usr/bin/ssh");
        command.add("-o");
        command.add("BatchMode=yes");
        command.add("-t");
        command.add("-t");
        command.add(username + "@" + host);
        
        command.add("qstat");
        command.add(this.jobID);

        try {
			String pbsState[] = singleResult(command);
			state = mapPbsStatetoGAT(pbsState);
		} catch (IOException e) {
			logger.error("retrieving job status sshpbsjob failed");
			e.printStackTrace();
			throw new GATInvocationException("Unable to retrieve the Job Status");
		}

        
        if (submissiontime == 0L) {
            setSubmissionTime();
        }
        if (state.equals(JobState.RUNNING)) {
            if (starttime == 0L) {
                setStartTime();
            }
        }
        else if (state.equals(JobState.STOPPED)) {
            if (stoptime == 0L) {
                setStopTime();
                }
            } 
        if (state != oldState) {
        	notifyAll();
        	}
    }
    
    /**
     * mapPbsStateToGAT maps a job status of PBS to a GAT job status.
     * 
     * @param String pbsState
     * @return JobState (GAT)
     * @author A. Beck-Ratzka, AEI, 14.09.2010
     */
    
    private JobState mapPbsStatetoGAT(String[] pbsState) {
    	
    	String pbsLine = null;
    	int ii;
    	boolean bJob = false;
 
        if (pbsState == null) {
            logger.error("Error in mapPbsStatetoGAT: no PbsState returned");
            return JobState.UNKNOWN;
            } else {
            	for (ii=0; ii<=pbsState.length; ii++) {
            		pbsLine = removeBlanksToOne(pbsState[ii]);
            		if (pbsLine.contains(this.jobID)) {
            			bJob = true;
            			break;
            			}
            		}
            	if (!bJob) {
        		logger.error("no job status information for '" + this.jobID + "' found.");
        		return JobState.UNKNOWN;
        		} 
        	else {
        		if (pbsLine.indexOf('W') >= 0) { // waiting
        			return JobState.SCHEDULED;
        			}
        		else if (pbsLine.indexOf('Q') >= 0) { // queued
        			return JobState.SCHEDULED;
        			}
        		else if (pbsLine.indexOf('T') >= 0) { // transition
        			return JobState.SCHEDULED;
        			}
        		// if (mState.indexOf('H') >= 0) {
        		// return Job.HOLD;
        		// }
        		else if (pbsLine.indexOf('S') >= 0) { // suspended
        			return JobState.RUNNING;
        			}
        		else if (pbsLine.indexOf('R') >= 0) { // running
        			return JobState.RUNNING;
        			}
        		else if (pbsLine.indexOf('E') >= 0) { // exiting
        			return JobState.STOPPED;
        			}
        		else if (pbsLine.indexOf('C') >= 0) { // exiting
        			return JobState.STOPPED;
        			} 
        		else {
        			return JobState.UNKNOWN;
        			}
        		}
        	}
        }

    /**
     * method removeBlanksToOne: exchange several blanks in 
     *                           a string a let only one as a
     *                           delimiter between word.
     *                           
     * @author - A. Beck-Ratzka, AEI.
     * @param  String changeString - String with more then one blank as delimiter.
     * @return String result - strings with only single blanks as delimiters between words.
     */
    
	private static String removeBlanksToOne (String changeString) {
		
		String result = null;
		
		result = changeString.replaceAll(regexString, " ");
		while (result.contains("  ")) {
			result = result.replaceAll(regexString, " ");
		}
		
		return(result);
	}
	
    public synchronized void stop() throws GATInvocationException {
    if ((getState() != JobState.RUNNING) && (getState() != JobState.ON_HOLD)
            && (getState() != JobState.SCHEDULED)) {
    	throw new GATInvocationException(
                "Cant stop(): job is not in a running state");
    } else {
        SshPbsJobStop(); // SshPbsJobStop still needs to be implemented.
        state = JobState.STOPPED;
        if (stoptime == 0L) {
            stoptime = System.currentTimeMillis();
        }
        logger.debug("SshPbs Job " + jobID + " stopped by user");
        }
    }
    
    /**
     * SshPbsJobStop
     * 
     * Stop pbs jobs started via an ssh command on a remote cluster
     * by omitting a qdel command via ssh. Only necessary is the jobID.
     * 
     * The job itself is executed using the method singleResult.
     * 
     * @author A. Beck-Ratzka, AEI, July 2010, created.
     */
    
    private synchronized void SshPbsJobStop() {
       
    	/**
    	 * securityinfo, getAuthority and getHostname still requires a solution..
    	 */
    	
    	String username = securityInfo.get("username");
    	
        
        String host = broker.pbsHost();
        if (host == null) {
            host = "localhost";
        }
    	
 //   	String host = "buran.aei.mpg.de";
    	
        ArrayList<String> command = new ArrayList<String>();

        command.add("/usr/bin/ssh");
        command.add("-o");
        command.add("BatchMode=yes");
        command.add("-t");
        command.add("-t");
        command.add(username + "@" + host);
        
        command.add("qdel");
        command.add(jobID);
        
        try {
			String[] outStr = singleResult(command);
			logger.info("SshPbsJob stop request returned:" + outStr[0]);
		} catch (IOException e) {
			logger.error("Failed to stop sshPbs job: " + jobID);
			e.printStackTrace();
		}
    }

private synchronized void poststageFiles (SoftwareDescription sd) throws GATInvocationException {
	
	if (b_PostStage) {
    		logger.debug("no post staging. b_PostStage = " + b_PostStage);
    		return;
    	} else {
    		
    		/**
    		 * let the sandbox do the poststage...
    		 */
    		
    		setState(JobState.POST_STAGING);
    		sandbox.retrieveAndCleanup(this);
    		
    		/**
    		 * poststage the file which contains the exit status of the aplication...
    		 */
    		
        	String username = securityInfo.get("username");
        	
            if (brokerURI.getPort() != -1) {
                ssh_port = brokerURI.getPort();
            } else {
                String port = (String) gatContext.getPreferences().get(SSH_PORT_STRING);
                if (port != null) {
                    ssh_port = Integer.parseInt(port);
                } else {
                    ssh_port = SSH_PORT;
                }
            }
            
            String host = broker.pbsHost();
            if (host == null) {
                host = "localhost";
            }
        	      	
            ArrayList<String> scpCommand = new ArrayList<String>();

            scpCommand.add("/usr/bin/scp");
            scpCommand.add(username + "@" + host + ":~/.rc." + jobID);
            scpCommand.add(homeDir + "/.rc." + jobID);
            
            try {
    			String outStr[] = singleResult(scpCommand);
    			logger.info("SshPbsJob scp of exit status file request returned:" + outStr);
    			
    			/**
    			 * delete the remote file with the exit status
    			 */
    			
    			ArrayList<String> rmCommand = new ArrayList<String>();
    	        rmCommand.add("/usr/bin/ssh");
    	        if (ssh_port != SSH_PORT) {
    	        	rmCommand.add("-p");
    	        	rmCommand.add("" + ssh_port);
    	        }
    	        rmCommand.add("-o");
    	        rmCommand.add("BatchMode=yes");
    	        rmCommand.add("-t");
    	        rmCommand.add("-t");
    	        rmCommand.add(username + "@" + host);
    	        rmCommand.add("rm " + "~/.rc." + jobID);
    	        try {
    	             String outrmStr[] = singleResult(rmCommand);
    	             } catch (IOException e) {
    	            	 logger.error("failed ssh " + username + "@" + host + " rm ~/.rc." + jobID);
    	            	 e.printStackTrace();
    	             }
    			
    		} catch (IOException e) {
    			logger.error("Failed scp " + username + "@" + host + ":~/.rc." + jobID + " " + homeDir + "/.rc." + jobID);
    			e.printStackTrace();
    		}
    	}
	}

/**
 * getExitstatus - retrieves the exits status of an application
 *                 run via sshPBS on a remote cluster by taking the
 *                 exit value from the dataset 
 *                 
 *                 $HOME/.rc.PBS_JOBID
 *                 
 *                 The location of the dataset containing the exit status
 *                 is defined in the qsub script (see method createQsubScript in
 *                 SshPbsResourceBrokerAdaptor). 
 *                 
 *                 Before evaluating the file content, the file must be copied via scp 
 *                 from the executing host to the submitting machines home directory.
 * 
 * @return int rc
 * @author Alexander Beck-Ratzka, AEI, 21.9.2010.
 * 
 */

    public int getExitStatus() {
    	
    	/**
    	 * retrieve the exit status from the file
    	 * $HOME/.rc.PBS_JOBID
    	 * 
    	 */
    	
    	
        
    	String marker = "retvalue = ";
		String line=null;
		int rc=-1;
		
		String fileName = homeDir + "/.rc." + jobID;
        
		BufferedReader rExit=null;
		try {
			
			java.io.File fi = new java.io.File(fileName); // for deleting it after getting the rc
			
			rExit = new BufferedReader(new FileReader(fileName));
			
			line = rExit.readLine().toString();
			
			String rc_String = line.substring(marker.length());
			if (rc_String!=null) {
				rc=Integer.parseInt(rc_String);
			}
			fi.delete();
			return rc;
			
		} catch (FileNotFoundException e) {
			logger.error("SshPbs adaptor: exit value file " + fileName + " not found!");
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			try {
				rExit.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				logger.error("SshPbs adaptor: Close error after read error on " + fileName);
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
        try {
            setState();
            m.put("adaptor.job.id", jobID);
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
                poststageFiles(Soft);
            }
            
            m.put("state", state.toString());
                

        } catch (GATObjectCreationException e) {
        	logger.error("setState() failed in getInfo()");
        	e.printStackTrace();
        	throw new GATInvocationException("setState() in getInfo()" + e);
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
    
    /**
     * The Method SingleResult submits a single SshPbs Job
     * @param command
     * @return
     * @throws IOException
     * 
     * @author Alexander Beck-Ratzka, AEI, July 2010
     */
    
    public synchronized String[] singleResult(ArrayList<String> command) throws IOException {
    	String[] result=new String[10];
        String line = null;
        int index=0;
        BufferedReader br = null;
        
        ArrayList<String> list = new ArrayList<String>(); list.add("one"); list.add("two"); list.add("three"); 
        
        StringBuilder sb = new StringBuilder(); 
        for (String s : command) { 
        	sb.append(s); 
        	sb.append(" "); 
        	} 
        
 //       System.out.println("sshpbs command:" + sb.toString()); 
        
        try {
            //            Process proc = Runtime.getRuntime().exec(sPbsBin + command, sExport);
            Process proc = Runtime.getRuntime().exec(sb.toString());
            proc.waitFor();
            if (proc.exitValue() == 0) {
                br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ( (line = br.readLine()) != null) {
                	result[index] = line;
                	index++; 
                	}
            }
            else {
                br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while ( (line = br.readLine()) != null) {
                	result[index] = line;
                	index++; 
                	}
                if (!sb.toString().contains("scp") || !sb.toString().contains("rm "))  throw new IOException("rejected: " + result);
            }
        }
        catch (InterruptedException ex) {
            throw new IOException("process was interupted");
        }
        finally {
            if (br != null) {
                br.close();
            }
        }
        return result;
    }
}

