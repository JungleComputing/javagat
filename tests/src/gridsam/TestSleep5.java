package gridsam;

import org.apache.log4j.Logger;

import org.icenigrid.gridsam.client.common.ClientSideJobManager;
import org.icenigrid.gridsam.core.*;
import org.icenigrid.gridsam.core.jsdl.JSDLSupport;
import org.icenigrid.schema.jsdl.y2005.m11.*;
import org.apache.xmlbeans.XmlException;
import java.io.*;
import java.util.*;

public class TestSleep5 {

    // private static String ftpServer = System.getProperty("ftp.server");

    private static String ftpServer = "localhost:55531";

    // private static String gridsamServer =
    // System.getProperty("gridsam.server");

    private static String gridsamServer = "localhost:18443";
    private static Logger logger = Logger.getLogger(TestSleep5.class);

    public static void main(String[] args) throws JobManagerException, SubmissionException, UnsupportedFeatureException, UnknownJobException, IOException,
            XmlException, InterruptedException {

        logger.debug("java.endorsed.dirs=" + System.getProperty("java.endorsed.dirs"));

        System.out.println("Creating a new client Job Manager..., server=" + gridsamServer);

        ClientSideJobManager jobManager = new ClientSideJobManager(

        new String[] { "-s", "https://" + gridsamServer + "/gridsam/services/gridsam?wsdl" },

        ClientSideJobManager.getStandardOptions());

        System.out.println("Creating JSDL description...");

        String xJSDLString = createJSDLDescription("/bin/ls", "-la");

        JobDefinitionDocument xJSDLDocument = JobDefinitionDocument.Factory.parse(xJSDLString);

        System.out.println("Submitting job to Job Manager...");

        JobInstance job = jobManager.submitJob(xJSDLDocument);

        String jobID = job.getID();

        // Get and report the status of job until complete

        System.out.println("Monitor " + jobID + " until completion...");

        String desc = "";

        String state = "";

        do {

            // Update the Job Manager's status of the job

            job = jobManager.findJobInstance(jobID);

            // Get the status of the job

            List jsList = job.getJobStages();

            if (jsList.size() > 0) {

                // Get last known job state

                JobStage js = (JobStage) jsList.get(jsList.size() - 1);

                state = js.getState().toString();

                // Report the status of the job

                System.out.println("  -> Current status of " + jobID + ": " + state);

                System.out.print("     States: ");

                for (int i = 0; i < jsList.size(); i++)

                    System.out.print(((JobStage) jsList.get(i)).getState().toString() + ", ");

                System.out.println();

            }

            Thread.sleep(2000);

        } while (!state.equals("done") && !state.equals("failed"));

        System.out.println("Example has finished - you can view the output stdout.txt and stderr.txt in your FTP datastaging directory.");

    }

    private static String createJSDLDescription(String execName, String args) {

        return "" +

        "<JobDefinition xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">" +

        "<JobDescription>" +

        "<Application>" +

        "<POSIXApplication xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">" +

        "<Executable>" + execName + "</Executable>" +

        "<Argument>" + args + "</Argument>" +

        "<Output>stdout.txt</Output>" +

        "<Error>stderr.txt</Error>" +

        "</POSIXApplication>" +

        "</Application>" +

        "<DataStaging>" +

        "<FileName>stdout.txt</FileName>" +

        "<CreationFlag>overwrite</CreationFlag>" +

        "<Target>" +

        "<URI>ftp://anonymous:anonymous@" + ftpServer + "/stdout.txt</URI>" +

        "</Target>" +

        "</DataStaging>" +

        "<DataStaging>" +

        "<FileName>stderr.txt</FileName>" +

        "<CreationFlag>overwrite</CreationFlag>" +

        "<Target>" +

        "<URI>ftp://anonymous:anonymous@" + ftpServer + "/stderr.txt</URI>" +

        "</Target>" +

        "</DataStaging>" +

        "</JobDescription>" +

        "</JobDefinition>";

    }

}

//
// public class TestSleep5 {
//
// private Logger logger = Logger.getLogger(TestSleep5.class);
//    
// private void run() {
//        
// }
//    
// public static void main(String[] args) {
// (new TestSleep5()).run();
// }
// }
