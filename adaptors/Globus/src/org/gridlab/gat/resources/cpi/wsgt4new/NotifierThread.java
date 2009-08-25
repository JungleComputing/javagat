package org.gridlab.gat.resources.cpi.wsgt4new;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.gt4.GlobusHardwareResource;

/* This class keeps monitoring the value of the metrics of the resource.
 * If the value changes and iy satisfies the values of the metrics received
 * in input then an event is fired 
 * */
public class NotifierThread extends Thread {

	private WSGT4newResourceBrokerAdaptor broker;

	private GlobusHardwareResource resource;

	private List<MetricListener> metricListeners = new ArrayList<MetricListener>();

	private List<Metric> metrics = new ArrayList<Metric>();

	private List<Long> updates = new ArrayList<Long>();

	private boolean death = false;

	public NotifierThread(GlobusHardwareResource resource,
			WSGT4newResourceBrokerAdaptor broker) {
		this.broker = broker;
		this.resource = resource;
		setDaemon(true);
	}

	public void add(Metric metric, MetricListener listener) {
		synchronized (NotifierThread.this) {
			metrics.add(metric);
			metricListeners.add(listener);
			updates.add(System.currentTimeMillis());
			NotifierThread.this.notifyAll();

		}
	}

	public void remove(Metric metric, MetricListener metricListener) {
		synchronized (NotifierThread.this) {
			for (int i = 0; i < metrics.size(); i++) {
				if (metrics.get(i).equals(metric)
						&& metricListeners.get(i).equals(metricListener)) {
					metrics.remove(i);
					metricListeners.remove(i);
					updates.remove(i);
				}
			}

			if (metrics.size() == 0) {// all the metrics have been removed
				this.death = true;
				System.out.println("Notifier Thread Killed");
			}
		}
	}

	public void run() {
		while (!this.death) {
			// get the results
			synchronized (NotifierThread.this) {
				for (int i = 0; i < metricListeners.size(); i++) {
					if (updates.get(i) <= System.currentTimeMillis()) {
						Map h = metrics.get(i).getMetricParameters();
						checkKeys(h, i);
						// update the time when we need our next update
						updates.set(i, updates.get(i)
								+ metrics.get(i).getFrequency());
					}
				}
			}// the synchonized block ends here! if the sleep is
			// synchronized, the remove method will never acquire the
			// resource
			// find out how long we have to sleep for the next update
			long nextUpdateTime = Long.MAX_VALUE;
			for (Long update : updates) {
				nextUpdateTime = Math.min(nextUpdateTime, update);
			}

			try {
				long time = nextUpdateTime - System.currentTimeMillis();
				System.out.println("Thread Id " + this.getId()
						+ " I m going to sleep for " + time);
				NotifierThread.this.sleep(time);

			} catch (InterruptedException e) {
			}

		}
	}

	/*
	 * This method finds out if we have to fire an event
	 */

	private void checkKeys(Map mapValues, int i) {
		String hostname = resource.getResourceName();
		GlobusHardwareResource matchedResource = (GlobusHardwareResource) broker
				.findResourceByName(hostname);
		Integer moreThen = (Integer) mapValues.get("moreThan");
		Integer lessThen = (Integer) mapValues.get("lessThan");
		Integer value = Integer.parseInt(matchedResource
				.getResourceDescription().getResourceAttribute(
						metrics.get(i).getDefinition().getMetricName())
				.toString());

		if (moreThen != null && lessThen != null) {// The user asked for a
													// range value
			if (value > moreThen && value < lessThen) {
				// I have to fire the metric
				System.out.println("Input less Than Value: "
						+ lessThen.intValue() + " - Input more Then Value: "
						+ moreThen.intValue() + " - Resource's Value: "
						+ value.intValue());
				MetricEvent event = new MetricEvent(resource, value, metrics
						.get(i), System.currentTimeMillis());
				resource.fireMetric(event);
			}
		} else {
			if (moreThen != null) {
				System.out.println("Input more Than Value: "
						+ moreThen.intValue() + " - Resource's Value: "
						+ value.intValue());
				if (moreThen.intValue() < value.intValue()) {
					// I have to fire the metric
					MetricEvent event = new MetricEvent(resource, value,
							metrics.get(i), System.currentTimeMillis());
					resource.fireMetric(event);

				}
			}
			if (lessThen != null) {
				System.out.println("Input less Then Value: "
						+ lessThen.intValue() + " - Resource's Value: "
						+ value.intValue());
				if (lessThen.intValue() > value.intValue()) {
					// I have to fire the metric
					MetricEvent event = new MetricEvent(resource, value,
							metrics.get(i), System.currentTimeMillis());
					resource.fireMetric(event);

				}
			}
		}

	}

}