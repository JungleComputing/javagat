
package steering;

import org.gridlab.gat.steering.SteeringManager;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;

import java.rmi.Naming;

// Arg: 1) hostname of machine where the SteeringExampleRMISteeredMain runs


public class SteeringExampleRMIMasterMain
{

	public static void main(String[] args)
	{
		System.out.println("MAIN: Getting SteeringManager ...");

                GATContext c = new GATContext();
                Preferences prefs = new Preferences();
                prefs.put("SteeringManager.adaptor.name", "RMI");
		prefs.put("RMI_STEERING_SERVER_LOCATION", "//"+args[0]+":20000/SteeringServer");

                try
                {
			SteeringManager sm = GAT.createSteeringManager(c, prefs);
			System.out.println("MAIN: Done.");

			SteeringMasterRMI master = new SteeringMasterRMI(sm, args[0]);

			System.out.println("MAIN: Starting master ...");
			master.start();

			System.out.println("MAIN: Done !");
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                        System.exit(1);
                }


	}
}

