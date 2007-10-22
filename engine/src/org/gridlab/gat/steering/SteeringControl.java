
package org.gridlab.gat.steering;

import java.util.Map;

import java.io.Serializable;


public class SteeringControl implements Serializable
{

	  SteeringControlDefinition scd = null;
	  Map actualParameters = null;


        public SteeringControl(SteeringControlDefinition scd, Map actualParameters)
	  {
		this.scd = scd;
		this.actualParameters = actualParameters;
	  }


        public SteeringControlDefinition getDefinition()
	  {
		return scd;
	  }


        public Map getParameters()
	  {
		return actualParameters;
	  }


        public Object getParameterByName(String name)
	  {
		return actualParameters.get(name);
	  }


        public boolean equals(Object o)
	  {
		SteeringControl sc = null;

		try
		{
			sc = (SteeringControl) o;
		}
		catch(Exception e)
		{
			return false;
		}


		boolean definitionSame = true, actualSame = true;


                if(this.scd == null)
		{
                        if(sc.getDefinition() != null);                                			definitionSame = false;
		}
                else if(!this.scd.equals(sc.getDefinition()))
                        definitionSame = false;

                if(this.actualParameters == null)
		{
                        if(sc.getParameters() != null)
                                actualSame = false;
		}
                else if(!this.actualParameters.equals(sc.getParameters()))
                        actualSame = false;

		return definitionSame && actualSame;
	  }


        public String toString()
	  {
		return "Definition: " + this.scd + "; Actual parameters: " + this.actualParameters;
	  }
}
