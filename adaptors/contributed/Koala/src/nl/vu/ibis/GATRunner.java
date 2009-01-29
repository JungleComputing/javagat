package nl.vu.ibis;

import java.util.Arrays;
import java.util.List;
//import java.util.prefs.Preferences;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.CoScheduleJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.security.SecurityContext;
import org.koala.internals.JCompRunnerInfo;
import org.koala.runnersFramework.AbstractRunner;

public class GATRunner extends AbstractRunner {

	private final GATContext parentContext;

	private final KoalaJob parent;
	private final CoScheduleJobDescription parentDescription;
	private final String JDS; 

	// TODO: This is wrong with co-scheduling!
	//private final CoScheduleJobDescription jobDescription;
	
	// TODO: This is wrong with co-scheduling!
	//private Job gatJob;
	
	// TODO: This is wrong with co-scheduling!
	// A metric object used to monitor the status of the job.
    //private Metric metric; 
    
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
		
		job.setJDS(JDS);
	}

/*	protected String getLabel(JobDescription jobDescription) { 
		return "gat-job0";
	}

	private void convertJobDescription(StringBuilder sb, JobDescription job) { 
		
		SoftwareDescription sd = job.getSoftwareDescription();
		
		// Exectuable
		sb.append("&( executable = \"");
		sb.append(sd.getExecutable());
		sb.append("\")\n");

		String [] arguments = sd.getArguments();

		if (arguments != null && arguments.length > 0) { 
			// Arguments
			sb.append(" ( arguments = ");

			for (String a : arguments) { 
				sb.append("\"");
				sb.append(a);
				sb.append("\" ");
			}
			sb.append(")\n");
		}

		// Count 
		sb.append(" ( count = \"");
		sb.append(job.getProcessCount());
		sb.append("\")\n");

		// Directory 
		// SKIP -- this is not required for GAT

		// Time
		sb.append(" ( maxWallTime = \"");
		sb.append(sd.getLongAttribute("time.max", 15));
		sb.append("\")\n");

		// Label
		sb.append(" ( label = \"");
		sb.append(getLabel(job));
		sb.append("\")\n");

		// Stdout 
		File stdout = sd.getStdout();

		if (stdout != null) { 
			sb.append(" ( stdout = \"");
			sb.append(stdout.getAbsolutePath());
			sb.append("\")\n");
		}

		// Stderr 
		File stderr = sd.getStderr();

		if (stderr != null) { 
			sb.append(" ( stderr = \"");
			sb.append(stderr.getAbsolutePath());
			sb.append("\")\n");
		}

		// Stdin ?
		// TODO -- is this supported by koala ? 

		// Stagein

		 Skip for now!!

			Set<File> preStaged = sd.getPreStaged().keySet();

			if (preStaged != null && preStaged.size() > 0) { 
				sb.append(" ( stagein = ");

				for (File in : preStaged) { 
					sb.append("\"");

					URI tmp = in.toGATURI();

					if (tmp.isLocal()) { 
						sb.append(in.getAbsolutePath());
					} else { 
						sb.append(tmp);
					}

					sb.append("\" ");
				}

				sb.append(")\n");
			}

			// Stageout
			Set<File> postStaged = sd.getPostStaged().keySet();

			if (postStaged != null && postStaged.size() > 0) { 
				sb.append(" ( stageout = ");

				for (File out : postStaged) {
					sb.append("\"");
					sb.append(out.toGATURI().toString());
					sb.append("\" ");
				}

				sb.append(")\n");
			}

		 
	}
	
	private String convertJobDescription() { 

		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaJob converting JobDescription to JDL");
		
		 Optional settings: 
			job.setFlexibleJob(true);
			job.setOptimizeComm(true);
			job.setClusterMinimize(true);
			job.setJDS(Utils.readFile(jdf_file));
			job.setExcludeClusters();
			job.setRunnerName("GATRunner");
			job.setCoallocate(true);
		 

		 We should now generate a string that looks something like this:

			   +( 
	 			   &( directory = "/home/jason/koala-test/files" )
	  				( arguments = "inputfile1 outputfile1" )
	  				( executable = "/home/jason/koala-test/files/copy.sh" )
	  				( maxWallTime = "15" )
	  				( label = "subjob 0" )
	  				( count = "1" )
	  				( stagein = "/home/jason/koala-test/files/inputfile1" )
	  				( filesize-stagein = "/home/jason/koala-test/files/inputfile1=10G" )
	  				( stageout = "/home/jason/koala-test/files/outputfile1" )
	  				( filesize-stageout = "/home/jason/koala-test/files/outputfile1=1G" )
	  				( resourceManagerContact = "fs3.das2.ewi.tudelft.nl" )
	  				( bandwidth = "subjob 1:30G"
	                			  "subjob 2:10G")
					( stdout = "/home/wlammers/demo/standard_out" )
	  				( stderr = "/home/wlammers/demo/standard_err" )
					( jobtype = mpi )
				)
				( 
				     next job...
				) 

				Since GAT will be responsible for the actual deployment of the 
				application, it is not necessary to do a 'perfect' translation 
				here. The JDF string should contain enough information to allow 
				the Koala scheduler to do it's job. The JDF may be incomplete
				however. For example, if the user does not require Koala to take 
				the file location into account (this is an option), then there 
				is no point in providing the stagein information to the 
				scheduler.
		 

		StringBuilder sb = new StringBuilder("");

		List<JobDescription> tmp = jobDescription.getJobDescriptions();		

		
		if (tmp.size() > 1) { 
			sb.append("+\n");
			sb.append("(");
		} 
		
		for (JobDescription j : tmp) { 
			convertJobDescription(sb, j);
		}
		
		if (tmp.size() > 1) { 
			sb.append(")");
		}  
			
		String result = sb.toString();
		
		KoalaResourceBrokerAdaptor.logger.info("KoalaJob generated JDS:\n" 
				+ result);

		return result;
	}
*/

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
		/*
		KoalaResourceBrokerAdaptor.logger.info("GATRunner.submitComponent(\n" + 
				component.getComponentNo() + "\n" + 
				component.getCds()+ "\n" + 
				component.getEstimatedRuntime()+ "\n" + 
				component.getExecID()+ "\n" + 
				component.getExecSite()+ "\n" + 
				component.getKreservePort()+ "\n" + 
				component.getSize()+ "\n" + 
				component.getStatus() +"\n" +
				component.getTmpData()+ "\n");
		 */

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
	
	/*

	public void processMetricEvent(MetricEvent event) {
		
		KoalaResourceBrokerAdaptor.logger.info(
				"GATRunner.processMetricEvent got " + event);
		
		// TODO: this is too simplistic ?
		try { 	
			parent.stateChange((Job.JobState) event.getValue(), 
					event.getEventTime());
		} catch (Exception e) {
			KoalaResourceBrokerAdaptor.logger.warn(
					"GATRunner.processMetricEvent failed to process " +
					"metric event " + event, e);
		}
	}*/
}
