package tutorial20;

import org.gridlab.gat.AdaptorInfo;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;

public class ShowAdaptors {

    /**
     * @param args
     * @throws GATInvocationException
     */
    public static void main(String[] args) throws GATInvocationException {
        for (String adaptorType : GAT.getAdaptorTypes()) {
            for (AdaptorInfo adaptorInfo : GAT.getAdaptors(adaptorType)) {
                System.out.println(adaptorInfo.toString());
            }
        }
    }

}
