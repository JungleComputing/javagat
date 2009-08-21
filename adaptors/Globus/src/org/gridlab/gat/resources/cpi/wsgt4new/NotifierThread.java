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
	
	private boolean death=false;

	public NotifierThread(GlobusHardwareResource resource, WSGT4newResourceBrokerAdaptor broker) {
		this.broker = broker;
		this.resource = resource;
		setDaemon(true);
	}

	public synchronized void add(Metric metric, MetricListener listener) {
		
		updates.add(System.currentTimeMillis()); // this add is the first coz there can be problem at if at line 64
		metrics.add(metric);
		metricListeners.add(listener);
		
		notifyAll();
	}

	public synchronized void remove(Metric metric, MetricListener metricListener) {
		for (int i = 0; i < metrics.size(); i++) {
			if (metrics.get(i).equals(metric)
					&& metricListeners.get(i).equals(metricListener)) {
				metrics.remove(i);
				metricListeners.remove(i);
				updates.remove(i);
			}
		}
		
       if(metrics.size()==0){//all the metrics have been removed
    	   this.death=true;
       System.out.println("Notifier Thread Killed");
       }
	}

	public void run() {
		while (!this.death) {
			// get the results
			for (int i = 0; i < metricListeners.size(); i++) {
				if (updates.get(i) <= System.currentTimeMillis()) {
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
				sleep(time);			
			    
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