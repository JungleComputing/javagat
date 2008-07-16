package benchmarks;

public class AdaptorTestResultEntry {

    private boolean result;

    private long time;

    private Exception e;

    public AdaptorTestResultEntry(boolean result, long time, Exception e) {
        this.result = result;
        this.time = time;
        this.e = e;
    }

    public boolean getResult() {
        return result;
    }

    public long getTime() {
        return time;
    }

    public Exception getException() {
        return e;
    }

}
