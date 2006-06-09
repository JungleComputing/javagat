package monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.Monitorable;

public class SimpleTest1 {
    public static void main(String[] args) {
        GATContext c = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("monitoring.adaptor.name", "mercury");

        try {
            // get a default Monitorable object:
            Monitorable m = GAT.createMonitorable(c, prefs);

            // query Monitorable object:
            List metricDefinitions = m.getMetricDefinitions();

            System.out.println("metricDefinitions.size() = "
                + metricDefinitions.size());

            Map params = new HashMap();

            params.put("host", "fs0.das2.cs.vu.nl");

            //params.put("cpu", new Integer(0));
            for (int i = 0; i < metricDefinitions.size(); i++)
                try {
                    System.out.println("Metric "
                        + ((MetricDefinition) metricDefinitions.get(i))
                            .getMetricName()
                        + ": "
                        + m.getMeasurement(
                            new Metric((MetricDefinition) metricDefinitions
                                .get(i), params, 0)).getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
