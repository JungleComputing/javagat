package org.gridlab.gat.resources.gt4;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.jdom.Attribute;
import org.jdom.Element;

public class GlobusHardwareResource implements HardwareResource{

	/*Ci devo mettere anche tutti gli i campi di HardwareResourceDescription??
	 * 
	 * */
	
	
	private HardwareResourceDescription description= new HardwareResourceDescription(); 
	
	
	/*private String CPU_CACHE_L1;
	
	private String CPU_CACHE_L1D;
	
	private String CPU_CACHE_L1I;
	
	private String CPU_CACHE_L2;
	
	private String AVAILABLE_MEMORY;
	
	private String VIRTUAL_MEMORY_SIZE;
	
	private String VIRTUAL_AVAILABLE__MEMORY_SIZE;
	
	private String FILE_SYSTEM_ROOT;
	
	private boolean FILE_SYSTEM_READ_ONLY;
	
	private String IP_ADDRESS;
	
	private boolean INBOUND_IP;
	
	private boolean OUTBOUND_IP;
	*/
	


	public GlobusHardwareResource(Element host) {
		List hostParameters=host.getChildren();
		description.addResourceAttribute(host.getName(), host.getValue());
		List hostAttributes=host.getAttributes();
		Iterator<Attribute> it3=hostAttributes.iterator();
		while(it3.hasNext()){
		Attribute att3=it3.next();
		description.addResourceAttribute(att3.getName(), att3.getValue());
		//System.out.println(att3.getName()+" Value: "+att3.getValue());
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
//capire bene cosa fa questo metodo
	public ResourceDescription getResourceDescription() {
		// TODO Auto-generated method stub
		return description;
	}

	public void addMetricListener(MetricListener metricListener, Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		
	}

	public MetricEvent getMeasurement(Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public MetricDefinition getMetricDefinitionByName(String name) throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<MetricDefinition> getMetricDefinitions() throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeMetricListener(MetricListener metricListener, Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		
	}

	public String marshal() {
		// TODO Auto-generated method stub
		return null;
	}
	public String toString(){
		Map p=description.getDescription();
		String hostName="HOST_NAME "+(String)p.get("HOST_NAME");
		String cpuSpeed="CPU_SPEED "+(String)p.get("CPU_SPEED");
		String cpuCount="CPU_COUNT "+(String)p.get("CPU_COUNT");
		String memoryAvailable="MEMORY_AVAILABLE "+(String)p.get("MEMORY_AVAILABLE");
		String availableDiskSize="AVAILABLE_DISK_SIZE "+(String)p.get("AVAILABLE_DISK_SIZE");
		
		return hostName+"\n"+cpuSpeed+"\n"+cpuCount+"\n"+memoryAvailable+"\n"+availableDiskSize+"\n";
				
	}
	

}
