//@@@ TODO:
// move classes to seperate files
// throw exceptions in some methods, like getMeasurement, don't print error
// otherwise, GAT cannot choose another adaptor
package org.gridlab.gat.monitoring.cpi.mercury;

import hu.sztaki.lpds.monitor.MonitorArg;
import hu.sztaki.lpds.monitor.MonitorConsumer;
import hu.sztaki.lpds.monitor.MonitorConsumer.CollectResult;
import hu.sztaki.lpds.monitor.MonitorConsumer.CommandResult;
import hu.sztaki.lpds.monitor.MonitorConsumer.MetricInstance;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

class Measuree {
    public MetricInstance metricInstance;

    public long timeLeft;

    public long frequency;

    public Measuree(MetricInstance metricInstance, long frequency) {
        this.metricInstance = metricInstance;
        this.frequency = frequency;
        this.timeLeft = frequency;
    }
}

class MeasurementTrigger extends Thread {
    long frequency = 0;

    Map<Metric, Measuree> measurees = java.util.Collections
            .synchronizedMap(new HashMap<Metric, Measuree>());

    boolean active = false;

    boolean stop = false;

    MonitorConsumer mc = null;

    public MeasurementTrigger(MonitorConsumer mc) {
        setDaemon(true);
        setName("Mercury Measurement Trigger Thread");
        this.mc = mc;
    }

    public synchronized void activate() {
        active = true;
        this.notifyAll();
    }

    public synchronized void deactivate() {
        active = false;
    }

    public synchronized void stopThread() {
        // uncollect measurees
        stop = true;
        this.notifyAll();
    }

    public synchronized boolean isStopped() {
        return stop;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void addMeasuree(Metric m, MetricInstance mi) {
        if (!measurees.containsKey(m)) {
            measurees.put(m, new Measuree(mi, m.getFrequency()));

            if (frequency == 0) {
                activate();
                frequency = m.getFrequency();
            } else {
                frequency = (new BigInteger(new Long(frequency).toString()))
                        .gcd(
                                new BigInteger(new Long(m.getFrequency())
                                        .toString())).intValue();
            }
        }
    }

    private long computeFrequency() {
        if (measurees.size() == 0) {
            frequency = 0;
        } else {
            Object[] metricsSet = measurees.keySet().toArray();
            BigInteger Frequency = new BigInteger(new Long(
                    ((Metric) metricsSet[0]).getFrequency()).toString());

            for (int i = 1; i < metricsSet.length; i++) {
                Frequency = Frequency.gcd(new BigInteger(new Long(
                        ((Metric) metricsSet[i]).getFrequency()).toString()));
            }

            frequency = Frequency.intValue();
        }

        return frequency;
    }

    public synchronized void removeMeasuree(Metric m) {
        measurees.remove(m);
        frequency = computeFrequency();

        if (frequency == 0) {
            deactivate();
        }
    }

    public synchronized void notifyMeasurees() {
        if (isStopped()) {
            return;
        }

        Object[] measureesSet = measurees.values().toArray();

        // do this only if frequency requires it ...
        for (int i = 0; i < measureesSet.length; i++) {
            Measuree m = (Measuree) measureesSet[i];

            if (m.timeLeft <= 0) {
                m.timeLeft = m.frequency;

                MonitorConsumer.MetricInstance mi = m.metricInstance;

                MonitorConsumer.CommandResult cmdres = mi.get();

                try {
                    cmdres.waitResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /* Check the returned status. */
                if (cmdres.hasFailed()) {
                    System.err.println("The GET command failed: "
                            + cmdres.getStatusStr());
                }
            } else {
                m.timeLeft -= frequency;
            }
        }
    }

    public void run() {
        if (isStopped()) {
            return;
        }

        while (isActive()) {
            notifyMeasurees();

            if (isStopped()) {
                return;
            }

            try {
                Thread.sleep(frequency);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class LocalMetricListener implements hu.sztaki.lpds.monitor.MetricListener {
    MercuryMonitorableAdaptor mma;

    public LocalMetricListener(MercuryMonitorableAdaptor mma) {
        this.mma = mma;
    }

    public boolean processMetric(hu.sztaki.lpds.monitor.MetricValue value) {
        hu.sztaki.lpds.monitor.MetricDefinition md_mercury = value
                .getDefinition();

        // System.out.println("Measurement type:
        // "+md_mercury.getMeasurementType().toString());
        MonitorizedMetric lookup = new MonitorizedMetric(null);
        lookup.metricID = value.getMetricId();

        int index = mma.monitorizedMetrics.indexOf(lookup);

        if (index > -1) {
            MonitorizedMetric mm = (MonitorizedMetric) mma.monitorizedMetrics
                    .get(index);

            for (int i = 0; i < mm.getListeners().size(); i++)
                ((MetricListener) (mm.getListeners().get(i)))
                        .processMetricEvent(new MetricEvent("localhost", value
                                .getValue(), mm.metric, value.getTimeStamp()
                                .getTime()));
        } else if (md_mercury.getMeasurementType().toString().equals(
                "continuous")) {
            mma.results.put(new Integer(value.getMetricId()), value);
        }

        return true;
    }

    public void processError(MonitorConsumer conn, Exception error) {
        error.printStackTrace();
    }
}

class MonitorizedMetric {
    static int MAX_BLOCK_TIME = 0;

    Metric metric;

    Vector<MetricListener> listeners = new Vector<MetricListener>();

    public MonitorConsumer.MetricInstance metricInstance;

    public int metricID;

    public MonitorizedMetric(Metric metric) {
        this.metric = metric;
    }

    public Vector<MetricListener> getListeners() {
        return listeners;
    }

    public void addListener(MetricListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(MetricListener listener) {
        listeners.remove(listener);
    }

    public Metric getMetric() {
        return metric;
    }

    public boolean equals(Object o) {
        // return (((MonitorizedMetric) o).getMetric()).equals(metric);
        return ((MonitorizedMetric) o).metricID == metricID;
    }
}

public class MercuryMonitorableAdaptor extends MonitorableCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi
                .getSupportedCapabilities();
        capabilities.put("addMetricListener", true);
        capabilities.put("removeMetricListener", true);
        capabilities.put("getMetricDefinitions", true);
        capabilities.put("getMeasurement", true);
        capabilities.put("getMetricDefinitionByName", true);

        return capabilities;
    }

    public static int MAX_BLOCK_TIME = 5000;

    public Vector<MonitorizedMetric> monitorizedMetrics = new Vector<MonitorizedMetric>();

    // public hu.sztaki.lpds.monitor.MetricValue crt_mv=null;
    public Map<Integer, hu.sztaki.lpds.monitor.MetricValue> results = java.util.Collections
            .synchronizedMap(new HashMap<Integer, hu.sztaki.lpds.monitor.MetricValue>());

    public Map<String, MetricDefinition> definitions = java.util.Collections
            .synchronizedMap(new HashMap<String, MetricDefinition>());

    private String[] metricNames = null;

    MonitorConsumer mc;

    LocalMetricListener listener = new LocalMetricListener(this);

    MeasurementTrigger trigger = new MeasurementTrigger(mc);

    public String producer_URL = "monp://localhost";

    /**
     * @param gatContext
     * @param preferences
     */
    public MercuryMonitorableAdaptor(GATContext gatContext)
            throws GATObjectCreationException {
        super(gatContext);

        if (gatContext.getPreferences().containsKey("mercury.producer")) {
            producer_URL = (String) gatContext.getPreferences().get(
                    "mercury.producer");
        }

        mc = new MonitorConsumer(producer_URL);

        mc.addMetricListener(listener);

        CommandResult resp = mc.auth();

        try {
            resp.waitResult(MAX_BLOCK_TIME);

            // resp.waitResult();
        } catch (Exception e) {
            System.err
                    .println("Mercury monitoring adaptor: Exception occured when performing authentication at producer at the following URL:"
                            + producer_URL + ":");
            e.printStackTrace();
        }

        if (resp.hasFailed()) {
            System.err
                    .println("Mercury monitoring adaptor: Authentication to producer at the following URL:"
                            + producer_URL + " has failed.");
        }

        /*
         * try { } catch (Exception e) { e.printStackTrace(); throw new
         * GATObjectCreationException(e); }
         */
    }

    public MonitorizedMetric lookupMonitorizedMetricByMetric(Metric metric) {
        for (int i = 0; i < monitorizedMetrics.size(); i++) {
            if (((MonitorizedMetric) monitorizedMetrics.get(i)).getMetric()
                    .equals(metric)) {
                return (MonitorizedMetric) monitorizedMetrics.get(i);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        MonitorizedMetric mm = lookupMonitorizedMetricByMetric(metric);

        // if the same metric is already monitorized ...
        if (mm != null) {
            mm.addListener(metricListener);

            return;
        }

        // else, if it's a new metric ...
        mm = new MonitorizedMetric(metric);

        mm.addListener(metricListener);

        if ((metric.getDefinition().getMeasurementType() == MetricDefinition.DISCRETE)
                || (metric.getFrequency() > 0)) {
            // activate metric monitorizing in Mercury consumer
            MonitorArg[] args = null;

            if (metric.getMetricParameters() != null) {
                Set arg = metric.getMetricParameters().entrySet();
                Object[] entries = arg.toArray();
                args = new MonitorArg[arg.size()];

                for (int i = 0; i < arg.size(); i++) {
                    args[i] = new MonitorArg((String) ((Map.Entry) entries[i])
                            .getKey(), ((Map.Entry) entries[i]).getValue());
                }
            }

            CollectResult collectres = null;

            if (args == null) {
                collectres = mc.collect(metric.getDefinition().getMetricName());
            } else {
                collectres = mc.collect(metric.getDefinition().getMetricName(),
                        args);
            }

            /* Wait for the result. */
            try {
                collectres.waitResult();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Check the returned status. */
            if (collectres.hasFailed()) {
                System.err.println("The COLLECT command failed: "
                        + collectres.getStatusStr());
            }

            MonitorConsumer.MetricInstance metricInstance = collectres
                    .getMetricInstance();

            if (metric.getDefinition().getMeasurementType() == MetricDefinition.DISCRETE) {
                /* Send a SUBSCRIBE command for the instance. */
                MonitorConsumer.CommandResult cmdres = metricInstance
                        .subscribe();

                /* Wait for the result. */
                try {
                    cmdres.waitResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /* Check the returned status. */
                if (cmdres.hasFailed()) {
                    System.err.println("The SUBSCRIBE command failed: "
                            + cmdres.getStatusStr());
                } else {
                    mm.metricInstance = metricInstance;
                    mm.metricID = metricInstance.getMetricId();
                    monitorizedMetrics.add(mm);
                }
            } // if discrete metric
            else {
                mm.metricInstance = metricInstance;
                mm.metricID = metricInstance.getMetricId();
                monitorizedMetrics.add(mm);
                trigger.addMeasuree(metric, metricInstance);

                if (!trigger.isAlive()) {
                    trigger.start();
                }
            }
        } // if discrete or continuous with frequency > 0
    }

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        MonitorizedMetric mm1 = lookupMonitorizedMetricByMetric(metric);

        if (mm1 != null) {
            mm1.removeListener(metricListener);

            // if there are no more listeners for this metric, stop monitorizing
            // the metric:
            if (mm1.getListeners().size() == 0) {
                if (mm1.metricInstance != null) {
                    MonitorConsumer.CommandResult cmdres = mm1.metricInstance
                            .stop();

                    /* Wait for the result. */
                    try {
                        cmdres.waitResult();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /* Check the returned status. */
                    if (cmdres.hasFailed()) {
                        System.err.println("The STOP command failed: "
                                + cmdres.getStatusStr());
                    }
                }

                if ((metric.getDefinition().getMeasurementType() == MetricDefinition.CONTINUOUS)
                        && (metric.getFrequency() > 0)) {
                    trigger.removeMeasuree(metric);
                }

                monitorizedMetrics.remove(mm1);
            }
        }
    }

    private void initializeMetricNames() {
        metricNames = new String[82];

        int index = 0;

        // Host metrics:
        metricNames[index++] = "host.cpu.available";
        metricNames[index++] = "host.cpu.clocktick";
        metricNames[index++] = "host.cpu.frequency";
        metricNames[index++] = "host.cpu.idle";
        metricNames[index++] = "host.cpu.indexes";
        metricNames[index++] = "host.cpu.iowait";
        metricNames[index++] = "host.cpu.irq";
        metricNames[index++] = "host.cpu.l1dcache";
        metricNames[index++] = "host.cpu.l1icache";
        metricNames[index++] = "host.cpu.l2cache";
        metricNames[index++] = "host.cpu.l3cache";
        metricNames[index++] = "host.cpu.nice";
        metricNames[index++] = "host.cpu.number";
        metricNames[index++] = "host.cpu.online";
        metricNames[index++] = "host.cpu.softirq";
        metricNames[index++] = "host.cpu.state";
        metricNames[index++] = "host.cpu.state_begin";
        metricNames[index++] = "host.cpu.system";
        metricNames[index++] = "host.cpu.type";
        metricNames[index++] = "host.cpu.usage";
        metricNames[index++] = "host.cpu.user";
        metricNames[index++] = "host.disk.disks";
        metricNames[index++] = "host.disk.iostat";
        metricNames[index++] = "host.disk.partitions";
        metricNames[index++] = "host.fs.inodes";
        metricNames[index++] = "host.fs.mounted";
        metricNames[index++] = "host.fs.space";
        metricNames[index++] = "host.loadavg";
        metricNames[index++] = "host.mem.buffers";
        metricNames[index++] = "host.mem.cached";
        metricNames[index++] = "host.mem.free";
        metricNames[index++] = "host.mem.shared";
        metricNames[index++] = "host.mem.size";
        metricNames[index++] = "host.net.addr.hwaddr";
        metricNames[index++] = "host.net.addr.ipv4";
        metricNames[index++] = "host.net.addr.ipv6";
        metricNames[index++] = "host.net.collision";
        metricNames[index++] = "host.net.hwtype";
        metricNames[index++] = "host.net.ifflags";
        metricNames[index++] = "host.net.ifindex";
        metricNames[index++] = "host.net.interfaces";
        metricNames[index++] = "host.net.mtu";
        metricNames[index++] = "host.net.recv.byte";
        metricNames[index++] = "host.net.recv.drop";
        metricNames[index++] = "host.net.recv.error";
        metricNames[index++] = "host.net.recv.packet";
        metricNames[index++] = "host.net.send.byte";
        metricNames[index++] = "host.net.send.carrier";
        metricNames[index++] = "host.net.send.drop";
        metricNames[index++] = "host.net.send.error";
        metricNames[index++] = "host.net.send.packet";
        metricNames[index++] = "host.net.total.byte";
        metricNames[index++] = "host.net.total.error";
        metricNames[index++] = "host.net.total.packet";
        metricNames[index++] = "host.os.boottime";
        metricNames[index++] = "host.os.machine";
        metricNames[index++] = "host.os.name";
        metricNames[index++] = "host.os.release";
        metricNames[index++] = "host.os.version";
        metricNames[index++] = "host.processes.all";
        metricNames[index++] = "host.processes.running";
        metricNames[index++] = "host.swap.free";
        metricNames[index++] = "host.swap.in";
        metricNames[index++] = "host.swap.out";
        metricNames[index++] = "host.swap.size";
        metricNames[index++] = "host.users";
        metricNames[index++] = "host.vm.page.fault";
        metricNames[index++] = "host.vm.page.in";
        metricNames[index++] = "host.vm.page.majorfault";
        metricNames[index++] = "host.vm.page.out";

        // Application metrics:
        metricNames[index++] = "app.message";
        metricNames[index++] = "app.event";
        metricNames[index++] = "app.getvar.int32";
        metricNames[index++] = "app.getvar.int64";
        metricNames[index++] = "app.getvar.double";
        metricNames[index++] = "app.getvar.string";
        metricNames[index++] = "app.getvar.bytes";
        metricNames[index++] = "app.getvar.info";

        // Mercury internal metrics:
        metricNames[index++] = "monitor.ctrl.def";
        metricNames[index++] = "monitor.metric.def";
        metricNames[index++] = "monitor.metric.killed";
        metricNames[index++] = "monitor.dropped";
    }

    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        if (metricNames == null) {
            initializeMetricNames();
        }

        CommandResult[] queries = new CommandResult[metricNames.length];
        Integer[] responded = new Integer[metricNames.length];

        MonitorArg[] args = new MonitorArg[1];

        // query the Mercury monitor for metric definitions for which the
        // definition is not yet known, using the "monitor.metric.def"
        // predefined metric:
        int no_queried = 0;

        for (int i = 0; i < metricNames.length; i++) {
            if (definitions.containsKey(metricNames[i])) {
                continue;
            }

            args[0] = new MonitorArg("name", metricNames[i]);
            queries[no_queried] = mc.query("monitor.metric.def", args);
            no_queried++;
        }

        // wait for results
        int no_responded = 0;

        for (int i = 0; i < no_queried; i++) {
            try {
                queries[i].waitResult(MAX_BLOCK_TIME);

                // queries[i].waitResult();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // System.out.println("queries["+i+"].getResultId()
            // ="+queries[i].getResultId());
            int status = queries[i].getStatus();

            if (status != 0) {
                System.err.println("Query for metric " + "monitor.metric.def"
                        + " has failed. Status returned ="
                        + queries[i].getStatusStr());
            }

            if ((status != 0) || queries[i].hasFailed()) {
                continue;
            }

            responded[no_responded] = new Integer(queries[i].getResultId());

            no_responded++;
        }

        hu.sztaki.lpds.monitor.MetricValue[] responses = new hu.sztaki.lpds.monitor.MetricValue[no_responded];

        for (int i = 0; i < no_responded; i++) {
            // look for result placed in the "results" HashMap by the listener
            // (indexed by result ID):
            responses[i] = (hu.sztaki.lpds.monitor.MetricValue) results
                    .get(responded[i]);

            // System.err.println("crt_mv1 = " + crt_mv1);
            while (responses[i] == null) {
                try {
                    Thread.sleep(30);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                responses[i] = (hu.sztaki.lpds.monitor.MetricValue) results
                        .get(responded[i]);

                // System.err.println("crt_mv1 = " + crt_mv1);
            }

            results.remove(responded[i]);
        }

        String metricType = null;
        String metricUnit = null;

        for (int i = 0; i < responses.length; i++) {
            // retrieve metric definition from returned result:
            Object[] fields = (Object[]) responses[i].getValue();

            String metricName = (String) fields[1];
            String metricTypeUnit = (String) fields[2];
            metricType = null;
            metricUnit = null;

            Object[] metricParams = (Object[]) fields[3];
            Integer metricMeasurement = (Integer) fields[4];

            int index = metricTypeUnit.indexOf("(unit:");

            int index_aux = metricTypeUnit.indexOf("rec");

            int index_aux1 = metricTypeUnit.indexOf("enum");

            if ((index_aux > -1) || (index_aux1 > -1)) // type is record type
            // or enum type
            {
                metricType = metricTypeUnit;
            } else if (index > -1) // type is not record type and unit is given
            {
                metricType = metricTypeUnit.substring(0, index).trim();

                int index2 = metricTypeUnit.indexOf('"', index + 6);

                if (index2 > -1) {
                    int index3 = metricTypeUnit.indexOf('"', index2 + 1);

                    if (index3 > -1) {
                        metricUnit = metricTypeUnit.substring(index2 + 1,
                                index3);
                    }
                }
            } else // type is not record type and unit is not given
            {
                metricType = metricTypeUnit;
            }

            Map<String, Object> param_defs = new HashMap<String, Object>();

            for (int j = 0; j < metricParams.length; j++)
                param_defs.put(((String[]) metricParams[j])[0],
                        ((Object[]) metricParams[j])[1]);

            int measurementType = MetricDefinition.CONTINUOUS;

            if (!metricMeasurement.equals(new Integer(0))) {
                measurementType = MetricDefinition.DISCRETE;
            }

            // System.out.println(metricName+", "+measurementType+", "+
            // metricType +", "+metricUnit+", "+param_defs+", "+"null");
            MetricDefinition rv = new MetricDefinition(metricName,
                    measurementType, metricType, metricUnit, param_defs, null);

            if (rv != null) {
                definitions.put(metricName, rv);
            }
        }

        Vector<MetricDefinition> rv = new Vector<MetricDefinition>();
        rv.addAll(definitions.values());

        return rv;
    }

    @SuppressWarnings("unchecked")
    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        // continuousListener
        if (metric == null) {
            return null;
        }

        if (metric.getDefinition() == null) {
            return null;
        }

        Set arg = metric.getMetricParameters().entrySet();
        Object[] entries = arg.toArray();
        MonitorArg[] args = new MonitorArg[arg.size()];

        for (int i = 0; i < arg.size(); i++) {
            args[i] = new MonitorArg(
                    (String) ((Map.Entry<String, Object>) entries[i]).getKey(),
                    ((Map.Entry<String, Object>) entries[i]).getValue());
        }

        CommandResult result = mc.query(metric.getDefinition().getMetricName(),
                args);

        try {
            // result.waitResult(MAX_BLOCK_TIME);
            result.waitResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println("result.getResultId() ="+result.getResultId());
        int status = result.getStatus();

        if (status != 0) {
            System.err.println("Query for metric "
                    + metric.getDefinition().getMetricName()
                    + " has failed. Status returned =" + result.getStatusStr());

            return null; // am modificat aici (adaugat) -> ev sursa de erori
        }

        if (result.hasFailed()) {
            return null;
        }

        Integer id = new Integer(result.getResultId());

        // look for result placed in the "results" HashMap by the listener
        // (indexed by result ID):
        hu.sztaki.lpds.monitor.MetricValue crt_mv1 = (hu.sztaki.lpds.monitor.MetricValue) results
                .get(id);

        // System.err.println("crt_mv1 = " + crt_mv1);
        while (crt_mv1 == null) {
            try {
                Thread.sleep(30);
            } catch (Exception e) {
                e.printStackTrace();
            }

            crt_mv1 = (hu.sztaki.lpds.monitor.MetricValue) results.get(id);

            // System.err.println("crt_mv1 = " + crt_mv1);
        }

        results.remove(id);

        return new MetricEvent("localhost", crt_mv1.getValue(), metric, crt_mv1
                .getTimeStamp().getTime());
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        if (name == null) {
            return null;
        }

        // return definition if already cached:
        if (definitions.containsKey(name)) {
            return (MetricDefinition) definitions.get(name);
        }

        // ... or else produce it now, using a query of the standard
        // "monitor.metric.def" metric:
        MetricDefinition md = new MetricDefinition("monitor.metric.def",
                MetricDefinition.CONTINUOUS, "", "", null, null);

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("name", name);

        MetricEvent result = getMeasurement(new Metric(md, params, 0));

        // retrieve metric definition from returned result:
        Object[] fields = (Object[]) result.getValue();

        String metricName = (String) fields[1];
        String metricTypeUnit = (String) fields[2];
        String metricType = null;
        String metricUnit = null;
        Object[] metricParams = (Object[]) fields[3];
        Integer metricMeasurement = (Integer) fields[4];

        int index = metricTypeUnit.indexOf("(unit:");

        int index_aux = metricTypeUnit.indexOf("rec");

        int index_aux1 = metricTypeUnit.indexOf("enum");

        if ((index_aux > -1) || (index_aux1 > -1)) // type is record type or
        // enum type
        {
            metricType = metricTypeUnit;
        } else if (index > -1) // type is not record type and unit is given
        {
            metricType = metricTypeUnit.substring(0, index).trim();

            int index2 = metricTypeUnit.indexOf('"', index + 6);

            if (index2 > -1) {
                int index3 = metricTypeUnit.indexOf('"', index2 + 1);

                if (index3 > -1) {
                    metricUnit = metricTypeUnit.substring(index2 + 1, index3);
                }
            }
        } else // type is not record type and unit is not given
        {
            metricType = metricTypeUnit;
        }

        Map<String, Object> param_defs = new HashMap<String, Object>();

        for (int i = 0; i < metricParams.length; i++)
            param_defs.put(((String[]) metricParams[i])[0],
                    ((Object[]) metricParams[i])[1]);

        int measurementType = MetricDefinition.CONTINUOUS;

        if (!metricMeasurement.equals(new Integer(0))) {
            measurementType = MetricDefinition.DISCRETE;
        }

        /*
         * System.out.println(metricName + ", " + measurementType + ", " +
         * metricType + ", " + metricUnit + ", " + param_defs + ", " + "null");
         */
        MetricDefinition rv = new MetricDefinition(metricName, measurementType,
                metricType, metricUnit, param_defs, null);

        if (rv != null) {
            definitions.put(name, rv);
        }

        /*
         * if(name.equals("host.cpu.number")) return new
         * MetricDefinition("host.cpu.number", MetricDefinition.CONTINUOUS,
         * "dataTypeInt", "pieces", null, null); if(name.equals("app.message"))
         * return new MetricDefinition(name, MetricDefinition.DISCRETE,
         * "dataTypeInt", "", null, null);
         * 
         * if(name.equals("app.priv.Mercury-Example-Java-Appmonitor-1.processed"))
         * return new MetricDefinition(name, MetricDefinition.DISCRETE,
         * "dataTypeInt", "", null, null);
         */
        return rv;
    }
}
