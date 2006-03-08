package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;

public class AdaptorBenchmark {

    public static void main(String[] args) {
        int cycles = 20;
        GATContext c = new GATContext();
        Preferences prefs = new Preferences();
        Endpoint other = null;
        String pwd = null;
        String[] found = null;

        prefs.put("advert.adaptor.name", "storagebox");

        try {
            AdvertService a = GAT.createAdvertService(c, prefs);
            Endpoint e = GAT.createEndpoint(c);
            MetaData m = new MetaData();
            m.put("name", "testEndpoint");

            long average_add = 0, average_getAdvertisable = 0, average_delete = 0, average_getMetaData = 0, average_find = 0, average_getPWD = 0, average_setPWD = 0, time1 = 0, time2 = 0;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                a.add(e, m, "/aagapi/benchmk_" + i);
                time2 = System.currentTimeMillis();
                average_add += time2 - time1;
            }
            average_add /= cycles;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                other = (Endpoint) a.getAdvertisable("/aagapi/benchmk_" + i);
                time2 = System.currentTimeMillis();
                average_getAdvertisable += time2 - time1;
            }
            average_getAdvertisable /= cycles;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                a.getMetaData("/aagapi/benchmk_0");
                time2 = System.currentTimeMillis();
                average_getMetaData += time2 - time1;
            }
            average_getMetaData /= cycles;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                found = a.find(m);
                time2 = System.currentTimeMillis();
                average_find += time2 - time1;
            }
            average_find /= cycles;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                a.setPWD("/aagapi");
                time2 = System.currentTimeMillis();
                average_setPWD += time2 - time1;
            }
            average_setPWD /= cycles;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                pwd = a.getPWD();
                time2 = System.currentTimeMillis();
                average_getPWD += time2 - time1;
            }
            average_getPWD /= cycles;

            for (int i = 0; i < cycles; i++) {
                time1 = System.currentTimeMillis();
                a.delete("/aagapi/benchmk_" + i);
                time2 = System.currentTimeMillis();
                average_delete += time2 - time1;
            }
            average_delete /= cycles;

            System.out.println("Average benchmarked times (ms): \n");

            System.out.println("add(): " + average_add);
            System.out.println("getAdvertisable(): " + average_getAdvertisable);
            System.out.println("getMetaData(): " + average_getMetaData);
            System.out.println("find(): " + average_find);
            System.out.println("getPWD(): " + average_getPWD);
            System.out.println("setPWD(): " + average_setPWD);
            System.out.println("delete(): " + average_delete);

            System.out.println("Correctness verifications: \n");

            System.out.println("Adverted endpoint: " + e);
            System.out.println("Got back endpoint: " + other);

            System.out.println("find() returned: ");

            for (int i = 0; i < found.length; i++)
                System.out.print(found[i] + "; ");
            System.out.println();

            System.out.println("Set PWD: " + "/aagapi");
            System.out.println("Get PWD: " + pwd);

        } catch (Exception x) {
            System.err.println("error: " + x);
            x.printStackTrace();
        }
    }
}
