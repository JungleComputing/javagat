package org.gridlab.gat.resources.cpi.glite;

import java.rmi.RemoteException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobFlags;
import org.glite.wsdl.types.lb.JobFlagsValue;
import org.glite.wsdl.types.lb.JobStatus;
import org.globus.axis.transport.HTTPSSender;
import org.gridlab.gat.GATInvocationException;

public class LBService {
	private final static int LB_PORT = 9003;
	
	private final java.net.URL lbURL;
	private final LoggingAndBookkeepingPortType loggingAndBookkeepingPortType;
	
	/**
	 * Instantiate the logging and bookkeeping service classes, which are used
	 * for status updates
	 * 
	 * @param jobIDWithLB
	 *            the JobID from which the LB URL will be constructed
	 * @throws GATInvocationException 
	 */
	public LBService(final String jobIDWithLB) throws GATInvocationException {
		// instantiate the logging and bookkeeping service
		try {
			java.net.URL jobUrl = new java.net.URL(jobIDWithLB);
			lbURL = new java.net.URL(jobUrl.getProtocol(), jobUrl.getHost(), LB_PORT, "/");

			// Set provider
			SimpleProvider provider = new SimpleProvider();
			SimpleTargetedChain c = null;
			c = new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("https", c);
			c = new SimpleTargetedChain(new HTTPSender());
			provider.deployTransport("http", c);

			// get LB Stub
			LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocator(provider);

			loggingAndBookkeepingPortType = loc.getLoggingAndBookkeeping(lbURL);
		} catch (Exception e) {
			throw new GATInvocationException("Problem instantiating Logging and Bookkeeping service ", e);
		}
	}

	public LoggingAndBookkeepingPortType getLoggingAndBookkeepingPortType() {
		return loggingAndBookkeepingPortType;
	}
	
	public JobStatus queryJobState(JobIdStructType jobIdStructType) throws GenericFault, RemoteException{
	    /* JobFlags: flags Specify details of the query. Determines which status
	     * fields are actually retrieved.
	     * Possible values:
	     * CLASSADS - Include the job description in the query result.</li>
	     * CHILDREN - Include the list of subjob id's in the query result.</li>
	     * CHILDSTAT - Apply the flags recursively to subjobs.</li>
	     * CHILDHIST_FAST - Include partially complete histogram of child job states.</li>
	     * CHILDHIST_THOROUGH - Include full and up-to date histogram of child job states.</li>
	     */
		JobFlagsValue[] jobFlagsValues = new JobFlagsValue[]{JobFlagsValue.CLASSADS, JobFlagsValue.CHILDREN, JobFlagsValue.CHILDSTAT}; 
		JobFlags jobFlags = new JobFlags();
		jobFlags.setFlag(jobFlagsValues);
		return loggingAndBookkeepingPortType.jobStatus(jobIdStructType.getId(), jobFlags);
	}	

}
