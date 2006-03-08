/**********************************************************
 *
 * @file: examples/JobSubmit.java
 *
 * @description:
 *   example number #: Submit the given binary as job to
 *                     any resource.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/
/*** LICENSE ***/
/*******************************************************************************
 * This program creates a full job description for the gives binary, and submits
 * it to a resource broker.
 ******************************************************************************/
package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

import java.util.Hashtable;
import java.util.Map;

public class JobSubmit {
    public static void main(String[] args) {
        // Declaring needed variables.
        ResourceBroker broker = null;
        ResourceDescription hwrDescription = null;
        SoftwareDescription swDescription = null;
        JobDescription jobDescription = null;
        Job job = null;
        URI binaryFile = null;

        // Checking for right commandline invocation.
        if (args.length < 1) {
            System.out
                .println("\tUsage: run_gat_app examples.JobSubmit <binary>"
                    + " [args]\n\tto run the given program.");
            System.exit(1);
        }

        // Create the GAT Context.
        GATContext context = new GATContext();

        try {
            // Create the ResourceBroker.
            broker = GAT.createResourceBroker(context);

            // Convert the first args to an URI, create the SoftwareDescription,
            // sets its location to the URI to be submit as job,
            // and sets its arguments as given in the commandline.
            binaryFile = new URI(args[0]);
            swDescription = new SoftwareDescription();
            swDescription.setLocation(binaryFile);

            String[] argsList = new String[args.length - 1];
            System.arraycopy(args, 1, argsList, 0, args.length - 1);
            swDescription.setArguments(argsList);

            // Create the HardwareDescription.
            Hashtable hardwareAttributes = new Hashtable();
            hwrDescription = new HardwareResourceDescription(hardwareAttributes);

            // Describe the job using the software- and the hardwaredescription.
            jobDescription = new JobDescription(swDescription, hwrDescription);
        } catch (Exception e) {
            System.err.println("Could not create description: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        try {
            job = broker.submitJob(jobDescription);
        } catch (Exception e) {
            System.err.println("Could not submit job: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            try {
                Map info = job.getInfo();
                System.err.print("job info: ");
                System.err.println(info);

                String state = (String) info.get("state");

                if ((state == null) || state.equals("STOPPED")
                    || state.equals("SUBMISSION_ERROR")) {
                    break;
                }

                Thread.sleep(10000);
            } catch (Exception e) {
                System.err.println("getInfo failed: " + e);
                e.printStackTrace();

                break;
            }
        }
    } // main
}
