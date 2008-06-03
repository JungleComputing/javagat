package file;

import java.util.HashMap;
import java.util.Map;

public class AdaptorTestResult {

    private String adaptor;

    private String host;

    private Map<String, AdaptorTestResultEntry> testResultEntries = new HashMap<String, AdaptorTestResultEntry>();

    public AdaptorTestResult(String adaptor, String host) {
        this.adaptor = adaptor;
        this.host = host;

    }

    public void put(String key, AdaptorTestResultEntry testResultEntry) {
        testResultEntries.put(key, testResultEntry);
    }

    public void print() {
        System.out.println("*** general results ***");
        System.out.println("adaptor:    " + adaptor);
        System.out.println("host:       " + host);
        System.out.println("total time: " + getTotalRunTime() + " msec");
        System.out.println("avg time  : " + getAverageRunTime() + " msec");
        System.out.println("*** method results  ***");

        // for (AdaptorTestResultEntry testResultEntry :
        // testResultEntries.values()) {
        // System.out.print(testResultEntry.getTestName());
        // for (int i = 0; i < (50 - testResultEntry.getTestName().length());
        // i++) {
        // System.out.print(" ");
        // }
        // if (testResultEntry.getResult()) {
        // System.out.println("SUCCESS \t" + testResultEntry.getTime()
        // + " msec");
        // } else {
        // System.out.println("FAILURE");
        // }
        // }
    }

    public long getTotalRunTime() {
        long result = 0L;
        for (AdaptorTestResultEntry testResultEntry : testResultEntries
                .values()) {
            if (testResultEntry.getResult()) {
                result += testResultEntry.getTime();
            }
        }
        return result;
    }

    public long getAverageRunTime() {
        long result = 0L;
        int i = 0;
        for (AdaptorTestResultEntry testResultEntry : testResultEntries
                .values()) {
            if (testResultEntry.getResult()) {
                result += testResultEntry.getTime();
                i++;
            }
        }
        if (i == 0) {
            return 0L;
        } else {
            return result / i;
        }
    }

    public String getAdaptor() {
        return adaptor;
    }

    public Map<String, AdaptorTestResultEntry> getTestResultEntries() {
        return testResultEntries;
    }

}
