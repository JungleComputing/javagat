package nl.vu.ibis;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.SoftwareDescription;

import org.apache.log4j.Logger;

import org.koala.common.Globals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;

public class KoalaJob extends JobCpi implements Runnable {

    // Generated to keep eclipse happy...
    private static final long serialVersionUID = 4321647692737044640L;

    //  The logger used for debugging output and warnings.
    private static Logger logger = Logger.getLogger("KoalaAdaptor.Runner");

    // Timeout for accepts. 
    private static final int DEFAULT_ACCEPT_TIMEOUT = 1000;

    // Contact address of the koala scheduler. 
    private final InetSocketAddress schedulerAddress;

    // Server socket for incoming replies.   
    private final ServerSocket server;

    // Buffer containing the command string. 
    private final String command;

    // The RSL describing the job.
    // private final String rsl;

    // The metric definition of the job status metric.   
    private final MetricDefinition statusMetricDefinition;

    // The actual status metric. 
    private final Metric statusMetric;
    
    // The number Koala assigns to the job.  
    private int jobNo;

    // The number of submission attempts so far.  
    private int jobTries;

    // Indicates if the job if done.  
    private boolean done = false;
    
    // A description of the job.
    private JobDescription jobDescription;

    // A map containing the components of the job. 
    private final Components components;
    
    // The thread running this Job.
    private Thread thread;
        
    // Simple thread to process Koala replies. 
    class MessageHandler extends Thread {

        private final Socket socket;

        public MessageHandler(Socket socket) {
            this.socket = socket;
            setName("KoalaJob Message Handler");
        }

        public void run() {
            processReply(socket);
        }        
    }
    
    protected KoalaJob(GATContext context, 
            Preferences preferences, JobDescription jobDescription, 
            InetSocketAddress schedulerAddress, String schedulingPolicy, 
            String rsl) throws GATInvocationException {

        super(context, preferences, jobDescription, null);

        // Save the GAT context and job description.
        this.components = new Components(this, context);
        this.jobDescription = jobDescription;
        
        // Save the shedulers contact address and job RSL.
        this.schedulerAddress = schedulerAddress;
        // this.rsl = rsl;
        
        // Make sure we have a default scheduling policy
        if (schedulingPolicy == null) {
            schedulingPolicy = "WF";
        }

        // Use a StringBuilder to create a Koala command.
        StringBuilder buffer = new StringBuilder("JOB_NEW#");

        // Get the software description of the job. 
        SoftwareDescription sw = jobDescription.getSoftwareDescription();
        Map<String, Object> attributes = sw.getAttributes();

        // Set the priority of the job. Default is 1.         
        int priority = 1;

        if (attributes.containsKey("koala.priority")) {
            try {
                priority = (Integer) attributes.get("koala.priority");
            } catch (Exception e) {
                logger.warn("Illegal \"koala.priority\" setting!");
            }
        }

        buffer.append("-j#" + priority + "#");

        // Check if the job is flexible (i.e., the size can be varied).
        if (attributes.containsKey("koala.flexible")) {
            try {
                int minimalSize = (Integer) attributes.get("koala.flexible");

                minimalSize = Math.max(1, minimalSize);

                buffer.append("-flex#");
                buffer.append(minimalSize);
                buffer.append("#");

                schedulingPolicy = "FWF";

            } catch (Exception e) {
                logger.warn("Illegal \"koala.flexible\" setting!");
            }
        }

        // Should we optimize the file transfertime ? 
        if (attributes.containsKey("koala.optimize.transfer")) {
            try {
                boolean optTransfer = (Boolean) attributes
                        .get("koala.optimize.transfer");

                if (optTransfer) {
                    schedulingPolicy = "BWF";
                }
            } catch (Exception e) {
                logger.warn("Illegal \"koala.optimize.transfer\" setting!");
            }

        }

        // Set the scheduling policy 
        buffer.append("-policy#" + schedulingPolicy + "#");

        // Add the RSL describing the job. 
        buffer.append("-rsl#");
        buffer.append(rsl);
        buffer.append("#");

        // Create a ServerSocketChannel so the scheduler can contact us. 
        try {
            server = new ServerSocket(0);
            server.setSoTimeout(DEFAULT_ACCEPT_TIMEOUT);
        } catch (Exception e) {
            throw new GATInvocationException("Failed to initialize Koala "
                    + "runner", e);
        }

        // Append contact, user and runner information to the command.   
        buffer.append("-A#");
        buffer.append(server.getLocalPort() + "#");
        buffer.append("-U#");
        buffer.append(System.getenv("USER") + "#");
        buffer.append("-R#");
        buffer.append("KRunner");

        // Store the end result in the command string. 
        command = buffer.toString();

        System.err.println("Generated KOALA command:\n" + command);
        
        // Tell the GAT engine that we provide job.status events
        HashMap<String, Object> definition = new HashMap<String, Object>();
        definition.put("status", String.class);
        
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, definition);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

    }

    public synchronized int getState() {        
        return state;
    }

    public Map<String, Object> getInfo() {
        return components.getInfo(null);           }

    public void submitToScheduler() throws IOException {
        // First start a reply handling thread!
        thread = new Thread(this, "KoalaJob<Unknown>");
        thread.start();
        send(schedulerAddress, command);
    }

    private synchronized boolean getDone() { 
        return done;
    }
        
    /**
     * Used in synchronisation. the Runners are supposed to wait 
     * until they are assigned job number by the Scheduler
     */
    

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

    /**
     * Checks if the received server message is not an old message intended for
     * previous job run (after a restart has occurred)
     * 
     * @param jobTryNumber
     *          the jobtry number of the server response
     * @return true if this message is valid for this jobrun
     */
    private boolean checkJobTryNumber(String jobTryNumber) {
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

        System.err.println("CHECK JOB NUMBER: " + jobTryNumber);

        return true;
    }

    private synchronized void setState(int state) {
        
        if (done) { 
            return;
        }

        this.state = state;
        
        if (state == STOPPED || state == SUBMISSION_ERROR) { 
            done = true;
            components.stop();
        
            try {
                System.err.println("@@@ Sending abort message to KOALA");
                send(schedulerAddress, "JOB_ABORT#" + jobNo + "#" + jobTries);
            } catch (IOException e) {
                System.err.println("Error sending abort message to KOALA");
            }
        }        
    }
    
    
    @Override
    public void stop() throws GATInvocationException {
        System.err.println("### stopping job");
        setState(STOPPED);
    }

    // Close a socket and the associated streams.
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
    }

    private void handleJobAbort(String[] message) {
        System.err.println("JOB_ABORT: " + Arrays.deepToString(message));
        
        // This will stop all components and set done to true.
        setState(SUBMISSION_ERROR);
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
    }

    private void handleSetJobID(String [] message) throws IOException {
        System.err.println("JOB_ID: " + Arrays.deepToString(message));

        jobNo = Integer.parseInt(message[1]);
        
        // We start with a single-component job.
        components.createComponent(jobDescription, preferences, jobNo);
        
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
    }

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
    }

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
    }

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
    }

    private void handleReport(String[] schedMsg) {

        System.err.println("JOB_REPORT: " + Arrays.deepToString(schedMsg));

        /*            
         if (schedMsg.length < 4) {
         logger.error("Incorrect server response: " +
         "no reporter message received");
         
         logger.debug("Server message: " + message);
         } else {
         logger.info("Koala server: " + schedMsg[3]);
         }*/
    }
    
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
    }
    
    public void stateChange(int state, long time) {
        setState(state);
        
        GATEngine.fireMetric(this, new MetricValue(this, 
                Job.getStateString(state), statusMetric, time));
    }        
}
