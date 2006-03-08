package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.Advertisable;

public class AdvertServicePrinter {

    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();

        AdvertService advert = GAT.createAdvertService(context);

        // And retrieve it.
        Advertisable result = advert.getAdvertisable(args[0]);
        if(result == null) {
            System.err.println("object not found");
            return;
        }
        
        System.err.println("got object back: " + result);
    }
}