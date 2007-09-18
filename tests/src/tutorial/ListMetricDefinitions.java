package tutorial;

import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.Monitorable;

public class ListMetricDefinitions {
    public static void main(String[] args) throws Exception {
        GATContext c = new GATContext();
        c.addPreference("monitoring.adaptor.name", "mercury");

        Monitorable m = GAT.createMonitorable(c);

        List<MetricDefinition> definitions = m.getMetricDefinitions();

        System.err.println("found " + definitions.size() + " definitions");

        for (int i = 0; i < definitions.size(); i++) {
            System.out.println(definitions.get(i));
        }
    }
}
