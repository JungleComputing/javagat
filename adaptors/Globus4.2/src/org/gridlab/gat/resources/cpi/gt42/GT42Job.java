package org.gridlab.gat.resources.cpi.gt42;

import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;


import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

public class GT42Job extends JobCpi implements GramJobListener, Runnable{

	protected GT42Job(GATContext gatContext, JobDescription jobDescription,
			Sandbox sandbox) {
		super(gatContext, jobDescription, sandbox);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void stateChanged(GramJob arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
