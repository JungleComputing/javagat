package nl.vu.ibis;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.CoScheduleJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.CoScheduleJobCpi;
import org.koala.runnersFramework.RunnerListener;

public class KoalaJob extends CoScheduleJobCpi {

	// Generated to keep eclipse happy...
	private static final long serialVersionUID = 4321647692737044640L;

	//  The logger used for debugging output and warnings.
	private static Logger logger = Logger.getLogger("KoalaAdaptor.Runner");

	private GATRunner runner;    
	private RunnerListener listner;	
	private Thread thread;

	// The metric definition of the job status metric.   
	private final MetricDefinition statusMetricDefinition;

	// The actual status metric. 
	private final Metric statusMetric;

	// The number Koala assigns to the job.  
//	private int jobNo;

	// The number of submission attempts so far.  
//	private int jobTries;

	// Indicates if the job if done.  
	private boolean done = false;

	// A description of the job. Note that we always use a 
	// CoScheduleJobDescription here, even if we only have a single job. This 
	// simplifies the GAT administration. 
	private CoScheduleJobDescription jobDescription;

	// A map containing the components of the job. 
	private final Components components;

	private static int jobID = 0;
	
	private final int jobNumber;
	
	protected KoalaJob(GATContext context, 
			CoScheduleJobDescription jobDescription) throws GATInvocationException {

		super(context, jobDescription);

		try { 
			System.err.println("@@@ Creating Koala job!");

			jobNumber = getNewJobNumber();
			
			// Save the GAT context and job description.
			this.components = new Components(this, context);
			this.jobDescription = jobDescription;

			splitJobDescription();
			
			// Create a Koala Runner and listener thread
			runner = new GATRunner(this, jobDescription, components, context);
			listner = new RunnerListener(runner);

			// Tell the GAT engine that we provide job.status events
			HashMap<String, Object> definition = new HashMap<String, Object>();
			definition.put("status", String.class);

			statusMetricDefinition = new MetricDefinition("job.status",
					MetricDefinition.DISCRETE, "String", null, null, definition);
			statusMetric = statusMetricDefinition.createMetric(null);
			GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
		} catch (Exception e) { 
			e.printStackTrace(System.err);
			throw new GATInvocationException("EEP", e);
		}
	}

//	protected synchronized String getLabel(JobDescription jobDescription) { 
//		return "gat-job" + jobID++;
//	}

	protected static synchronized int getNewJobNumber() { 
		return jobID++;
	}
	
	
	
	private void splitJobDescription() { 

		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaJob splitting JobDescription into components");
		
		/* Optional settings: 
			job.setFlexibleJob(true);
			job.setOptimizeComm(true);
			job.setClusterMinimize(true);
			job.setJDS(Utils.readFile(jdf_file));
			job.setExcludeClusters();
			job.setRunnerName("GATRunner");
			job.setCoallocate(true);
		 */

		/* We should now generate a string that looks something like this:

			   +( 
	 			   &( directory = "/home/jason/koala-test/files" )
	  				( arguments = "inputfile1 outputfile1" )
	  				( executable = "/home/jason/koala-test/files/copy.sh" )
	  				( maxWallTime = "15" )
	  				( label = "subjob 0" )
	  				( count = "1" )
	  				( stagein = "/home/jason/koala-test/files/inputfile1" )
	  				( filesize-stagein = "/home/jason/koala-test/files/inputfile1=10G" )
	  				( stageout = "/home/jason/koala-test/files/outputfile1" )
	  				( filesize-stageout = "/home/jason/koala-test/files/outputfile1=1G" )
	  				( resourceManagerContact = "fs3.das2.ewi.tudelft.nl" )
	  				( bandwidth = "subjob 1:30G"
	                			  "subjob 2:10G")
					( stdout = "/home/wlammers/demo/standard_out" )
	  				( stderr = "/home/wlammers/demo/standard_err" )
					( jobtype = mpi )
				)
				( 
				     next job...
				) 

				Since GAT will be responsible for the actual deployment of the 
				application, it is not necessary to do a 'perfect' translation 
				here. The JDF string should contain enough information to allow 
				the Koala scheduler to do it's job. The JDF may be incomplete
				however. For example, if the user does not require Koala to take 
				the file location into account (this is an option), then there 
				is no point in providing the stagein information to the 
				scheduler.
		 */

		List<JobDescription> tmp = jobDescription.getJobDescriptions();		

		for (JobDescription j : tmp) { 
			components.createComponent(j, jobNumber);
		}
		
		KoalaResourceBrokerAdaptor.logger.info("KoalaJob generated JDS:\n" 
				+ components.getJDLDescription());
	}

	public synchronized JobState getState() {        
		return state;
	}

	public Map getInfo() {
		return components.getInfo(null);       
	}

	public void submitToScheduler() throws IOException {
		// Start the listener 
		thread = new Thread(listner, "KoalaJob<Unknown>");
		thread.start();
	}

	private synchronized boolean getDone() { 
		return done;
	}

	/**
	 * Used in synchronisation. the Runners are supposed to wait 
	 * until they are assigned job number by the Scheduler
	 */

	/*
    private void send(InetSocketAddress ch, String buffer) throws IOException {

        // TODO: re-write.
        boolean done = false;

        while (!done) {
            SocketChannel toClient = SocketChannel.open();
            toClient.connect(ch);
            if (toClient.finishConnect()) {
                toClient.write(ByteBuffer.wrap(buffer.toString().getBytes()));
            }
            toClient.close();
            toClient = null;
            done = true;
        }
    }

    public void run() {

        System.err.println("@@@ Starting KOALA JOB accept thread");

        while (!getDone()) {

            try {
                Socket s = server.accept();
                new MessageHandler(s).start();
            } catch (SocketTimeoutException e) {
                // -- ignore
            } catch (Exception e) {
                System.err.println("Koala runner failed to accept!" + e);
            }
        }

        System.err.println("@@@ Stopped KOALA JOB accept thread");
    }
	 */
	/**
	 * Checks if the received server message is not an old message intended for
	 * previous job run (after a restart has occurred)
	 * 
	 * @param jobTryNumber
	 *          the jobtry number of the server response
	 * @return true if this message is valid for this jobrun
	 */
	//   private boolean checkJobTryNumber(String jobTryNumber) {
	/*
         try {
         int receivedJobTryNumber = Integer.parseInt(jobTryNumber);
         if (receivedJobTryNumber != jobTries.get()) {
         // this is an old message, ignore this.
         logger.debug("Old message (JT:" + receivedJobTryNumber + ")");
         return false;
         }
         } catch (NumberFormatException nfe) {
         // something wrong with jobtrynumber
         logger.error("Error in jobTryNumber");
         return false;
         }*/

//	System.err.println("CHECK JOB NUMBER: " + jobTryNumber);

//	return true;
//	}

	private synchronized void setState(JobState state) {

		if (done) { 
			return;
		}

		this.state = state;

		if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) { 
			done = true;
			components.stop();
			listner.done();
				
			/* 
            try {
                System.err.println("@@@ Sending abort message to KOALA");
  // TODO              send(schedulerAddress, "JOB_ABORT#" + jobNo + "#" + jobTries);
            } catch (IOException e) {
                System.err.println("Error sending abort message to KOALA");
            }*/
		}        
	}


	@Override
	public void stop() throws GATInvocationException {
		System.err.println("### stopping job");
		setState(JobState.STOPPED);
	}

	// Close a socket and the associated streams.
	/*
    private void close(Socket s, InputStream in, OutputStream out) { 
        try {
            if (in != null) { 
                in.close();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            if (out != null) { 
                out.close();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            if (s!= null) { 
                s.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    // Read all data up to the end-of-stream. For security reasons, an upper 
    // limit to the data size may be specified.   
    private byte [] readFully(InputStream in, long maximumSize) 
        throws IOException {        

        byte[] tmp = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int read = 0;

        do {
            // Read up to 1 K of data
            read = in.read(tmp, 0, tmp.length);

            if (read > 0) {
                // Store this data in the ByteArrayOutputStream.
                out.write(tmp, 0, read);

                // Check the message size
                if (maximumSize > 0 && out.size() > maximumSize) { 
                    throw new IOException("Maximum message size exceeded!");                
                }
            }

        } while (read >= 0);

        // Convert the ByteArrayOutputStream to a regular array 
        return out.toByteArray();        
    }

    // Reads a Koala reply message from a socket.  
    private String readReply(Socket s) {

        InputStream in = null;

        try {
            in = s.getInputStream();

            // We get the input stream from the socket and read all data.
            byte [] tmp = readFully(in, 32*1024);

            // Once all data is read, we convert it to a string.             
            return new String(tmp, System.getProperty("file.encoding"));

        } catch (Exception e) {
            System.err.println("Failed to handle request!" + e);
            return null;

        } finally {
            // Always close the socket and all streams.
            close(s, in, null);
        }
    }

    private void handleJobRestart(String [] message) {
        System.err.println("JOB_RESTART: " + Arrays.deepToString(message));

        // components.stop();

        /*
         synchronized (transfLock) {
         jobTries.incrementAndGet();
         if ((null != schedMsg[1]) && (!(schedMsg[1].trim()).equals(""))) {
         logger.fatal("Received restart message from Koala server: "
         + schedMsg[3].trim());
         }

         failureOccurred.set(true);
         serverPreemptedUs.set(true);
         componentRestart.set(true);
         stopComponents();
         if (flexibleJob.get()) {
         componentsAdded.set(false);
         }

         for (RunnerJobComponent jComp : componentsList) {
         jComp.setReserved(false);
         }
         }*/
//	}
/*
	private void handleJobAbort(String[] message) {
		System.err.println("JOB_ABORT: " + Arrays.deepToString(message));

		// This will stop all components and set done to true.
		setState(JobState.SUBMISSION_ERROR);
	}

	private void handleResetComponents() {

		System.err.println("COMPONENTS_RESET");

		components.reset();

		/*

         synchronized (runnerLock) {
         componentsList.clear();

         jobSize = RslCommon.addRunnerComponent(componentsList, rsl, 
         jobNo);

         compCount = componentsList.size();
         logger.debug("Reset components " + jobSize + " RSL " + rsl);
         if (flexibleJob.get()) {
         componentsAdded.set(false);
         }
         }*/
//	}

	/*
    private void handleSetJobID(String [] message) throws IOException {
        System.err.println("JOB_ID: " + Arrays.deepToString(message));

        jobNo = Integer.parseInt(message[1]);

        // We start with a single-component job.
        components.createComponent(jobDescription, jobNo);

        thread.setName("KoalaJob<" + jobNo + ">");

        /*
         synchronized (runnerLock) {
         componentsList.clear();
         jobSize = RslCommon.addRunnerComponent(componentsList,
         rsl, jobNo);
         compCount = componentsList.size();
         if (!flexibleJob.get()) {
         logger.debug("Waking up FILE_TRANSFER thread ");
         componentsAdded.set(true);
         runnerLock.notifyAll();
         }
         }

         logger.info("Koala server has assigned us Job ID " + jobNo
         + " Total Job Components " + compCount);

         // start the job prephase
         if (!prePhase()) {
         stopComponents();
         logger.fatal("Prephase was not successfull, aborting job "
         + jobNo);
         send(schedulerAddress, "JOB_ABORT#" + jobNo + "#"
         + jobTries.get());
         cleanUpRunner();
         }*/
	//   }

	/*
	private void handleCloneComponent(String [] message) {

		System.err.println("JOB_CLONE: " + Arrays.deepToString(message));

		if (message.length < 4) {
			logger.warn("Incorrect server response: " + Arrays.toString(message));
			return;
		}

		try {
			String component = message[1];
			int clones = Integer.valueOf(message[3]);

			logger.debug("Splitting component " + component
					+ " to " + clones + " components");

			if (!components.cloneComponent(component, clones)) { 
				// TODO: print error
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
*/


		/*

         synchronized (runnerLock) {
         String componentNo = schedMsg[1];
         int cloneCount = Integer.valueOf(schedMsg[3])
         .intValue();
         logger.debug("Splitting component " + componentNo
         + " to " + cloneCount + " Components"
         + " Original Component Count: "
         + componentsList.size());

         for (int j = 0; j < componentsList.size(); j++) {
         RunnerJobComponent component = componentsList
         .get(j);
         if (component.getComponentNo().equals(componentNo)) {
         compCount--;
         componentsList.remove(j);
         for (int i = 0; i < cloneCount; i++) {
         RunnerJobComponent tmpcomp = (RunnerJobComponent) component
         .clone();
         tmpcomp.setComponentNo(jobNo + "&007");
         tmpcomp.execSites = null;
         tmpcomp.execSites = new ArrayList<String>();
         componentsList.add(tmpcomp);
         }
         }
         }
         if (compCount < 1) {
         compCount = componentsList.size();
         int i = 1;
         for (RunnerJobComponent component : componentsList) {
         component.setComponentNo(jobNo + "&" + i);
         logger.debug("Added component number "
         + component.getComponentNo());
         i++;
         }
         logger.debug("Total Components added "
         + componentsList.size());

         }
         if (componentsList.size() > 0) {
         componentsAdded.set(true);
         runnerLock.notifyAll();
         }
         }*/
	//}
/*
	private void handleTransferFiles(String [] message) {

		// This is the place message. We now have a new (or retried) job. 
		System.err.println("JOB_FILES: " + Arrays.deepToString(message));

		if (message.length < 4) {
			logger.warn("Incorrect server response: " + Arrays.toString(message));
			return;
		}

		String component = message[1];
		String site      = message[3];

		logger.info("Execution site for " + component + " = " + site);

		if (!components.setSite(component, site)) { 
			// FIXME: print error ?
			return;
		}
*/

		// This is actually the PLACE message 
		// if we get here it means we have a new job or a retried job
		// this is never a stray message from an 'old' job try cause
		// the jobTrynumber 'ticket' prevents that. so reset
		// 'failureOccurred' here.
		/*
         failureOccurred.set(false);
         serverPreemptedUs.set(false);
         componentRestart.set(false);
         JLT = 0;

         synchronized (transfLock) {
         waitForJobId();

         if (schedMsg.length < 4) {
         logger.error("Incorrect server response: no execution site received");
         logger.debug("Server message: " + message);
         }

         String componentNo = schedMsg[1];
         String execSite = schedMsg[3];
         logger.info("Execution site for " + componentNo + " = "
         + execSite);

         RunnerJobComponent thisComponent = null;
         List inputFilesRsl = null;
         // find job and add filesite information and get inputFiles
         for (RunnerJobComponent component : componentsList) {
         if (component.getComponentNo().equals(componentNo)) {
         thisComponent = component;
         } else if (componentNo.indexOf(jobNo) != -1) {
         logger.debug("The job was splitted ! ");
         }
         }

         // assert
         if (null == thisComponent) {
         logger.error("Could not find component " + componentNo);
         return;
         }

         thisComponent.setReserved(true);
         synchronized (thisComponent.execSites) {
         inputFilesRsl = RslCommon.getInputFiles(thisComponent
         .getComponentRSL());

         if (null == inputFilesRsl)
         thisComponent.setFtm(
         Globals.FileTransferMutex.FTM_UNSUBMITTED);
         else
         thisComponent.setFtm(
         Globals.FileTransferMutex.FTM_IN_TRANSFER);

         thisComponent.execSites.clear();
         thisComponent.execSites.add(execSite);
         thisComponent.execSites.notify();
         }

         // Transfer input files if necessary
         //this is a big buggy at the moment !! disable it
         //   RunnerCommon.transferFiles(schedMsg, inputFilesRsl,
         //       filesInTransfer, thisComponent);



         }*/
//	}

	/*
	private void handleSubmit(String [] message) {

		System.err.println("JOB_SUMBIT: " + Arrays.deepToString(message));

		if (message.length < 4) {
			logger.warn("Incorrect server response: " + Arrays.toString(message));
			return;
		}

		String component = message[1];
		String rsl       = message[3];

		logger.info("Execution rsl for " + component + " = " + rsl);

		if (!components.setRSL(component, rsl)) { 
			// FIXME: print error ?
			return;
		}
*/
		/*
         if (schedMsg.length < 4) {
         logger.error("Incorrect server response: no rsl received");
         logger.debug("Server message: " + message);
         }

         String compNo = schedMsg[1];
         String compRsl = schedMsg[3];

         if (componentsList.size() == 0) {
         try {
         synchronized (runnerLock) {
         runnerLock.wait();
         }
         } catch (InterruptedException e) {
         e.printStackTrace();
         }
         }

         if (JLT == 0) {
         JLT = System.currentTimeMillis();
         }

         synchronized (SubmitLock) {
         if (!checkJobTryNumber(schedMsg[2])) {
         logger.debug(compNo + " Jobtry number changed while in " +
         "comp_submit! Ignoring submission request.");
         return;
         }

         // integrate new rsl into complist and submit
         for (RunnerJobComponent component : componentsList) {
         if (component.getComponentNo().equals(compNo)) {
         logger.debug("Submitting components for execution "
         + componentsList.size()
         + " exec site "
         + component.execSites.size());
         synchronized (component.execSites) {
         while (component.execSites.size() == 0) {
         try {
         component.execSites.wait();
         } catch (InterruptedException e) {
         e.printStackTrace();
         }
         }
         }

         synchronized (component.getFtm()) {
         if (haveInputFiles) {
         // if ftm_transferred than we dont need
         // two-phase commit, if ftm_unsbmitted
         // means
         // this component didn't have input
         // files so no two-phase commit
         if (component.getFtm() != Globals.FileTransferMutex.FTM_TRANSFERRED
         && component.getFtm() != Globals.FileTransferMutex.FTM_UNSUBMITTED) {
         // add twoPhase stuff to rsl with
         // jst
         compRsl += "( two_phase = 600 )";
         }
         }

         component.setComponentRSL(compRsl);
         component.setStatus(Status.COMPONENT_PENDING);

         logger.info("Submitting job component "
         + component.getComponentNo() + " ("
         + component.execSites.get(0) + ")");
         int gramResult = submitComponent(component);

         if (gramResult != Globals.GRAMJOB_SUCCESS) {
         logger.error("GRAM error: "
         + Assist.getGramErrorString(gramResult)
         + " (" + gramResult
         + ")");
         // for two-phase commits
         component
         .setFtm(Globals.FileTransferMutex.FTM_ERROR);
         if (!failureOccurred.getAndSet(true)) { // check
         // if a failure already occurred if one already occurred than this
         // job will be canceled anyway and this
         // message is old a failure occurred for the first time (of this jobtry)!
         jobTries.incrementAndGet();
         //   Deactivator.deactivateAll();
         component
         .setStatus(Status.COMPONENT_FAIL);

         if (GramErrorHandler
         .needUpdateErrorCount(gramResult)) {
         FailOver.jobFailed(componentsList);
         }

         if (GramErrorHandler
         .canRestartJob(gramResult)) {
         componentRestart.set(true);
         stopComponents();
         logger.info("Restarting job "
         + jobNo);
         send(schedulerAddress, "JOB_ERROR#"
         + jobNo + "#" + (jobTries.get()-1));
         } else {
         componentRestart.set(false);
         stopComponents();
         logger.fatal("Aborting job "
         + jobNo);
         send(schedulerAddress, "JOB_ABORT#"
         + jobNo + "#" + (jobTries.get()-1));
         postPhase(false);
         cleanUpRunner();
         }

         }
         break;
         }
         // for two-phase commits
         component.setFtm(Globals.FileTransferMutex.FTM_SUBMITTED);
         }
         }
         }
         }*/
//	}
/*
	private void handleReport(String[] schedMsg) {

		System.err.println("JOB_REPORT: " + Arrays.deepToString(schedMsg));

		/*            
         if (schedMsg.length < 4) {
         logger.error("Incorrect server response: " +
         "no reporter message received");

         logger.debug("Server message: " + message);
         } else {
         logger.info("Koala server: " + schedMsg[3]);
 //        }*/
	//}

	/*
    private void processReply(Socket socket) {

        // Read the reply from the socket
        String message = readReply(socket);

        // Check if we succeeded in reading the reply.
        if (message == null) {
            return;
        }

        // Split reply into the seperate parts.
        String[] schedMsg = message.split("#");

        // Make sure it has the correct form. 
        // NOTE: even if this check fails we continue to process the message. 
        //       (this was also done in the 'official' runners)/ 
        if (schedMsg.length < 3) {
            logger.error("Incorrect server response");
            logger.debug("Server message: " + message);
        }

        try {
            // First extract the command. 
            Globals.KCmds cmd = Globals.KCmds.valueOf(schedMsg[0]);

            // Make sure it is not an old reply. 
            if (!checkJobTryNumber(schedMsg[2])) {
                return;
            }

            // See which command was issued.
            switch (cmd) {
            case JOB_RESTART:
                handleJobRestart(schedMsg);
                break;

                // this means we have to abort the job, no db updates or
                // restarts.
            case JOB_ABORT:
                handleJobAbort(schedMsg);
                break;

                // The server sents this message if the job rsl has been
                // received and parsed correctly
                // the job is now in the placement queue
            case RESET_COMPONENTS:
                handleResetComponents();
                break;

            case JOB_ID:
                handleSetJobID(schedMsg);
                break;

            case CLONE_COMPONENT:
                handleCloneComponent(schedMsg);
                break;

                // this message is sent when the job is placed, whether or not
                // any files are to be transferred!
            case TRANSFER_FILES:
                handleTransferFiles(schedMsg);
                break;

                // Once a component is successfully placed and ready to be
                // claimed the
                // KOALA server will instruct us to submit this component with
                // COMP_SUBMIT command
            case COMP_SUBMIT:
                handleSubmit(schedMsg);
                break;

                // This command is used to pass informational server message to
                // the runner
            case REPORTER:
                handleReport(schedMsg);
                break;

            default:
                logger.warn("Ignoring unknown command from server");
                logger.debug("Server message: " + message);
            }

        } catch (IllegalArgumentException iae) {
            logger.error(iae);
            if (logger.isDebugEnabled())
                iae.printStackTrace();
        } catch (IOException e) {
            logger.error("Failed to connect to KOALA");
            if (logger.isDebugEnabled())
                e.printStackTrace();
        }
    }*/

	public void stateChange(JobState state, long time) {
		setState(state);

		GATEngine.fireMetric(this, new MetricEvent(this, 
				state.toString(), statusMetric, time));
	}

	public Job getJob(JobDescription arg0) {
		// TODO Auto-generated method stub
		return null;
	}        
}
