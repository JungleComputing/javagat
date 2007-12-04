package org.gridlab.gat.resources.cpi.gridsam;

import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;

public interface GridSAMJSDLGenerator {

    public String generate(SoftwareDescription description, Sandbox sandbox);

}
