package nl.vu.ibis;

import java.util.HashMap;
import java.util.Iterator;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

public class Components {
    
    // The time that we wait for a job to appear. Default is 30 seconds.
    private static final int DEFAULT_TIMEOUT = 30000;
    
    // The GAT context (used when executing jobs).
    private final GATContext context;
    
    // The Koala Job that owns this datastructure (used for callbacks).
    private final KoalaJob owner;
        
    // A map containing the components of the job. 
    private final HashMap<String, Component> components = 
        new HashMap<String, Component>();

    // The state of the entire job. 
    private int state = Job.INITIAL;
    
    public Components(KoalaJob owner, GATContext context) {        
        this.context = context;
        this.owner = owner;
    }
    
    public synchronized void createComponent(JobDescription description, 
            Preferences preferences, int jobNumber) {
        
        Component tmp = new Component(this, context, description, preferences, 
                jobNumber, 1);
        
        // Sanity check.
        if (components.containsKey(tmp.getIdentifier())) { 
            System.err.println("EEK: overwriting existing job component " 
                    + tmp.getIdentifier() + " !!!");
        }
        
        components.put(tmp.getIdentifier(), tmp);
        
        notifyAll();
    }
    
    public Component getComponent(String identifier) {
        return getComponent(identifier, DEFAULT_TIMEOUT);
    }
    
    public synchronized Component getComponent(String identifier, long timeout) { 
        
        long endTime = System.currentTimeMillis() + timeout;
        
        while (!components.containsKey(identifier)) {
            
            long timeLeft = 0;
            
            if (timeout > 0) { 
                timeLeft = endTime - System.currentTimeMillis();
                
                if (timeLeft <= 0) { 
                    return null;
                }
            } 
            
            try { 
                wait(timeLeft);
            } catch (InterruptedException e) {
                // ignore
            }         
        }
        
        return components.get(identifier);
    }
    
    public synchronized boolean setSite(String identifier, String site) { 
        
        Component jc = getComponent(identifier);
        
        if (jc == null) { 
            return false;
        }
        
        jc.setSite(site);
        
        return true;
    }

    public synchronized boolean cloneComponent(String identifier, int clones) {
        
        System.err.println("EEK: clone component is fishy!!");
        
        // We now clone a component by removing the original one and inserting
        // one or more replacement jobs.
        Component jc = getComponent(identifier);
        
        if (jc == null) { 
            // FIXME: print error
            return false;
        }
       
        components.remove(identifier);
        
        // TODO/FIXME: This doesn't seem right to me... how can you be sure 
        // that the component id's are unique ? This only works if you only 
        // split a single job once. Splitting again will result in trouble..
        // This is the way the koala runner do it though -- must ask Hashim!!
        for (int i=1;i<=clones;i++) { 
            Component tmp = new Component(this, context, jc.getDescription(), 
                    jc.getPreferences(), jc.getJobNumber(), i);
            
            // Sanity check.
            if (components.containsKey(tmp.getIdentifier())) { 
                System.err.println("EEK: replacing existing job component " 
                        + tmp.getIdentifier() + " !!!");
            }
            
            components.put(tmp.getIdentifier(), tmp);
        }
        
        return true;
    }


    public boolean setRSL(String identifier, String rsl) {
        
        Component jc = getComponent(identifier);
        
        if (jc == null) { 
            // FIXME: print error
            return false;
        }
        
        jc.setRsl(rsl);
        
        return true;
    }

    public void reset() {
        // TODO: implement
    }

    public void stop() {        
        for (Component c : components.values()) { 
            c.stop();
        }
    }
    
    // Merge the state of two subjobs. Basic rules are as follows (sorted in 
    // order of importance). 
    // 
    // - SUBMISSION_ERROR when at least one sub state is SUBMISSION_ERROR
    // - RUNNING when at least one sub state is RUNNING
    // - PRE_STAGING when at least one sub state is PRE_STAGING
    // - SCHEDULED when at least one sub state is SCHEDULED
    // - POST_STAGING when at least one sub state is POST_STAGING
    // - HOLD when at least one sub state is HOLD    
    // - INITIAL when at least one sub state is INITIAL
    // - UNKNOWN when at least one sub state is UNKNOWN
    // - STOPPED when at least one sub state is STOPPED    
    
    private int mergeStates(int state1, int state2) { 

        if (state1 == Job.SUBMISSION_ERROR || state2 == Job.SUBMISSION_ERROR) { 
            return Job.SUBMISSION_ERROR;
        }
        
        if (state1 == Job.RUNNING || state2 == Job.RUNNING) { 
            return Job.RUNNING;
        }
        
        if (state1 == Job.PRE_STAGING || state2 == Job.PRE_STAGING) { 
            return Job.PRE_STAGING;
        }
        
        if (state1 == Job.SCHEDULED || state2 == Job.SCHEDULED) { 
            return Job.SCHEDULED;
        }
        
        if (state1 == Job.POST_STAGING || state2 == Job.POST_STAGING) { 
            return Job.POST_STAGING;
        }
        
        if (state1 == Job.ON_HOLD || state2 == Job.ON_HOLD) { 
            return Job.ON_HOLD;
        }

        if (state1 == Job.INITIAL || state2 == Job.INITIAL) { 
            return Job.INITIAL;
        }
        
        if (state1 == Job.UNKNOWN || state2 == Job.UNKNOWN) { 
            return Job.UNKNOWN;
        }

        if (state1 == Job.STOPPED || state2 == Job.STOPPED) { 
            return Job.STOPPED;
        }
        
        return Job.UNKNOWN;
    }
    
    private int mergeStates() { 
        
        if (components.size() == 0) { 
            return Job.INITIAL;
        }
        
        int state = Job.STOPPED;
        
        for (Component c : components.values()) { 
            state = mergeStates(state, c.getState());
        }
        
        return state;
    }
   
    public synchronized HashMap<String, Object> getInfo(HashMap<String, 
            Object> map) { 

        String jobSite = null;
        
        if (map == null) { 
            map = new HashMap<String, Object>();
        }
        
        if (components.size() == 0) { 
            map.put("state", Job.getStateString(Job.INITIAL));            
            return map;
        }
        
        map.put("state", Job.getStateString(mergeStates()));
                
        StringBuilder states = new StringBuilder();
        StringBuilder sites  = new StringBuilder();
                
        Iterator<Component> itt = components.values().iterator();
        
        while (itt.hasNext()) { 
            
            Component c = itt.next();
            
            states.append("["); 
            states.append(c.getIdentifier());
            states.append(", ");
            states.append(Job.getStateString(c.getState()));                 
            states.append("]");
        
            String site = c.getSite();
            
            if (site == null) { 
                site = "unknown";
            } else { 
                if (jobSite == null) { 
                    jobSite = site;
                } else if (!jobSite.equals(site)) { 
                    jobSite = "multiple";
                }
            }
            
            sites.append("["); 
            sites.append(c.getIdentifier());
            sites.append(", ");
            sites.append(site);                 
            sites.append("]");
            
            if (itt.hasNext()) { 
                states.append(", ");
                sites.append(", ");
            }
        }   
        
        map.put("components.states", states.toString());
        map.put("components.sites", sites);
        map.put("site", jobSite);
                
        return map;
    }
  
    public void stateChange(Component component, int state) {
        
        long time = -1;
        int newState = -1;
        
        // NOTE: the merging of states is synchronized to prevent race
        // conditions. This also implies that we have to copy the resulting 
        // state to a local variable when it has changed. If we wouldn't do this
        // it may have changed already when we forward the stateChange to out 
        // parent. We can't perform the stateChange inside the sync. block since 
        // that may result in deadlocks. 
        synchronized (this) { 
            int tmp = this.state;                  
            this.state = mergeStates();
            
            if (this.state != tmp) {
                newState = this.state;
                time = System.currentTimeMillis();
            }
        }
        
        if (newState != -1) { 
            
            System.out.println("State change of koalajob to " + Job.getStateString(newState));
            
            owner.stateChange(newState, time);
        } else { 
            System.out.println("State change not propagated to koalajob: " + Job.getStateString(state));
            
        }
    }    
}
