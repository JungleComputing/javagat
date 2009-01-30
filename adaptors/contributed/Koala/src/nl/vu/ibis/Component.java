package nl.vu.ibis;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;

public class Component implements MetricListener {

	// The compontents structure this component belongs to  
    private final Components owner;

    // A description of the job. 
    private final JobDescription description;
    
    // The GAT context (used when executing jobs).
    private final GATContext context;
    
    // The job number (assigned by the Koala scheduler). 
    private final int jobNumber;
    
    // The component number (identifying a 'part' of the job). 
    private final int componentNumber;
    
    // The identifier koala uses for this component.
    private final String identifier;
    
    // The execution site as provided by koala.
    private String site;
    
    // The job rsl as provided by koala. 
    // private String rsl;

    // The delegated job. 
    private Job job;
    
    // Is the job started ?
    private boolean start = false;
    
    // A metric object used to monitor the status of the job.
    private Metric metric; 
    
    // The current state of the job.
    private Job.JobState state = JobState.UNKNOWN;
    
    // The job description as we provide it to Koala 
    private String JDL;
    
    public Component(Components owner, GATContext context, 
            JobDescription description, int jobNumber, int componentNumber) {
        
        this.owner = owner;
        this.context = context;
        this.description = description;
        this.jobNumber = jobNumber;
        this.componentNumber = componentNumber;
        this.identifier = jobNumber + "&" + componentNumber;
        this.JDL = createJobDescription();
        
        // TODO: IS THIS STILL CORRECT????
        //this.preferences = new Preferences(context.getPreferences());
        //this.preferences.put("postKoala", true);
    }

    private String createJobDescription() { 
		
    	StringBuilder sb = new StringBuilder("");
    	
		SoftwareDescription sd = description.getSoftwareDescription();
		
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
		sb.append(description.getProcessCount());
		sb.append("\")\n");

		// Directory 
		// SKIP -- this is not required for GAT

		// Time
		sb.append(" ( maxWallTime = \"");
		sb.append(sd.getLongAttribute("time.max", 15));
		sb.append("\")\n");

		// Label
		sb.append(" ( label = \"");
		sb.append("GAT_KOALA_JOB_" + identifier);
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

		/* Skip for now!!

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

		 */
		
		return sb.toString();
	}
    
    /**
     * @return the componentNumber
     */
    public synchronized int getComponentNumber() {
        return componentNumber;
    }

    /**
     * @return the identifier
     */
    public synchronized String getIdentifier() {
        return identifier;
    }

    /**
     * @return the job
     */
    public synchronized Job getJob() {
        return job;
    }

    /**
     * @param job the job to set
     * @throws GATInvocationException 
     */
    public synchronized void setJob(Job job) throws GATInvocationException {
        this.job = job;
    
        MetricDefinition md = job.getMetricDefinitionByName("job.status");
    	metric = md.createMetric(null);
    	job.addMetricListener(this, metric);
    	
    	// Fire the state change manually (since there is a race condition 
		// in GAT. 
		owner.stateChange(this, Job.JobState.INITIAL, System.currentTimeMillis());
    }
    
    
    /**
     * @return the jobNumber
     */
    public synchronized int getJobNumber() {
        return jobNumber;
    }

    /**
     * @return the site
     */
    public synchronized String getSite() {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(String site) {
        
        synchronized (this) { 
            if (this.site != null) { 
            	KoalaResourceBrokerAdaptor.logger.warn(
            			"EEK: multiple Component.setSite calls!");
                return;
            }
        
            this.site = site;
        }
    }
    
    /**
     * @return the description
     */
    public JobDescription getDescription() {
        return description;
    }

    public String getJDLDescription() {
        return JDL;
    }
    
    public Preferences getPreferences() {
        return context.getPreferences();
    }

    public void stop() {
    	KoalaResourceBrokerAdaptor.logger.info("Stopping component: " 
    			+ identifier);      
        
        boolean mustStop = false;
        
        synchronized (this) {
            mustStop = (start && job != null);
            start = false;
        }
        
        if (mustStop) {
            try {
                job.stop();
            } catch (GATInvocationException e) {
            	KoalaResourceBrokerAdaptor.logger.warn(
            			"Failed to stop component: " + identifier);      
            }
        }
    }

    public synchronized Job.JobState getState() {
        
    	if (job == null) { 
            return JobState.UNKNOWN;
        }
        
        return state;
    }
    
    private void stateChange(JobState state, long time) {
  
    	KoalaResourceBrokerAdaptor.logger.info("Component " + identifier 
    			+ " changed state to " + state);
        
        synchronized (this) {
            this.state = state;            
        }
        
        owner.stateChange(this, this.state, time);
    }
    
    public void processMetricEvent(MetricEvent event) {
		
		KoalaResourceBrokerAdaptor.logger.info(
				"Component.processMetricEvent got " + event);
		
		// TODO: this is too simplistic ?
		try { 	
			stateChange((Job.JobState) event.getValue(), 
					event.getEventTime());
		} catch (Exception e) {
			KoalaResourceBrokerAdaptor.logger.warn(
					"Component.processMetricEvent failed to process " +
					"metric event " + event, e);
		}
	}
        
	public void setMetric(Metric metric) {
		this.metric = metric;		
	}
}
