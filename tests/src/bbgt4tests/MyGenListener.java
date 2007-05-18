package mytests;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.GridFile;
import java.util.Enumeration;
import java.util.Collection;

class MyGenListener implements StatusListener {
    Task task;
    public MyGenListener(Task t) {
	task = t;
    }
    public void statusChanged(StatusEvent event) {
	Status status = event.getStatus();
	System.out.println(status);
	if (status.getStatusCode() == Status.COMPLETED) {
	    for (Enumeration e = task.getAllAttributes() ; e.hasMoreElements() ;) {
		System.out.println("Attributes: " + e.nextElement());
		
	    }
	    Collection mycol = (Collection) task.getAttribute("output");
	    for (Object o : mycol)
		System.out.println(o);


	    System.out.println("gencomp");
	}
	if (status.getStatusCode() == Status.ACTIVE) {
	    System.out.println("genact");
	}
	if (status.getStatusCode() == Status.CANCELED) {
	    System.out.println("gencanc");
	}
	if (status.getStatusCode() == Status.FAILED) {
	    System.out.println("genfail");
	    System.out.println(task.getAttribute("sessionid"));
	}
	if (status.getStatusCode() == Status.SUBMITTED) {
	    System.out.println("gensubm");
	}
	if (status.getStatusCode() == Status.SUSPENDED) {
	    System.out.println("gensusp");
	}
	if (status.getStatusCode() == Status.UNKNOWN) {
	    System.out.println("genunknown");
	}
	if (status.getStatusCode() == Status.UNSUBMITTED) {
	    System.out.println("genunsubm");
	}
	if (status.getStatusCode() == Status.RESUMED) {
	    System.out.println("genresumed");
	}

    }
}
