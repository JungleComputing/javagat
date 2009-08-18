package org.gridlab.gat.resources.cpi.wsgt4new;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.gt4.GlobusHardwareResource;

public class NotifierThread extends Thread {
	
	private WSGT4newResourceBrokerAdaptor broker;

	private GlobusHardwareResource resource;

	private List<MetricListener> metricListeners = new ArrayList<MetricListener>();

	private List<Metric> metrics = new ArrayList<Metric>();

	private List<Long> updates = new ArrayList<Long>();

	public NotifierThread(GlobusHardwareResource resource, WSGT4newResourceBrokerAdaptor broker) {
		this.broker = broker;
		this.resource = resource;
		//setDaemon(true);
	}

	public synchronized void add(Metric metric, MetricListener listener) {
		System.out.println("metric added --");
		metrics.add(metric);
		metricListeners.add(listener);
		updates.add(System.currentTimeMillis()); // 1.51
		notifyAll();
	}

	public synchronized void remove(Metric metric, MetricListener metricListener) {
		for (int i = 0; i < metrics.size(); i++) {
			if (metrics.get(i) == metric
					&& metricListeners.get(i) == metricListener) {
				metrics.remove(i);
				metricListeners.remove(i);
				updates.remove(i);
			}
		}
	}

	public void run() {
		while (true) {
			
			// get the results
			for (int i = 0; i < metricListeners.size(); i++) {
				// do we need to fire an event for this metric?
				System.out.println("do we need to fire an event for the metric "+i);
				if (updates.get(i) < System.currentTimeMillis()) {
					System.out.println("I have to create a new Event");
					// use the broker to get the new information!
				
					broker.queryDefaultIndexService();
					/*Selezionare i nomi degli ost di interesse
					 * dopo di che fare cio che si desidera
					 * 
					 * */
					
					// metrics.get(i).getMetricParameterByName("maximum"));
					
					MetricEvent event = new MetricEvent(resource, "200",
							metrics.get(i), System.currentTimeMillis());
					resource.fireMetric(event);
					// update the time when we need our next update
					updates.set(i, updates.get(i)
							+ metrics.get(i).getFrequency());
				}
			}

			// find out how long we have to sleep for the next update
			long nextUpdateTime = Long.MAX_VALUE;
			for (Long update : updates) {
				nextUpdateTime = Math.min(nextUpdateTime, update);
			}

			try {
				long time=nextUpdateTime - System.currentTimeMillis();
				System.out.println("I m going to sleep for "+time);
				sleep(time);
			    
			} catch (InterruptedException e) {
				}

		}
	}

}