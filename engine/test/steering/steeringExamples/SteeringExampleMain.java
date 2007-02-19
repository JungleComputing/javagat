
package steeringExamples;

import org.gridlab.gat.steering.SteeringManager;
//import org.gridlab.gat.steering.cpi.local.SteeringLocalManager;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;



public class SteeringExampleMain
{

	public static void main(String[] args)
	{
		System.out.println("MAIN: Getting SteeringManager ...");

                GATContext c = new GATContext();
                Preferences prefs = new Preferences();
                prefs.put("SteeringManager.adaptor.name", "SameGAT");
		//prefs.put("adaptors.name", "local");

                try
                {
			SteeringManager sm = GAT.createSteeringManager(c, prefs);
			//SteeringManager sm = GAT.createSteeringManager(c, null);
			System.out.println("MAIN: Done.");

			//SteeringManager sm = new SteeringLocalManager();
			SteeredObject so = new SteeredObject(sm);
			SteeringMaster master = new SteeringMaster(sm);

			System.out.println("MAIN: Starting steered object ...");
			so.start();

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
