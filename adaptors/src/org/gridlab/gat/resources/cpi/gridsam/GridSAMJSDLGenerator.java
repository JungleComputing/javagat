package org.gridlab.gat.resources.cpi.gridsam;

import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.Sandbox;

public interface GridSAMJSDLGenerator {

    public String generate(JobDescription description, Sandbox sandbox);

}
