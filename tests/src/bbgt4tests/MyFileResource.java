package mytests;

import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;

import java.util.Enumeration;

class MyFileResource {
    static public void main(String[] args) {
	FileResource resource = null;
	try {
	    resource = AbstractionFactory.newFileResource(args[0]);
	} catch(Exception e) {
	    System.err.println("some trouble: " + e);
	    System.exit(1);
	}
	SecurityContext securityContext = null;
	try {
	    securityContext = AbstractionFactory.newSecurityContext("local");
	} catch(Exception e) {
	    System.err.println("some trouble: " + e);
	    System.exit(1);
	}
	ServiceContact serviceContact =  new ServiceContactImpl();
	resource.setServiceContact(serviceContact);
	try { 
	    resource.start();
	} catch(Exception e) {
	    System.err.println("some trouble: " + e);
	    System.exit(1);
	}
	for (Enumeration e = resource.getAllAttributes() ; e.hasMoreElements() ;) {
	    System.out.println("Attributes: " + e.nextElement());
	    
	}
	//resource.getFile(
	try {
	    resource.stop();
	} catch(Exception e) {
	    System.err.println("some trouble: " + e);
	    System.exit(1);
	}
    }
}
