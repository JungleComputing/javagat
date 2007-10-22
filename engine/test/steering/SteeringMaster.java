
package steering;


import org.gridlab.gat.steering.SteeringControl;
import org.gridlab.gat.steering.SteeringControlDefinition;
import org.gridlab.gat.steering.SteeringManager;


import java.util.List;
import java.util.Map;
import java.util.HashMap;


class SteeringMaster extends Thread
{
	SteeringManager sm;

	public SteeringMaster(SteeringManager sm)
	{
		this.sm = sm;
	}

	public void run()
	{
                try
                {
			List steerables = sm.getSteeredObjectsIDs();
			if(!steerables.contains("MY_STEERABLE_COMPONENT"))
			{
				System.out.println("MASTER: Steerable object MY_STEERABLE_COMPONENT not found. Exiting ...");
				System.exit(1);
			}

			List controls = null;

			System.out.println("MASTER: Querying controls available ...");

			controls = sm.getControlDefinitions("MY_STEERABLE_COMPONENT");

			System.out.println("MASTER: Controls available for MY_STEERABLE_COMPONENT:");
	
			for(int i = 0; i < controls.size(); i++)
				System.out.println( ((SteeringControlDefinition) controls.get(i)).toString());
		

		/*
			The control definitions could ultimately be dynamically determined/parsed from the returned List
			based on their names for instance. For simplicity, we assigned them statically in this example,
			in the following order:
				1 - startSimulation
				2 - stopSimulation
				3 - setvar.Integer
				4 - setvar.Double
		*/


			SteeringControlDefinition startDefinition = (SteeringControlDefinition) controls.get(0);
			SteeringControlDefinition stopDefinition = (SteeringControlDefinition) controls.get(1);
			SteeringControlDefinition setIntDefinition = (SteeringControlDefinition) controls.get(2);
			SteeringControlDefinition setDoubleDefinition = (SteeringControlDefinition) controls.get(3);


			// Wait for 3 seconds ...
			Thread.sleep(3000);

			// Start simulation:

			System.out.println("MASTER: Sending command to start simulation at MY_STEERABLE_COMPONENT ...");	
			sm.executeControl("MY_STEERABLE_COMPONENT", new SteeringControl(startDefinition, null));
			System.out.println("MASTER: Command successfully sent.");	

			Map actualParameters1 = null, actualParameters2 = null, actualParameters3 = null;

			for(int i = 0; i < 10; i++)
			{
				// Wait for 3 seconds ...
				Thread.sleep(3000);

				// Set value of first steered int variable to i:

				actualParameters1 = new HashMap();
				actualParameters1.put("var.name", "MY_STEERED_INT_1");
				actualParameters1.put("var.type", "Integer");
				actualParameters1.put("var.value", new Integer(i));

				System.out.println("MASTER: Sending command MY_STEERED_INT_1 = " + i + " to MY_STEERABLE_COMPONENT ...");	
				sm.executeControl("MY_STEERABLE_COMPONENT", new SteeringControl(setIntDefinition, actualParameters1));
				System.out.println("MASTER: Command successfully sent.");	


				// Set value of second steered int variable to 2*i:

				actualParameters2 = new HashMap();
				actualParameters2.put("var.name", "MY_STEERED_INT_2");
				actualParameters2.put("var.type", "Integer");
				actualParameters2.put("var.value", new Integer(2*i));

				System.out.println("MASTER: Sending command MY_STEERED_INT_2 = " + 2*i + " to MY_STEERABLE_COMPONENT ...");	
				sm.executeControl("MY_STEERABLE_COMPONENT", new SteeringControl(setIntDefinition, actualParameters2));
				System.out.println("MASTER: Command successfully sent.");	


				// Set value of the steered double variable to 10.0/i:

				actualParameters3 = new HashMap();
				actualParameters3.put("var.name", "MY_STEERED_DOUBLE");
				actualParameters3.put("var.type", "Double");
				actualParameters3.put("var.value", new Double(10.0/i));

				System.out.println("MASTER: Sending command MY_STEERED_DOUBLE = " + 10.0/i + " to MY_STEERABLE_COMPONENT ...");	
				sm.executeControl("MY_STEERABLE_COMPONENT", new SteeringControl(setDoubleDefinition, actualParameters3));
				System.out.println("MASTER: Command successfully sent.");	
			}


			// Wait for 3 seconds ...
			Thread.sleep(3000);

			// Stop simulation:

			System.out.println("MASTER: Sending command to stop simulation at MY_STEERABLE_COMPONENT ...");	
			sm.executeControl("MY_STEERABLE_COMPONENT", new SteeringControl(stopDefinition, null));
			System.out.println("MASTER: Command successfully sent.");	


		}
		catch(Exception e)
		{
			e.printStackTrace();
		}



	}
}
