package nl.vu.ibis;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
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
    private String rsl;

    // The delegated job.
    private Job job;

    // Is the job started ?
    private boolean start = false;

    // A metric object used to monitor the status of the job.
    private Metric metric;

    // The current state of the job.
    private JobState state;

    private Preferences preferences;

    public Component(Components owner, GATContext context,
            JobDescription description, Preferences preferences, int jobNumber,
            int componentNumber) {

        this.owner = owner;
        this.context = context;
        this.description = description;
        this.jobNumber = jobNumber;
        this.componentNumber = componentNumber;
        this.identifier = jobNumber + "&" + componentNumber;

        this.preferences = new Preferences(preferences);
        this.preferences.put("postKoala", true);
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
     * @param job
     *                the job to set
     */
    public synchronized void setJob(Job job) {
        this.job = job;
    }

    /**
     * @return the jobNumber
     */
    public synchronized int getJobNumber() {
        return jobNumber;
    }

    /**
     * @return the rsl
     */
    public synchronized String getRsl() {
        return rsl;
    }

    /**
     * @param rsl
     *                the rsl to set
     */
    public void setRsl(String rsl) {

        synchronized (this) {
            if (this.rsl != null) {
                System.err.println("EEK: multiple rsl calls to component!");
                return;
            }

            this.rsl = rsl;
        }

        submitComponent();
    }

    /**
     * @return the site
     */
    public synchronized String getSite() {
        return site;
    }

    /**
     * @param site
     *                the site to set
     */
    public void setSite(String site) {

        synchronized (this) {
            if (this.site != null) {
                System.err.println("EEK: multiple site calls to component!");
                return;
            }

            this.site = site;
        }

        submitComponent();
    }

    private void submitComponent() {

        synchronized (this) {

            if (start) {
                System.err.println("EEK: multiple startComponent calls!");
                return;
            }

            if (site == null || rsl == null) {
                System.err.println("Component " + identifier + " not ready");
                return;
            }

            start = true;
        }

        try {
            System.err.println("Start of component: " + identifier);

            // Create a copy of preferences used to select the next broker.
            Preferences tmp = new Preferences(preferences);

            // Remove the explicit adaptor name. This is either not set, or set
            // to the Koala adaptor. Either way, it won't be set after this
            // call.
            // tmp.remove("ResourceBroker.adaptor.name");

            // Set the target for the submission.
            String brokers = (String) preferences
                    .get("resourcebroker.adaptor.name");
            if (brokers == null) {
                brokers = "";
            }
            // don't use koala the next time
            tmp.put("resourcebroker.adaptor.name", "!koala," + brokers);

            // Add magic preference to prevent recursive calls to Koala broker.
            tmp.put("postKoala", true);

            // Create a new broker that allows us to perform the real
            // submission.
            ResourceBroker broker = GAT.createResourceBroker(context, tmp,
                    new URI(site));

            // Submit the job
            job = broker.submitJob(description);

            // Register a state listner
            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            metric = md.createMetric(null);
            job.addMetricListener(this, metric);

            // Fire the state change manually (since there is a race condition
            // in GAT.
            stateChange("INITIAL");

        } catch (Exception e) {
            System.err.println("Failed to start component: " + identifier);
        }
    }

    /**
     * @return the description
     */
    public JobDescription getDescription() {
        return description;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void stop() {
        System.err.println("Stopping component: " + identifier);

        boolean mustStop = false;

        synchronized (this) {
            mustStop = (start && job != null);
            start = false;
        }

        if (mustStop) {
            try {
                job.stop();
            } catch (GATInvocationException e) {
                System.err.println("Failed to stop component: " + identifier);
            }
        }
    }

    public synchronized JobState getState() {
        return state;

        /*
         * if (job == null) { return Job.UNKNOWN; }
         * 
         * return job.getState();
         */
    }

    private JobState parseState(String state) {

        if (state.equals("INITIAL")) {
            return Job.JobState.INITIAL;
        } else if (state.equals("SCHEDULED")) {
            return Job.JobState.SCHEDULED;
        } else if (state.equals("RUNNING")) {
            return Job.JobState.RUNNING;
        } else if (state.equals("STOPPED")) {
            return Job.JobState.STOPPED;
        } else if (state.equals("SUBMISSION_ERROR")) {
            return Job.JobState.SUBMISSION_ERROR;
        } else if (state.equals("ON_HOLD")) {
            return Job.JobState.ON_HOLD;
        } else if (state.equals("PRE_STAGING")) {
            return Job.JobState.PRE_STAGING;
        } else if (state.equals("POST_STAGING")) {
            return Job.JobState.POST_STAGING;
        } else if (state.equals("UNKNOWN")) {
            return Job.JobState.UNKNOWN;
        } else {
            return Job.JobState.UNKNOWN;
        }
    }

    private void stateChange(String state) {

        System.out.println("Component " + identifier + " changed state to "
                + state);

        JobState newState = parseState(state);

        synchronized (this) {
            this.state = newState;
        }

        owner.stateChange(this, this.state);
    }

    public void processMetricEvent(MetricEvent value) {
        stateChange((String) value.getValue());
    }
}
