package job;


import java.io.IOException;
import java.lang.*;
import java.net.URISyntaxException;
import java.io.InputStream;

import java.io.File;

public class MkdirsJavaFile {
	
	/**
	 * test mkdirs with GATFile, and aftwerwards with java.io.File...
	 * 
	 * Compare the rc, and print them out.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	

	
	public static void main (String[] args) throws URISyntaxException, IOException, InterruptedException {
		
		

		/**
		 * The same thing for a java file..
		 */
		
		File javaFile = new java.io.File(args[0]);
		
		Boolean creationFlag = javaFile.mkdirs();
		System.out.println("javaFile.mkdirs of '" + args[0] +"' has delivered:" + creationFlag);

		Process p1 = Runtime.getRuntime().exec("ls -l " + args[0]);
		p1.waitFor();
		InputStream output = p1.getInputStream();
		int amount = output.available();
		byte[] b = new byte[amount];
		
		output.read(b,0,amount);
		String outString = new String(b);
		
		System.out.println("Output ls -l " + args[0] + ": " + outString);
		
		p1.destroy();

		p1 =  Runtime.getRuntime().exec("rm -rf /home/alibeck/test1");
		p1.waitFor();
		int rc = p1.exitValue();
		
		System.out.println("Exitvaule of rm -rf /home/alibeck/test1: " + rc);
		p1.destroy();


		
		}
}

