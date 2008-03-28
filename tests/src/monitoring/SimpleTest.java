package monitoring;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.Monitorable;

class Server implements Runnable {
    public void run() {
        int runs = 100;

        while ((runs--) > 0) {
            try {
                Thread.sleep(1000);
                System.out.println("SERVER: Running -> " + runs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class SimpleTest {
    public static void main(String[] args) {
        GATContext c = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("monitoring.adaptor.name", "mercury");

        try {
            // get a default Monitorable object:
            Monitorable m = GAT.createMonitorable(c, prefs);

            // spawn a server task:
            //new Thread(new Server()).start();
            // query Monitorable object:
            //List metricDefinitions = m.getMetricDefinitions();
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("host", "fs0.das2.cs.vu.nl");

            MetricEvent result = m.getMeasurement(new Metric(m
                .getMetricDefinitionByName("host.cpu.number"), params, 0));

            //MetricValue result = m.getMeasurement(null);
            System.out.println("" + result.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
