package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.Monitorable;

import java.util.List;

public class ListMetricDefinitions {
    public static void main(String[] args) throws Exception {
        GATContext c = new GATContext();
        c.addPreference("monitoring.adaptor.name", "mercury");

        Monitorable m = GAT.createMonitorable(c);

        List definitions = m.getMetricDefinitions();

        System.err.println("found " + definitions.size() + " definitions");

        for (int i = 0; i < definitions.size(); i++) {
            System.err.println(definitions.get(i));
        }
    }
}
