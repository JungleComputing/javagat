package examples20;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.Monitorable;

public class MonitorableExample {

    /**
     * @param args
     * @throws GATInvocationException
     * @throws GATObjectCreationException
     */
    public static void main(String[] args) throws GATObjectCreationException,
            GATInvocationException {
        new MonitorableExample().start();
        GAT.end();
    }

    public void start() throws GATObjectCreationException,
            GATInvocationException {
        Monitorable monitorable = GAT.createMonitorable();
        for (MetricDefinition m : monitorable.getMetricDefinitions()) {
            System.out.println(m);
        }
    }
}
