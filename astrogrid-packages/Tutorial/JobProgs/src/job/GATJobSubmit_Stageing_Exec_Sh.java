package job;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;


//import java.io.File;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;

public class GATJobSubmit_Stageing_Exec_Sh {

    public GATJobSubmit_Stageing_Exec_Sh() {

    }
    
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

            GATJobSubmit_Stageing_Exec_Sh gatDemo = new GATJobSubmit_Stageing_Exec_Sh();
            GATContext context = new GATContext();

/*            CredentialSecurityContext securityContext = gatDemo.obtainProxyCertificate();
            context.addSecurityContext(securityContext);*/

            Preferences gatprefs = new Preferences();
//            gatprefs.put("File.adaptor.name", "gt4gridftp");
//            gatprefs.put("File.adaptor.name", "gridftp");
            gatprefs.put("File.adaptor.name", "commandlinessh");
            gatprefs.put("ResourceBroker.adaptor.name", "commandlinessh"); 
//            gatprefs.put("FileOutputStream.adaptor.name", "gridftp local");
//           gatprefs.put("ResourceBroker.adaptor.name", "unicore"); 
//            gatprefs.put("ResourceBroker.adaptor.name", "wsgt4new"); 
            context.addPreferences(gatprefs);

            
            // create URIs for the files and GATFiles...
            
            try {
                
                // add the executable to "Executable"
                executable  = "Executable";
                swDescr = new SoftwareDescription();
                swDescr.setExecutable("/bin/sh");
//                swDescr.setExecutable(executable);

                // add executable as prestage file; on the remote host it should have the name
                // "Executable"
                String remoteFile = executable;
                String localFile  = args[1];
                String locFile1 = "/home/alibeck/bin/geo600-scripts/build-reset.sh";
                String remoteFile1="reset.sh";
                java.net.URI testURI = new java.net.URI(localFile);
                String testPath = testURI.getPath();
                swDescr.addPreStagedFile(GAT.createFile(context, new URI(localFile)),
                                          GAT.createFile(context, new URI(remoteFile)));
                
                swDescr.addPreStagedFile(GAT.createFile(context, new URI(locFile1)),
                		                 GAT.createFile(context, new URI(remoteFile1)));
                 
                
                //add the arguments
                 String[] argsList = new String[args.length];
                 argsList[0] = executable;
//                 System.arraycopy(args, 1, argsList, 1, args.length - 1);
//                  String[] argsList = new String[args.length-2];
                 System.arraycopy(args, 2, argsList, 1, args.length - 2);
//                 System.arraycopy(args, 2, argsList, 0, args.length - 2);
                  swDescr.setArguments(argsList);

                // add stdout and stderr
                FileStdout = GAT.createFile(context, gatprefs, new URI(DEFAULT_STDOUT));
                FileStderr = GAT.createFile(context, gatprefs, new URI(DEFAULT_STDERR));

                swDescr.addAttribute("globus.exitvalue.enable", "true");
//                swDescr.addAttribute("sandbox.useroot", "/home/Hya/Agrid/agrid003/sandboxtest");
                //                swDescr.addAttribute("sandbox.delete", "true");

                swDescr.setStdout(FileStdout);
                swDescr.setStderr(FileStderr);

                /**
                 * for test purposes only
                 */
                
            	Map preStaged=null;
    	    	preStaged=swDescr.getPreStaged();
    	    	if (preStaged!=null) {
    	    		Set keys = preStaged.keySet();
    	    		Iterator i = keys.iterator();
    	            while (i.hasNext()) {
    	                File srcFile = (File) i.next();
    	                File destFile = (File) preStaged.get(srcFile);
    	                
    	                String srcName = srcFile.getPath();
    	                System.out.println("srcName: '" + srcName + "'");
    	            }
    	    	}
                
                // create the resource Boker
                String JobManagerContact = new String("any://" + args[0]);
                // String JobManagerContact = new String("any://" + args[0]);
                broker = GAT.createResourceBroker(context,new URI(JobManagerContact));

                // create the HardwareDescription
//                Hashtable hwAttr = new Hashtable();
//                hwrDescr = new HardwareResourceDescription(hwAttr);

                // create the job description
                swDescr.addAttribute("nodes", "12");
                String aa= (String) swDescr.getObjectAttribute("nodes");
//                jobDescr = new JobDescription(swDescr,hwrDescr);
                jobDescr = new JobDescription(swDescr);
                

            } catch (Exception e) {
                System.err.println("Could not create description:: " + e);
                e.printStackTrace();
                System.exit(1);
            }

            try {
                job = broker.submitJob(jobDescr);
            } catch (Exception e) {
                System.err.println("Could not submit job: " + e);
                e.printStackTrace();
                System.exit(1);
            }


            // poll for the job status

            while (true) {
                try {
                    System.out.println("Job status: " + job.getState());
                     Map info = job.getInfo();
                     System.out.print("job info: ");
                     System.out.println(info);
                    
//                    String state = (String) info.get("state");
                    
//                     if (state.equals("STOPPED")) {
                    if (job.getState() == JobState.STOPPED) {
                        System.out.println();
                        System.out.println("JOB SUMMARY");
                        System.out.println("================================================");
                        System.out.println("Command:    "+swDescr.getExecutable());
//                        System.out.println("Job ID:     "+job.getJobID());
                        System.out.println("Job ID:     "+info.get("adaptor.job.id"));
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
//                                FileStdout.deleteOnExit();
//                                FileStderr.deleteOnExit();
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
                    else if (job.getState()==JobState.SUBMISSION_ERROR) {
                        break;
                    }
                    
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("job.getState failed: " + e);
                    e.printStackTrace();
                    
                    break;
                }
            }

        GAT.end();
        System.exit(0);
        }
    }


     /**
     * Obtain a proxy certificate. 
     * Get its location either from the environment variable 'X509_USER_PROXY',
     * or from the CogKit property 'proxy'.
     * 
     * @return a CredentialSecurityContext with the certificate
     */
    protected CredentialSecurityContext obtainProxyCertificate() {
        CredentialSecurityContext securityContext = null;

        // Try to get the path to the proxy certificate ...
        // ... from the environment variable X509_USER_PROXY, set by GLOBUS
        String proxyPath = System.getenv("X509_USER_PROXY");

        if (proxyPath == null) {
            // ... from the cog.properties file, which is used by CogKit
            try {
                String userHomePath = System.getProperty("user.home");
                String propsFilePath = userHomePath + "/.globus/cog.properties";
                java.io.File propsFile = new java.io.File(propsFilePath);
                java.io.FileInputStream propsInputStream = new java.io.FileInputStream(propsFile);
                Properties props = new Properties();
                props.load(propsInputStream);
                proxyPath = props.getProperty("proxy");
            } catch (FileNotFoundException fnfe) {
                System.err.println("CogKit property file not found: " + fnfe);
                fnfe.printStackTrace(System.err);
            } catch (IOException ioe) {
                System.err.println("Input/output exception while trying to read CogKit property file: " + ioe);
                ioe.printStackTrace(System.err);
            }
        }

        // Now read the proxy certificate and create a security context

        try {
            java.io.File proxyFile = new java.io.File(proxyPath);
            java.io.FileInputStream inputStream = new java.io.FileInputStream(proxyFile);
            int length = inputStream.available();
            byte[] b = new byte[length];
            inputStream.read(b, 0, length);
            securityContext = new CredentialSecurityContext(b);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Proxy certificate file not found: " + fnfe);
            System.err.println("Did you issue the required 'grid-proxy-init'?");
            fnfe.printStackTrace(System.err);
        } catch (IOException ioe) {
            System.err.println("Input/output exception while trying to read proxy certificate file: " + ioe);
            ioe.printStackTrace(System.err);
        }
        return securityContext;
    }
   
    
}
