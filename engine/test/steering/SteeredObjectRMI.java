
package steering;

import org.gridlab.gat.steering.SteeringControl;
import org.gridlab.gat.steering.SteeringControlDefinition;
import org.gridlab.gat.steering.SteeringManager;
import org.gridlab.gat.steering.SteeredIDExistsException;
import org.gridlab.gat.steering.SteeredIDUnknownException;
import org.gridlab.gat.steering.NoSuchControlException;

import org.gridlab.gat.steering.cpi.rmi.SteerableRMI;


import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;


public class SteeredObjectRMI extends UnicastRemoteObject implements SteerableRMI
{
	SteeringManager sm;
	Vector controls;
	String state = "INIT";
	int wallClockTime = 0;
	int steeredIntVariable1 = 0;
	int steeredIntVariable2 = 0;
	double steeredDoubleVariable = 0.0;

	String localHost=null;
	int localPort=-1;

	public SteeredObjectRMI(SteeringManager sm, int localPort) throws RemoteException
	{
		this.sm = sm;

		// build list of available SteeringControlDefinitions

		controls = new Vector();

		// control that starts the simulation:
		controls.add(new SteeringControlDefinition("startSimulation", null, null));

		// control that stops the simulation:
		controls.add(new SteeringControlDefinition("stopSimulation", null, null));

		// control that allows a integer variable to be steered:

		Map formalParameters = new HashMap();
		formalParameters.put("var.name", "String");
		formalParameters.put("var.type", "Integer");
		formalParameters.put("var.value", new Integer(0));
		SteeringControlDefinition setIntDef = new SteeringControlDefinition("setvar.Integer", formalParameters, null);

		// control that allows a double variable to be steered:

		Map formalParameters2 = new HashMap();
		formalParameters2.put("var.name", "String");
		formalParameters2.put("var.type", "Double");
		formalParameters2.put("var.value", new Double(0.0));
		SteeringControlDefinition setDoubleDef = new SteeringControlDefinition("setvar.Double", formalParameters2, null);

		controls.add(setIntDef);
		controls.add(setDoubleDef);

		this.localPort=localPort;

		try
		{
			localHost = InetAddress.getLocalHost().getHostName();

			System.out.println("SteeredObjectRMI: "+"//"+localHost+":"+localPort+"/"+"MY_STEERABLE_COMPONENT");
			sm.registerSteered("//"+localHost+":"+localPort+"/"+"MY_STEERABLE_COMPONENT", this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RemoteException(e.toString());
		}
	}

	public Map executeControl(SteeringControl c) throws NoSuchControlException, RemoteException
	{
                if(!controls.contains(c.getDefinition())) throw new NoSuchControlException();

		if(c.getDefinition().getControlName().equals("startSimulation"))

			synchronized(this)
			{
				state="RUNNING";
			}

		if(c.getDefinition().getControlName().equals("stopSimulation"))

			synchronized(this)
			{
				state="STOPPED";
			}

		if(c.getDefinition().getControlName().equals("setvar.Integer"))
		{
			if(c.getParameterByName("var.name").equals("MY_STEERED_INT_1"))
			{
				synchronized(this)
				{
					steeredIntVariable1 = ((Integer) c.getParameterByName("var.value")).intValue();
				}
			}

			if(c.getParameterByName("var.name").equals("MY_STEERED_INT_2"))
			{
				synchronized(this)
				{
					steeredIntVariable2 = ((Integer) c.getParameterByName("var.value")).intValue();
				}
			}
		}

		if(c.getDefinition().getControlName().equals("setvar.Double"))
		{	
			if(c.getParameterByName("var.name").equals("MY_STEERED_DOUBLE"))
			{
				synchronized(this)
				{
					System.out.println("STEERED: Setting double to value: " + ((Double) c.getParameterByName("var.value")).doubleValue());
					steeredDoubleVariable = ((Double) c.getParameterByName("var.value")).doubleValue();
				}
			}
		}
		
			return null;
	}

	public List getControlDefinitions() throws RemoteException
	{
		return controls;
	}


	private void printVariables()
	{
		System.out.println("STEERED: WallClockTime = "+wallClockTime+", SIMULATION STATE = "+state+", STEERED VARIABLES STATE: ");
		System.out.println("STEERED: steeredIntVariable1 = " + steeredIntVariable1);
		System.out.println("STEERED: steeredIntVariable2 = " + steeredIntVariable2);
		System.out.println("STEERED: steeredDoubleVariable = " + steeredDoubleVariable);
	}

	public void run()
	{
		System.out.println("STEERED: Simulation has not started yet.");

		while(state.equals("INIT"))
		{
			synchronized(this){
				try
				{
					Thread.sleep(1000);
					wallClockTime++;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		while(state.equals("RUNNING"))
		{
			synchronized(this){
				try
				{
					printVariables();
					Thread.sleep(1000);
					wallClockTime++;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		System.out.println("STEERED: Simulation has been stopped.");
		printVariables();

		System.out.println("STEERED: Unregistering...");

		try{
			sm.unregisterSteered("//"+localHost+":"+localPort+"/"+"MY_STEERABLE_COMPONENT", this);
		}catch(Exception e){e.printStackTrace();}

		System.out.println("STEERED: Done!");
	}
}

