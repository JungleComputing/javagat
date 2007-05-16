/*
 * Created on Aug 16, 2004
 */
package advert;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.resources.Job;

/**
 * @author rob
 */
public class ConnectToAdvertJob {
    public static void main(String[] args) throws Exception {
        GATContext c = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("AdvertService.adaptor.name", "local");
        prefs.put("ResourceBroker.adaptor.name", "globus");

        AdvertService a = GAT.createAdvertService(c, prefs);
        Job other = (Job) a.getAdvertisable("/rob/testJob");

        System.err.println("got job back: " + other);

        while ((other.getState() != Job.STOPPED)
                && (other.getState() != Job.SUBMISSION_ERROR)) {
            System.err.println("job state = " + other.getInfo());
            Thread.sleep(1000);
        }

        System.err.println("job DONE, state = " + other.getInfo());
        GAT.end();
    }
}
