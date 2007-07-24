/*
 * Created on Jul 24, 2007 by rob
 */
package org.gridlab.gat.resources.cpi.globus;

import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.cpi.globus.GramError;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class CogGramTest implements GramJobListener, org.globus.gram.internal.GRAMConstants {
    boolean exit = false;
    
    public static void main(String[] args) throws Exception {
        new CogGramTest().start();
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

        long start = System.currentTimeMillis();
        submitGramJob(credential, "", "fs0.das2.cs.vu.nl/jobmanager-sge");
        
        synchronized (this) {
            while(!exit) {
                wait();
            }
        }

        long end = System.currentTimeMillis();
        System.err.println("job took: " + (end - start) + " ms");
        
        System.err.println("done");
    }

    void submitGramJob(GSSCredential credential, String rsl,
        String contact) throws GATInvocationException {
        GramJob j = new GramJob(credential, rsl);
        j.addListener(this);

        try {
            Gram.request(contact, j);
        } catch (GramException e) {
            if (GATEngine.VERBOSE) {
                System.err.println("could not run job: "
                    + GramError.getGramErrorString(e.getErrorCode()));
            }

        } catch (GSSException e2) {
            throw new CouldNotInitializeCredentialException("globus", e2);
        }
    }

    public void statusChanged(GramJob newJob) {
        int globusState = newJob.getStatus();
        if ((globusState == STATUS_DONE) || (globusState == STATUS_FAILED)) {
            synchronized (this) {
                exit = true;
                notifyAll();
            }
        }
    }
}
