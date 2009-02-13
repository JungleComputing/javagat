package nl.vu.ibis;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.CoScheduleJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.CoScheduleJobCpi;
import org.koala.runnersFramework.RunnerListener;

public class KoalaJob extends CoScheduleJobCpi {

	// Generated to keep eclipse happy...
	private static final long serialVersionUID = 4321647692737044640L;

	//  The logger used for debugging output and warnings.
	// private static Logger logger = Logger.getLogger("KoalaAdaptor.Runner");

	private GATRunner runner;    
	private RunnerListener listner;	
	private Thread thread;

	// The metric definition of the job status metric.   
	private final MetricDefinition statusMetricDefinition;

	// The actual status metric. 
	private final Metric statusMetric;

	// Indicates if the job if done.  
	private boolean done = false;

	// A description of the job. Note that we always use a 
	// CoScheduleJobDescription here, even if we only have a single job. This 
	// simplifies the GAT administration. 
	private CoScheduleJobDescription jobDescription;

	// A map containing the components of the job. 
	private final Components components;

	private static int jobID = 0;
	
	private final int jobNumber;
	
	protected KoalaJob(GATContext context, 
			CoScheduleJobDescription jobDescription) throws GATInvocationException {

		super(context, jobDescription);

		try { 
			//System.err.println("@@@ Creating Koala job!");
			
			jobNumber = getNewJobNumber();
	
			KoalaResourceBrokerAdaptor.logger.info("Creating KoalaJob " + jobNumber);
			
			// Save the GAT context and job description.
			this.components = new Components(this, context);
			this.jobDescription = jobDescription;

			splitJobDescription();
			
			// Create a Koala Runner and listener thread
			runner = new GATRunner(this, jobDescription, components, context);
			listner = new RunnerListener(runner);

			// Tell the GAT engine that we provide job.status events
			HashMap<String, Object> definition = new HashMap<String, Object>();
			definition.put("status", String.class);

			statusMetricDefinition = new MetricDefinition("job.status",
					MetricDefinition.DISCRETE, "String", null, null, definition);
			statusMetric = statusMetricDefinition.createMetric(null);
			registerMetric("getJobStatus", statusMetricDefinition);
		} catch (Exception e) { 
		//	e.printStackTrace(System.err);
			throw new GATInvocationException("Failed to create KoalaJob", e);
		}
	}

	protected static synchronized int getNewJobNumber() { 
		return jobID++;
	}
	
	private void splitJobDescription() { 

		KoalaResourceBrokerAdaptor.logger.info(
				"KoalaJob splitting JobDescription into components");
		
		/* Optional settings: 
			job.setFlexibleJob(true);
			job.setOptimizeComm(true);
			job.setClusterMinimize(true);
			job.setJDS(Utils.readFile(jdf_file));
			job.setExcludeClusters();
			job.setRunnerName("GATRunner");
			job.setCoallocate(true);
		 */

		/* We should now generate a string that looks something like this:

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
		 */

		List<JobDescription> tmp = jobDescription.getJobDescriptions();		

		for (JobDescription j : tmp) { 
			components.createComponent(j, jobNumber);
		}
		
		KoalaResourceBrokerAdaptor.logger.info("KoalaJob generated JDS:\n" 
				+ components.getJDLDescription());
	}

	public synchronized JobState getState() {        
		return state;
	}

	public Map<String, Object> getInfo() {
		return components.getInfo(null);       
	}

	public void submitToScheduler() throws IOException {
		// Start the listener 
		thread = new Thread(listner, "KoalaJob<Unknown>");
		thread.start();
	}

	private synchronized void setState(JobState state) {

		if (done) { 
			return;
		}

		this.state = state;

		if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) { 
			done = true;
			components.stop();
			listner.done();
		}        
	}

	@Override
	public void stop() throws GATInvocationException {
		KoalaResourceBrokerAdaptor.logger.info("Stopping KoalaJob " + jobNumber);
		setState(JobState.STOPPED);
	}
	
	public void stateChange(JobState state, long time) {
		setState(state);

		fireMetric(new MetricEvent(this, 
				state.toString(), statusMetric, time));
	}

	public Job getJob(JobDescription arg0) {
		// TODO Auto-generated method stub
		return null;
	}        
}
