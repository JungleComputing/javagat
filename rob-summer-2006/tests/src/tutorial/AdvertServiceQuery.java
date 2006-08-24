package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;

public class AdvertServiceQuery {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();

        AdvertService advert = GAT.createAdvertService(context);

        MetaData meta = new MetaData();
        meta.put(args[0], args[1]);

        // And retrieve it again.
        String[] paths = advert.find(meta);

        if (paths == null) {
            System.err.println("no objects found");

            return;
        }

        System.err.println(paths.length + " adverts found");

        for (int i = 0; i < paths.length; i++) {
            Advertisable result = advert.getAdvertisable(paths[i]);
            System.err.println("advert " + i + " is " + result);
        }
    }
}
