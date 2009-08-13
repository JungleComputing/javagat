package tutorial20;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class ReadIndexService {

	public static void main(String[] args) throws Exception {
		SoftwareDescription sd = new SoftwareDescription();
		sd.setExecutable("/bin/hostname");
		File stdout = GAT.createFile("hostname.txt");
		sd.setStdout(stdout);

		Preferences preferences = new Preferences();
		preferences.put("resourcebroker.adaptor.name", "wsgt4new"); // "gt42"wsgt4new

		JobDescription jd = new JobDescription(sd);
		ResourceBroker broker = GAT.createResourceBroker(preferences, new URI(
				args[0]));
		HardwareResourceDescription hd = new HardwareResourceDescription();
		hd.addResourceAttribute("CPU_SPEED", 2300);
		hd.addResourceAttribute("AVAILABLE_DISK_SIZE", 216000);
		// hd.addResourceAttribute("MEMORY_AVAILABLE", 1900);
		hd.addResourceAttribute("CPU_COUNT", 4);

		List<HardwareResource> resources = broker.findResources(hd);
		for (int i = 0; i < resources.size(); i++)
			System.out.println(resources.get(i));
		resources = broker.findResources(hd);
	//	for (int i = 0; i < resources.size(); i++)
	//		System.out.println(resources.get(i));
		
		Map p= new HashMap();    
	    p.put("MEMORY_AVAILABLE", 8000000);
	    MetricDefinition metricDefinition= 
	    	         new MetricDefinition("host.memory",2,"MB",null,null,null);
		Metric metric=metricDefinition.createMetric(p);
		for(int i=0; i<resources.size();i++){
			try {
				resources.get(i).addMetricListener(new MetricListener(){
					public void processMetricEvent(MetricEvent e){
				System.out.println("\n"+e.getValue()+"\n"+e.getSource()+" "
						+e.getMetric()+"\n"+"MEM AVAILABLE  "+e.getMetric().getMetricParameterByName("MEMORY_AVAILABLE"))		
					  ;
					}
				}, metric);
			} catch (GATInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}System.out.println("Metric listeners added");
		}
}
