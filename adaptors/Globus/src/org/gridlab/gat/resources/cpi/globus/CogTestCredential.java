// Test with supplied credential security context.

package org.gridlab.gat.resources.cpi.globus;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class CogTestCredential implements MetricListener {

    protected static Logger logger = LoggerFactory.getLogger(CogTestCredential.class);

    boolean exit = false;

    public static void main(String[] args) throws Exception {
        new CogTestCredential().start();
    }

    void start() throws Exception {
        GSSCredential credential = null;

        try {
            // Get the user credential
            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                    .getInstance();

            // try to get default user proxy certificate from file in /tmp
            credential = manager
                    .createCredential(GSSCredential.INITIATE_AND_ACCEPT);
        } catch (GSSException x) {
            System.err.println("default credential failed: " + x);

            // handled below
        }
        
        Preferences preferences = new Preferences();
        preferences.put("resourcebroker.adaptor.name", "globus,wsgt4new");
        // preferences.put("file.adaptor.name", "gt4gridftp");
        
        GATContext context = GAT.getDefaultGATContext();
        CredentialSecurityContext secCtx = new CredentialSecurityContext(credential);
        context.addSecurityContext(secCtx);

        submitJobEasy(preferences, "fs0.das3.cs.vu.nl");

    }
    
    private void submitJobEasy(Preferences prefs, String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/echo");
        sd.setArguments("test", "1", "2", "3");
        Map<String, Object> attributes = new HashMap<String, Object>();

        sd.setAttributes(attributes);
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = null;
        try {
            broker = GAT.createResourceBroker(prefs, new URI("any://" + host));
        } catch (Throwable e) {
            System.err.println("Got exception " + e);
        }
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (Throwable e) {
            System.err.println("Got exception " + e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }

    public void processMetricEvent(MetricEvent val) {
        if (val.getValue().equals(Job.JobState.STOPPED)) {
            synchronized (this) {
                notifyAll();
            }
        }        
    }



}
