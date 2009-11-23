package org.gridlab.gat.resources.cpi.glite;

import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.resources.Job;

public interface GliteJobInterface extends Job{
	
	public boolean isJobKilled();
	public Metric getStatusMetric();
	public void updateState();
	public void receiveOutput();
}
