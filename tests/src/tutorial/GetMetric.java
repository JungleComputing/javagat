package tutorial;

import java.util.Date;
import java.util.HashMap;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.Monitorable;

public class GetMetric {
    public static void main(String[] args) throws Exception {
        GATContext c = new GATContext();
        c.addPreference("monitoring.adaptor.name", "mercury");

        Monitorable m = GAT.createMonitorable(c);

        MetricDefinition def = m.getMetricDefinitionByName("host.mem.free");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("host", args[0]);

        Metric metric = def.createMetric(params);
        MetricEvent result = m.getMeasurement(metric);
        System.err.println("Free memory at " + new Date(result.getEventTime())
            + " was: " + result.getValue());
    }
}
