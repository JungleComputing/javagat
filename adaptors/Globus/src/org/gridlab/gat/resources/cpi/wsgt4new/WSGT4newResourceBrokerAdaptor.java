package org.gridlab.gat.resources.cpi.wsgt4new;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.addressing.ReferencePropertiesType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.globus.common.ResourceManagerContact;
import org.globus.exec.client.GramJob;
import org.globus.exec.utils.ManagedJobConstants;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
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
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * Implements the <code>ResourceBrokerCpi</code> abstract class.
 * 
 * @author Roelof Kemp
 * @version 1.0
 * @since 1.0
 */
public class WSGT4newResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", true);
        capabilities.put("endMultiJob", true);
        capabilities.put("submitJob", true);

        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences.put("wsgt4new.sandbox.gram", "false");
        preferences.put("wsgt4new.factory.type", "<FORK CONSTANT>");
        return preferences;
    }

    protected static Logger logger = Logger
            .getLogger(WSGT4newResourceBrokerAdaptor.class);

    protected GSSCredential getCred() throws GATInvocationException {
        GSSCredential cred = null;
        URI location = null;
        try {
            location = new URI(getHostname());
        } catch (Exception e) {
            throw new GATInvocationException(
                    "WSGT4Job: getSecurityContext, initialization of location failed, "
                            + e);
        }
        try {
            cred = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "ws-gram", location, ResourceManagerContact.DEFAULT_PORT);
        } catch (Exception e) {
            throw new GATInvocationException(
                    "WSGT4Job: could not initialize credentials, " + e);
        }
        return cred;
    }

    public WSGT4newResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
        // accept if broker URI is compatible with https or with any
        // if wrong scheme, throw exception!
        if (brokerURI.getScheme() != null) {
            if (!brokerURI.isCompatible("https")) {
                throw new GATObjectCreationException(
                        "Unable to handle incompatible scheme '"
                                + brokerURI.getScheme() + "' in broker uri '"
                                + brokerURI.toString() + "'");
            }
        }

        if (System.getProperty("GLOBUS_LOCATION") == null) {
            String globusLocation = System.getProperty("gat.adaptor.path")
                    + java.io.File.separator + "GlobusAdaptor"
                    + java.io.File.separator;
            System.setProperty("GLOBUS_LOCATION", globusLocation);
        }

        if (System.getProperty("axis.ClientConfigFile") == null) {
            String axisClientConfigFile = System
                    .getProperty("gat.adaptor.path")
                    + java.io.File.separator
                    + "GlobusAdaptor"
                    + java.io.File.separator + "client-config.wsdd";
            System.setProperty("axis.ClientConfigFile", axisClientConfigFile);
        }
    }

    protected String createRSL(JobDescription description, Sandbox sandbox,
            boolean useGramSandbox) throws GATInvocationException {
        String rsl = new String("<job>");
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        rsl += "<executable>";
        rsl += getExecutable(description);
        rsl += "</executable>";
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = (Object[]) s.toArray();

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                rsl += "<environment>";
                rsl += "<name>" + keys[i] + "</name>";
                rsl += "<value>" + val + "</value>";
                rsl += "</environment>";
            }
        }

        String[] argsA = getArgumentsArray(description);

        if (argsA != null) {
            for (int i = 0; i < argsA.length; i++) {
                rsl += "<argument>";
                rsl += argsA[i];
                rsl += "</argument>";
            }
        }

        // set the environment
        rsl += "<count>";
        rsl += description.getProcessCount();
        rsl += "</count>";
        rsl += "<directory>";
        if (sandbox.getSandbox().startsWith(File.separator)) {
            rsl += sandbox.getSandbox();
        } else {
            rsl += "${GLOBUS_USER_HOME}/" + sandbox.getSandbox();
        }
        rsl += "</directory>";

        File stdout = sd.getStdout();
        if (stdout != null) {
            rsl += "<stdout>";
            rsl += sandbox.getRelativeStdout().getPath();
            rsl += "</stdout>";
        }

        File stderr = sd.getStderr();
        if (stderr != null) {
            rsl += "<stderr>";
            rsl += sandbox.getRelativeStderr().getPath();
            rsl += "</stderr>";
        }

        File stdin = sd.getStdin();
        if (stdin != null) {
            rsl += "<stdin>";
            rsl += sandbox.getRelativeStdin().getPath();
            rsl += "</stdin>";
        }

        if (useGramSandbox) {
            Map<File, File> preStaged = sd.getPreStaged();
            if (preStaged != null) {
                Set<File> keys = preStaged.keySet();
                Iterator<File> i = keys.iterator();
                rsl += "<fileStageIn>";
                while (i.hasNext()) {
                    File srcFile = (File) i.next();
                    File destFile = (File) preStaged.get(srcFile);
                    if (destFile == null) {
                        logger
                                .debug("ignoring prestaged file, no destination set!");
                        continue;
                    }
                    rsl += "<transfer>";
                    try {
                        rsl += "<sourceUrl>" + srcFile.toURL() + "</sourceUrl>";
                        String destUrlString = null;
                        if (destFile.isAbsolute()) {
                            destUrlString = destFile.toURL().toString();
                        } else {
                            destUrlString = destFile.toURL().toString()
                                    .replace(
                                            destFile.getPath(),
                                            "${GLOBUS_USER_HOME}/"
                                                    + destFile.getPath());
                        }
                        rsl += "<destinationUrl>" + destUrlString
                                + "</destinationUrl>";
                    } catch (MalformedURLException e) {
                        throw new GATInvocationException(
                                "WSGT4ResourceBrokerAdaptor", e);
                    }
                    rsl += "</transfer>";
                }
                rsl += "</fileStageIn>";
            }

            Map<File, File> postStaged = sd.getPostStaged();
            if (preStaged != null) {
                Set<File> keys = postStaged.keySet();
                Iterator<File> i = keys.iterator();
                rsl += "<fileStageOut>";
                while (i.hasNext()) {
                    File srcFile = (File) i.next();
                    File destFile = (File) postStaged.get(srcFile);
                    if (destFile == null) {
                        logger
                                .debug("ignoring poststaged file, no destination set!");
                        continue;
                    }
                    rsl += "<transfer>";
                    try {
                        rsl += "<sourceUrl>" + srcFile.toURL() + "</sourceUrl>";
                        rsl += "<destinationUrl>" + destFile.toURL()
                                + "</destinationUrl>"; // TODO: Add
                        // ${GLOBUS_USER_HOME}
                    } catch (MalformedURLException e) {
                        throw new GATInvocationException(
                                "WSGT4ResourceBrokerAdaptor", e);
                    }
                    rsl += "</transfer>";
                }
                rsl += "</fileStageOut>";
            }
        }
        rsl += "</job>";

        if (logger.isInfoEnabled()) {
            logger.info("RSL: " + rsl);
        }

        return rsl;
    }

    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {

        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        // if wrapper is enabled, do the wrapper stuff
        String host = getHostname();
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "WSGT4ResourceBroker: the job description does not contain a software description");
        }

        // create an endpoint reference type
        EndpointReferenceType endpoint = new EndpointReferenceType();
        try {
            endpoint.setAddress(new Address(createAddressString()));
        } catch (MalformedURIException e) {
            throw new GATInvocationException("WSGT4newResourceBrokerAdaptor", e);
        }

        // test whether gram sandbox should be used
        String s = (String) gatContext.getPreferences().get(
                "wsgt4new.sandbox.gram");
        boolean useGramSandbox = (s != null && s.equalsIgnoreCase("true"));
        Sandbox sandbox = null;
        if (!useGramSandbox) {
            sandbox = new Sandbox(gatContext, description, host, null, true,
                    true, true, true);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("using gram sandbox");
            }
        }
        WSGT4newJob wsgt4job = new WSGT4newJob(gatContext, description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, wsgt4job);
            listener = tmp;
            job = tmp;
        } else {
            job = wsgt4job;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }

        if (!useGramSandbox) {
            wsgt4job.setState(Job.JobState.PRE_STAGING);
            sandbox.prestage();
        }

        // create a gramjob according to the jobdescription
        GramJob gramjob = null;
        try {
            gramjob = new GramJob(createRSL(description, sandbox,
                    useGramSandbox));
        } catch (RSLParseException e) {
            throw new GATInvocationException("WSGT4newResourceBrokerAdaptor", e);
        }

        // inform the wsgt4 job of which gram job is related to it.
        wsgt4job.setGramJob(gramjob);

        // Was: gramjob.setAuthorization(HostAuthorization.getInstance());
        // Modified to use a supplied credential. --Ceriel
        GSSCredential cred = getCred();
        if (cred != null) {
            gramjob.setCredentials(getCred());
            if (logger.isDebugEnabled()) {
                logger.debug("submitJob: credential = " + cred);
            }
        } else {
            gramjob.setAuthorization(HostAuthorization.getInstance());
        }
        // end modification.
        gramjob.setMessageProtectionType(Constants.ENCRYPTION);
        gramjob.setDelegationEnabled(true);

        // wsgt4 job object listens to the gram job
        gramjob.addListener(wsgt4job);

        String factoryType = (String) gatContext.getPreferences().get(
                "wsgt4new.factory.type");
        if (factoryType == null || factoryType.equals("")) {
            factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.FORK;
            if (logger.isDebugEnabled()) {
                logger.debug("no factory type supplied, using default: "
                        + ManagedJobFactoryConstants.FACTORY_TYPE.FORK);
            }
        }

        ReferencePropertiesType props = new ReferencePropertiesType();
        SimpleResourceKey key = new SimpleResourceKey(
                ManagedJobConstants.RESOURCE_KEY_QNAME, factoryType);
        try {
            props.add(key.toSOAPElement());
        } catch (SerializationException e) {
            throw new GATInvocationException("WSGT4newResourceBrokerAdaptor", e);
        }
        endpoint.setProperties(props);

        UUIDGen uuidgen = UUIDGenFactory.getUUIDGen();
        String submissionID = "uuid:" + uuidgen.nextUUID();

        if (logger.isDebugEnabled()) {
            logger.debug("submission id for job: " + submissionID);
        }
        wsgt4job.setSubmissionID(submissionID);
        try {
            gramjob.submit(endpoint, false, false, submissionID);
            wsgt4job.submitted();
        } catch (Exception e) {
            throw new GATInvocationException("WSGT4newResourceBrokerAdaptor", e);
        }

        // second parameter is batch, should be set to false.
        // third parameter is limitedDelegation, currently hardcoded to false
        return job;
    }

    private String createAddressString() {
        // default scheme: https
        // default port: 8443
        // default path: /wsrf/services/ManagedJobFactoryService
        logger.debug("brokerURI: " + brokerURI);
        String scheme = "https";
        if (brokerURI == null || !brokerURI.getScheme().equals("any")) {
            scheme = brokerURI.getScheme();
        }

        int port = brokerURI.getPort(8443);

        String path = "/wsrf/services/ManagedJobFactoryService";
        if (brokerURI.getUnresolvedPath() != null) {
            path = brokerURI.getUnresolvedPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        return scheme + "://" + brokerURI.getHost() + ":" + port + path;
    }
}
