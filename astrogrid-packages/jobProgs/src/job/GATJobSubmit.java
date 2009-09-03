package job;

import java.util.Hashtable;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;

//import java.io.File;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;


public class GATJobSubmit {
    
    public static void main(String[] args) {

        org.gridlab.gat.io.File FileStdout     = null;
        org.gridlab.gat.io.File FileStderr     = null;
        String DEFAULT_STDOUT = "GATJobRun_STDOUT.tmp";
        String DEFAULT_STDERR = "GATJobRun_STDERR.tmp";

        ResourceBroker broker = null;
        ResourceDescription hwrDescr = null;
        SoftwareDescription swDescr = null;
        JobDescription jobDescr = null;
        Job job = null;
        String executable = null;

        if ( args.length < 2 ) {
            System.out.println("\tUsage: GATJobSubmit <host> <executable> <arguments>\n");
            System.exit(1);
        } else {
            
            // create GATContext, Preferences
            GATContext context = new GATContext();
            Preferences gatprefs = new Preferences();
            gatprefs.put("ResourceBroker.jobmanagerContact", args[0]);
            gatprefs.put("File.adaptor.name", "local,gridftp");
      //                  gatprefs.put("ResourceBroker.adaptor.name", "globus");
            gatprefs.put("ResourceBroker.adaptor.name", "wsgt4new");
            context.addPreferences(gatprefs);

            
            // create URIs for the files and GATFiles...
            
            try {
                
                // add the executable
                executable  = args[1];
                swDescr = new SoftwareDescription();
                swDescr.setExecutable(executable);
                
                //add the arguments
                String[] argsList = new String[args.length - 1];
                System.arraycopy(args, 1, argsList, 0, args.length - 1);
                //swDescr.setArguments(argsList);

                // add stdout and stderr
                FileStdout = GAT.createFile(context, gatprefs, new URI(DEFAULT_STDOUT));
                FileStderr = GAT.createFile(context, gatprefs, new URI(DEFAULT_STDERR));

                swDescr.addAttribute("globus.exitvalue.enable", "true");

                swDescr.setStdout(FileStdout);
                swDescr.setStderr(FileStderr);

                
                // create the resource Boker
                String JobManagerContact = new String("https://" + args[0]);
                broker = GAT.createResourceBroker(context,new URI(JobManagerContact));

                // create the HardwareDescription
                Hashtable hwAttr = new Hashtable();
                hwrDescr = new HardwareResourceDescription(hwAttr);

                // create the job description

                jobDescr = new JobDescription(swDescr,hwrDescr);
                

            } catch (Exception e) {
                System.err.println("Could not create description:: " + e);
                e.printStackTrace();
                System.exit(1);
            }

            try {
                job = broker.submitJob(jobDescr);
                System.out.println("have submitted job to wsgram");
            } catch (Exception e) {
                System.err.println("Could not submit job: " + e);
                e.printStackTrace();
                System.exit(1);
            }


            // poll for the job status

            while (true) {
                try {
//                     Map info = job.getInfo();
//                     System.err.print("job info: ");
//                     System.err.println(info);
                    
                    String state = job.getStateString(job.getState());
                    System.out.println("State of job: " + state);
                    
                    if (job.getState() == job.STOPPED) {
                        System.out.println();
                        System.out.println("JOB SUMMARY");
                        System.out.println("================================================");
                        System.out.println("Command:    "+swDescr.getExecutable());
                        System.out.println("Job ID:     "+job.getJobID());
                        System.out.println("Exit value: "+job.getExitStatus());
                        System.out.println("================================================");
                        System.out.println();
                        
                        if (FileStdout.toGATURI().toString().equals( DEFAULT_STDOUT ))  {
                            
                            FileInputStream in = GAT.createFileInputStream(context,
                                                                           FileStdout.toGATURI());
                            
                            java.io.InputStreamReader reader =  new java.io.InputStreamReader(in);
                            
                            BufferedReader stdoutReader = new  BufferedReader(reader);
                            
                            String         line;
                    
                            System.out.println("STDOUT");
                            System.out.println("================================================");
                            
                            try {
                                while ((line = stdoutReader.readLine()) != null) 
                                    {
                                        System.out.println(line);
                                    }
                                stdoutReader.close();
//                                 FileStdout.deleteOnExit();
//                                 FileStderr.deleteOnExit();
                            } catch (IOException e1) {
                                System.out.println("error reading GATs default stdout");
                                e1.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("error reading GATs default stdout. Value of Reader: " + reader);
                                e.printStackTrace();
                            }
                            System.out.println("================================================");
                            System.out.println();
                            
                            break;
                        }
                    }
                    else if (job.getState() == job.SUBMISSION_ERROR) {
                        break;
                    }
                    
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("getInfo failed: " + e);
                    e.printStackTrace();
                    
                    break;
                }
            }

        GAT.end();
        System.exit(0);
        }
    }
    
    
}
