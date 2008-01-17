package org.gridlab.gat.resources.cpi;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.Environment;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class WrapperSubmitter {

    protected static Logger logger = Logger.getLogger(WrapperSubmitter.class);

    private static final String WELL_KNOWN_REMOTE_GAT_LOCATION = ".tempGAT";

    private static HashSet<String> hostsWithRemoteGAT = new HashSet<String>();

    static int wrapperCounter = 0;

    synchronized static int getCounter() {
        return wrapperCounter++;
    }

    private boolean multiJob = false;
    private GATContext gatContext;
    private Preferences preferences;
    private URI brokerURI;
    private List<JobDescription> descriptions = new ArrayList<JobDescription>();
    private List<WrappedJobImpl> jobs = new ArrayList<WrappedJobImpl>();
    private String[] preStageDoneLocations;

    public WrapperSubmitter(GATContext gatContext, Preferences preferences,
            URI brokerURI, boolean multiJob) {
        this.multiJob = multiJob;
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.brokerURI = brokerURI;
    }

    public Job submitJob(JobDescription description)
            throws GATInvocationException {
        WrappedJobImpl result = new WrappedJobImpl(gatContext, preferences,
                description);
        descriptions.add(description);
        jobs.add(result);
        if (!multiJob) {
            flushJobSubmission(); // in a new thread ?
        }
        return result;
    }

    public Job flushJobSubmission() throws GATInvocationException {
        if (descriptions.size() == 0) {
            throw new GATInvocationException("no jobs to submit!");
        }
        JobDescription mainDescription = descriptions.get(0);
        String host = brokerURI.getHost();
        String singleRemoteGAT = (String) preferences.get("singleRemoteGAT");
        if (singleRemoteGAT != null && singleRemoteGAT.equalsIgnoreCase("true")) {
            SoftwareDescription sd = mainDescription.getSoftwareDescription();
            String remoteGATLocation = sd.getStringAttribute(
                    "remoteGatLocation", null);
            if (remoteGATLocation == null) {
                if (!hostsWithRemoteGAT.contains(host)) {
                    // copy the gat
                    copyGAT(host);
                }
                sd.addAttribute("remoteGatLocation", "../"
                        + WELL_KNOWN_REMOTE_GAT_LOCATION);
                mainDescription.setSoftwareDescription(sd);
            }
        }
        return doSubmitJob();
    }

    private Job doSubmitJob() throws GATInvocationException {
        try {
            Preferences newPreferences = new Preferences(preferences);
            newPreferences.put("wrapper.enable", "false");

            SoftwareDescription origSd = descriptions.get(0)
                    .getSoftwareDescription();
            if (origSd == null) {
                throw new GATInvocationException(
                        "The job description does not contain a software description");
            }

            SoftwareDescription sd = new SoftwareDescription();

            // start with all old attributes.
            // incorrect ones will be overwritten below
            // sd.setAttributes(origSd.getAttributes());

            Map<String, Object> environment = new HashMap<String, Object>();
            Environment localEnv = new Environment();
            int counter = getCounter();

            String getRemoteOutput = origSd.getStringAttribute(
                    "wrapper.output", null);
            if (getRemoteOutput != null
                    && getRemoteOutput.equalsIgnoreCase("true")) {
                String remoteOutputURI = origSd.getStringAttribute(
                        "wrapper.output.location", "any:///wrapper");
                File outFile = GAT.createFile(gatContext, newPreferences,
                        new URI(remoteOutputURI + "." + counter + ".out"));
                File errFile = GAT.createFile(gatContext, newPreferences,
                        new URI(remoteOutputURI + "." + counter + ".err"));
                sd.setStdout(outFile);
                sd.setStderr(errFile);
            }

            preStageDoneLocations = new String[descriptions.size()];
            for (int i = 0; i < preStageDoneLocations.length; i++) {
                String sequentialPreStaging = descriptions.get(i)
                        .getSoftwareDescription().getStringAttribute(
                                "wrapper.prestage", "parallel");

                if (sequentialPreStaging.equalsIgnoreCase("sequential")) {
                    preStageDoneLocations[i] = PreStageSequencer
                            .createPreStageMonitor();
                }
            }

            // sd.setExecutable("java:org.gridlab.gat.resources.cpi.Wrapper");

            Object javaHome = origSd.getObjectAttribute("java.home");
            if (javaHome == null) {
                throw new GATInvocationException("java.home not set");
            }
            sd.addAttribute("java.home", javaHome);
            sd.setExecutable(javaHome + "/bin/java");

            boolean remoteIsGatEnabled = false;
            String remoteEngineLibLocation = "./lib/";

            String remoteGatLocation = origSd.getStringAttribute(
                    "remoteGatLocation", null);
            if (remoteGatLocation != null) {
                remoteEngineLibLocation = remoteGatLocation + "/lib/";
                remoteIsGatEnabled = true;
            }
            String localGATLocation = localEnv.getVar("GAT_LOCATION");
            java.io.File engineDir = new java.io.File(localGATLocation + "/lib");
            String[] files = engineDir.list();
            String classPath = ".:" + remoteGatLocation;
            for (int i = 0; i < files.length; i++) {
                classPath += ":" + remoteEngineLibLocation + files[i];
            }
            sd.addAttribute("java.classpath", classPath);

            if (remoteIsGatEnabled) {
                environment.put("gat.adaptor.path", remoteGatLocation
                        + "/lib/adaptors");
            } else {
                environment.put("gat.adaptor.path", "lib/adaptors");
            }

            sd.setEnvironment(environment);

            if (!remoteIsGatEnabled) {
                // prestage the gat itself
                sd.addPreStagedFile(GAT.createFile(gatContext, newPreferences,
                        new URI(localGATLocation + "/log4j.properties")));
                sd.addPreStagedFile(GAT.createFile(gatContext, newPreferences,
                        new URI(localGATLocation + "/lib")));
            }

            java.io.File descriptorFile = writeDescriptionsToFile(newPreferences);
            sd.addPreStagedFile(GAT.createFile(gatContext, newPreferences,
                    new URI(descriptorFile.getAbsolutePath())));

            String cwd = System.getProperty("user.dir");

            String jobIDs = "";
            for (int i = 0; i < jobs.size(); i++) {
                jobIDs += jobs.get(i).getJobID() + ",";
            }

            sd
                    .setArguments(new String[] {
                            "-cp",
                            classPath,
                            "-Dgat.adaptor.path="
                                    + environment.get("gat.adaptor.path"),
                            "org.gridlab.gat.resources.cpi.Wrapper",
                            descriptorFile.getName(),
                            GATEngine.getLocalHostName(),
                            cwd,
                            ""
                                    + origSd
                                            .getBooleanAttribute(
                                                    "verboseWrapper",
                                                    GATEngine.VERBOSE),
                            ""
                                    + origSd.getBooleanAttribute(
                                            "debugWrapper", GATEngine.DEBUG),
                            ""
                                    + origSd.getBooleanAttribute("timeWrapper",
                                            GATEngine.TIMING), jobIDs });

            String queue = origSd.getStringAttribute("queue", null);
            if (queue != null) {
                sd.addAttribute("queue", queue);
            }

            long maxTime = origSd.getLongAttribute("maxTime", -1);
            if (maxTime > 0) {
                sd.addAttribute("maxTime", new Long(maxTime));
            }

            long maxWallTime = origSd.getLongAttribute("maxWallTime", -1);
            if (maxWallTime > 0) {
                sd.addAttribute("maxWallTime", new Long(maxWallTime));
            }

            long maxCPUTime = origSd.getLongAttribute("maxCPUTime", -1);
            if (maxCPUTime > 0) {
                sd.addAttribute("maxCPUTime", new Long(maxCPUTime));
            }

            if (origSd.getAttributes().containsKey("wrapper.sandbox.root")) {
                sd.addAttribute("sandbox.root", origSd.getAttributes().get(
                        "wrapper.sandbox.root"));
            }

            JobDescription jd = new JobDescription(sd);
            ResourceBroker broker = GAT.createResourceBroker(gatContext,
                    newPreferences, brokerURI);
            Job j = broker.submitJob(jd);
            descriptorFile.delete();
            Iterator<WrappedJobImpl> it = jobs.iterator();
            // we can now safely delete the descriptor file, it has been
            // prestaged.

            while (it.hasNext()) {
                WrappedJobImpl job = (WrappedJobImpl) it.next();
                job.setWrapperJob(j);
            }
            return j;
        } catch (Exception e) {
            throw new GATInvocationException("WrapperSubmitter", e);
        }
    }

    private void copyGAT(String host) {
        Environment localEnv = new Environment();
        String localGATLocation = localEnv.getVar("GAT_LOCATION");
        try {
            File gatDir = GAT.createFile(gatContext, localGATLocation + "/lib");
            File log4jFile = GAT.createFile(gatContext, localGATLocation
                    + "/log4j.properties");
            File destDir = GAT.createFile(gatContext, "any://" + host + "/"
                    + WELL_KNOWN_REMOTE_GAT_LOCATION);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            gatDir.copy(new URI("any://" + host + "/"
                    + WELL_KNOWN_REMOTE_GAT_LOCATION + "/lib"));
            log4jFile.copy(new URI("any://" + host + "/"
                    + WELL_KNOWN_REMOTE_GAT_LOCATION + "/log4j.properties"));
        } catch (GATObjectCreationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to create remote file:" + e);
            }
        } catch (URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Wrong URI:" + e);
            }
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        }
        hostsWithRemoteGAT.add(host);
    }

    private java.io.File writeDescriptionsToFile(Preferences preferences)
            throws GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("writing description: " + descriptions);
        }
        java.io.File f = null;
        try {
            f = File.createTempFile("GAT", "jobDescription");
            FileOutputStream tmp = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(tmp);
            out.writeObject(preferences);
            out.writeObject((JobDescription[]) descriptions
                    .toArray(new JobDescription[descriptions.size()]));
            out.writeObject(preStageDoneLocations);
            out.close();
        } catch (Exception e) {
            throw new GATInvocationException("WrapperSubmitter", e);
        }

        return f;
    }

    // public String getHostname(JobDescription description)
    // throws GATInvocationException {
    // String contactHostname = null;
    //
    // String contact = (String) preferences
    // .get("ResourceBroker.jobmanagerContact");
    // if (contact != null) {
    // StringTokenizer st = new StringTokenizer(contact, ":/");
    // contactHostname = st.nextToken();
    // }
    //
    // ResourceDescription d = description.getResourceDescription();
    //
    // if (d == null) {
    // return contactHostname;
    // }
    //
    // if (!(d instanceof HardwareResourceDescription)) {
    // if (contactHostname != null)
    // return contactHostname;
    //
    // throw new GATInvocationException(
    // "Currently only hardware resource descriptions are supported");
    // }
    //
    // Map<String, Object> m = d.getDescription();
    // Set<String> keys = m.keySet();
    // Iterator<String> i = keys.iterator();
    //
    // while (i.hasNext()) {
    // String key = (String) i.next();
    // Object val = m.get(key);
    //
    // if (key.equals("machine.node")) {
    // if (val instanceof String) {
    // return (String) val;
    // } else {
    // String[] hostList = (String[]) val;
    // return hostList[0];
    // }
    // }
    // }
    //
    // return contactHostname;
    // }

    public static void end() {
        Iterator<String> it = hostsWithRemoteGAT.iterator();
        GATContext context = new GATContext();
        while (it.hasNext()) {
            String host = (String) it.next();
            File dir;
            try {
                dir = GAT.createFile(context, "any://" + host + "/"
                        + WELL_KNOWN_REMOTE_GAT_LOCATION);
                dir.recursivelyDeleteDirectory();
            } catch (GATObjectCreationException e) {
                if (logger.isInfoEnabled()) {
                    logger
                            .info("Unable to remove temporarly remote GAT directory:"
                                    + "any://"
                                    + host
                                    + "/"
                                    + WELL_KNOWN_REMOTE_GAT_LOCATION
                                    + " ("
                                    + e
                                    + ")");
                }
            } catch (GATInvocationException e) {
                if (logger.isInfoEnabled()) {
                    logger
                            .info("Unable to remove temporarly remote GAT directory:"
                                    + "any://"
                                    + host
                                    + "/"
                                    + WELL_KNOWN_REMOTE_GAT_LOCATION
                                    + " ("
                                    + e
                                    + ")");
                }
            }
        }
    }

    public boolean isMultiJob() {
        return multiJob;
    }
}
