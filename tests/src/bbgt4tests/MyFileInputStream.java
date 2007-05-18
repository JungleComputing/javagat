package mytests;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.FileOperationTaskHandler;
import org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;

public class MyFileInputStream {
    static public void main(String[] args) {
	Task task = new TaskImpl("myFileOpTask", Task.FILE_OPERATION);
	task.setProvider("gridftp");

	FileOperationSpecification spec = new FileOperationSpecificationImpl();
	spec.setOperation(FileOperationSpecification.START);
 	task.setSpecification(spec);

	Service service = new ServiceImpl(Service.FILE_OPERATION);
	service.setProvider("gridftp");
	SecurityContext securityContext = null;
	try {
	    securityContext = AbstractionFactory.newSecurityContext("gridftp");
	}
	catch( Exception e ) {
	    System.err.println("Some trouble: " + e);
	    System.exit(1);
	}
	securityContext.setCredentials(null);
	service.setSecurityContext(securityContext);

	ServiceContact serviceContact =
	    new ServiceContactImpl("fs0.das3.cs.vu.nl");
	service.setServiceContact(serviceContact);
	task.setService(Service.DEFAULT_SERVICE, service);

	MyFileISListener listener = new MyFileISListener(task);
	task.addStatusListener(listener);
	FileOperationTaskHandler handler = new FileOperationTaskHandler();
	try {
	    handler.submit(task);
	}
	catch( Exception e ) {
	    System.err.println("Some trouble: " + e);
	}
	try {
	    task.waitFor();
	}
	catch(Exception e) {
	    System.err.println("Some trouble: " + e);
	}

	Task mytask = new TaskImpl("mySecondTask", Task.FILE_OPERATION);
	mytask.setProvider("gridftp");

	FileOperationSpecification myspec = new FileOperationSpecificationImpl();
	myspec.setOperation(FileOperationSpecification.LS);
	myspec.addArgument("/home0/bbokodi/tmp/mess");
	mytask.setSpecification(myspec);
	mytask.setAttribute("sessionID", task.getAttribute("output"));
	
	MyGenListener mylistener = new MyGenListener(mytask);
	mytask.addStatusListener(mylistener);
	try {
	    handler.submit(mytask);
	}
	catch( Exception e ) {
	    System.err.println("Some trouble: " + e);
	}
	
    }
}
