package org.gridlab.gat.resources.cpi.wsgt4new;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.axis.message.MessageElement;
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
			//System.out.println("metric size of Thread "+this.getId()+" "+metricListeners.size());
			for (int i = 0; i < metricListeners.size(); i++) {
				// do we need to fire an event for this metric?
			//	System.out.println("sono dentro il for "+this.getId()+"  "+metricListeners.size());
				if (updates.get(i) <= System.currentTimeMillis()) {
		//			System.out.println("I have to check if it needs to fire a new Event  "+this.getId());
					// use the broker to get the new information!
									
					Map h=metrics.get(i).getMetricParameters();//prendere i valori della mappa e fare lo switch
					checkKeys(h,i);

                    //update the time when we need our next update
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
				System.out.println("Thread Id "+this.getId()+" I m going to sleep for "+time);
				if(time>0)//no negative values are allowed
				sleep(time);
				else sleep(5000);//standard sleep time
			    
			} catch (InterruptedException e) {
				}

		}
	}

	private void checkKeys(Map h,int i) {
		String hostname=resource.getResourceName();
		GlobusHardwareResource matchedResource=(GlobusHardwareResource) broker.findResourceByName(hostname);					
		
		if(h.containsKey("minimum")){
			Integer minimumValue= (Integer) h.get("minimum");
			matchedResource.getResourceName();
			Integer value=Integer.parseInt(matchedResource.getResourceDescription().getResourceAttribute(metrics.get(i).getDefinition().getMetricName()).toString());
		System.out.println("valore minimo: "+minimumValue.intValue()+" valore ottenuto:"+ value.intValue());
		if(minimumValue.intValue()>value.intValue()){
		//I have to fire the metric
		MetricEvent event = new MetricEvent(resource, value,
		metrics.get(i), System.currentTimeMillis());
		resource.fireMetric(event);
		
		          }
			}
		if(h.containsKey("maximum")){//h contains maximun
		Integer maximumValue= (Integer) h.get("maximum");
		matchedResource.getResourceName();
		Integer value=Integer.parseInt(matchedResource.getResourceDescription().getResourceAttribute(metrics.get(i).getDefinition().getMetricName()).toString());
		System.out.println("valore massimo: "+maximumValue.intValue()+" valore ottenuto:"+ value.intValue());
		if(maximumValue.intValue()<value.intValue()){
		//I have to fire the metric
		MetricEvent event = new MetricEvent(resource, value,
		metrics.get(i), System.currentTimeMillis());
		resource.fireMetric(event);
		
		          }	
		}
		
	}

}