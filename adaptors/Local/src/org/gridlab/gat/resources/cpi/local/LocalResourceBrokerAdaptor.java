package org.gridlab.gat.resources.cpi.local;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperSubmitter;

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

    protected static Logger logger = Logger
            .getLogger(LocalResourceBrokerAdaptor.class);

    private WrapperSubmitter submitter;

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

        // the brokerURI should point to the local host else throw exception
        if (!brokerURI.refersToLocalHost()) {
            throw new GATObjectCreationException(
                    "The LocalResourceBrokerAdaptor doesn't refer to localhost, but to a remote host: "
                            + brokerURI.toString());
        }
    }

    public void beginMultiJob() throws GATInvocationException {
        if (submitter != null && submitter.isMultiJob()) {
            throw new GATInvocationException("Multi job started twice!");
        }
        submitter = new WrapperSubmitter(gatContext, brokerURI, true);
    }

    public Job endMultiJob() throws GATInvocationException {
        if (submitter == null) {
            throw new GATInvocationException(
                    "Multi job ended, without being started!");
        }
        Job job = submitter.flushJobSubmission();
        submitter = null;
        return job;
    }

    /**
     * This method attempts to reserve the specified hardware resource for the
     * specified time period. Upon reserving the specified hardware resource
     * this method returns a Reservation. Upon failing to reserve the specified
     * hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *                A description, a HardwareResourceDescription, of the
     *                hardware resource to reserve
     * @param timePeriod
     *                The time period, a TimePeriod , for which to reserve the
     *                hardware resource
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method attempts to find one or more matching hardware resources.
     * Upon finding the specified hardware resource(s) this method returns a
     * java.util.List of HardwareResource instances. Upon failing to find the
     * specified hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *                A description, a HardwareResoucreDescription, of the
     *                hardware resource(s) to find
     * @return java.util.List of HardwareResources upon success
     */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        if (getBooleanAttribute(description, "wrapper.enable", false)) {
            if (logger.isDebugEnabled()) {
                logger.debug("wrapper enabled: using wrapper application.");
            }
            if (submitter == null) {
                submitter = new WrapperSubmitter(gatContext, brokerURI, false);
            }
            return submitter.submitJob(description);
        }

        // fill in the environment
        String[] environment = null;
        Map<String, Object> env = sd.getEnvironment();
        int index = 0;
        if (env != null) {
            environment = new String[env.size()];
            Set<String> keys = env.keySet();
            Iterator<String> i = keys.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String val = (String) env.get(key);
                environment[index] = key + "=" + val;
                index++;
            }
        }

        String home = System.getProperty("user.home");
        if (home == null) {
            throw new GATInvocationException(
                    "local broker could not get user home dir");
        }

        Sandbox sandbox = new Sandbox(gatContext, description, "localhost",
                home, true, true, true, true);

        LocalJob job = new LocalJob(gatContext, description, sandbox);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.setState(Job.PRE_STAGING);
        sandbox.prestage();

        String exe;
        if (sandbox.getResolvedExecutable() != null) {
            exe = sandbox.getResolvedExecutable().getPath();
        } else {
            exe = getExecutable(description);
        }

        // try to set the executable bit, it might be lost
        try {
            new CommandRunner("/bin/chmod +x " + exe);
        } catch (Throwable t) {
            // ignore
        }
        try {
            new CommandRunner("/usr/bin/chmod +x " + exe);
        } catch (Throwable t) {
            // ignore
        }

        String command = exe + " " + getArguments(description);

        java.io.File f = new java.io.File(sandbox.getSandboxURI().getPath());

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + command);

            if (environment != null) {
                logger.info("  environment:");
                for (int i = 0; i < environment.length; i++) {
                    logger.info("    " + environment[i]);
                }
            }

            if (home != null) {
                logger.info("working dir is: "
                        + sandbox.getSandboxURI().getPath());
            }
        }

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command, environment, f);
            job.setState(Job.RUNNING);
            job.setProcess(p);
            job.setSubmissionTime();
            job.setStartTime();
        } catch (IOException e) {
            throw new CommandNotFoundException("LocalResourceBrokerAdaptor", e);
        }

        // org.gridlab.gat.io.File stdin = sandbox.getResolvedStdin();
        //
        // if (stdin == null) {
        // // close stdin.
        // try {
        // p.getOutputStream().close();
        // } catch (Throwable e) {
        // // ignore
        // }
        // } else {
        // try {
        // java.io.FileInputStream fin = new java.io.FileInputStream(stdin
        // .getAbsolutePath());
        // OutputStream out = p.getOutputStream();
        // new InputForwarder(out, fin);
        // } catch (Exception e) {
        // throw new GATInvocationException("local broker", e);
        // }
        // }
        String executable = sd.getExecutable();
        if (sd instanceof JavaSoftwareDescription) {
            executable = ((JavaSoftwareDescription) sd).getJavaMain();
        }

        StreamForwarder outForwarder = null;
        StreamForwarder errForwarder = null;

        // handle the input
        if (sd.getStdinFile() != null) {
            try {
                new StreamForwarder(GAT.createFileInputStream(sandbox
                        .getResolvedStdin()), p.getOutputStream(),
                        "local input " + executable);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Could not create a FileInputStream to read from the input '"
                                + sandbox.getResolvedStdin() + "'", e);
            }
        } else if (sd.getStdinStream() != null) {
            new StreamForwarder(sd.getStdinStream(), p.getOutputStream(),
                    "local input" + executable);
        } else {
            try {
                p.getOutputStream().close();
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Failed to close the OutputStream of the process: "
                                    + e);
                }
            }
        }

        if (sd.getStdoutFile() != null) {
            try {
                outForwarder = new StreamForwarder(p.getInputStream(), GAT
                        .createFileOutputStream(sandbox.getResolvedStdout()),
                        "local output" + executable);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Could not creat a FileOutputStream to write the output to '"
                                + sandbox.getResolvedStdout() + "'", e);
            }
        } else if (sd.getStdoutStream() != null) {
            outForwarder = new StreamForwarder(p.getInputStream(), sd
                    .getStdoutStream(), "local output" + executable);
        } else {
            try {
                p.getInputStream().close();
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Failed to close the InputStream of the process: "
                                    + e);
                }
            }
        }

        if (sd.getStderrFile() != null) {
            try {
                errForwarder = new StreamForwarder(p.getErrorStream(), GAT
                        .createFileOutputStream(sandbox.getResolvedStderr()),
                        "local error" + executable);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Could not creat a FileOutputStream to write the error to '"
                                + sandbox.getResolvedStderr() + "'", e);
            }
        } else if (sd.getStderrStream() != null) {
            errForwarder = new StreamForwarder(p.getErrorStream(), sd
                    .getStderrStream(), "local error" + executable);
        } else {
            try {
                p.getErrorStream().close();
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Failed to close the ErrorStream of the process: "
                                    + e);
                }
            }
        }

        job.startOutputWaiter(outForwarder, errForwarder);

        return job;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
     *      org.gridlab.gat.engine.util.TimePeriod)
     */
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public static void end() {
        if (logger.isDebugEnabled()) {
            logger.debug("local broker adaptor end");
        }
        WrapperSubmitter.end();
    }
}
