package nl.vu.ibis;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.CoScheduleJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.SecurityContext;
import org.koala.internals.JCompRunnerInfo;
import org.koala.internals.KLogger;
import org.koala.runnersFramework.AbstractRunner;

public class GATRunner extends AbstractRunner {

	private final GATContext parentContext;

	private final KoalaJob parent;
	private final CoScheduleJobDescription parentDescription;
	private final String JDS; 

	private final Components components;
    
	public GATRunner(KoalaJob parent, CoScheduleJobDescription jobDescription, 
			Components components, GATContext context) {

		super();		
		
		this.parent = parent;
		this.parentDescription = jobDescription;
		this.components = components;
		
		parentContext = context;

		JDS = components.getJDLDescription();

		KoalaResourceBrokerAdaptor.logger.info("GATRunner starting with " 
				+ components.numberOfComponents() + " components.");
		
		KoalaResourceBrokerAdaptor.logger.info("GATRunner using JDS: " + JDS);
		
		KLogger.setLogging(Level.WARN);
		
		job.setJDS(JDS);
	}

	@Override
	public void parseParameters(String [] p) {
		KoalaResourceBrokerAdaptor.logger.info(
				"GATRunner.parseParameters(" + Arrays.toString(p) + ")");
	}

	@Override
	public void postPhase(boolean jobRunWasSuccessful) {
		KoalaResourceBrokerAdaptor.logger.info("GATRunner.postPhase(" +
				"" + jobRunWasSuccessful + ")");
	}

	@Override
	public boolean prePhase() {
		KoalaResourceBrokerAdaptor.logger.info("GATRunner.prePhase()");
		return true;
	}

	@Override
	public void precondSubmit(JCompRunnerInfo component) {
		KoalaResourceBrokerAdaptor.logger.info("GATRunner.precondSubmit(...)");	
	}

	@Override
	public void stopComponents() {
		KoalaResourceBrokerAdaptor.logger.info("GATRunner.stopComponents()");	
	}

	@Override
	public int submitComponent(JCompRunnerInfo component) {
		
		try { 

			KoalaResourceBrokerAdaptor.logger.info("GATRunner.submitComponent " 
					+ component.getComponentNo() + " to site " 
					+ component.getExecSite()); 

			Component c = components.getComponent(component.getComponentNo());
			c.setSite(component.getExecSite());
			
			// Create a copy of context used to select the next broker.
			GATContext context = new GATContext();

			Preferences tmp = parentContext.getPreferences();

			// Remove the explicit adaptor name. This is either not set, or set 
			// to the Koala adaptor. Either way, it won't be set after this 
			// call.
			
			// tmp.remove("ResourceBroker.adaptor.name");

			// Set the target for the submission. 
			// TODO: don't like that this explicity selects globus!!!
			tmp.put("ResourceBroker.adaptor.name", "globus");
			//      tmp.put("ResourceBroker.jobmanagerContact", site);

			// Add magic preference to prevent recursive calls to Koala broker.
			tmp.put("postKoala", true);

			context.addPreferences(tmp);

			for (SecurityContext sc : parentContext.getSecurityContexts()) { 
				context.addSecurityContext(sc);
			}

			URI brokerURI = new URI("any://" + c.getSite());

			KoalaResourceBrokerAdaptor.logger.info(
					"GATRunner.submitComponent creating nested broker!");
			
			// Create a new broker that allows us to perform a real submission.
			ResourceBroker broker = GAT.createResourceBroker(context, brokerURI);

			KoalaResourceBrokerAdaptor.logger.info(
					"GATRunner.submitComponent got " + broker);
			
			// Submit the job 
			KoalaResourceBrokerAdaptor.logger.info(
					"GATRunner.submitComponent submitting nested job!");
			
			Job job = broker.submitJob(c.getDescription());
			c.setJob(job);
			
			KoalaResourceBrokerAdaptor.logger.info(
					"GATRunner.submitComponent nested job started!");
			
		} catch (Exception e) { 
			KoalaResourceBrokerAdaptor.logger.info(
					"GATRunner.submitComponent failed ", e);
			return 1;
		}

		return 0;
	}
}
