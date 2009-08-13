package org.gridlab.gat.resources.gt4;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.wsgt4new.NotifierThread;
import org.jdom.Attribute;
import org.jdom.Element;

public class GlobusHardwareResource implements HardwareResource{
	
	private HardwareResourceDescription description= new HardwareResourceDescription(); 
	
	//private MetricListener metricListener=null;
	//private Metric metric=null;
	private List<MetricDefinition> listMetricDefinition=null;
	
	public GlobusHardwareResource(Element host) {
		List hostParameters=host.getChildren();
		description.addResourceAttribute(host.getName(), host.getValue());
		List hostAttributes=host.getAttributes();
		Iterator<Attribute> it3=hostAttributes.iterator();
		while(it3.hasNext()){
		Attribute att3=it3.next();
		description.addResourceAttribute(att3.getName(), att3.getValue());
		Iterator<Element> it1=hostParameters.iterator();
		while(it1.hasNext()){
			Element parameter=it1.next();
			List attributes=parameter.getAttributes();
			Iterator<Attribute> it2=attributes.iterator();
			while(it2.hasNext()){
				Attribute att=it2.next();	
				String attribute=att.getName();
				//System.out.println(att.getName()+" Value: "+att.getValue());
				description.addResourceAttribute(att.getName(), att.getValue());
			
			}
		}}
		}
	
	

	public Reservation getReservation() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceDescription getResourceDescription() {
		
		return this.description;
	}

	public void addMetricListener( MetricListener metricListener,  Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
	/*	this.listener = metricListener;
		this.metric = metric;
		// start a thread to monitor the metric
		new Thread() {
			public void run() {
				while (true) {
					sleep(interval);
					updateInfo();
					listener.processMetricEvent();
				}
			}
		}
		*/
		//this.metricListener = metricListener;
		//this.metric = metric;
		NotifierThread notifier=new NotifierThread(this,metricListener,metric);
	    notifier.start();
	    		
	}


	
	
	
	public MetricEvent getMeasurement(Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public MetricDefinition getMetricDefinitionByName(String name) throws GATInvocationException {
		for(int i=0;i<this.listMetricDefinition.size();i++){
			if(this.listMetricDefinition.get(i).getMetricName().equals(name))
				return listMetricDefinition.get(i);
		}
		return null;
	}

	public List<MetricDefinition> getMetricDefinitions() throws GATInvocationException {
		 
		return this.listMetricDefinition;
	}

	public void removeMetricListener(MetricListener metricListener, Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		
	}

	public String marshal() {
		// TODO Auto-generated method stub
		return null;
	}
	public String toString(){// Attenzione: SMPSize e' il CPUCOUNT??
		Map p=description.getDescription();
	/*
		Iterator it=p.keySet().iterator();
		String output="";
		while(it.hasNext()){
			String s=(String) it.next();
			Object o=p.get(s);
			output+=s+"  "+o+" ";
			
		}
		return output;
	*/
	    String hostName="HOST_NAME "+(String)p.get("NAME");
		String cpuSpeed="CPU_SPEED "+(String)p.get("CPU_SPEED");
		String cacheL1="CACHE_L1 "+(String)p.get("CACHE_L1");
		String cacheL1D="CACHE_L1D "+(String)p.get("CACHE_L1D");
		String cacheL1I="CACHE_L1I "+(String)p.get("CACHE_L1I");
		String cacheL2="CACHE_L2 "+(String)p.get("CACHE_L2");
		String cpuCount="CPU_COUNT "+(String)p.get("CPU_COUNT");
		
		String memorySize="MEMORY_SIZE "+(String)p.get("MEMORY_SIZE");
		String memoryAvailable="MEMORY_AVAILABLE "+(String)p.get("MEMORY_AVAILABLE");
		String virtualAvailable="VIRTUAL_AVAILABLE_MEMORY "+(String)p.get("VIRTUAL_AVAILABLE_MEMORY");
		String virtualSize="VIRTUAL_MEMORY_SIZE "+(String)p.get("VIRTUAL_MEMORY_SIZE");
				
		String diskSize="DISK_SIZE "+(String)p.get("DISK_SIZE");
		String availableDiskSize="AVAILABLE_DISK_SIZE "+(String)p.get("AVAILABLE_DISK_SIZE");
		String readOnlyDisk="READ_ONLY_DISK "+(String)p.get("READ_ONLY_DISK");
		String diskRoot="ROOT_DISK "+(String)p.get("DISK_ROOT");
		
		String ipAddress="IP_ADDRESS "+(String)p.get("IP_ADDRESS");
		String inboundIP="INBOUND_IP "+(String)p.get("INBOUND_IP");
		String outboundIP="OUTBOUND_IP "+(String)p.get("OUTBOUND_IP");
		String MTU="MTU "+(String)p.get("MTU");
		
		String prLoad1="PROCESSOR_LOAD_LAST_1_MIN "+(String)p.get("PROCESSOR_LOAD_LAST_1_MIN");
		String prLoad5="PROCESSOR_LOAD_LAST_5_MIN "+(String)p.get("PROCESSOR_LOAD_LAST_5_MIN");
		String prLoad15="PROCESSOR_LOAD_LAST_15_MIN "+(String)p.get("PROCESSOR_LOAD_LAST_15_MIN");
		
		return hostName+"\n"+cpuSpeed+"\n"+cpuCount+"\n"+cacheL1+"\n"+cacheL1D+"\n"
		+cacheL1I+"\n"+cacheL2+"\n"+memorySize+"\n"+memoryAvailable+"\n"+virtualAvailable+"\n"
		+virtualSize+"\n"+diskSize+"\n"+availableDiskSize+"\n"+readOnlyDisk+"\n"
		+diskRoot+"\n"+ipAddress+"\n"+inboundIP+"\n"+outboundIP+"\n"+MTU+"\n"+prLoad1
		+"\n"+prLoad5+"\n"+prLoad15+"\n";
		
	}
	

}
