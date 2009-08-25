package org.gridlab.gat.resources.gt4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;
import org.gridlab.gat.resources.cpi.wsgt4new.MetricFrequencyException;
import org.gridlab.gat.resources.cpi.wsgt4new.NotifierThread;
import org.gridlab.gat.resources.cpi.wsgt4new.WSGT4newResourceBrokerAdaptor;
import org.jdom.Attribute;
import org.jdom.Element;

public class GlobusHardwareResource extends HardwareResourceCpi {

	private HardwareResourceDescription description = new HardwareResourceDescription();

	private MetricDefinition diskSizeAvailableDefinition;
	
	private MetricDefinition memorySizeAvailableDefinition;
	
	private MetricDefinition memoryVirtualSizeAvailableDefinition;
	
	private MetricDefinition diskReadOnlyDefinition;
	
	private MetricDefinition processorLoadLast1MinDefinition;
	
	private MetricDefinition processorLoadLast5MinDefinition;
	
	private MetricDefinition processorLoadLast15MinDefinition;
	
	private NotifierThread notifierThread;
	
	private WSGT4newResourceBrokerAdaptor broker;

	/*
	 * The Globus Hardware Resource is created from a JDOM Element
	 * */
	public GlobusHardwareResource(Element host, WSGT4newResourceBrokerAdaptor broker) {
		super(null);// it needs to invoke the superclass's constructor
		this.broker = broker;
		createReturnDefinitions();		
		List hostParameters = host.getChildren();
		description.addResourceAttribute(host.getName(), host.getValue());
		List hostAttributes = host.getAttributes();
		Iterator<Attribute> it3 = hostAttributes.iterator();
		while (it3.hasNext()) {
			Attribute att3 = it3.next();
			description.addResourceAttribute(att3.getName(), att3.getValue());
			Iterator<Element> it1 = hostParameters.iterator();
			while (it1.hasNext()) {
				Element parameter = it1.next();
				List attributes = parameter.getAttributes();
				Iterator<Attribute> it2 = attributes.iterator();
				while (it2.hasNext()) {
					Attribute att = it2.next();
					description.addResourceAttribute(att.getName(), att
							.getValue());

				}
			}
		}
	}

	
	/*
	 * The method creates the return definition that are needed for to implement 
	 * the metric listeners
	 * */
	private void createReturnDefinitions() {
		
		HashMap<String, Object> memorySizeAvailableReturnDefinition = new HashMap<String, Object>();
		memorySizeAvailableReturnDefinition.put("memory.size.available",
				Integer.class);
		memorySizeAvailableDefinition = new MetricDefinition(
				"memory.size.available", MetricDefinition.CONTINUOUS, null, "MB",
				null, memorySizeAvailableReturnDefinition);
		registerMetric("getAvailableMemorySize", memorySizeAvailableDefinition);
		
		HashMap<String, Object> memoryVirtualSizeAvailableReturnDefinition = new HashMap<String, Object>();
		memoryVirtualSizeAvailableReturnDefinition.put("memory.virtual.size.available",
				Integer.class);
		memoryVirtualSizeAvailableDefinition = new MetricDefinition(
				"memory.virtual.size.available", MetricDefinition.CONTINUOUS, null, "MB",
				null, memoryVirtualSizeAvailableReturnDefinition);
		registerMetric("getAvailableVirtualMemorySize", memoryVirtualSizeAvailableDefinition);
		
		HashMap<String, Object> diskSizeAvailableReturnDefinition = new HashMap<String, Object>();
		diskSizeAvailableReturnDefinition.put("disk.size.available",
				Integer.class);
		diskSizeAvailableDefinition = new MetricDefinition(
				"disk.size.available", MetricDefinition.CONTINUOUS, null, "MB",
				null, diskSizeAvailableReturnDefinition);
		registerMetric("getAvailableDiskSize", diskSizeAvailableDefinition);
	 
		HashMap<String, Object> diskReadOnlyReturnDefinition = new HashMap<String, Object>();
		diskReadOnlyReturnDefinition.put("disk.readOnly",
				String.class);
		diskReadOnlyDefinition = new MetricDefinition(
				"disk.readOnly", MetricDefinition.CONTINUOUS, null, "",
				null, diskReadOnlyReturnDefinition);
		registerMetric("getAvailableDiskSize", diskReadOnlyDefinition);
		
		HashMap<String, Object> processorLoadLast1MinReturnDefinition = new HashMap<String, Object>();
		processorLoadLast1MinReturnDefinition.put("processor.load.1min",
				Integer.class);
		processorLoadLast1MinDefinition = new MetricDefinition(
				"processor.load.1min", MetricDefinition.CONTINUOUS, null, "",
				null, processorLoadLast1MinReturnDefinition);
		registerMetric("getProcessorLoadLast1Min", processorLoadLast1MinDefinition);
		
		HashMap<String, Object> processorLoadLast5MinReturnDefinition = new HashMap<String, Object>();
		processorLoadLast5MinReturnDefinition.put("processor.load.5min",
				Integer.class);
		processorLoadLast5MinDefinition = new MetricDefinition(
				"processor.load.5min", MetricDefinition.CONTINUOUS, null, "",
				null, processorLoadLast5MinReturnDefinition);
		registerMetric("getProcessorLoadLast5Min", processorLoadLast5MinDefinition);
	
		HashMap<String, Object> processorLoadLast15MinReturnDefinition = new HashMap<String, Object>();
		processorLoadLast15MinReturnDefinition.put("processor.load.15min",
				Integer.class);
		processorLoadLast15MinDefinition = new MetricDefinition(
				"processor.load.15min", MetricDefinition.CONTINUOUS, null, "",
				null, processorLoadLast15MinReturnDefinition);
		registerMetric("getProcessorLoadLast15Min", processorLoadLast15MinDefinition);
	
	}

	public Reservation getReservation() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceDescription getResourceDescription() {

		return this.description;
	}

	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws GATInvocationException {
		if(metric.getFrequency()% (5*60000)!=0)
			throw new MetricFrequencyException("The frequency must be a multiple of 5 minutes");
		super.addMetricListener(metricListener, metric);
		if(!broker.queryingThreadIsCreated()){
	       	broker.startQueryingThread();
	       	System.out.println("\n Querying ThreadStarted\n");
		}
		if (notifierThread == null) {
			notifierThread = new NotifierThread(this, broker);
			
		}
		broker.addListenerToQueryingThread();
		notifierThread.add(metric, metricListener);
		if(!notifierThread.isAlive())
		notifierThread.start();
	}

	public synchronized void removeMetricListener(
			MetricListener metricListener, Metric metric)
			throws GATInvocationException {
		super.removeMetricListener(metricListener, metric);
		notifierThread.remove(metric, metricListener);
		broker.removeListenerToQueryingThread();
		}

	public String marshal() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getResourceName(){
		Map p = description.getDescription();
		return (String) p.get("NAME");
	}

	public String toString() {
		Map p = description.getDescription();
		 
		/*
		 Iterator it=p.keySet().iterator(); String output="";
		 while(it.hasNext()){ String s=(String) it.next(); Object o=p.get(s);
		  output+=s+" "+o+" "; }
		 return output;
			
		*/
		String hostName = "Host Name: " + (String) p.get("NAME");
		String cpuSpeed = "cpu.speed " + (String) p.get("cpu.speed");
		String cacheL1 = "cpu.cache.l1 " + (String) p.get("cpu.cache.l1");
		String cacheL1D = "cpu.cache.l1d " + (String) p.get("cpu.cache.l1d");
		String cacheL1I = "cpu.cache.l1i " + (String) p.get("cpu.cache.l1i");
		String cacheL2 = "cpu.cache.l2 " + (String) p.get("cpu.cache.l2");
		String cpuCount = "cpu.count " + (String) p.get("cpu.count");

		String memorySize = "memory.size " + (String) p.get("memory.size");
		String memoryAvailable = "memory.size.available "
				+ (String) p.get("memory.size.available");
		String virtualAvailable = "memory.virtual.size.available "
				+ (String) p.get("memory.virtual.size.available");
		String virtualSize = "memory.virtual.size "
				+ (String) p.get("memory.virtual.size");

		String diskSize = "disk.size " + (String) p.get("disk.size");
		String availableDiskSize = "disk.size.available "
				+ (String) p.get("disk.size.available");
		String readOnlyDisk = "disk.readonly "
				+ (String) p.get("disk.readonly");
		String diskRoot = "disk.root " + (String) p.get("disk.root");

		String ipAddress = "network.ip " + (String) p.get("network.ip");
		String inboundIP = "network.inboundip " + (String) p.get("network.inboundip");
		String outboundIP = "network.outboundip " + (String) p.get("network.outboundip");
		String MTU = "network.mtu " + (String) p.get("network.mtu");

		String prLoad1 = "processor.load.1min "
				+ (String) p.get("processor.load.1min");
		String prLoad5 = "processor.load.5min "
				+ (String) p.get("processor.load.5min");
		String prLoad15 = "processor.load.15min "
				+ (String) p.get("processor.load.15min");

		return hostName + "\n" + cpuSpeed + "\n" + cpuCount + "\n" + cacheL1
				+ "\n" + cacheL1D + "\n" + cacheL1I + "\n" + cacheL2 + "\n"
				+ memorySize + " \n" + memoryAvailable + "\n" + virtualAvailable
				+ "\n" + virtualSize + "\n" + diskSize + "\n"
				+ availableDiskSize + "\n" + readOnlyDisk + "\n" + diskRoot
				+ "\n" + ipAddress + "\n" + inboundIP + "\n" + outboundIP
				+ "\n" + MTU + "\n" + prLoad1 + "\n" + prLoad5 + "\n"
				+ prLoad15 + "\n";

	}

}
