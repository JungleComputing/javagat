package org.gridlab.gat.scheduler.cpi.gt4;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.client.Stub;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.AttributedURI;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.mds.aggregator.types.AggregatorContent;
import org.globus.mds.aggregator.types.AggregatorData;
import org.globus.mds.glue.ClusterType;
import org.globus.mds.glue.ComputingElementType;
import org.globus.mds.glue.GLUECERPType;
import org.globus.mds.glue.HostType;
import org.globus.mds.glue.InfoType;
import org.globus.mds.glue.PolicyType;
import org.globus.mds.glue.StateType;
import org.globus.mds.glue.SubClusterType;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.security.Constants;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.cpi.SchedulerCpi;
import org.gridlab.gat.resources.cpi.wsgt4new.GlobusEngineConfigurationFactory;
import org.gridlab.gat.scheduler.Queue;
import org.gridlab.gat.scheduler.Scheduler;
import org.oasis.wsrf.properties.QueryExpressionType;
import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
import org.oasis.wsrf.properties.QueryResourceProperties_Element;
import org.oasis.wsrf.properties.QueryResourceProperties_PortType;
import org.oasis.wsrf.properties.WSResourcePropertiesServiceAddressingLocator;
import org.oasis.wsrf.servicegroup.EntryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GAT-Adaptor for retrieving Scheduler Informations from a Globus-MDS4-Service. The Adaptor retrieves the scheduler
 * informations by querying the MDS with a WebService call.
 * 
 * @author Stefan Bozic
 */
public class WSGT4SchedulerAdaptor extends SchedulerCpi implements Scheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(WSGT4SchedulerAdaptor.class);

	// some useful things to be used many times:
	private static String patternStr = "[0-2]{0,1}[0-9]{0,2}.[0-2]{0,1}[0-9]{0,2}.[0-2]{0,1}[0-9]{0,2}.[0-2]{0,1}[0-9]{0,2}";
	private static Pattern patternIPString = Pattern.compile(patternStr);

	// XPATH query to obtain the most important site informations
	private final static String QUERY_GRAM_SERVICES_EXPR = "//*[local-name()=\"Entry\" and"
			+ "  child::*[local-name()=\"MemberServiceEPR\" and"
			+ "     child::*[local-name()=\"Address\" and contains(text(), \"ManagedJobFactoryService\")] and"
			+ "     child::*[local-name()=\"ReferenceProperties\" and"
			+ "        child::*[local-name()=\"ResourceID\"]" + "     ]" + "  ]" + "]";

	// instance initializer sets personalized EngineConfigurationFactory for the axis client
	{
		if (System.getProperty("GLOBUS_LOCATION") == null) {
			String globusLocation = System.getProperty("gat.adaptor.path") + java.io.File.separator + "GlobusAdaptor"
					+ java.io.File.separator;
			System.setProperty("GLOBUS_LOCATION", globusLocation);
		}

		AxisProperties.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,	GlobusEngineConfigurationFactory.class.getName());
	}

	/**
	 * Constructor. Initialize the path to the axis client-config file.
	 * 
	 * @param gatContext
	 *            the {@link GATContext}
	 * @param uri
	 *            the {@link URI} of the MDS.
	 */
	public WSGT4SchedulerAdaptor(final GATContext gatContext, final URI uri) {
		super(gatContext, uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.cpi.SchedulerCpi#getQueues()
	 */
	@Override
	public List<Queue> getQueues() throws GATInvocationException {
		List<Queue> queueList = new ArrayList<Queue>();

		try {
			final String myURL = "https://" + getInformationSystemUri().getHost()
					+ ":8443/wsrf/services/DefaultIndexService";
			final WSResourcePropertiesServiceAddressingLocator locator = new WSResourcePropertiesServiceAddressingLocator();

			final QueryResourceProperties_PortType port;
			final EndpointReferenceType endpoint = new EndpointReferenceType();
			endpoint.setAddress(new AttributedURI(myURL));

			port = locator.getQueryResourcePropertiesPort(endpoint);

			((Stub) port)._setProperty(Constants.GSI_ANONYMOUS, Boolean.TRUE);

			// String xpathQuery = "/*";
			final String xpathQuery = QUERY_GRAM_SERVICES_EXPR;

			final QueryExpressionType query = new QueryExpressionType();

			query.setDialect(WSRFConstants.XPATH_1_DIALECT);
			query.setValue(xpathQuery);

			final QueryResourceProperties_Element request = new QueryResourceProperties_Element();
			request.setQueryExpression(query);

			QueryResourcePropertiesResponse response;
			response = port.queryResourceProperties(request);

			final MessageElement[] entries = response.get_any();
			MessageElement messageElement;

			for (int ii = 0; ii < entries.length; ++ii) {
				messageElement = entries[ii];

				if (messageElement == null) {
					LOGGER.debug("Entry is null!");
					continue;
				}

				final EntryType entry = (EntryType) ObjectDeserializer.toObject(messageElement, EntryType.class);
				queueList.addAll(getJobQueuesFromEntry(entry));

			}
		} catch (Exception e1) {
			throw new GATInvocationException("WSGT4SchedulerAdaptor", e1);
		}

		return queueList;
	}

	/**
	 * Handles the given {@link EntryType} and call methods to gain detailed informations.
	 * 
	 * @param entryType
	 *            the {@link EntryType} to handle
	 * @throws Exception
	 *             an {@link Exception} that might occurs
	 */
	private List<Queue> handleEntryType(final EntryType entryType) throws Exception {
		final List<Queue> queues = new ArrayList<Queue>();
		final EndpointReferenceType managerType = entryType.getServiceGroupEntryEPR();

		if (managerType == null) {
			throw new Exception("Service group entry EPR null for entry type");
		}

		// Get host name from entry
		final AttributedURI managerURI = managerType.getAddress();
		final String hostName = getFQDNHostName(managerURI);

		// Update hardware resource attributes
		try {
			getHardwareInformationsfromEntry(entryType, hostName);
		} catch (Exception e) {
			LOGGER.error("An error occured during retrieving Hardware infromations.", e);
		}

		// Update job queues
		try {
			getJobQueuesFromEntry(entryType);
		} catch (Exception e) {
			LOGGER.error("An error occured during retrieving JobQueues.", e);
		}
		return queues;
	}

	/**
	 * Trace the Hardware-Infos to a given {@link EntryType}.
	 * 
	 * @param entryType
	 *            the {@link EntryType}
	 * @param hostName
	 *            the name of the host
	 */
	private void getHardwareInformationsfromEntry(final EntryType entryType, final String hostName) {
		GLUECERPType gluece = null; // it is not required - if null then we have
		try {
			AggregatorContent content = (AggregatorContent) entryType.getContent();
			AggregatorData data = content.getAggregatorData();

			if (data != null) {
				gluece = (GLUECERPType) ObjectDeserializer.toObject(data.get_any()[0], GLUECERPType.class);
			}
		} catch (Exception ex) {
			LOGGER.trace("An exception occurs during analysing the gluece: ", ex);
		}
		if (gluece != null) {
			ClusterType[] clusters = gluece.getCluster();
			if (clusters != null) {
				for (int i = 0; i < clusters.length; i++) {
					if (clusters[i] == null) {
						// just in case...
						continue;
					}
					SubClusterType[] subClusters = clusters[i].getSubCluster();
					if (subClusters == null) {
						continue;
					}
					for (int ii = 0; ii < subClusters.length; ii++) {
						if (subClusters[ii] == null) {
							// just in case...
							continue;
						}
						HostType[] hosts = subClusters[ii].getHost();
						if (hosts == null) {
							continue;
						}
						for (int h = 0; h < hosts.length; h++) {
							if (hosts[h] == null) {
								// just in case... [we're so careful]
								continue;
							}

							if (hosts[h].getName() != null
									&& hosts[h].getName().trim().toLowerCase().startsWith(hostName.toLowerCase())) {
								HostType hostType = hosts[h];
								LOGGER.debug("MainMemory " + hostType.getMainMemory());
							}

						}
					}
				}
			}
		}

	}

	/**
	 * Retrieves all JobQueue-Informations from a {@link EntryType} and map them to a {@link List} of {@link Queue}.
	 * 
	 * @param entryType
	 *            the given {@link EntryType}
	 * 
	 * @return a {@link List} of {@link Queue}
	 */
	private List<Queue> getJobQueuesFromEntry(final EntryType entryType) {
		LOGGER.debug("new EntryType " + entryType.getTypeDesc().toString());
		List<Queue> queues = new ArrayList<Queue>();

		GLUECERPType gluece = null; // it is not required - if null then we have
		try {
			AggregatorContent content = (AggregatorContent) entryType.getContent();
			AggregatorData data = content.getAggregatorData();

			if ((data != null) && (data.get_any() != null) && (data.get_any().length > 0)) {
				gluece = (GLUECERPType) ObjectDeserializer.toObject(data.get_any()[0], GLUECERPType.class);
			}
		} catch (Exception ex) {
			LOGGER.trace("An exception occurs during analysing the gluece: ", ex);
		}
		if (gluece != null) {
			ComputingElementType[] computingElements = gluece.getComputingElement();
			if (computingElements != null) {
				for (int i = 0; i < computingElements.length; i++) {
					Queue currentQueue = new Queue();

					if (computingElements[i] == null) {
						// just in case...
						continue;
					}
					String name = computingElements[i].getName();
					LOGGER.debug("name" + ":" + name);
					currentQueue.setName(name);

					String uniqueId = computingElements[i].getUniqueID();
					LOGGER.debug("uniqueid" + ":" + uniqueId);
					currentQueue.setUniqueId(uniqueId);

					InfoType info = computingElements[i].getInfo();

					if (info != null) {

						if (info.getLRMSType() != null && !info.getLRMSType().equals("")) {
							LOGGER.debug("LRM: " + info.getLRMSType());
							currentQueue.setLrm(info.getLRMSType());
						}
						if (info.getLRMSVersion() != null && !info.getLRMSVersion().equals("")) {
							LOGGER.debug("LRM_Version: " + info.getLRMSVersion());
							currentQueue.setLrmVersion(info.getLRMSVersion());
						}
						if (info.getGRAMVersion() != null && !info.getGRAMVersion().equals("")) {
							LOGGER.debug("GRAM_Version: " + info.getGRAMVersion());
							currentQueue.setGramVersion(info.getGRAMVersion());
						}
						if (info.getHostName() != null && !info.getHostName().equals("")) {
							LOGGER.debug("HOSTNAME: " + info.getHostName());
						}
						if (info.getGatekeeperPort() != null && !info.getGatekeeperPort().equals("")) {
							LOGGER.debug("GATEKEEPERPORT_ATTRIBUTE: " + info.getGatekeeperPort());
						}
						if (info.getTotalCPUs() != null && !info.getTotalCPUs().equals("")) {
							LOGGER.debug("TOTALCPUS_ATTRIBUTE: " + info.getTotalCPUs());
							currentQueue.setTotalCpus(Long.valueOf(info.getTotalCPUs()));
						}

					}

					StateType state = computingElements[i].getState();
					if (state != null) {

						if (state.getStatus() != null && !state.getStatus().equals("")) {
							LOGGER.debug("STATE_STATUS_ATTRIBUTE: " + state.getStatus());
							currentQueue.setStatus(state.getStatus());
						}
						if (!Integer.toString(state.getTotalJobs()).equals("")) {
							LOGGER.debug("STATE_TOTALJOBS_ATTRIBUTE: " + state.getTotalJobs());
							currentQueue.setTotalJobs(Long.valueOf(state.getTotalJobs()));
						}
						if (!Integer.toString(state.getRunningJobs()).equals("")) {
							LOGGER.debug("STATE_RUNNINGJOBS_ATTRIBUTE: " + state.getRunningJobs());
							currentQueue.setRunningJobs(Long.valueOf(state.getRunningJobs()));
						}
						if (!Integer.toString(state.getWaitingJobs()).equals("")) {
							LOGGER.debug("STATE_WAITINGJOBS_ATTRIBUTE: " + state.getWaitingJobs());
							currentQueue.setWaitingJobs(Long.valueOf(state.getWaitingJobs()));
						}
						if (!Integer.toString(state.getWorstResponseTime()).equals("")) {
							LOGGER.debug("STATE_WORSTRESPONSETIME_ATTRIBUTE: " + state.getWorstResponseTime());
						}
						if (!Integer.toString(state.getEstimatedResponseTime()).equals("")) {
							LOGGER.debug("STATE_ESTIMATEDRESPONSETIME_ATTRIBUTE: " + state.getEstimatedResponseTime());
						}
						if (!Integer.toString(state.getFreeCPUs()).equals("")) {
							LOGGER.debug("NUM_FREE_NODES_ATTRIBUTE: " + state.getFreeCPUs());
							currentQueue.setFreeCpus(Long.valueOf(state.getFreeCPUs()));
						}
					}

					PolicyType policy = computingElements[i].getPolicy();
					if (policy != null) {
						currentQueue.setMaxCPUTime(policy.getMaxCPUTime());
						currentQueue.setMaxWallTime(policy.getMaxWallClockTime());
						currentQueue.setMaxRunningJobs(policy.getMaxRunningJobs());
						currentQueue.setMaxTotalJobs(policy.getMaxTotalJobs());
						currentQueue.setPriority(policy.getPriority());
					}

					queues.add(currentQueue);
				}
			}
		}

		return queues;
	}

	/**
	 * Returns the canonical name of the host for a given {@link AttributedURI}
	 * 
	 * @param uri
	 *            the {@link AttributedURI}
	 * @return name the canonical name of the host
	 */
	private String getFQDNHostName(final AttributedURI uri) {
		if (uri == null) {
			return null;
		}

		String hostname = uri.getHost();
		if (hostname == null || hostname.trim().equals("")) {
			return null;
		}

		Matcher matcher = patternIPString.matcher(hostname);
		boolean isIP = matcher.matches();

		if (isIP) {
			try {
				InetAddress ia = InetAddress.getByName(hostname);
				hostname = ia.getCanonicalHostName();
				return hostname;
			} catch (UnknownHostException ex) {
				LOGGER.debug("UnknownHost: " + hostname, ex);
				return null;
			}

		}
		if (hostname.indexOf('.') == -1) {
			try {
				InetAddress ia = InetAddress.getByName(hostname);
				hostname = ia.getCanonicalHostName();
			} catch (UnknownHostException ex) {
				LOGGER.debug("UnknownHost: " + hostname, ex);
			}
		}

		return hostname;
	}
}
