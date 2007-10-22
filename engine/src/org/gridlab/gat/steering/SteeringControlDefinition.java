package org.gridlab.gat.steering;

import java.util.Map;

import java.io.Serializable;


public class SteeringControlDefinition implements Serializable
{
	  String name = null;
	  Map formalParameters = null;
	  Map returnDefinition = null;

        public SteeringControlDefinition(String name, Map formalParameters, Map returnDefinition)
	  {
		this.name = name;
		this.formalParameters = formalParameters;
		this.returnDefinition = returnDefinition;
	  }

        public SteeringControl createSteeringControl(Map actualParameters)
	  {
		// perform "type safety" check

		return new SteeringControl(this, actualParameters);
	  }

        public String getControlName()
	  {
		return name;
	  }

        public Map getParameterDefinitions()
	  {
		return formalParameters;
	  }

        public Map getReturnDefinition()
	  {
		return returnDefinition;
	  }

        public boolean equals(Object o)
	  {

		SteeringControlDefinition scd = null;

		try
		{
			scd = (SteeringControlDefinition) o;
		}
		catch(Exception e)
		{
			return false;
		}

		boolean nameSame = true, formalSame = true, returnSame = true;

		if(this.name == null)
		{
			if(scd.getControlName() != null)
				nameSame = false;
		}
		else if(!this.name.equals(scd.getControlName()))
			nameSame = false;


		if(this.formalParameters == null)
		{
                        if(scd.getParameterDefinitions() != null)
                                formalSame = false;
		}
                else if(!this.formalParameters.equals(scd.getParameterDefinitions()))
                        formalSame = false;

                if(this.returnDefinition == null)
                {
		        if(scd.getReturnDefinition() != null)
                                returnSame = false;
		}
                else if(!this.returnDefinition.equals(scd.getReturnDefinition()))
                        returnSame = false;

		return nameSame && formalSame && returnSame;
	  }


        public String toString()
	  {
		return "Name:" + this.name + "; Formal Parameters: " + this.formalParameters + "; Return Definition: " + this.returnDefinition;
	  }
}

