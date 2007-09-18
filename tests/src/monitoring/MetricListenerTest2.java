package monitoring;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.Monitorable;

// This class can be used along with Mercury's monapptest command line client
public class MetricListenerTest2 implements MetricListener {
    public int times = 0;

    public void processMetricEvent(MetricValue val) {
        System.out.println("MetricListenerTest: Received metric event: " + val);
        times++;
    }

    public static void main(String[] args) {
        GATContext c = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("monitoring.adaptor.name", "mercury");

        MetricListenerTest2 listener = new MetricListenerTest2();

        try {
            // get a default Monitorable object:
            Monitorable m = GAT.createMonitorable(c, prefs);

            // spawn a server task:
            //new Thread(new Server()).start();
            // query Monitorable object:
            //List metricDefinitions = m.getMetricDefinitions();
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("jobid", "1000");

            /*
             params.put("jobid", "1000");

             MetricDefinition md = m.getMetricDefinitionByName("app.priv.Mercury-Example-Java-Appmonitor-1.processed");
             System.out.println("Metric definition of app.message:"+md);
             */
            MetricDefinition md = m.getMetricDefinitionByName("app.event");
            System.out.println("Metric definition of app.message:" + md);

            Metric metric = new Metric(md, params);

            //MetricValue result = m.getMeasurement(new Metric(m.getMetricDefinitionByName("host.cpu.number"), params, 0));
            //MetricValue result = m.getMeasurement(null);
            m.addMetricListener(listener, metric);

            while (listener.times < 10) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            m.removeMetricListener(listener, metric);

            //System.out.println(""+result.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
