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
					String attribute = att.getName();
					// System.out.println(att.getName()+" Value:
					// "+att.getValue());
					description.addResourceAttribute(att.getName(), att
							.getValue());

				}
			}
		}
	}

	private void createReturnDefinitions() {
		
		HashMap<String, Object> cpuSpeedReturnDefinition = new HashMap<String, Object>();
		cpuSpeedReturnDefinition.put("cpu.speed", Integer.class);
		cpuSpeedDefinition= new MetricDefinition("cpu.speed",
				MetricDefinition.CONTINUOUS, null, "MHZ", null,
				cpuSpeedReturnDefinition);
		registerMetric("getCpuSpeed", cpuSpeedDefinition);
		
		HashMap<String, Object> cpuCountReturnDefinition = new HashMap<String, Object>();
		cpuCountReturnDefinition.put("cpu.count", Integer.class);
	     cpuCountDefinition= new MetricDefinition("cpu.count",
				MetricDefinition.CONTINUOUS, null, "", null,
				cpuCountReturnDefinition);
		registerMetric("getCpuCount", cpuCountDefinition);
		
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
			
		HashMap<String, Object> memoryVirtualSizeReturnDefinition = new HashMap<String, Object>();
		memoryVirtualSizeReturnDefinition.put("memory.virtual.size",
				Integer.class);
		memoryVirtualSizeDefinition = new MetricDefinition(
				"memory.virtual.size", MetricDefinition.CONTINUOUS, null, "MB",
				null, memoryVirtualSizeReturnDefinition);
		registerMetric("getVirtualMemorySize", memoryVirtualSizeDefinition);
			
		HashMap<String, Object> diskSizeReturnDefinition = new HashMap<String, Object>();
		diskSizeReturnDefinition.put("disk.size", Integer.class);
		diskSizeDefinition = new MetricDefinition("disk.size",
				MetricDefinition.CONTINUOUS, null, "MB", null,
				diskSizeReturnDefinition);
		registerMetric("getDiskSize", diskSizeDefinition);

		HashMap<String, Object> diskSizeAvailableReturnDefinition = new HashMap<String, Object>();
		diskSizeAvailableReturnDefinition.put("disk.size.available",
				Integer.class);
		diskSizeAvailableDefinition = new MetricDefinition(
				"disk.size.available", MetricDefinition.CONTINUOUS, null, "MB",
				null, diskSizeAvailableReturnDefinition);
		registerMetric("getAvailableDiskSize", diskSizeAvailableDefinition);
	
		
		
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
		 * Iterator it=p.keySet().iterator(); String output="";
		 * while(it.hasNext()){ String s=(String) it.next(); Object o=p.get(s);
		 * output+=s+" "+o+" "; } return output;
		 */
		String hostName = "HOST_NAME " + (String) p.get("NAME");
		String cpuSpeed = "CPU_SPEED " + (String) p.get("CPU_SPEED");
		String cacheL1 = "CACHE_L1 " + (String) p.get("CACHE_L1");
		String cacheL1D = "CACHE_L1D " + (String) p.get("CACHE_L1D");
		String cacheL1I = "CACHE_L1I " + (String) p.get("CACHE_L1I");
		String cacheL2 = "CACHE_L2 " + (String) p.get("CACHE_L2");
		String cpuCount = "CPU_COUNT " + (String) p.get("CPU_COUNT");

		String memorySize = "MEMORY_SIZE " + (String) p.get("MEMORY_SIZE");
		String memoryAvailable = "memory.size.available"
				+ (String) p.get("memory.size.available");
		String virtualAvailable = "memory.virtual.size.available"
				+ (String) p.get("memory.virtual.size.available");
		String virtualSize = "VIRTUAL_MEMORY_SIZE "
				+ (String) p.get("VIRTUAL_MEMORY_SIZE");

		String diskSize = "DISK_SIZE " + (String) p.get("DISK_SIZE");
		String availableDiskSize = "AVAILABLE_DISK_SIZE "
				+ (String) p.get("AVAILABLE_DISK_SIZE");
		String readOnlyDisk = "READ_ONLY_DISK "
				+ (String) p.get("READ_ONLY_DISK");
		String diskRoot = "ROOT_DISK " + (String) p.get("DISK_ROOT");

		String ipAddress = "IP_ADDRESS " + (String) p.get("IP_ADDRESS");
		String inboundIP = "INBOUND_IP " + (String) p.get("INBOUND_IP");
		String outboundIP = "OUTBOUND_IP " + (String) p.get("OUTBOUND_IP");
		String MTU = "MTU " + (String) p.get("MTU");

		String prLoad1 = "PROCESSOR_LOAD_LAST_1_MIN "
				+ (String) p.get("PROCESSOR_LOAD_LAST_1_MIN");
		String prLoad5 = "PROCESSOR_LOAD_LAST_5_MIN "
				+ (String) p.get("PROCESSOR_LOAD_LAST_5_MIN");
		String prLoad15 = "PROCESSOR_LOAD_LAST_15_MIN "
				+ (String) p.get("PROCESSOR_LOAD_LAST_15_MIN");

		return hostName + "\n\n" + cpuSpeed + "\n" + cpuCount + "\n" + cacheL1
//				+ "\n" + cacheL1D + "\n" + cacheL1I + "\n" + cacheL2 + "\n"
				+ memorySize + " \n " + memoryAvailable + " \n " + virtualAvailable;
//				+ "\n" + virtualSize + "\n" + diskSize + "\n"
//				+ availableDiskSize + "\n" + readOnlyDisk + "\n" + diskRoot
//				+ "\n" + ipAddress + "\n" + inboundIP + "\n" + outboundIP
//				+ "\n" + MTU + "\n" + prLoad1 + "\n" + prLoad5 + "\n"
//				+ prLoad15 + "\n";

	}

}
