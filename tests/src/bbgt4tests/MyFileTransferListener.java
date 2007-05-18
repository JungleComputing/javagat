package mytests;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;

class MyFileTransferListener implements StatusListener {
    Task task;
    public MyFileTransferListener(Task t) {
	task = t;
    }
    public void statusChanged(StatusEvent event) {
	Status status = event.getStatus();
	System.out.println(status);
	if (status.getStatusCode() == Status.COMPLETED) {
	    System.out.println("comp");
	    System.out.println(task.getAttribute("output"));
	}
	if (status.getStatusCode() == Status.ACTIVE) {
	    System.out.println("act");
	}
	if (status.getStatusCode() == Status.CANCELED) {
	    System.out.println("canc");
	}
	if (status.getStatusCode() == Status.FAILED) {
	    System.out.println("fail");
	    System.out.println(task.getAttribute("output"));
	}
	if (status.getStatusCode() == Status.SUBMITTED) {
	    System.out.println("subm");
	}
	if (status.getStatusCode() == Status.SUSPENDED) {
	    System.out.println("susp");
	}
	if (status.getStatusCode() == Status.UNKNOWN) {
	    System.out.println("unknown");
	}
	if (status.getStatusCode() == Status.UNSUBMITTED) {
	    System.out.println("unsubm");
	}
	if (status.getStatusCode() == Status.RESUMED) {
	    System.out.println("resumed");
	}

    }
}
