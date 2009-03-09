package nl.vu.ibis;

import java.util.ArrayList;
import java.util.HashMap;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Job.JobState;

public class Components {

	// The time that we wait for a job to appear. Default is 30 seconds.
	// private static final int DEFAULT_TIMEOUT = 30000;

	// The GAT context (used when executing jobs).
	private final GATContext context;

	// The Koala Job that owns this datastructure (used for callbacks).
	private final KoalaJob owner;

	// A map containing the components of the job.
	private final ArrayList<Component> components = new ArrayList<Component>();

	// The state of the entire job.
	private Job.JobState state = Job.JobState.INITIAL;

	public Components(KoalaJob owner, GATContext context) {
		this.context = context;
		this.owner = owner;
	}

	public synchronized int numberOfComponents() {
		return components.size();
	}

	protected synchronized String getJDLDescription() {

		StringBuilder sb = new StringBuilder("");

		boolean coschedule = components.size() > 1;
		
		if (coschedule) {
			sb.append("+\n");
		}

		for (Component c : components) {
			if (coschedule) {
				sb.append("(\n");
			}
			
			sb.append(c.getJDLDescription());

			if (coschedule) {
				sb.append(")");
			}
		}
		
		return sb.toString();
	}

	public synchronized void createComponent(JobDescription description,
			int jobNumber) {

		Component tmp = new Component(this, context, description, jobNumber,
				components.size() + 1); // KOALA starts at 1 ?

		components.add(tmp);

		notifyAll();
	}

	public synchronized Component getComponent(int number) {

		if (number < 1 && number > components.size()) {
			return null;
		}

		return components.get(number - 1);
	}

	/*
	 * public synchronized boolean setSite(String identifier, String site) {
	 * 
	 * Component jc = getComponent(identifier);
	 * 
	 * if (jc == null) { return false; }
	 * 
	 * jc.setSite(site);
	 * 
	 * return true; }
	 */

	public synchronized boolean cloneComponent(int number, int clones) {

		System.err.println("EEK: clone component is fishy!!");

		// We now clone a component by removing the original one and inserting
		// one or more replacement jobs.
		Component jc = getComponent(number);

		if (jc == null) {
			// FIXME: print error
			return false;
		}

		components.remove(number);

		// TODO/FIXME: This doesn't seem right to me... how can you be sure
		// that the component id's are unique ? This only works if you only
		// split a single job once. Splitting again will result in trouble..
		// This is the way the koala runner does it though -- must ask Hashim!!
		for (int i = 1; i <= clones; i++) {
			Component tmp = new Component(this, context, jc.getDescription(),
					jc.getJobNumber(), i);

			components.add(tmp);
		}

		return true;
	}

	/*
	 * public boolean setRSL(int numeber String rsl) {
	 * 
	 * Component jc = getComponent(identifier);
	 * 
	 * if (jc == null) { // FIXME: print error return false; }
	 * 
	 * jc.setRsl(rsl);
	 * 
	 * return true; }
	 */

	public void reset() {
		// TODO: implement
	}

	public void stop() {
		for (Component c : components) {
			c.stop();
		}
	}

	// Merge the state of two subjobs. Basic rules are as follows (sorted in
	// order of importance).
	// 
	// - SUBMISSION_ERROR when at least one sub state is SUBMISSION_ERROR
	// - PRE_STAGING when at least one sub state is PRE_STAGING
	// - SCHEDULED when at least one sub state is SCHEDULED
	// - POST_STAGING when at least one sub state is POST_STAGING
	// - HOLD when at least one sub state is HOLD
	// - INITIAL when at least one sub state is INITIAL
	// - UNKNOWN when at least one sub state is UNKNOWN
	// - STOPPED when at least one sub state is STOPPED
	// - RUNNING when all sub states is RUNNING

	private JobState mergeStates(JobState state1, JobState state2) { 

        if (state1 == JobState.SUBMISSION_ERROR || state2 == JobState.SUBMISSION_ERROR) { 
            return JobState.SUBMISSION_ERROR;
        }
        
        if (state1 == JobState.PRE_STAGING || state2 == JobState.PRE_STAGING) { 
            return JobState.PRE_STAGING;
        }
        
        if (state1 == JobState.SCHEDULED || state2 == JobState.SCHEDULED) { 
            return JobState.SCHEDULED;
        }
        
        if (state1 == JobState.POST_STAGING || state2 == JobState.POST_STAGING) { 
            return JobState.POST_STAGING;
        }
        
        if (state1 == JobState.ON_HOLD || state2 == JobState.ON_HOLD) { 
            return JobState.ON_HOLD;
        }

        if (state1 == JobState.INITIAL || state2 == JobState.INITIAL) { 
            return JobState.INITIAL;
        }
        
        if (state1 == JobState.UNKNOWN || state2 == JobState.UNKNOWN) { 
            return JobState.UNKNOWN;
        }

        if (state1 == JobState.STOPPED || state2 == JobState.STOPPED) { 
            return JobState.STOPPED;
        }

        if (state1 == JobState.RUNNING && state2 == JobState.RUNNING) { 
            return JobState.RUNNING;
        }
        
        return JobState.UNKNOWN;
    }

	private Job.JobState mergeStates() {

		if (components.size() == 0) {
			return JobState.UNKNOWN;
		}

		Job.JobState state = JobState.UNKNOWN;
		boolean initial = true;
		
		for (Component c : components) {
			
			if (initial) { 
				state = c.getState();
				initial = false;
			} else { 
				state = mergeStates(state, c.getState());
			}
		}

		return state;
	}

	public synchronized HashMap<String, Object> getInfo(
			HashMap<String, Object> map) {

		// String jobSite = null;

		if (map == null) {
			map = new HashMap<String, Object>();
		}
		/*
		 * if (components.size() == 0) { map.put("state",
		 * Job.getStateString(Job.INITIAL)); return map; }
		 * 
		 * map.put("state", Job.getStateString(mergeStates()));
		 * 
		 * StringBuilder states = new StringBuilder(); StringBuilder sites = new
		 * StringBuilder();
		 * 
		 * Iterator<Component> itt = components.values().iterator();
		 * 
		 * while (itt.hasNext()) {
		 * 
		 * Component c = itt.next();
		 * 
		 * states.append("["); states.append(c.getIdentifier());
		 * states.append(", "); states.append(Job.getStateString(c.getState()));
		 * states.append("]");
		 * 
		 * String site = c.getSite();
		 * 
		 * if (site == null) { site = "unknown"; } else { if (jobSite == null) {
		 * jobSite = site; } else if (!jobSite.equals(site)) { jobSite =
		 * "multiple"; } }
		 * 
		 * sites.append("["); sites.append(c.getIdentifier()); sites.append(",
		 * "); sites.append(site); sites.append("]");
		 * 
		 * if (itt.hasNext()) { states.append(", "); sites.append(", "); } }
		 * 
		 * map.put("components.states", states.toString());
		 * map.put("components.sites", sites); map.put("site", jobSite);
		 */
		return map;
	}

	public void stateChange(Component component, Job.JobState state, long time) {

		Job.JobState newState = Job.JobState.UNKNOWN;

		// NOTE: the merging of states is synchronized to prevent race
		// conditions. This also implies that we have to copy the resulting
		// state to a local variable when it has changed. If we wouldn't do this
		// it may have changed already when we forward the stateChange to out
		// parent. We can't perform the stateChange inside the sync. block since
		// that may result in deadlocks.
		synchronized (this) {
			Job.JobState tmp = this.state;
			this.state = mergeStates();

			if (this.state != tmp) {
				newState = this.state;
			}
		}

		if (newState != Job.JobState.UNKNOWN) {
			KoalaResourceBrokerAdaptor.logger.info("State change of KoalaJob " +
					"to " + newState.toString());
			owner.stateChange(newState, time);
		} else {
			KoalaResourceBrokerAdaptor.logger.info("State change not " +
					"propagated to koalajob: " + state + " " + this.state);
		}
	}
}
