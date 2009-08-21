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

public class ReadIndexService {

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
		//hd.addResourceAttribute("CPU_SPEED", 2300);
		hd.addResourceAttribute("memory.size.available", 2800);
		// hd.addResourceAttribute("MEMORY_AVAILABLE", 1900);
	//	hd.addResourceAttribute("CPU_COUNT", 4);

		List<HardwareResource> resources = broker.findResources(hd);
		for (int i = 0; i < resources.size(); i++)
			System.out.println(resources.get(i));		
		
		Metric metric=null;
		Metric metric1=null;
		MetricListener ml=new MetricListener() {
			public void processMetricEvent(MetricEvent e) {
				System.out.println("\n ----- evento memory catturato: "					
						+ e.getValue()+"\n"					
						);
				}
		};
		MetricListener ml1=new MetricListener() {
			public void processMetricEvent(MetricEvent e) {
				System.out.println("\n ----- evento processor load catturato: "					
						+ e.getValue()+"\n"					
						);
				}
		};
		
		for (int i = 0; i < resources.size(); i++) {
			Map<String, Object> p = new HashMap<String, Object>();
			Map<String, Object> p1 = new HashMap<String, Object>();
			p.put("minimum", 2750);
			p1.put("maximum", 10);
			
			//E' il caso di mettere un ritardo standard nel thread della metrica
			// per evitare che il querying thred non faccia in tempo ad aggiornare i dati
			//prima di essere letti dal notifier thread?? oppure si puo ridorre a meno di 5 minuti 
			//lupdate. bisogna vedere se prende i dati aggiornati
			 metric = resources.get(i).getMetricDefinitionByName(
					"memory.size.available").createMetric(p, 5*60000);
			 metric1 = resources.get(i).getMetricDefinitionByName(
			"processor.load.5min").createMetric(p1, 5*60000);
			try {
				resources.get(i).addMetricListener(ml, metric);
				resources.get(i).addMetricListener(ml1, metric1);			
				
			} catch (GATInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
		Thread.sleep(7000);
	/*	
		for (int i = 0; i < resources.size(); i++) {			
		resources.get(i).removeMetricListener(ml, metric);
		resources.get(i).removeMetricListener(ml1, metric1);
		}
	*/
	boolean end=false;
	while(!end){
	  Thread.sleep(11*60000);
	  System.out.println("I m gonna die!!");
	  end=true;
	}
	}
}
