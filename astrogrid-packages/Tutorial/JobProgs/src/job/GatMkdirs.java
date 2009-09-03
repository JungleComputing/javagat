package job;


import java.io.IOException;
import java.lang.*;
import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class GatMkdirs {
	
	/**
	 * test mkdirs with GATFile, and aftwerwards with java.io.File...
	 * 
	 * Compare the rc, and print them out.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	

	
	public static void main (String[] args) throws GATObjectCreationException, URISyntaxException, IOException, InterruptedException {
		
		
		/** 
		 * first the GAT stuff...
		 */
		
		GATContext context = new GATContext();
		
		
		File dirToCreate = GAT.createFile(context, args[0]);
	
		/**
		 * create the directory and its parent directoruies using mkdirs...
		 * 
		 */
		
		URI dir = new URI(args[0]);
		boolean creationFlag = dirToCreate.mkdirs();
		
		System.out.println("GATFile.mkdirs of '" + dir  + "' which was GATURI of '" + args[0] +"' has delivered:" + creationFlag);
		
		Process p1 = null;
		
		/**
		 * list and delete the directories created, using Linuy system shell commands
		 */
		
		java.io.FileInputStream inStream = null;
		p1 = Runtime.getRuntime().exec("ls -l " + args[0]);
		p1.waitFor();
		java.io.InputStream output = p1.getInputStream();
		int amount = output.available();
		byte[] b = new byte[amount];
		
		output.read(b,0,amount);
		String outString = new String(b);
		
		System.out.println("Output ls -l " + args[0] + ": " + outString);
		
		p1.destroy();
		
		p1 =  Runtime.getRuntime().exec("rm -rf /home/alibeck/test1");
		p1.waitFor();
		int rc = p1.exitValue();
		
		System.out.println("Exitvaule of rm -rf test1: " + rc);
		p1.destroy();

		/**
		 * The same thing for a java file..
		 */
		
		java.io.File javaFile = new java.io.File(args[0]);
		creationFlag = false;
		creationFlag = javaFile.mkdirs();
		System.out.println("javaFile.mkdirs of '" + args[0] +"' has delivered:" + creationFlag);

		p1 = Runtime.getRuntime().exec("ls -l " + args[0]);
		p1.waitFor();
		output = p1.getInputStream();
		amount = output.available();
		b = new byte[amount];
		
		output.read(b,0,amount);
		outString = new String(b);
		
		System.out.println("Output ls -l " + args[0] + ": " + outString);
		
		p1.destroy();

		p1 =  Runtime.getRuntime().exec("rm -rf /home/alibeck/test1");
		p1.waitFor();
		rc = p1.exitValue();
		
		System.out.println("Exitvaule of rm -rf /home/alibeck/test1: " + rc);
		p1.destroy();


		
		}
}

