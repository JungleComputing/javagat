package org.gridlab.gat.resources.cpi.gt42;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.globus.axis.message.addressing.AttributedURIType;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.globus.axis.message.addressing.ReferenceParametersType;
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
import org.gridlab.gat.engine.GATEngine;
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
import org.gridlab.gat.security.gt42.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GT42ResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static String getDescription() {
        return "The GT42 ResourceBroker Adaptor implements the ResourceBroker object on Globus 4.2 WS.";
    }

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
        preferences.put("GT42.sandbox.gram", "false");
        preferences.put("GT42.factory.type", "<FORK CONSTANT>");
        return preferences;
    }

       
    public static String[] getSupportedSchemes() {
        return new String[] { "gt42", "https"};
    }

    protected static Logger logger = LoggerFactory
            .getLogger(GT42ResourceBrokerAdaptor.class);

    // instance initializer sets personalized
    // EngineConfigurationFactory for the axis client.
    static {
        if (System.getProperty("GT42_LOCATION") == null) {
            String globusLocation = System.getProperty("gat.adaptor.path")
                    + java.io.File.separator + "GT42Adaptor"
                    + java.io.File.separator;
            System.setProperty("GT42_LOCATION", globusLocation);
        }
        if (AxisProperties.getProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME) == null) {
            AxisProperties.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,
            "org.gridlab.gat.resources.cpi.gt42.GlobusEngineConfigurationFactory");
        }
    }
    public static void init() {
        GATEngine.registerUnmarshaller(GT42Job.class);
    }

    private EndpointReferenceType endpoint;
    private GSSCredential cred;
    private long lastCredTime;

    protected synchronized GSSCredential getCred() throws GATInvocationException {
        long time = System.currentTimeMillis() - lastCredTime;
        if (cred == null || time > 15000) {
            try {
                cred = GlobusSecurityUtils.getGlobusCredential(gatContext,
                        "gt42", brokerURI, ResourceManagerContact.DEFAULT_PORT);
                lastCredTime = System.currentTimeMillis();
            } catch (Exception e) {
                throw new GATInvocationException(
                        "GT42Job: could not initialize credentials, " + e);
            }
        }
        return cred;
    }

    public GT42ResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
    }

    protected String createRSL(JobDescription description, Sandbox sandbox,
            boolean useGramSandbox) throws GATInvocationException {

        String rsl = new String("<job>");

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String queue = (String) sd.getAttributes().get("machine.queue");
        if (queue == null) {
            queue = sd.getStringAttribute(SoftwareDescription.JOB_QUEUE, null);
        }
        if (null != queue) {
            rsl += "<queue>" + queue + "</queue>";
        }

        String scheduler = (String) sd.getAttributes().get("machine.scheduler");
        String wsa = (String) sd.getAttributes().get("machine.wsa");
        if (null != scheduler && null != wsa) {
            rsl += "<factoryEndpoint ";
            rsl += "xmlns:gram=\"http://www.globus.org/namespaces/2004/10/gram/job\" ";
            rsl += "xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">";
            rsl += "<wsa:Address>";
            rsl += wsa;
            // rsl +=
            // "https://iwrgt4.fzk.de:8443/wsrf/services/ManagedJobFactoryService";

            rsl += "</wsa:Address>";
            rsl += "<wsa:ReferenceProperties>";
            rsl += "<gram:ResourceID>";
            rsl += scheduler;
            // rsl += "PBS";
            rsl += "</gram:ResourceID>";
            rsl += "</wsa:ReferenceProperties>";
            rsl += "</factoryEndpoint>";
        }

        rsl += "<executable>";
        rsl += getExecutable(description);
        rsl += "</executable>";
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = s.toArray();

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
                    File srcFile = i.next();
                    File destFile = preStaged.get(srcFile);
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
                                "GT42ResourceBrokerAdaptor", e);
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
                    File srcFile = i.next();
                    File destFile = postStaged.get(srcFile);
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
                                "GT42ResourceBrokerAdaptor", e);
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
                    "GT4ResourceBroker: the job description does not contain a software description");
        }

        synchronized(this) {
            if (endpoint == null) {
                // create an endpoint reference type
                EndpointReferenceType endpoint = new EndpointReferenceType();
                try {

                    // setAddress method is changed. His parameter is an
                    // AttributedUriType
                    // AttributedURIType doesn't have any documentation
                    String address = createAddressString();
                    AttributedURIType attributedAddress = new AttributedURIType(address);
                    endpoint.setAddress(attributedAddress);

                    // The original code from WSGT4ResourceBroker was
                    // endpoint.setAddress(new Address(createAddressString()));

                    String factoryType = (String) gatContext.getPreferences().get(
                            "GT42.factory.type");
                    if (factoryType == null || factoryType.equals("")) {
                        factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.FORK;
                        if (logger.isDebugEnabled()) {
                            logger.debug("no factory type supplied, using default: "
                                    + ManagedJobFactoryConstants.FACTORY_TYPE.FORK);
                        }
                    }
                    /*
                     * ReferencePropertiesType doen't exist anymore ReferencePropertiesType
                     * props = new ReferencePropertiesType();
                     */

                    ReferenceParametersType params = new ReferenceParametersType();
                    SimpleResourceKey key = new SimpleResourceKey(
                            ManagedJobConstants.RESOURCE_KEY_QNAME, factoryType);
                    try {
                        params.add(key.toSOAPElement());
                    } catch (SerializationException e) {
                        throw new GATInvocationException("GT42ResourceBrokerAdaptor", e);
                    }

                    endpoint.setParameters(params);
                } catch (Exception e) {// Ho modificato anche il tipo di eccezione
                                        // sollevata
                    throw new GATInvocationException("GT42ResourceBrokerAdaptor", e);
                }
            }
        }

        // test whether gram sandbox should be used
        String s = (String) gatContext.getPreferences()
                .get("GT42.sandbox.gram");
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

        GT42Job gt42job = null;

        try {
            gt42job = new GT42Job(gatContext, description, sandbox);
            Job job;

            if (description instanceof WrapperJobDescription) {
                WrapperJobCpi tmp = new WrapperJobCpi(gatContext, gt42job, listener,
                        metricDefinitionName);
                listener = tmp;
                job = tmp;
            } else {
                job = gt42job;
            }

            if (listener != null && metricDefinitionName != null) {
                Metric metric = gt42job.getMetricDefinitionByName(metricDefinitionName)
                        .createMetric(null);
                gt42job.addMetricListener(listener, metric);
            }

            if (sandbox != null) {
                gt42job.setState(Job.JobState.PRE_STAGING);
                sandbox.prestage();
            }

            if (gt42job.stopped) {
                throw new GATInvocationException("Job stopped before it was started");
            }

            GramJob gramjob = null;
            try {

                gramjob = new GramJob(createRSL(description, sandbox,
                        useGramSandbox));

            } catch (RSLParseException e) {

                throw new GATInvocationException("GT42ResourceBrokerAdaptor", e);

            }

            // Was: gramjob.setAuthorization(HostAuthorization.getInstance());
            // Modified to use a supplied credential. --Ceriel
            GSSCredential cred = getCred();
            if (cred != null) {
                gramjob.setCredentials(cred);
                if (logger.isDebugEnabled()) {
                    logger.debug("submitJob: credential = " + cred);
                }
            } else {
                gramjob.setAuthorization(HostAuthorization.getInstance());
            }
            // end modification.
            gramjob.setMessageProtectionType(Constants.ENCRYPTION);
            gramjob.setDelegationEnabled(true);

            // inform the wsgt4 job of which gram job is related to it.
            gt42job.setGramJob(gramjob);


            UUIDGen uuidgen = UUIDGenFactory.getUUIDGen();
            String submissionID = "uuid:" + uuidgen.nextUUID();

            if (logger.isDebugEnabled()) {
                logger.debug("submission id for job: " + submissionID);
            }

            // second parameter is batch, should be set to false.
            // third parameter is limitedDelegation, currently hardcoded to false
            gramjob.submit(endpoint, false, false, submissionID);

            gt42job.submitted();
            
            String handle = gramjob.getHandle();
            try {
                String handleHost = new URI(handle).getHost();
                handle = handle.replace(handleHost, host);
            } catch (URISyntaxException e) {
                // ignored
            }

            gt42job.setSubmissionID(handle);

            // wsgt4 job object listens to the gram job
            gramjob.addListener(gt42job);

            return job;
        } catch(GATInvocationException e) {
            if (gt42job != null) {
                gt42job.finishJob();
            } else {
                sandbox.removeSandboxDir();
            }
            throw e;
        } catch (Exception e) {
            if (gt42job != null) {
                gt42job.finishJob();
            } else {
                sandbox.removeSandboxDir();
            }
            throw new GATInvocationException("GT42ResourceBrokerAdaptor", e);
        }
    }

    // -------------------------------------------------------------------------------

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
        if (brokerURI.getUnresolvedPath() != null && ! "".equals(brokerURI.getUnresolvedPath())) {
            path = brokerURI.getUnresolvedPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        return scheme + "://" + brokerURI.getHost() + ":" + port + path;
    }

    /*
    public List<HardwareResource> findResources(ResourceDescription s) {
        // Create index service EPR

        String indexURI = "https://fs0.das3.cs.vu.nl:8443/wsrf/services/DefaultIndexService";

        // String indexURI =
        // "https://gt4test.lrz-muenchen.de:8443/wsrf/services/DefaultIndexService";
        EndpointReferenceType indexEPR = new EndpointReferenceType();
        try {
            indexEPR.setAddress(new Address(indexURI));
        } catch (Exception e) {
            logger.error("ERROR: Malformed index URI '" + indexURI + "'");
            try {
                throw new RemoteException("ERROR: Malformed index URI '"
                        + indexURI + "'", e);
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        // Get QueryResourceProperties portType
        WSResourcePropertiesServiceAddressingLocator queryLocator;
        queryLocator = new WSResourcePropertiesServiceAddressingLocator();
        QueryResourceProperties_PortType query = null;
        try {
            query = queryLocator.getQueryResourcePropertiesPort(indexEPR);
        } catch (ServiceException e) {
            logger.error("ERROR: Unable to obtain query portType.");
            try {
                throw new RemoteException(
                        "ERROR: Unable to obtain query portType.", e);
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        // Setup security options
        ((Stub) query)._setProperty(Constants.GSI_TRANSPORT,
                Constants.SIGNATURE);
        ((Stub) query)._setProperty(Constants.AUTHORIZATION, NoAuthorization
                .getInstance());

        // The following XPath query retrieves all the files with the specified
        // name
        String xpathQuery = "/*";
        // Create request to QueryResourceProperties
        QueryExpressionType queryExpr = new QueryExpressionType();
        String dialect = WSRFConstants.XPATH_1_DIALECT;
        System.out.println("----> " + dialect);
        try {// Ho aggiunto il to string!!!
            queryExpr.setDialect(new org.apache.axis.types.URI(
                    WSRFConstants.XPATH_1_DIALECT));
            // queryExpr.setDialect(dialect);
        } catch (Exception e) {
            logger
                    .error("ERROR: Malformed URI (WSRFConstants.XPATH_1_DIALECT)");
            try {
                throw new RemoteException(
                        "ERROR: Malformed URI (WSRFConstants.XPATH_1_DIALECT)",
                        e);
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        queryExpr.setValue(xpathQuery);
        QueryResourceProperties_Element queryRequest = new QueryResourceProperties_Element(
                queryExpr);
        System.out.println("5555555555555555555");
        // Invoke QueryResourceProperties
        QueryResourcePropertiesResponse queryResponse = null;
        try {
            queryResponse = query.queryResourceProperties(queryRequest);
            System.out.println("66665555555555");
        } catch (RemoteException e) {
            logger.error("ERROR: Unable to invoke QueryRP operation.");
            try {
                System.out.println("66666pppppp");
                throw new RemoteException(
                        "ERROR: Unable to invoke QueryRP operation.", e);
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        System.out.println("777777777777777777777777");
        System.out.println(queryResponse.toString());
        System.out.println("88888888888888888888888");
        return null;
    }
    */

}
