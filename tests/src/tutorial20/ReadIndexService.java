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
		//hd.addResourceAttribute("CPU_SPEED", 2300);
		hd.addResourceAttribute("memory.size.available", 1000);
		// hd.addResourceAttribute("MEMORY_AVAILABLE", 1900);
	//	hd.addResourceAttribute("CPU_COUNT", 4);

		List<HardwareResource> resources = broker.findResources(hd);
		for (int i = 0; i < resources.size(); i++)
			System.out.println(resources.get(i));
				
		for (int i = 0; i < resources.size(); i++) {
			Map<String, Object> p = new HashMap<String, Object>();
			Map<String, Object> p1 = new HashMap<String, Object>();
			p.put("minimum", 950);
			p1.put("maximum", 19000);
			Metric metric = resources.get(i).getMetricDefinitionByName(
					"memory.size.available").createMetric(p, 5*62000);
			Metric metric1 = resources.get(i).getMetricDefinitionByName(
			"memory.virtual.size.available").createMetric(p1, 5*62000);
			try {
				resources.get(i).addMetricListener(new MetricListener() {
					public void processMetricEvent(MetricEvent e) {
						System.out.println("\n ----- evento memory catturato: "					
								+ e.getValue()+"\n"					
								);
						}
				}, metric);
				resources.get(i).addMetricListener(new MetricListener() {
					public void processMetricEvent(MetricEvent e) {
						System.out.println("\n ----- evento virtual memory catturato: "					
								+ e.getValue()+"\n"					
								);
						}
				}, metric1);
				
				
				
			} catch (GATInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
}
