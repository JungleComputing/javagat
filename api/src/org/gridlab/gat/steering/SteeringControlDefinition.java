package org.gridlab.gat.steering;

import java.util.Map;

public class SteeringControlDefinition {
    String name = null;

    Map<String, Object> formalParameters = null;

    Map<String, Object> returnDefinition = null;

    public SteeringControlDefinition(String name,
            Map<String, Object> formalParameters,
            Map<String, Object> returnDefinition) {
        this.name = name;
        this.formalParameters = formalParameters;
        this.returnDefinition = returnDefinition;
    }

    public SteeringControl createSteeringControl(
            Map<String, Object> actualParameters) {
        // perform "type safety" check

        return new SteeringControl(this, actualParameters);
    }

    public String getControlName() {
        return name;
    }

    public Map<String, Object> getParameterDefinitions() {
        return formalParameters;
    }

    public Map<String, Object> getReturnDefinition() {
        return returnDefinition;
    }

    public boolean equals(Object o) {

        SteeringControlDefinition scd = null;

        try {
            scd = (SteeringControlDefinition) o;
        } catch (Exception e) {
            return false;
        }

        boolean nameSame = true, formalSame = true, returnSame = true;

        if (this.name == null) {
            if (scd.getControlName() != null)
                nameSame = false;
        } else if (!this.name.equals(scd.getControlName()))
            nameSame = false;

        if (this.formalParameters == null) {
            if (scd.getParameterDefinitions() != null)
                formalSame = false;
        } else if (!this.formalParameters.equals(scd.getParameterDefinitions()))
            formalSame = false;

        if (this.returnDefinition == null) {
            if (scd.getReturnDefinition() != null)
                returnSame = false;
        } else if (!this.returnDefinition.equals(scd.getReturnDefinition()))
            returnSame = false;

        return nameSame && formalSame && returnSame;
    }

    public String toString() {
        return "Name:" + this.name + "; Formal Parameters: "
                + this.formalParameters + "; Return Definition: "
                + this.returnDefinition;
    }
}
