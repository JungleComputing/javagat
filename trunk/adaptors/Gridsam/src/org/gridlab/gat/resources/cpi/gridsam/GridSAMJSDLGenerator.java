package org.gridlab.gat.resources.cpi.gridsam;

import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;

public interface GridSAMJSDLGenerator {

    public JobDefinitionDocument generate(JobDescription description, Sandbox sandbox);

}
