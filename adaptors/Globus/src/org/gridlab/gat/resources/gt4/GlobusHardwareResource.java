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
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;
import org.gridlab.gat.resources.cpi.wsgt4new.MetricFrequencyException;
import org.gridlab.gat.resources.cpi.wsgt4new.NotifierThread;
import org.gridlab.gat.resources.cpi.wsgt4new.WSGT4newResourceBrokerAdaptor;
import org.jdom.Attribute;
import org.jdom.Element;

public class GlobusHardwareResource extends HardwareResourceCpi {

	private HardwareResourceDescription description = new HardwareResourceDescription();

	// private MetricListener metricListener=null;
	// private Metric metric=null;

	private MetricDefinition diskSizeDefinition;

	private MetricDefinition diskSizeAvailableDefinition;
	
	private MetricDefinition memorySizeAvailableDefinition;
	
	private MetricDefinition memoryVirtualSizeAvailableDefinition;
	
	private MetricDefinition cpuSpeedDefinition;
	
	private MetricDefinition cpuCountDefinition;
	
	private MetricDefinition cpuCacheL1Definition;
	
	private MetricDefinition cpuCacheL1DDefinition;
	
	private MetricDefinition cpuCacheL1IDefinition;
	
	private MetricDefinition cpuCacheL2Definition;
	
	private MetricDefinition memorySizeDefinition;
	
	private MetricDefinition memoryVirtualSizeDefinition;
	
	private MetricDefinition osNameDefinition;
	
	private MetricDefinition osReleaseDefinition;
	
	private MetricDefinition osTypeDefinition;
	
	private MetricDefinition diskReadOnlyDefinition;
	
	private MetricDefinition diskRootDefinition;
	
	private MetricDefinition networkIpDefinition;
	
	private MetricDefinition networkInboundIpDefinition;
	
	private MetricDefinition networkOutboundIpDefinition;
	
	private MetricDefinition networkMtuDefinition;
	
	private MetricDefinition processorLoadLast1MinDefinition;
	
	private MetricDefinition processorLoadLast5MinDefinition;
	
	private MetricDefinition processorLoadLast15MinDefinition;
	
	private NotifierThread notifierThread;
	
	private WSGT4newResourceBrokerAdaptor broker;

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

	private void createReturnDefinitions() {
		/*
		HashMap<String, Object> cpuSpeedReturnDefinition = new HashMap<String, Object>();
		cpuSpeedReturnDefinition.put("cpu.speed", Integer.class);
		cpuSpeedDefinition= new MetricDefinition("cpu.speed",
				MetricDefinition.CONTINUOUS, null, "MHZ", null,
				cpuSpeedReturnDefinition);
		registerMetric("getCpuSpeed", cpuSpeedDefinition);
		*/
		
		/*
		HashMap<String, Object> cpuCountReturnDefinition = new HashMap<String, Object>();
		cpuCountReturnDefinition.put("cpu.count", Integer.class);
	     cpuCountDefinition= new MetricDefinition("cpu.count",
				MetricDefinition.CONTINUOUS, null, "", null,
				cpuCountReturnDefinition);
		registerMetric("getCpuCount", cpuCountDefinition);
		*/
		/*
		HashMap<String, Object> cacheL1ReturnDefinition = new HashMap<String, Object>();
		cacheL1ReturnDefinition.put("cpu.cacheL1", Integer.class);
	    cpuCacheL1Definition= new MetricDefinition("cpu.cacheL1",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				cacheL1ReturnDefinition);
		registerMetric("getCpuCacheL1", cpuCacheL1Definition);
		
		HashMap<String, Object> cacheL1DReturnDefinition = new HashMap<String, Object>();
		cacheL1DReturnDefinition.put("cpu.cacheL1D", Integer.class);
	    cpuCacheL1DDefinition= new MetricDefinition("cpu.cacheL1D",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				cacheL1DReturnDefinition);
		registerMetric("getCpuCacheL1D", cpuCacheL1DDefinition);
		
		HashMap<String, Object> cacheL2ReturnDefinition = new HashMap<String, Object>();
		cacheL2ReturnDefinition.put("cpu.cacheL2", Integer.class);
	    cpuCacheL2Definition= new MetricDefinition("cpu.cacheL2",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				cacheL2ReturnDefinition);
		registerMetric("getCpuCacheL2", cpuCacheL2Definition);
		
		HashMap<String, Object> cacheL1IReturnDefinition = new HashMap<String, Object>();
		cacheL1IReturnDefinition.put("cpu.cacheL1I", Integer.class);
	    cpuCacheL1IDefinition= new MetricDefinition("cpu.cacheL1I",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				cacheL1IReturnDefinition);
		registerMetric("getCpuCacheL1I", cpuCacheL1IDefinition);
		
		HashMap<String, Object> memorySizeReturnDefinition = new HashMap<String, Object>();
		memorySizeReturnDefinition.put("memory.size", Integer.class);
	    memorySizeDefinition= new MetricDefinition("memory.size",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				memorySizeReturnDefinition);
		registerMetric("getMemorySize", memorySizeDefinition);
		*/
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
		/*	
		HashMap<String, Object> memoryVirtualSizeReturnDefinition = new HashMap<String, Object>();
		memoryVirtualSizeReturnDefinition.put("memory.virtual.size",
				Integer.class);
		memoryVirtualSizeDefinition = new MetricDefinition(
				"memory.virtual.size", MetricDefinition.CONTINUOUS, null, "MB",
				null, memoryVirtualSizeReturnDefinition);
		registerMetric("getVirtualMemorySize", memoryVirtualSizeDefinition);
			
		HashMap<String, Object> osNameReturnDefinition = new HashMap<String, Object>();
		osNameReturnDefinition.put("os.name",
				String.class);
		osNameDefinition = new MetricDefinition(
				"os.name", MetricDefinition.CONTINUOUS, null, "",
				null, osNameReturnDefinition);
		registerMetric("getOsName", osNameDefinition);
		
		HashMap<String, Object> osReleaseReturnDefinition = new HashMap<String, Object>();
		osReleaseReturnDefinition.put("os.release",
				String.class);
		osReleaseDefinition = new MetricDefinition(
				"os.release", MetricDefinition.CONTINUOUS, null, "",
				null, osReleaseReturnDefinition);
		registerMetric("getOsRelease", osReleaseDefinition);
		
		HashMap<String, Object> osTypeReturnDefinition = new HashMap<String, Object>();
		osTypeReturnDefinition.put("os.type",
				String.class);
		osTypeDefinition = new MetricDefinition(
				"os.type", MetricDefinition.CONTINUOUS, null, "",
				null, osTypeReturnDefinition);
		registerMetric("getOsType", osTypeDefinition);		
		
		HashMap<String, Object> diskSizeReturnDefinition = new HashMap<String, Object>();
		diskSizeReturnDefinition.put("disk.size", Integer.class);
		diskSizeDefinition = new MetricDefinition("disk.size",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				diskSizeReturnDefinition);
		registerMetric("getDiskSize", diskSizeDefinition);
        */
		HashMap<String, Object> diskSizeAvailableReturnDefinition = new HashMap<String, Object>();
		diskSizeAvailableReturnDefinition.put("disk.size.available",
				Integer.class);
		diskSizeAvailableDefinition = new MetricDefinition(
				"disk.size.available", MetricDefinition.CONTINUOUS, null, "MB",
				null, diskSizeAvailableReturnDefinition);
		registerMetric("getAvailableDiskSize", diskSizeAvailableDefinition);
	 
		//inserire un altra condizione oltre maximum e minumum??
		HashMap<String, Object> diskReadOnlyReturnDefinition = new HashMap<String, Object>();
		diskReadOnlyReturnDefinition.put("disk.readOnly",
				Boolean.class);
		diskReadOnlyDefinition = new MetricDefinition(
				"disk.readOnly", MetricDefinition.CONTINUOUS, null, "",
				null, diskReadOnlyReturnDefinition);
		registerMetric("getAvailableDiskSize", diskReadOnlyDefinition);
		/*
		HashMap<String, Object> diskRootReturnDefinition = new HashMap<String, Object>();
		diskRootReturnDefinition.put("disk.root",
				String.class);
		diskRootDefinition = new MetricDefinition(
				"disk.root", MetricDefinition.CONTINUOUS, null, "",
				null, diskReadOnlyReturnDefinition);
		registerMetric("getAvailableDiskSize", diskRootDefinition);
		
		HashMap<String, Object> networkIPReturnDefinition = new HashMap<String, Object>();
		networkIPReturnDefinition.put("network.ip",
				String.class);
		networkIpDefinition = new MetricDefinition(
				"network.ip", MetricDefinition.CONTINUOUS, null, "",
				null, networkIPReturnDefinition);
		registerMetric("getNetworkIp", networkIpDefinition);
		
		HashMap<String, Object> networkInboundIPReturnDefinition = new HashMap<String, Object>();
		networkInboundIPReturnDefinition.put("network.inboundip",
				Boolean.class);
		networkInboundIpDefinition = new MetricDefinition(
				"network.inboundip", MetricDefinition.CONTINUOUS, null, "",
				null, networkInboundIPReturnDefinition);
		registerMetric("getNetworkInboundIp", networkInboundIpDefinition);
		
		HashMap<String, Object> networkOutboundIPReturnDefinition = new HashMap<String, Object>();
		networkOutboundIPReturnDefinition.put("network.outboundip",
				Boolean.class);
		networkOutboundIpDefinition = new MetricDefinition(
				"network.outboundip", MetricDefinition.CONTINUOUS, null, "",
				null, networkOutboundIPReturnDefinition);
		registerMetric("getNetworkOutboundIp", networkOutboundIpDefinition);
		
		HashMap<String, Object> networkMtuReturnDefinition = new HashMap<String, Object>();
		networkMtuReturnDefinition.put("network.mtu",
				Integer.class);
		networkMtuDefinition = new MetricDefinition(
				"network.mtu", MetricDefinition.CONTINUOUS, null, "",
				null, networkMtuReturnDefinition);
		registerMetric("getNetworkMtu", networkMtuDefinition);
		*/
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
			throw new MetricFrequencyException("The frequency must be a multiple of 300000 milliseconds");
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

	public String toString() {// Attenzione: SMPSize e' il CPUCOUNT??
		Map p = description.getDescription();
		 
		/*
		 Iterator it=p.keySet().iterator(); String output="";
		 while(it.hasNext()){ String s=(String) it.next(); Object o=p.get(s);
		  output+=s+" "+o+" "; }
		 return output;
			
		*/
		String hostName = "HOST_NAME " + (String) p.get("NAME");
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
