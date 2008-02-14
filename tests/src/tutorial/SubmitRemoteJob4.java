package tutorial;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.CertificateSecurityContext;

public class SubmitRemoteJob4 {
	/*
	 * Security context test for globus (avoid grid-proxy-init)
	 * 
	 * PREREQUISITES: 1. have a local private key (e.g.
	 * $HOME/.globus/userkey.pem) 2. have a local certificate (e.g.
	 * $HOME/.globus/usercert.pem) 3. have a local $HOME/.globus/certificates
	 * with the certificates
	 * 
	 * In case of problems: 
	 * 1. bad certificate 
	 * --> make sure your local time settings match the compute server's 
	 * 
	 * 2. job submission error (the job manager failed to open stdout, gram error 73)
	 * --> see http://www.afs.enea.it/scio/faq_errors.html#stdout
	 */

	public static void main(String[] args) throws Exception {
		GATContext context = new GATContext();
		context.addPreference("ResourceBroker.adaptor.name", "globus");
		String passphrase = null;
		JPasswordField pwd = new JPasswordField();
		Object[] message = { "grid-proxy-init\nPlease enter your passphrase.",
				pwd };
		JOptionPane.showMessageDialog(null, message, "Grid-Proxy-Init",
				JOptionPane.QUESTION_MESSAGE);
		passphrase = new String(pwd.getPassword());
		CertificateSecurityContext securityContext = new CertificateSecurityContext(
				new URI("/home/rkemp/.globus/userkey.pem"), new URI(
						"/home/rkemp/.globus/usercert.pem"), passphrase);
		context.addSecurityContext(securityContext);

		SoftwareDescription sd1 = new SoftwareDescription();
		sd1.setExecutable("/bin/sh");
		sd1.setArguments(new String[] { "../script.sh" });
		File stdout1 = GAT.createFile(context, "stdout");
		sd1.setStdout(stdout1);
		JobDescription jd1 = new JobDescription(sd1);

		ResourceBroker broker = GAT.createResourceBroker(context, new URI("any://fs1.das3.liacs.nl/jobmanager-sge"));
		Job job1 = broker.submitJob(jd1);
		System.out.println("job submitted!");

		while ((job1.getState() != Job.STOPPED)
				&& (job1.getState() != Job.SUBMISSION_ERROR)) {
			Thread.sleep(1000);
		}
		GAT.end();
		System.out.println("job DONE!");
	}
}
