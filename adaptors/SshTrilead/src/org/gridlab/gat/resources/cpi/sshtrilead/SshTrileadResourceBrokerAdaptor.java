package org.gridlab.gat.resources.cpi.sshtrilead;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.io.cpi.sshtrilead.SshTrileadFileAdaptor;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperSubmitter;

import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

/**
 * An instance of this class is used to execute remote jobs.
 */

public class SshTrileadResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = Logger
            .getLogger(SshTrileadResourceBrokerAdaptor.class);

    public static final int IN = 0, ERR = 1, OUT = 2;
    public static final int SSH_PORT = 22;

    private WrapperSubmitter submitter;

    /**
     * This method constructs a SshResourceBrokerAdaptor instance corresponding
     * to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to execute remote jobs
     */
    public SshTrileadResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences, URI brokerURI) throws Exception {
        super(gatContext, preferences, brokerURI);

        // accept if broker URI is compatible with ssh or with file
        if (!(brokerURI.isCompatible("ssh") || brokerURI.isCompatible("file"))) {
            throw new GATObjectCreationException(
                    "Cannot handle the scheme, scheme is: "
                            + brokerURI.getScheme());
        }
    }

    public void beginMultiJob() {
        submitter = new WrapperSubmitter(gatContext, preferences, brokerURI,
                true);
    }

    public Job endMultiJob() throws GATInvocationException {
        Job job = submitter.flushJobSubmission();
        submitter = null;
        return job;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
            // TODO: this broker is not Windows compatible (&&, export)

            // check whether there's a software description in the job
            // description
            SoftwareDescription sd = description.getSoftwareDescription();
            if (sd == null) {
                throw new GATInvocationException(
                        "The job description does not contain a software description");
            }

            // create the sandbox
            Sandbox sandbox = new Sandbox(gatContext, preferences, description,
                    getAuthority(), null, true, false, false, false);
            // create the job object
            SshTrileadJob job = new SshTrileadJob(gatContext, preferences,
                    description, sandbox);
            // add the listener to the job if specified
            if (listener != null && metricDefinitionName != null) {
                Metric metric = job.getMetricDefinitionByName(
                        metricDefinitionName).createMetric(null);
                job.addMetricListener(listener, metric);
            }
            // and now do the prestaging
            job.setState(Job.PRE_STAGING);
            sandbox.prestage();

            // construct the ssh command
            // 1. cd to the execution dir
            String command = "cd " + sandbox.getSandbox() + " && ";
            // 2. set necessary env variables using export
            Map<String, Object> env = sd.getEnvironment();
            if (env != null && !env.isEmpty()) {
                Set<String> s = env.keySet();
                Object[] keys = (Object[]) s.toArray();

                for (int i = 0; i < keys.length; i++) {
                    String val = (String) env.get(keys[i]);
                    command += "export " + keys[i] + "=" + val + " && ";
                }
            }
            // 3. and finally add the executable with its arguments
            command += "exec " + getExecutable(description) + " "
                    + getArguments(description);

            if (logger.isInfoEnabled()) {
                logger.info("running command: " + command);
            }

            OutputStream userOut;
            OutputStream userErr;
            if (sd.stdoutIsStreaming()) {
                userOut = sd.getStdoutStream();
            } else {
                try {
                    userOut = GAT.createFileOutputStream(sd.getStdout());
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException("failed to create outputstream to write in output file: '" + sd.getStdout() + "'", e);
                }
            }
            if (sd.stderrIsStreaming()) {
                userErr = sd.getStderrStream();
            } else {
                try {
                    userErr = GAT.createFileOutputStream(sd.getStderr());
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException("failed to create outputstream to write in error file: '" + sd.getStderr() + "'", e);
                }
            }
            Session session;
            try {
                session = SshTrileadFileAdaptor.getConnection(brokerURI,
                        gatContext, preferences).openSession();
                session.requestDumbPTY();
            } catch (Exception e) {
                throw new GATInvocationException("Unable to connect!", e);
            }
            job.setSession(session);
            try {
                session.execCommand(command);
            } catch (IOException e) {
                throw new GATInvocationException("execution failed!", e);
            }
            job.setState(Job.RUNNING);
            // see http://www.trilead.com/Products/Trilead-SSH-2-Java/FAQ/#blocking
            InputStream stdout = new StreamGobbler(session.getStdout());
            InputStream stderr = new StreamGobbler(session.getStderr());
            OutputForwarder outForwarder = new OutputForwarder(stdout, userOut);
            OutputForwarder errForwarder = new OutputForwarder(stderr, userErr);
            job.startOutputWaiter(outForwarder, errForwarder);
            return job;
    }
}
