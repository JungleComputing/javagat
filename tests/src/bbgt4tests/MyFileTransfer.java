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
import org.globus.cog.abstraction.impl.common.task.FileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;

public class MyFileTransfer {
    static public void main(String[] args) {
	Task task = new TaskImpl("myTestTask", Task.FILE_TRANSFER);
	FileTransferSpecification spec = new FileTransferSpecificationImpl();
	//spec.setSourceDirectory("/home0/bbokodi/tmp");
	//spec.setSourceFile("zero10mb");
	//spec.setDestinationDirectory("/home0/bbokodi/tmp");
	//spec.setDestinationFile("copyofzero10mb");
	spec.setSource("/home0/bbokodi/tmp/zero10mb");
	spec.setDestination("/home0/bbokodi/tmp/copyofzero10mb");
	task.setSpecification(spec);
	

	Service sourceService = new ServiceImpl(Service.FILE_TRANSFER);
	try {
	    sourceService.setProvider("GridFTP2");
	} catch(Exception e) {
	    System.out.println("blabla");
	    System.exit(1);
	}
 
	SecurityContext sourceSecurityContext = null;
	try {
	    sourceSecurityContext =
		AbstractionFactory.newSecurityContext("GridFTP");
	}
	catch( Exception e ) {
	    System.err.println("Some trouble: " + e);
	}
	sourceSecurityContext.setCredentials(null);
	ServiceContact sourceServiceContact =
	    new ServiceContactImpl();
	sourceServiceContact.setHost("fs0.das3.cs.vu.nl");
	//sourceServiceContact.setPort(2811);
 	sourceService.setServiceContact(sourceServiceContact);
 
	task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, sourceService);
 
	Service destinationService = new ServiceImpl(Service.FILE_TRANSFER);
 
	destinationService.setProvider("GridFTP");
 
	SecurityContext destinationSecurityContext = null;
	try {
	    destinationSecurityContext = AbstractionFactory.newSecurityContext("GridFTP");
	}
	catch( Exception e ) {
	    System.err.println("Some trouble: " + e);
	}
 
	destinationSecurityContext.setCredentials(null);
 
	destinationService.setSecurityContext(destinationSecurityContext);
 
	ServiceContact destinationServiceContact =
	    new ServiceContactImpl();
 
	destinationServiceContact.setHost("fs2.das3.science.uva.nl");
	//destinationServiceContact.setPort(2811);
 	destinationService.setServiceContact(destinationServiceContact);
 
	task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE, destinationService);
	FileTransferTaskHandler handler = new FileTransferTaskHandler();
	try {
	    handler.submit(task);
	}
	catch( Exception e ) {
	    System.err.println("Some trouble: " + e);
	    System.err.println("hello");
	    System.exit(2);
	    
	}

    }
}
















