/*
 * Created on Aug 16, 2004
 */
package advert;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;

/**
 * @author rob
 */
public class AdvertTest {

	public static void main(String[] args) {
		GATContext c = new GATContext();
		Preferences prefs = new Preferences();
		prefs.put("advert.adaptor.name", "storagebox");

		try {
			Endpoint e = GAT.createEndpoint(c);
			AdvertService a = GAT.createAdvertService(c, prefs);
			MetaData m = new MetaData();
			m.put("name", "testEndpoint");
			a.add(e, m, "/rob/testadvert");
			
			Endpoint other = (Endpoint) a.getAdvertisable("/rob/testadvert");
			
			System.err.println("got endpoint back: " + other);
		} catch (Exception x) {
			System.err.println("error: " + x);
			x.printStackTrace();
		}
	}
}
