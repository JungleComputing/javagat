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
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class QueryIndexService {

	public static void main(String[] args) throws Exception {
		SoftwareDescription sd = new SoftwareDescription();
		sd.setExecutable("/bin/hostname");
		File stdout = GAT.createFile("hostname.txt");
		sd.setStdout(stdout);

		Preferences preferences = new Preferences();
		preferences.put("resourcebroker.adaptor.name", "wsgt4new"); // "gt42"wsgt4new

		ResourceBroker broker = GAT.createResourceBroker(preferences, new URI(
				args[0]));
		HardwareResourceDescription hd = new HardwareResourceDescription();
		//hd.addResourceAttribute("cpu.speed", 2411);
		hd.addResourceAttribute("memory.size.available", 1800);
		
		List<HardwareResource> resources = broker.findResources(hd);
		for (int i = 0; i < resources.size(); i++)
			System.out.println(resources.get(i));

		Metric metric = null;
		Metric metric1 = null;
		MetricListener ml = new MetricListener() {
			public void processMetricEvent(MetricEvent e) {
				if (e.getMetric().getDefinition().getMetricName().equals(
						"memory.size.available"))
					System.out
							.println("\n ----->  memory event caught, value: "
									+ e.getValue() + " <-----\n");
				else if (e.getMetric().getDefinition().getMetricName().equals(
						"processor.load.5min"))
					System.out
							.println("\n -----> processor event load caught, value: "
									+ e.getValue() + " <-----\n");
			}
		};

		for (int i = 0; i < resources.size(); i++) {
			Map<String, Object> metricsValues1 = new HashMap<String, Object>();
			Map<String, Object> metricsValues2 = new HashMap<String, Object>();
			//I want to be notified when the value is more than 1800 and less than 2400
			metricsValues1.put("moreThan", 1800);
			metricsValues1.put("lessThan", 2400);

			metricsValues2.put("moreThan", 10);
			metric = resources.get(i).getMetricDefinitionByName(
					"memory.size.available").createMetric(metricsValues1,
					5*60000);
			metric1 = resources.get(i).getMetricDefinitionByName(
					"processor.load.5min").createMetric(metricsValues2,
					5*60000);
			try {
				resources.get(i).addMetricListener(ml, metric);
				resources.get(i).addMetricListener(ml, metric1);

			} catch (GATInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Thread.sleep(15*62000);
		
		
		
		for (int i = 0; i < resources.size(); i++) {
			resources.get(i).removeMetricListener(ml, metric);
			resources.get(i).removeMetricListener(ml, metric1);
		}

		boolean end = false;
		while (!end) {
			System.out.println("I m gonna die!!");
			Thread.sleep(5000);			
			end = true;
		}
	}
}
