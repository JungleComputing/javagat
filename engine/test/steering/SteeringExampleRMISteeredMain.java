
package steering;

import org.gridlab.gat.steering.SteeringManager;
import org.gridlab.gat.steering.cpi.rmi.SteeringRMIServerImpl;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;

import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

import java.net.InetAddress;


public class SteeringExampleRMISteeredMain
{

	public static void main(String[] args)
	{
		try
		{

			System.out.println("MAIN: Getting SteeringManager ...");

                	GATContext c = new GATContext();
                	Preferences prefs = new Preferences();
                	prefs.put("SteeringManager.adaptor.name", "RMI");
			prefs.put("RMI_LOCAL_REGISTRY_PORT", new Integer(20000));
			String localHost=InetAddress.getLocalHost().getHostName();
			prefs.put("RMI_STEERING_SERVER_LOCATION", "//"+localHost+":20000/SteeringServer");

                        SteeringRMIServerImpl steeringServer=new SteeringRMIServerImpl();
                        Naming.rebind("//"+localHost+":20000/SteeringServer", steeringServer);

			SteeringManager sm = GAT.createSteeringManager(c, prefs);
			System.out.println("MAIN: Done.");

			SteeredObjectRMI so = new SteeredObjectRMI(sm, 20000);

			System.out.println("MAIN: Starting steered object ...");
			so.run();

			System.out.println("MAIN: Done.\n Unbinding SteeringServer ... ");

			Naming.unbind("//"+localHost+":20000/SteeringServer");

			UnicastRemoteObject.unexportObject(steeringServer, true);
			System.out.println("MAIN: Done !");
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                        System.exit(1);
                }
	}
}
