package org.gridlab.gat.resources.cpi.wsgt4new;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.PrefixedQName;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.addressing.ReferencePropertiesType;
import org.apache.axis.types.URI.MalformedURIException;
import org.oasis.wsrf.properties.QueryExpressionType;
import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
import org.oasis.wsrf.properties.QueryResourceProperties_Element;
import org.oasis.wsrf.properties.QueryResourceProperties_PortType;
import org.oasis.wsrf.properties.WSResourcePropertiesServiceAddressingLocator;
import org.oasis.wsrf.servicegroup.EntryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.globus.common.ResourceManagerContact;
import org.globus.exec.client.GramJob;
import org.globus.exec.utils.ManagedJobConstants;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.mds.aggregator.types.AggregatorContent;
import org.globus.mds.aggregator.types.AggregatorData;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;
import org.gridlab.gat.resources.cpi.wsgt4new.WSGT4newJob;
import org.gridlab.gat.resources.utils.XMLUtil;
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

	
	private String indexPath="/wsrf/services/DefaultIndexService";
	private XMLUtil xmlUtil= new XMLUtil();
	private LinkedList<HardwareResource> resourcesList;
	
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

    protected static Logger logger = LoggerFactory
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

        if (sandbox != null) {
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
    
 public List<HardwareResource> findResources(ResourceDescription s){
	
	/*Prima di fare la query posso verificare quali sono gli attributi richiesti 
	 Se sono tutti del tipo che non variano come ad esempio la velocita' della
	 CPU, allora posso evitare di fare la query se gia ne ho fatta una in precedenza.
	 Devo controllare se il valore del document e' diverso da null
	 */
	
	 boolean allStatic=areAllStaticAttributes(s);
	 resourcesList=new LinkedList<HardwareResource>();

	 if(xmlUtil.getDocument()==null || !allStatic) {
		
		System.out.println("\n I'm querying the Index Service \n"); 
		
	     String indexURI = this.brokerURI.toString()+this.indexPath;
		 System.out.println(indexURI);
		 
	    EndpointReferenceType indexEPR = new EndpointReferenceType();
		try {
			indexEPR.setAddress(new Address(indexURI));
		} catch (Exception e) {			
				e.printStackTrace();
			}
		System.setProperty("javax.net.ssl.trustStore","/home/gni200/cacerts"); 
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");
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

	
	    String xpathQuery = "/*/*/*/*/*/*/*[local-name()='SubCluster']";
		//String xpathQuery = "/*/*/*/*/*[local-name()='GLUECE'][1]/*[local-name()='Cluster'][1]";
		// Create request to QueryResourceProperties
		QueryExpressionType queryExpr = new QueryExpressionType();
		try {
			queryExpr.setDialect(new org.apache.axis.types.URI(WSRFConstants.XPATH_1_DIALECT));
			//queryExpr.setDialect(dialect);
		} catch (Exception e) {			
				e.printStackTrace();
			}
		
		queryExpr.setValue(xpathQuery);
		QueryResourceProperties_Element queryRequest = new QueryResourceProperties_Element(
				queryExpr);
		// Invoke QueryResourceProperties
		QueryResourcePropertiesResponse queryResponse = null;
		try {
			queryResponse = query.queryResourceProperties(queryRequest);
			} catch (RemoteException e) {
							e.printStackTrace();
			}
		
		// The response includes 0 or more entries from the index service.
		MessageElement[] entries = queryResponse.get_any();
		
		if (entries == null || entries.length == 0) {
			System.out.println("Lunghezza 0 e mo so cazzi");
		} else {
			for (int i = 0; i <1; i++) {
				try {
					Element root=entries[i].getAsDOM();
					NodeList hosts=root.getChildNodes();
							
					xmlUtil.createXMLDocument(hosts);
					// qui con un istance of posso capire se creare una lista di soft o hard resources
					Map description=s.getDescription();	
					resourcesList=xmlUtil.matchResources(description);
					System.out.println("size: "+resourcesList.size());
			
					//String a=entries[i].getAttribute("ns1:UniqueID");
							

				} catch (Exception e) {
					e.printStackTrace();
				}}
			}
		
		
	}
	 else{	 
		     System.out.println("\n I dont need to query the Index Service \n");
		     Map description=s.getDescription();	
			 resourcesList=xmlUtil.matchResources(description);
	 }
	 
		 
		return resourcesList;
}

private boolean areAllStaticAttributes(ResourceDescription s) {
	 int numberofResources=s.getDescription().size();
	 int count=0;
	 //ricordarsi di aggiungere le altre
	 if(s.getResourceAttribute("CPU_SPEED")!=null)count++;
	 if(s.getResourceAttribute("CPU_COUNT")!=null)count++;
	 if(s.getResourceAttribute("MEMORY_SIZE")!=null)count++;
	 if(s.getResourceAttribute("DISK_SIZE")!=null)count++;
	 if (count==numberofResources) return true;
	 return false;
	}


}