/*
 * Created on July 25, 2007
 */
package org.gridlab.gat.resources.cpi.glite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributeValueException;

import org.apache.log4j.Logger;
import org.glite.jdl.JobAd;
import org.glite.jdl.JobAdException;
import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.BaseException;
import org.glite.wms.wmproxy.InvalidArgumentFaultException;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.NoSuitableResourcesFaultException;
import org.glite.wms.wmproxy.ServiceException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.Environment;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author anna
 */

public class GliteResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = Logger
            .getLogger(GliteResourceBrokerAdaptor.class);

    private String delegationId;

    public GliteResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences, URI brokerURI) throws GATObjectCreationException {
        super(gatContext, preferences, brokerURI);
    }

    private String getDelegationId() {
        if (delegationId == null)
            delegationId = "delegationId" + Math.round(Math.random() * 10000.0);
        return delegationId;
    }

    protected String getResourceManagerContact(JobDescription description)
            throws GATInvocationException {
        if (brokerURI.getPath() == null || brokerURI.getPath().equals("")) {
            return brokerURI.toString() + "glite_wms_wmproxy_server";
        }
        return brokerURI.toString();
    }

    // right now it's the simplest method for finding proxy location
    // maybe in the future it will be more elaborate
    private String getProxyLocation() throws GATInvocationException {
        Environment e = new Environment();
        String proxyLocation = e.getVar("X509_USER_PROXY");

        if (proxyLocation == null) {
            throw new GATInvocationException(
                    "No proxy location under X509_USER_PROXY");
        } else {
            return proxyLocation;
        }
    }

    /* connect to WMProxy */
    private WMProxyAPI connectToWMProxy(String delegationId,
            JobDescription description) throws GATInvocationException {
        WMProxyAPI client = null;
        try {
            String clientContact = getResourceManagerContact(description);
            String proxyLocation = getProxyLocation();
            if (logger.isInfoEnabled()) {
                logger.info("client-contact: " + clientContact);
                logger.info("proxy-location: " + proxyLocation);
            }
            client = new WMProxyAPI(clientContact, proxyLocation);
            // delegation of user credentials
            String proxy = client.grstGetProxyReq(delegationId);
            client.grstPutProxy(delegationId, proxy);
        } catch (BaseException e) {
            throw new GATInvocationException(
                    "Unable to connect to WMProxy service", e);
        }
        return client;
    }

    /* check correctness of jobAd */
    private void checkJobAd(JobAd jobAd) throws GATInvocationException {
        try {
            jobAd.checkAll();
        } catch (IllegalArgumentException iae) {
            throw new GATInvocationException(
                    "Wrong type/format/value for an attribute in JobDescription",
                    iae);
        } catch (JobAdException jae) {
            throw new GATInvocationException(
                    "One or more values do not match with semantic rule", jae);
        }
    }

    private String getJobType(SoftwareDescription sd, String type) {
        // now only "Normal" type of job is supported
        String jobType = "Normal";
        return jobType;
    }

    private void setEnvironment(JobAd jobAd, SoftwareDescription sd)
            throws Exception {
        HashMap<String, Object> environment = (HashMap<String, Object>) sd
                .getEnvironment();
        if (environment == null)
            return;
        Iterator<String> it = environment.keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            String value = (String) environment.get(name);
            jobAd.addAttribute("Environment", name + "=" + value);
        }
    }

    private void setStdInputOutputError(JobAd jobAd, SoftwareDescription sd)
            throws Exception {
        File stdin = sd.getStdin();
        File stdout = sd.getStdout();
        File stderr = sd.getStderr();
        if (stdin != null) {
            jobAd.setAttribute("StdInput", stdin.getPath());
            jobAd.addAttribute("InputSandbox", stdin.getPath());
        }
        if (stdout != null) {
            jobAd.setAttribute("StdOutput", stdout.getPath());
            jobAd.addAttribute("OutputSandbox", stdout.getPath());
        }
        if (stderr != null) {
            jobAd.setAttribute("StdError", stderr.getPath());
            jobAd.addAttribute("OutputSandbox", stderr.getPath());
        }
    }

    private void setInputSandbox(JobAd jobAd, SoftwareDescription sd)
            throws Exception {
        HashMap<File, File> files = (HashMap<File, File>) sd.getPreStaged();
        if (files == null)
            return;
        Iterator<File> it = files.keySet().iterator();
        while (it.hasNext()) {
            File src = (File) it.next();
            File dest = (File) files.get(src);
            if (dest == null) {
                jobAd.addAttribute("InputSandbox", src.getName());
            } else {
                jobAd.addAttribute("InputSandbox", dest.getName());
            }
        }
        // all files are put into one directory (without tree structure)
        // maybe names are enough?
    }

    private void setOutputSandbox(JobAd jobAd, SoftwareDescription sd)
            throws Exception {
        HashMap<File, File> files = (HashMap<File, File>) sd.getPostStaged();
        if (files == null)
            return;
        Iterator<File> it = files.keySet().iterator();
        while (it.hasNext()) {
            File src = (File) it.next();
            jobAd.addAttribute("OutputSandbox", src.getPath());
        }
    }

    private void setVirtualOrganisation(JobAd jobAd, SoftwareDescription sd)
            throws GATInvocationException, IllegalArgumentException,
            InvalidAttributeValueException {
        //TODO get the VO from somewhere
        String vo = "";
        if (vo == null || vo.equals("")) {
            throw new GATInvocationException(
                    "no virtual organisation specified: " + vo);
        }
        jobAd.setAttribute("VirtualOrganisation", vo);
    }

    /* create jdl description of the job */
    /* for now only softwareDescription is supported */
    private JobAd createJobDescription(JobDescription description)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null)
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        JobAd jobAd = new JobAd();
        try {
            // Type options: "Job", "DAG", "Collection" (only Job is now
            // supported)
            jobAd.setAttribute("Type", "Job");
            // JobType options: "Normal", "Interactive", "MPICH",
            // "Partitinable", "Checkpointable", "Parametric"
            // "Partitinable" and "Parametric" options are not available for
            // DAGs and Collections
            jobAd.setAttribute("JobType", getJobType(sd, "Job"));
            // Executable (here only the path not the URI is needed and correct)
            // if executable is not already on the remote CE WN it should be in
            // prestage files
            jobAd.setAttribute("Executable", getExecutable(description));
            // Arguments
            jobAd.setAttribute("Arguments", getArguments(description));
            // Environment
            setEnvironment(jobAd, sd);
            // StdInput, StdOutput, StdError
            setStdInputOutputError(jobAd, sd);
            // InputSandbox
            setInputSandbox(jobAd, sd);
            // OutputSandbox
            setOutputSandbox(jobAd, sd);
            // Virtual Organisation
            setVirtualOrganisation(jobAd, sd);

            // parsing
            // directory (String): working directory - not supported
            // count (Integer/String): number of executables to run. - not
            // supported
            // hostCount (Integer/String): number of hosts to distribute on. -
            // not supported
            // maxTime (Long/String): maximal time in minutes. - not supported
            // TODO: maxWallTime (Long/String): maximal WALL time in minutes.
            // TODO: maxCPUTime (Long/String): maximal CPU time in minutes.
            // TODO: queue (String): target queue name.
            // project (String): project account to use. - not supported
            // TODO: minMemory (Integer/String): minimal required memory in MB.
            // maxMemory (Integer/String): maximal required memory in MB. - not
            // supported (why use it?)
            // TODO: (check if possible) saveState (Boolean/String): keep job
            // data persistent for restart.
            // TODO: (check if possible) restart=ID (String): restart job with
            // given ID.

            // the default rank
            jobAd.setAttribute("Rank", 0);
            jobAd.setAttributeExpr("requirements",
                    "other.GlueCEStateStatus==\"Production\"");
            jobAd.setAttribute("RetryCount", 3);
        } catch (Exception e) {
            throw new GATInvocationException(
                    "Exception while parsing softwareDescription", e);
        }
        checkJobAd(jobAd);
        return jobAd;
    }

    private JobAd createJobDescription(ResourceDescription resourceDescription)
            throws GATInvocationException {
        JobAd jobAd = new JobAd();
        // TODO: creating jdl
        checkJobAd(jobAd);
        return jobAd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
     */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription)
            throws GATInvocationException {
        JobDescription jd = new JobDescription(null, resourceDescription);
        WMProxyAPI client = connectToWMProxy(delegationId, jd);
        ArrayList<HardwareResource> resources = null;
        JobAd jobAd = createJobDescription(resourceDescription);
        String jdlString = jobAd.toString();
        // CE Ids satisfying the job Requirements specified in the JDL, ordered
        // according to the decreasing Rank
        StringAndLongList matchingResources = null;
        try {
            matchingResources = client.jobListMatch(jdlString, delegationId);
        } catch (AuthorizationFaultException afe) {
            throw new GATInvocationException(
                    "The client is not authorized to perform this operation",
                    afe);
        } catch (AuthenticationFaultException aufe) {
            throw new GATInvocationException(
                    "A generic authentication problem occurred", aufe);
        } catch (NoSuitableResourcesFaultException nsrfe) {
            // no suitable resources matching job requirements have been found.
            return resources;
        } catch (InvalidArgumentFaultException iafe) {
            throw new GATInvocationException(
                    "The given job JDL expression is not valid", iafe);
        } catch (ServiceException se) {
            throw new GATInvocationException(
                    "Unknown error occured during the execution of the remote method call to the WMProxy server",
                    se);
        } catch (Exception e) {
            throw new GATInvocationException(
                    "Unknown error occured during the execution of the remote method call to the WMProxy server",
                    e);
        }
        if (matchingResources != null) {
            resources = new ArrayList<HardwareResource>();
            StringAndLongType[] matchingResourcesList = matchingResources
                    .getFile();
            for (int i = 0; i < matchingResourcesList.length; i++) {
                System.out.println(matchingResourcesList[i].getName());
                // TODO make a HardwareResource subclass
                // resources.add(matchingResourcesList[i].getName());
            }
        }
        // CE Ids satisfying the job Requirements specified in the JDL
        return resources;
    }

    public boolean isDryRun(JobDescription description)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null)
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        HashMap<String, Object> attributes = (HashMap<String, Object>) sd
                .getAttributes();
        if (attributes == null)
            return false;
        Boolean dryRun = (Boolean) attributes.get("dry.run");
        if (dryRun != null)
            return dryRun.booleanValue();
        else
            return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
        String delegationId = getDelegationId();
        WMProxyAPI client = connectToWMProxy(delegationId, description);
        JobAd jobAd = createJobDescription(description);
        if (isDryRun(description)) {
            GliteJob job = new GliteJob(gatContext, preferences, description,
                    null);
            job.setClient(client);
            job.setState(Job.STOPPED);
            return job;
        }
        String jdlString = jobAd.toString();
        if (logger.isInfoEnabled()) {
            logger.info(jdlString);
        }
        JobIdStructType jobId = null;
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }
        Map<File, File> preStageFiles = sd.getPreStaged();
        File[] srcFiles = preStageFiles.keySet().toArray(
                new File[preStageFiles.size()]);
        for (File srcFile : srcFiles) {
            if (preStageFiles.get(srcFile) != null) {
                throw new GATInvocationException(
                        "Glite cannot handle pre staged files with a non-null destination.");
            }
        }
        GliteJob job = null;
        try {
            if (preStageFiles.size() > 0) {
                jobId = client.jobRegister(jdlString, delegationId);
                // TODO: optimization, provide the protocol to the
                // getSandboxDestURI
                String[] sandboxURIs = client.getSandboxDestURI(jobId.getId())
                        .getItem();
                boolean prestaged = false;
                GATInvocationException exception = new GATInvocationException(
                        "Unable to prestage!");
                for (String sandboxURI : sandboxURIs) {
                    // one sandbox has to succeed
                    URI uri = new URI(sandboxURI);
                    try {
                        // use the sandbox to prestage the prestage files.
                        Sandbox sandbox = new Sandbox(gatContext, preferences,
                                description, uri.getHost(),
                                "/" + uri.getPath(), false, false, false, false);
                        job = new GliteJob(gatContext, preferences,
                                description, sandbox);
                        // now the job is created, immediately add the listener
                        // to it, so that it will receive each state
                        if (listener != null && metricDefinitionName != null) {
                            Metric metric = job.getMetricDefinitionByName(
                                    metricDefinitionName).createMetric(null);
                            job.addMetricListener(listener, metric);
                        }
                        job.setClient(client);
                        job.setState(Job.PRE_STAGING);
                        sandbox.prestage();
                        prestaged = true;
                        break;
                    } catch (Exception e) {
                        exception.add("Sandbox", e);
                    }
                }
                // if prestaged was successful start the job, else cancel the
                // job and throw the appropriate exception
                if (prestaged) {
                    client.jobStart(jobId.getId());
                } else {
                    client.jobCancel(jobId.getId());
                    throw exception;
                }
            } else {
                job = new GliteJob(gatContext, preferences, description, null);
                job.setClient(client);
                jobId = client.jobSubmit(jdlString, delegationId);
            }
        } catch (Exception e) {
            throw new GATInvocationException("Exception at job submission", e);
        }
        job.setJobID(jobId);
        job.startPoller();
        return job;
    }
}
