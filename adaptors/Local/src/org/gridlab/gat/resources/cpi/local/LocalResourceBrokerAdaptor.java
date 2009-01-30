package org.gridlab.gat.resources.cpi.local;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;

/**
 * An instance of this class is used to reserve resources.
 * <p>
 * A resource can either be a hardware resource or a software resource. A
 * software resource is simply an executable it makes little sense to reserve
 * such. Thus an instance of this class can currently only reserve a hardware
 * resource.
 * <p>
 * If one wishes to reserve a hardware resource, one must first describe the
 * hardware resource that one wishes to reserve. This is accomplished by
 * creating an instance of the class HardwareResourceDescription which describes
 * the hardware resource that one wishes to reserve. After creating such an
 * instance of the class HardwareResourceDescription that describes the hardware
 * resource one wishes to reserve, one must specify the time period for which
 * one wishes to reserve the hardware resource. This is accomplished by creating
 * an instance of the class TimePeriod which specifies the time period for which
 * one wishes to reserve the hardware resource. Finally, one must obtain a
 * reservation for the desired hardware resource for the desired time period.
 * This is accomplished by calling the method ReserveHardwareResource() on an
 * instance of the class LocalResourceBrokerAdaptor with the appropriate
 * instance of HardwareResourceDescription and the appropriate instance of
 * TimePeriod.
 * <p>
 * In addition an instance of this class can be used to find hardware resources.
 * This is accomplished using the method FindHardwareResources(). This is
 * accomplished by creating an instance of the class HardwareResourceDescription
 * which describes the hardware resource that one wishes to find. After creating
 * such an instance of the class HardwareResourceDescription that describes the
 * hardware resource one wishes to find, one must find the corresponding
 * hardware resource. This is accomplished by calling the method
 * FindHardwareResources() on an instance of the class
 * LocalResourceBrokerAdaptor with the appropriate instance of
 * HardwareResourceDescription.
 */
public class LocalResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", true);
        capabilities.put("endMultiJob", true);
        capabilities.put("submitJob", true);

        return capabilities;
    }

    protected static Logger logger = Logger
            .getLogger(LocalResourceBrokerAdaptor.class);

    /**
     * This method constructs a LocalResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     */
    public LocalResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);

        // if wrong scheme, throw exception!
        if (brokerURI.getScheme() != null) {
            if (!brokerURI.isCompatible("local")) {
                throw new GATObjectCreationException(
                        "Unable to handle incompatible scheme '"
                                + brokerURI.getScheme() + "' in broker uri '"
                                + brokerURI.toString() + "'");
            }
        }

        // the brokerURI should point to the local host else throw exception
        if (!brokerURI.refersToLocalHost()) {
            throw new GATObjectCreationException(
                    "The LocalResourceBrokerAdaptor doesn't refer to localhost, but to a remote host: "
                            + brokerURI.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {

        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        if (description.getProcessCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: process count > 1: "
                            + description.getProcessCount());
        }

        if (description.getResourceCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: resource count > 1: "
                            + description.getResourceCount());
        }

        String home = System.getProperty("user.home");
        if (home == null) {
            throw new GATInvocationException(
                    "local broker could not get user home dir");
        }

        Sandbox sandbox = new Sandbox(gatContext, description, "localhost",
                home, true, true, false, false);

        LocalJob localJob = new LocalJob(gatContext, description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, localJob);
            listener = tmp;
            job = tmp;
        } else {
            job = localJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        localJob.setState(Job.JobState.PRE_STAGING);
        sandbox.prestage();

        String exe;
        if (sandbox.getResolvedExecutable() != null) {
            exe = sandbox.getResolvedExecutable().getPath();
        } else {
            exe = getExecutable(description);
        }

        ProcessBuilder builder = new ProcessBuilder();
        
        // try to set the executable bit, it might be lost
        try {
            new CommandRunner("chmod", "+x", exe);
        } catch (Throwable t) {
            // ignore
        }

        builder.command().add(exe);
        String[] args = getArgumentsArray(description);
        if (args != null) {
            for (String arg : args) {
                builder.command().add(arg);
            }
        }

        java.io.File f = new java.io.File(sandbox.getSandboxPath());
               
        builder.directory(f);
        
        // fill in the environment
        Map<String, Object> env = sd.getEnvironment();
        if (env != null) {
            Map<String, String> e = builder.environment();
            e.clear();
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                builder.environment().put(entry.getKey(),
                        (String) entry.getValue());
            }
        }

        Process p = null;
        try {
            p = builder.start();
            localJob.setState(Job.JobState.RUNNING);
            localJob.setProcess(p);
            localJob.setSubmissionTime();
            localJob.setStartTime();
        } catch (IOException e) {
            throw new CommandNotFoundException("LocalResourceBrokerAdaptor", e);
        }

        if (!sd.streamingStderrEnabled()) {
            // read away the stderr

            try {
                if (sd.getStderr() != null) {
                    // to file
                    new StreamForwarder(p.getErrorStream(), GAT
                            .createFileOutputStream(sd.getStderr()), sd
                            .getExecutable()
                            + " [stderr]");
                } else {
                    // or throw it away
                    new StreamForwarder(p.getErrorStream(), null, sd
                            .getExecutable()
                            + " [stderr]");
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stderr!", e);
            }
        }

        if (!sd.streamingStdoutEnabled()) {
            // read away the stdout
            try {
                if (sd.getStdout() != null) {
                    // to file
                    new StreamForwarder(p.getInputStream(), GAT
                            .createFileOutputStream(sd.getStdout()), sd
                            .getExecutable()
                            + " [stdout]");
                } else {
                    // or throw it away
                    new StreamForwarder(p.getInputStream(), null, sd
                            .getExecutable()
                            + " [stdout]");
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stdout!", e);
            }
        }

        if (!sd.streamingStdinEnabled() && sd.getStdin() != null) {
            // forward the stdin from file
            try {
                new StreamForwarder(GAT.createFileInputStream(sd.getStdin()), p
                        .getOutputStream(), sd.getExecutable() + " [stdin]");
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file input stream for stdin!", e);
            }
        }

        localJob.monitorState();

        return job;
    }

}
