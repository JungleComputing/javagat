package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.Monitorable;

import java.util.Date;
import java.util.HashMap;

public class GetMetric {
    public static void main(String[] args) throws Exception {
        GATContext c = new GATContext();
        c.addPreference("monitoring.adaptor.name", "mercury");

        Monitorable m = GAT.createMonitorable(c);

        MetricDefinition def = m.getMetricDefinitionByName("host.mem.free");

        HashMap params = new HashMap();
        params.put("host", args[0]);

        Metric metric = def.createMetric(params);
        MetricValue result = m.getMeasurement(metric);
        System.err.println("Free memory at " + new Date(result.getEventTime())
            + " was: " + result.getValue());
    }
}
