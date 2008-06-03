package file;


public class AdaptorTestResultEntry {

    private boolean result;

    private long time;

    private Exception e;

    public AdaptorTestResultEntry(boolean result, long time,
            Exception e) {
        this.result = result;
        this.time = time;
        this.e = e;
    }

    protected boolean getResult() {
        return result;
    }

    protected long getTime() {
        return time;
    }

    protected Exception getException() {
        return e;
    }

}
